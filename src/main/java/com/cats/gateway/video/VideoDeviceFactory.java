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

import com.cats.gateway.slotmapping.model.Device;
import com.cats.gateway.slotmapping.model.SlotToPortMappings;
import com.cats.gateway.slotmapping.model.VideoType;
import com.cats.gateway.slotmapping.service.SlotMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.TreeMap;

/**
 * Factory class to load the corresponding video devices.
 *
 **/
@Service
@Slf4j
public class VideoDeviceFactory {

    @Autowired
    SlotMappingService slotMappingService;

    private final TreeMap<Integer, VideoDevice> videoDeviceMap = new TreeMap<>();


    /**
     * Method to load the video devices during application start up time and store it in a tree map.
     */
    @PostConstruct()
    public void init() {
        log.info("Initialising video devices");
        SlotToPortMappings slotToPortMappings = slotMappingService.getMappings();
        List<Device> devices = slotToPortMappings.getDevices();
        if (devices != null && !devices.isEmpty()) {
            devices.forEach(device -> {
                VideoType videoType = VideoType.findType(device.getType());

                if (videoType != null) {
                    if (videoType.equals(VideoType.AXIS_P7216) || videoType.equals(VideoType.AXIS_FA54)) {
                        videoDeviceMap.put(device.getId(), new AxisVideoDevice(device.getInternalIp(), device.getInternalPort(), device.getNatPort(), device.getNatSSLPort(), device.getNatRTSPPort(), slotToPortMappings.getRackHost(), slotToPortMappings.getRackIp(), slotToPortMappings.getUseProxy(), slotToPortMappings.getProxyBaseUrl()));
                    } else if (videoType.equals(VideoType.HANWHA_SPE_1620)) {
                        videoDeviceMap.put(device.getId(), new HanwhaVideoDevice(device.getInternalIp(), device.getInternalPort(), device.getNatPort(), device.getNatSSLPort(), device.getNatRTSPPort(), slotToPortMappings.getRackHost(), slotToPortMappings.getRackIp(), slotToPortMappings.getUseProxy(), slotToPortMappings.getProxyBaseUrl()));
                    } else {
                        log.info("No video devices configured for the device id {}", device.getId());
                    }
                }
            });
        }
    }

    /**
     * Method to load the video device.
     *
     * @param device -- Device
     */
    public VideoDevice getVideoDevice(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be empty");
        }
        log.info("retrieving video device from the cache");
        return videoDeviceMap.get(device.getId());
    }
}
