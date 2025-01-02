package com.cats.gateway.video.service;

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
import com.cats.gateway.slotmapping.service.SlotMappingService;
import com.cats.gateway.video.VideoDevice;
import com.cats.gateway.video.VideoDeviceFactory;
import com.cats.gateway.exceptions.SlotMappingException;
import com.cats.gateway.exceptions.VideoGatewayException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class AbstractGatewayService {


    @Autowired
    SlotMappingService slotMappingService;

    @Getter
    private String rackHost;

    @Getter
    private String rackIp;

    @PostConstruct()
    void init() {
        SlotToPortMappings slotToPortMappings = slotMappingService.getMappings();
        this.rackHost = slotToPortMappings.getRackHost();
        this.rackIp = slotToPortMappings.getRackIp();
    }

    @Autowired
    protected VideoDeviceFactory videoDeviceFactory;

    protected static final String API_REGEX = "/video/rest/(.*)/slot/([0-9]+)/(.*)";

    /**
     * Method to get the mappings corresponding to a slot.
     *
     * @param slot
     *      -- Slot number
     * @return mappings
     *      -- An array of mappings.
     * */
    protected String[] getMappings(Integer slot) {
        String mappings = slotMappingService.getMapping(slot.toString());
        if (mappings == null) {
            throw new SlotMappingException(HttpStatus.NOT_FOUND, "Mappings not found for the slot " + slot);
        }

        return mappings.split(":");
    }

    /**
     * Method to get the device information based on a given slot.
     *
     * @param slot
     *      -- Slot number
     * */
    protected Device getDeviceBySlot(Integer slot) {

        String[] deviceAndPort = getMappings(slot);

        List<Device> deviceList = slotMappingService.getVideoDevices();

        return deviceList.stream().filter(
                device -> device.getId().equals(Integer.parseInt(deviceAndPort[0]))
        ).findFirst().orElse(null);

    }

    /**
     * Method to get the video device associated with a device.
     *
     * @param device
     *      -- An instance of device
     * */
    protected VideoDevice getVideoDevice(Device device) {
        return videoDeviceFactory.getVideoDevice(device);
    }

    /**
     * Method to get the video device associated with a device given a slot.
     *
     * @param slot
     *      -- Slot number.
     * */
    protected VideoDevice getVideoDevice(Integer slot) {
        Device device = getDeviceBySlot(slot);
        log.info("Device information for the slot {}: {}", slot, device);

        if (device == null) {
            log.info("video device is not configured for the slot {}", slot);
            throw new VideoGatewayException(HttpStatus.BAD_REQUEST, "Video device not configured");
        }

        VideoDevice videoDevice = getVideoDevice(device);

        if (null == videoDevice) {
            throw new VideoGatewayException(HttpStatus.BAD_REQUEST, "Video device factory could not be initialised");
        }

        return videoDevice;
    }

    /***
     * Method to extract the slot from a given request path.
     *
     * @param path
     *      -- Http request path
     * */
    protected Integer getSlotInfo(String path) {
        log.info("Retrieving slot information from the path {}", path);

        Pattern pattern = Pattern.compile(API_REGEX);
        Matcher matcher = pattern.matcher(path);
        boolean matches = matcher.matches();

        if (!matches) {
            throw new VideoGatewayException(HttpStatus.NOT_FOUND, "Invalid API path");
        }

        String slot = matcher.group(2);

        return Integer.parseInt(slot);
    }

}
