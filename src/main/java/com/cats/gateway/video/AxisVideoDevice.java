package com.cats.gateway.video;

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

import com.cats.gateway.health.model.HealthReport;
import com.cats.gateway.health.model.HealthStatusBean;
import com.cats.gateway.slotmapping.model.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;


/**
 * Class to load the Axis Video instance.
 *
 */


@Slf4j
public record AxisVideoDevice(String internalIp, String internalPort, String natPort, String natSSLPort,
                              String natRTSPPort, String rackHost, String rackIp, Boolean useProxy,
                              String proxyBaseUrl) implements VideoDevice {


    private static final List<String> SUPPORTED_RESOLUTIONS = List.of("704x480","720x480", "1024x768", "1920x1080");

    public AxisVideoDevice(String internalIp, String internalPort, String natPort, String natSSLPort, String natRTSPPort, String rackHost, String rackIp, Boolean useProxy, String proxyBaseUrl) {
        this.natPort = natPort;
        this.natSSLPort = natSSLPort;
        this.natRTSPPort = natRTSPPort;
        this.rackHost = rackHost;
        this.rackIp = rackIp;
        this.useProxy = useProxy;
        this.proxyBaseUrl = proxyBaseUrl;
        this.internalIp = internalIp == null || internalIp.trim().isEmpty() ? "192.168.100." + natPort.substring(2) : internalIp;
        this.internalPort = internalPort == null || internalPort.trim().isEmpty() ? "80" : internalPort;
    }

    /**
     * Method to get snapshot url based on a given slot.
     *
     * @param outlet
     *      -- Port of video device
     * @param resolution
     *      -- resolution of the snapshot
     * @param videoCodec
     *       -- video codec of the snapshot
     * @param squarePixel
     *      -- square pixel of the snapshot
     * @param useSSL
     *     -- use SSL or not
     * @param isLocal
     *    -- url requested is with internal ip or not
     * @return String
     *     -- snapshot url
     * */

    @Override
    public String getSnapShotUrl(Integer outlet, String resolution, String videoCodec, String squarePixel, Boolean useSSL, Boolean isLocal) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isLocal) {
            stringBuilder.append("http://");
            stringBuilder.append(internalIp());
            stringBuilder.append(":");
            stringBuilder.append(internalPort());
        } else if (useSSL && useProxy) {
            stringBuilder.append("https://");
            stringBuilder.append(proxyBaseUrl());
            stringBuilder.append(rackHost());
            stringBuilder.append("/video/");
            stringBuilder.append(natSSLPort());
        } else if (useSSL) {
            stringBuilder.append("https://");
            stringBuilder.append(rackHost());
            stringBuilder.append(":");
            stringBuilder.append(natSSLPort());
        } else if (useProxy) {
            stringBuilder.append("http://");
            stringBuilder.append(proxyBaseUrl());
            stringBuilder.append(rackHost());
            stringBuilder.append("/video/");
            stringBuilder.append(natPort());
        } else {
            stringBuilder.append("http://");
            stringBuilder.append(rackHost());
            stringBuilder.append(":");
            stringBuilder.append(natPort());
        }

        stringBuilder.append("/axis-cgi/jpg/image.cgi?camera=");
        stringBuilder.append(outlet);

        if (squarePixel != null && !squarePixel.trim().isEmpty()) {
            stringBuilder.append("&squarepixel=");
            stringBuilder.append(squarePixel);
        }

        if (resolution != null && !resolution.trim().isEmpty()) {
            switch (resolution) {
                case "704x480", "704x576" -> stringBuilder.append("&resolution=4CIF");
                case "720x480", "720x576" -> stringBuilder.append("&resolution=D1");
                case "704x240", "704x288" -> stringBuilder.append("&resolution=2CIF");
                case "352x240", "352x288" -> stringBuilder.append("&resolution=CIF");
                case "176x120", "176x144" -> stringBuilder.append("&resolution=QCIF");
                default -> {
                    stringBuilder.append("&resolution=");
                    stringBuilder.append(resolution);
                }
            }
        }

        if (videoCodec != null && !videoCodec.trim().isEmpty()) {
            stringBuilder.append("&videocodec=");
            stringBuilder.append(videoCodec);
        }

        return stringBuilder.toString();
    }

    /**
     * Method to get video url based on a given slot.
     *
     * @param outlet
     *      -- Port of video device
     * @param resolution
     *      -- resolution of the snapshot
     * @param videoCodec
     *       -- video codec of the snapshot
     * @param squarePixel
     *      -- square pixel of the snapshot
     * @param useSSL
     *     -- use SSL or not
     * @param isLocal
     *    -- url requested is with internal ip or not
     * @param isRtsp
     *   -- url requested is with rtsp or not
     * @return String
     *     -- video url
     * */
    @Override
    public String getVideoUrl(Integer outlet, String resolution, String videoCodec, String squarePixel, String fps, Boolean useSSL, Boolean isLocal, Boolean isRtsp) {
        StringBuilder stringBuilder = new StringBuilder();

        if (isRtsp) {
            stringBuilder.append("rtsp://");
            stringBuilder.append(rackHost());
            stringBuilder.append(":");
            stringBuilder.append(natRTSPPort());
            stringBuilder.append("/axis-media/media.amp?camera=");
        } else {
            if (isLocal) {
                stringBuilder.append("http://");
                stringBuilder.append(internalIp());
                stringBuilder.append(":");
                stringBuilder.append(internalPort());
            } else if (useSSL && useProxy) {
                stringBuilder.append("https://");
                stringBuilder.append(proxyBaseUrl());
                stringBuilder.append(rackHost());
                stringBuilder.append("/video/");
                stringBuilder.append(natSSLPort());
            } else if (useSSL) {
                stringBuilder.append("https://");
                stringBuilder.append(rackHost());
                stringBuilder.append(":");
                stringBuilder.append(natSSLPort());
            } else if (useProxy) {
                stringBuilder.append("http://");
                stringBuilder.append(proxyBaseUrl());
                stringBuilder.append(rackHost());
                stringBuilder.append("/video/");
                stringBuilder.append(natPort());
            } else {
                stringBuilder.append("http://");
                stringBuilder.append(rackHost());
                stringBuilder.append(":");
                stringBuilder.append(natPort());
            }
            stringBuilder.append("/mjpg/video.mjpg?camera=");
        }

        stringBuilder.append(outlet);

        if (fps != null && !fps.trim().isEmpty()) {
            stringBuilder.append("&fps=");
            stringBuilder.append(fps);
        }

        if (squarePixel != null && !squarePixel.trim().isEmpty()) {
            stringBuilder.append("&squarepixel=");
            stringBuilder.append(squarePixel);
        }

        if (resolution != null && !resolution.trim().isEmpty()) {
            switch (resolution) {
                case "704x480", "704x576" -> stringBuilder.append("&resolution=4CIF");
                case "720x480", "720x576" -> stringBuilder.append("&resolution=D1");
                case "704x240", "704x288" -> stringBuilder.append("&resolution=2CIF");
                case "352x240", "352x288" -> stringBuilder.append("&resolution=CIF");
                case "176x120", "176x144" -> stringBuilder.append("&resolution=QCIF");
                default -> {
                    stringBuilder.append("&resolution=");
                    stringBuilder.append(resolution);
                }
            }
        }

        if (videoCodec != null && !videoCodec.trim().isEmpty()) {
            stringBuilder.append("&videocodec=");
            stringBuilder.append(videoCodec);
        }

        return stringBuilder.toString();

    }

    /**
     * Method to get video resolutions based on a given slot.
     *
     * @param slot
     *      -- slot of video device
     * @return List<String>
     *     -- list of supported resolutions
     * */
    @Override
    public List<String> getSupportedResolutions(Integer slot) {
        return SUPPORTED_RESOLUTIONS;
    }

    public Mono<HealthStatusBean> getHealthStatus(HealthStatusBean healthStatusBean, Device device, List<HealthReport> healthReports) {
        Mono<HealthReport> axisVideoHealthReports = getAxisSnapshotHealthReports(device);
        return axisVideoHealthReports.flatMap(axisHealthReports -> {
            healthReports.add(axisHealthReports);
            healthStatusBean.setHwDevicesHealthStatus(healthReports);
            return Mono.just(healthStatusBean);
        }).onErrorResume(e -> {
            healthStatusBean.setIsHealthy(false);
            healthStatusBean.setRemarks(e.getMessage());
            return Mono.just(healthStatusBean);
        });
    }

    /**
     * Method to get video encoder health give a device.
     *
     * @param device
     *      -- video device
     * @return Mono<HealthReport>
     *     -- health report of the video device
     * */
    private Mono<HealthReport> getAxisSnapshotHealthReports(Device device) {
        if(device == null) {
            return Mono.just(new HealthReport());
        }

        WebClient webClient = WebClient.create();
        String cameraOne = "/axis-cgi/jpg/image.cgi?camera=1";

        //setting the health report for axis video device

        String videoId = device.getInternalIp().substring(device.getInternalIp().length() - 2);
        HealthReport healthReport = new HealthReport();
        healthReport.setHost(device.getInternalIp());
        healthReport.setEntity("VID"+videoId);

        String deviceId = device.getInternalIp();
        String url = "http://" + deviceId + cameraOne;
        log.info("The Axis video url is {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(byte[].class)
                .map(body -> {
                    healthReport.setIsHealthy(true);
                    return healthReport;
                })
                .onErrorResume(e -> {
                    healthReport.setIsHealthy(false);
                    healthReport.setRemarks(e.getMessage());
                    return Mono.just(healthReport);
                });
    }
}
