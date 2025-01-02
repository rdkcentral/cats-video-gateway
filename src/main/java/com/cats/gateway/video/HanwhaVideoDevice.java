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
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Class to load hanwha video instance.
 *
 */
@Slf4j
public record HanwhaVideoDevice(String internalIp, String internalPort, String natPort, String natSSLPort,
                                String natRTSPPort, String rackHost, String rackIp, Boolean useProxy,
                                String proxyBaseUrl) implements VideoDevice {

    private static final List<String> SUPPORTED_RESOLUTIONS = List.of("704x480","720x480","1024x768", "1920x1080");

    public HanwhaVideoDevice(String internalIp, String internalPort, String natPort, String natSSLPort, String natRTSPPort, String rackHost, String rackIp, Boolean useProxy, String proxyBaseUrl) {
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

        stringBuilder.append("/stw-cgi/video.cgi?msubmenu=snapshot&action=view&Profile=1&Channel=");
        stringBuilder.append(outlet - 1);

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
            stringBuilder.append("/stw-cgi/video.cgi?msubmenu=stream&action=view&Profile=1&Channel=");
        }

        stringBuilder.append(outlet - 1);

        if (fps != null && !fps.trim().isEmpty()) {
            stringBuilder.append("&FrameRate=");
            stringBuilder.append(fps);
        }

        if (resolution != null && !resolution.trim().isEmpty()) {
            if (resolution.equals("4CIF")) {
                stringBuilder.append("&Resolution=704x480");
            } else {
                stringBuilder.append("&Resolution=");
                stringBuilder.append(resolution);
            }
        }

        if (videoCodec != null && !videoCodec.trim().isEmpty()) {
            stringBuilder.append("&CodecType=");
            stringBuilder.append(videoCodec);
        } else {
            stringBuilder.append("&CodecType=MJPEG");
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

    /**
     * Method to get video encoder health give a device.
     *
     * @param device
     *      -- video device
     * @return Mono<HealthReport>
     *     -- health report of the video device
     * */
    public Mono<HealthStatusBean> getHealthStatus(HealthStatusBean healthStatusBean, Device device, List<HealthReport> healthReports) {
        throw new UnsupportedOperationException("Operation not supported for hanwha video device");

    }
}
