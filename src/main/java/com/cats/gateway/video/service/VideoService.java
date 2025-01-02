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

import com.cats.gateway.video.VideoDevice;
import com.cats.gateway.video.service.AbstractGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/***
 * Service class for video service based implementations.
 *
 * */
@Service
@Slf4j
public class VideoService extends AbstractGatewayService {


    /**
     * Method to generate a snapshot url. The method accepts a request path, extracts the slot information and generate
     * the corresponding video device to generate the snapshot url.
     *
     * @param path
     *      -- Http request path.
     * */
    public String generateSnapShotUrl(String path, String resolution, String videoCodec, String squarePixel, Boolean useSSL, Boolean isLocal) {

        Integer slot = getSlotInfo(path);

        return generateSnapShotUrl(slot, resolution, videoCodec, squarePixel, useSSL, isLocal);
    }

    /**
     * Method to generate a snapshot url. The method accepts a request path, extracts the slot information and generate
     * the corresponding video device to generate the snapshot url.
     *
     * @param slot
     *      -- slot info.
     * */
    public String generateSnapShotUrl(Integer slot, String resolution, String videoCodec, String squarePixel,  Boolean useSSL, Boolean isLocal) {

        VideoDevice videoDevice = getVideoDevice(slot);

        String[] deviceAndPort = getMappings(slot);

        return videoDevice.getSnapShotUrl(Integer.parseInt(deviceAndPort[1]), resolution, videoCodec, squarePixel, useSSL, isLocal);
    }

    /**
     * Method to generate video url based on given parameters.
     *
     * @param slot
     *      -- slot information.
     * @param resolution
     *      -- video resolution.
     * @param videoCodec
     *      -- video codec information.
     * @param squarePixel
     *      -- square pixel information.
     * @param useSSL
     *      -- flag to enable ssl video url.
     * @param isLocal
     *      -- flag to enable local video url.
     * @param isRtsp
     *      -- flag to enable rtsp url.
     * */
    public String generateVideoUrl(Integer slot, String resolution, String videoCodec, String squarePixel, String fps, Boolean useSSL, Boolean isLocal, Boolean isRtsp) {

        VideoDevice videoDevice = getVideoDevice(slot);

        String[] deviceAndPort = getMappings(slot);

        return videoDevice.getVideoUrl(Integer.parseInt(deviceAndPort[1]), resolution, videoCodec, squarePixel, fps, useSSL, isLocal, isRtsp);
    }

    /**
     * Method to get video resolutions based on a given slot.
     *
     * @param slot
     *      -- slot of video device
     * @return List<String>
     *     -- list of supported resolutions
     * */
    public List<String> getSupportedResolutions(Integer slot) {

        VideoDevice videoDevice = getVideoDevice(slot);

        String[] deviceAndPort = getMappings(slot);

        return videoDevice.getSupportedResolutions(Integer.parseInt(deviceAndPort[1]));
    }

}