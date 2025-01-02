package com.cats.gateway.health.resource;

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

import com.cats.gateway.health.model.HealthStatusBean;
import com.cats.gateway.health.service.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Video Health Resource", description = "Api to get video health status")
@RestController
public class VideoHealthResource {

    @Autowired
    private HealthService healthService;

    /**
     * Method to get the video health status of all the devices in the slot mapping.
     *
     * @return Mono<HealthStatusBean>
     */
    @Operation(summary = "Get video health", description = "Get video health for all video devices in a rack")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json" , schema = @Schema(implementation = HealthStatusBean.class)) }),
            @ApiResponse(responseCode = "404", description = "Video devices not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Video devices not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))})
    })
    @GetMapping(value= "/health", produces= "application/json")
    public Mono<HealthStatusBean> getVideoHealthStatus()  {
        return healthService.getVideoHealthReport();
    }

    /**
     * Method to get the video health status of a device in the slot mapping.
     *
     * @param slotNo
     *      -- Slot number of the device
     * @return Mono<String>
     */
    @Operation(summary = "Get video screen status", description = "Get video health for slot and the status of the screen")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json" , schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "404", description = "Video device not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Video device not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))})
    })
    @GetMapping(value ="{slotNo}/status")
    public Mono<String> getImage(@Parameter(description = "Slot number for which status is to be fetched") @PathVariable("slotNo") long slotNo) {
        return healthService.getScreenStatus(slotNo);
    }

    /**
     * Method to get the video health status of a device in the slot mapping.
     *
     * @param slotNo
     *      -- Slot number of the device
     * @return Mono<byte[]>
     */
    @Operation(summary = "Get video slot screenshot", description = "Get video screenshot ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "image/jpeg" , array = @ArraySchema(schema = @Schema(implementation = Byte.class))) }),
            @ApiResponse(responseCode = "404", description = "Video device not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Video device not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))})
    })
    @GetMapping(value = "{slotNo}/screenshot", produces = MediaType.IMAGE_JPEG_VALUE)
    public Mono<byte[]> getScreen(@Parameter(description = "Slot number for which status is to be fetched") @PathVariable("slotNo") long slotNo) {
        return healthService.getImageByteArray(slotNo);
    }

}
