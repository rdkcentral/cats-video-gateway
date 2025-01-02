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
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Interface to declare all the command methods required for the video devices.
 *
 * **/
public interface VideoDevice {

    String getSnapShotUrl(Integer outlet, String resolution, String videoCodec, String squarePixel, Boolean useSSL, Boolean isLocal);

    String getVideoUrl(Integer outlet, String resolution, String videoCodec, String squarePixel, String fps, Boolean useSSL, Boolean isLocal, Boolean isRtsp);

    List<String> getSupportedResolutions(Integer slot);

    Mono<HealthStatusBean> getHealthStatus(HealthStatusBean healthStatusBean, Device device, List<HealthReport> healthReports);

}
