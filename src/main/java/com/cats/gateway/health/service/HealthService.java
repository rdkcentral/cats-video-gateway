package com.cats.gateway.health.service;

/*
 * Copyright 2021 Comcast Cable Communications Management, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.cats.gateway.health.model.*;
import com.cats.gateway.config.Configuration;
import com.cats.gateway.slotmapping.model.Device;
import com.cats.gateway.slotmapping.service.SlotMappingService;
import com.cats.gateway.video.VideoDevice;
import com.cats.gateway.video.VideoDeviceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class HealthService {

    @Autowired
    SlotMappingService slotMappingService;

    @Autowired
    private Configuration config;

    private VideoDevice videoDevice;

    @Autowired
    private VideoDeviceFactory videoDeviceFactory;

    @Value("${cats.rack.capability.details}")
    public String catsRackCapability;

    @Value("${cats.rack.url}")
    private String rackUrl;


    //Collection HealthStatus Bean from every device in the slot mapping
    /**
     * Method to get the health report of all the devices in the slot mapping.
     *
     * @return Mono<List<HealthStatusBean>>
     */
    public Mono<List<HealthStatusBean>> getHealthReport() {

        HealthStatusBean result = new HealthStatusBean();
        List<HealthReport> healthReportArrayList = new ArrayList<>();
        result.setIsHealthy(true);

        List<Device> deviceAndPort = slotMappingService.getVideoDevices();

        return Flux.fromIterable(deviceAndPort)
                .flatMap(device -> {
                    videoDevice = videoDeviceFactory.getVideoDevice(device);
                    return videoDevice.getHealthStatus(result, device, healthReportArrayList);
                })
                .collectList();
    }

    /**
     * Method to get the health report of all the devices in the slot mapping.
     *
     * @return Mono<List<HealthStatusBean>>
     */
    public Mono<HealthStatusBean> getVideoHealthReport() {
        Mono<List<RouterLeaseStatus>> leaseStatusMono = getVideoLeaseStatus();
        HealthStatusBean videoHealthStatusBean = new HealthStatusBean();
        videoHealthStatusBean.setIsHealthy(true);
        videoHealthStatusBean.getVersion().put("MS_VERSION", getMicroServiceVersion());
        List<LeaseMetadata> leaseMetadata = new ArrayList<>();
        StringBuilder comment = new StringBuilder();
        RouterLeaseStatus videoRouterLeaseStatus = new RouterLeaseStatus();
        videoRouterLeaseStatus.setIsHealthy(true);

        //Setting the video health status bean
        return leaseStatusMono.flatMap(routerLeaseStatuses -> {
            for (RouterLeaseStatus routerLeaseStatus : routerLeaseStatuses) {
                log.info("Processing routerLeaseStatus: {}", routerLeaseStatus);
                if(routerLeaseStatus.getMetadata()!=null){
                    leaseMetadata.addAll(routerLeaseStatus.getMetadata());
                }
                if(routerLeaseStatus.getIsHealthy()!=null&&!routerLeaseStatus.getIsHealthy()){
                    videoHealthStatusBean.setIsHealthy(false);
                    videoRouterLeaseStatus.setIsHealthy(false);
                }
                if(routerLeaseStatus.getComment()!=null){
                    comment.append(routerLeaseStatus.getComment());
                }
            }
            //setting the video lease status
            videoRouterLeaseStatus.setMetadata(leaseMetadata);
            videoRouterLeaseStatus.setComment(comment.toString());
            videoHealthStatusBean.setLeaseHealthStatus(videoRouterLeaseStatus);
                return getHealthReport()
                        .flatMap(healthStatusBeans -> {
                            healthStatusBeans.forEach(healthStatusBean -> {
                                if(!healthStatusBean.getIsHealthy()){
                                    videoHealthStatusBean.setIsHealthy(false);
                                }
                                videoHealthStatusBean.setHwDevicesHealthStatus(healthStatusBean.getHwDevicesHealthStatus());
                            });
                            log.info("the video health is {}",videoHealthStatusBean.getIsHealthy());
                            return Mono.just(videoHealthStatusBean);
                        })
                        .onErrorResume( error -> {
                            videoHealthStatusBean.setIsHealthy(false);
                            videoHealthStatusBean.setRemarks(error.getMessage());
                            return Mono.just(videoHealthStatusBean);
                        });
        }).onErrorResume(error -> {
            videoHealthStatusBean.setIsHealthy(false);
            videoHealthStatusBean.setRemarks(String.format("Failed to fetch details of lease %s", error.getMessage()));
            return Mono.just(videoHealthStatusBean);
        });
    }

    //Get Axis and Hp server lease status from the mt query
    /**
     * Method to get the lease status of the video devices.
     *
     * @return Mono<List<RouterLeaseStatus>>
     */
    public Mono<List<RouterLeaseStatus>> getVideoLeaseStatus() {
        log.info("Capability url: {}", catsRackCapability);
        WebClient webClient = WebClient.create();
        return webClient.get()
                .uri(catsRackCapability)
                .retrieve()
                .bodyToMono(RouterHealthStatus.class)
                .map(videoDeviceDetails -> {
                    List<RouterLeaseStatus> routerLeaseStatuses = new ArrayList<>();
                    RouterLeaseStatus vidStatus = videoDeviceDetails.get("VID");
                    RouterLeaseStatus mtrStatus = videoDeviceDetails.get("MTR");
                    if (vidStatus != null) {
                        routerLeaseStatuses.add(vidStatus);
                    }
                    if (mtrStatus != null) {
                        routerLeaseStatuses.add(mtrStatus);
                    }
                    return routerLeaseStatuses;
                });
    }

    /**
     * Method to get the version of the microservice.
     *
     * @return String
     */
    public String getMicroServiceVersion()  {
        String version = config.getBuildVersion();
        if(version == null || version.isEmpty()) {
            version = "development";
        }
        return version;
    }


    /**
     * Method to get the video health status of a device in the slot mapping.
     *
     * @param slotNo
     *      -- Slot number of the device
     * @return Mono<String>
     */
    public Mono<String> getScreenStatus(long slotNo) {
        return getImage(slotNo)
                .flatMap(image -> {
                    try {
                        return screenInfo(image, slotNo)
                                .map(info -> "Observed the screen to be " + info);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }

    /**
     * Method to get the video health status of a device in the slot mapping.
     *
     * @param slotNo
     *      -- Slot number of the device
     * @return Mono<BufferedImage>
     */
    public Mono<BufferedImage> getImage(long slotNo) {
        WebClient webClient = WebClient.create();
        return webClient.get()
                .uri(rackUrl + "minion/rest/rack/" + slotNo + "/screenshot?resolution=4CIF&squarepixel=0")
                .retrieve()
                .bodyToMono(byte[].class)
                .map(bytes -> {
                    try {
                        return ImageIO.read(new ByteArrayInputStream(bytes));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Method to get the video health status of a device in the slot mapping.
     *
     * @param image
     *      -- BufferedImage
     * @param slotNo
     *      -- Slot number of the device
     * @return Mono<String>
     */
    public Mono<String> screenInfo(BufferedImage image, long slotNo) {
        int black = 0;
        int blue = 0;
        int green = 0;
        int other = 0;
        int count = 0;
        int width = image.getWidth();
        int height = image.getHeight();
        int startWidth = width/3;
        int endWidth = width - startWidth;
        int startHeight = height/3;
        int endHeight = height - startHeight;
        if (image != null) {
            for (int x = startWidth; x < endWidth; x++) {
                for (int y = startHeight; y < endHeight; y++) {
                    Color color = new Color(image.getRGB(x, y));
                    if (color.getRed() <= 35 && color.getGreen() <= 35) {
                        if (color.getBlue() > 200) {
                            blue++;
                        } else if (color.getBlue() <= 35) {
                            black++;
                        } else {
                            other++;
                        }
                    }
                    else if (color.getBlue() < 10 && color.getRed() < 10 && color.getGreen() > 40 ) {
                        green++;
                    }
                    else {
                        other++;
                    }
                    count++;
                }
            }
        }
        int bluePercent = blue * 100 / count;
        int blackPercent = black * 100 / count;
        int greenPercent = green * 100 / count;
        if (blackPercent > 95) {
            return checkImageDifference(image, slotNo);
        } else if (bluePercent > 95) {
            return Mono.just("Blue");
        } else if (greenPercent > 50 ) {
            if(greenPercent < 85 ) {
                return Mono.just(checkGreenScreen(image));
            }
            return Mono.just("Green");
        }
        else {
            return Mono.just("Normal");
        }
    }

    /**
     * Method to get the video health status of a device in the slot mapping.
     *
     * @param initialFrame
     *      -- BufferedImage
     * @param slotNo
     *      -- Slot number of the device
     * @return Mono<String>
     */
    public Mono<String> checkImageDifference(BufferedImage initialFrame, long slotNo) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            return Mono.just(e.getMessage());
        }

        return getImage(slotNo).flatMap(nextFrame -> {
            int initialWidth = initialFrame.getWidth();
            int initialHeight = initialFrame.getHeight();
            int finalHeight = nextFrame.getHeight();
            int finalWidth = nextFrame.getWidth();

            if(initialHeight != finalHeight || initialWidth != finalWidth) {
                return Mono.just("Normal");
            }

            for (int x = initialWidth/3 ; x < initialWidth - initialWidth/3 ; x++) {
                for( int y = initialHeight/3; y < initialHeight - initialHeight/3; y++ ) {
                    if(initialFrame.getRGB(x,y) != nextFrame.getRGB(x,y)) {
                        Color newColor = new Color(nextFrame.getRGB(x,y));
                        Color oldColor = new Color(initialFrame.getRGB(x,y));
                        int diff = Math.abs(newColor.getBlue() - oldColor.getBlue()) + Math.abs(newColor.getRed() - oldColor.getRed()) + Math.abs(newColor.getGreen() - oldColor.getGreen());
                        if(diff > 6 ) {
                            return Mono.just("Normal");
                        }
                    }
                }
            }
            return Mono.just("Black");
        });
    }

    /**
     * Method to get the video health status of a device in the slot mapping.
     *
     * @param image
     *      -- BufferedImage
     * @return String
     */
    private String checkGreenScreen(BufferedImage image) {
        int green = 0;
        int count = 0;
        for (int x=0; x < image.getWidth(); x++) {
            for (int y=0; y < image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x,y));
                if (color.getBlue() < 10 && color.getRed() < 10 && color.getGreen() > 40  || (color.getBlue() < 60 && color.getBlue() < 60 && color.getGreen() > 150)
                        || (color.getBlue() < 40 && color.getRed() < 40 && color.getGreen() > 100)) {
                    green++;
                }
                count ++;
            }
        }
        int greenPercent = green * 100 / count;
        if(greenPercent > 70) {
            return "Green";
        }
        return "Normal";
    }

    /**
     * Method to get the video health status of a device in the slot mapping.
     *
     * @param slotNo
     *      -- Slot number of the device
     * @return Mono<byte[]>
     */
    public Mono<byte[]> getImageByteArray(long slotNo) {
        WebClient webClient = WebClient.create();
        return webClient.get()
                .uri(rackUrl + "minion/rest/rack/" + slotNo + "/screenshot?resolution=4CIF&squarepixel=0")
                .retrieve()
                .bodyToMono(byte[].class);
    }
}
