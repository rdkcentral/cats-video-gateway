package com.cats.gateway.video.resource;

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

import com.cats.gateway.video.service.VideoService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Tag(name = "Video Resource", description = "APIs to generate video urls")
@RestController
@RequestMapping("/v1/slot/{slot}")
public class VideoResource {

    @Autowired
    VideoService videoService;

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
    @Operation(summary = "Get Video url ", description = "Get video url for video devices for a rack given slot number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "text/plain" , schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "404", description = "Video devices not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Video devices not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))})
    })
    @GetMapping("/url")
    protected String generateVideoUrl(@PathVariable @Valid @NotNull(message = "Slot cannot be empty") Integer slot,
                                      @Parameter(description = "resolution of the video required") @RequestParam(required = false, defaultValue = "") String resolution,
                                      @Parameter(description = "video codec of the video required") @RequestParam(required = false, defaultValue = "") String videoCodec,
                                      @Parameter(description = "squarePixel of the video") @RequestParam(required = false, defaultValue = "") String squarePixel,
                                      @Parameter(description = "frames per second of the video") @RequestParam(required = false, defaultValue = "15") String fps,
                                      @Parameter(description = "To identify if https url is required") @RequestParam(required = false, defaultValue = "true") Boolean useSSL,
                                      @Parameter(description = "To identify if internal server url is required")@RequestParam(required = false, defaultValue = "false") Boolean isLocal,
                                      @Parameter(description = "RTSP protocol url required") @RequestParam(required = false, defaultValue = "false") Boolean isRtsp) {
        String safeResolution = StringEscapeUtils.escapeHtml4(resolution);
        String safeVideoCodec = StringEscapeUtils.escapeHtml4(videoCodec);
        String safeSquarePixel = StringEscapeUtils.escapeHtml4(squarePixel);
        String safeFps = StringEscapeUtils.escapeHtml4(fps);

        // Use sanitized inputs in video URL generation
        return videoService.generateVideoUrl(slot, safeResolution, safeVideoCodec, safeSquarePixel, safeFps, useSSL, isLocal, isRtsp);
    }

    /**
     * Method to get snapshot url based on a given slot.
     *
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
    @Operation(summary = "Get snapshot url", description = "Get snapshot url for all video devices for a rack given slot number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "text/plain" , schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "404", description = "Video devices not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Video devices not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))})
    })
    @GetMapping("/url/snapshot")
    protected String generateSnapshotUrl(@PathVariable Integer slot,
                                      @Parameter(description = "resolution of the video required") @RequestParam(required = false, defaultValue = "704x480") String resolution,
                                      @Parameter(description = "video codec of the video required") @RequestParam(required = false, defaultValue = "") String videoCodec,
                                      @Parameter(description = "squarePixel of the video") @RequestParam(required = false, defaultValue = "") String squarePixel,
                                      @Parameter(description = "To identify if https url is required") @RequestParam(required = false, defaultValue = "true") Boolean useSSL,
                                      @Parameter(description = "To identify if internal server url is required") @RequestParam(required = false, defaultValue = "false") Boolean isLocal) {

        String safeResolution = StringEscapeUtils.escapeHtml4(resolution);
        String safeVideoCodec = StringEscapeUtils.escapeHtml4(videoCodec);
        String safeSquarePixel = StringEscapeUtils.escapeHtml4(squarePixel);

        // Use sanitized parameters to generate the URL
        return videoService.generateSnapShotUrl(slot, safeResolution, safeVideoCodec, safeSquarePixel, useSSL, isLocal);
    }

    /**
     * Method to get video resolutions based on a given slot.
     *
     * @return List<String>
     *     -- list of supported resolutions
     * */
    @Operation(summary = "Get video resolutions", description = "Get video resolutions for all video devices for a rack given slot number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json" , array = @ArraySchema(schema = @Schema(implementation = String.class))) }),
            @ApiResponse(responseCode = "404", description = "Video devices not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Video devices not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))})
    })
    @GetMapping("/resolutions")
    protected List<String> getSupportedResolutions(@Parameter(description = "slot number for which resolutions is requested") @PathVariable @Valid @NotNull(message = "Slot cannot be empty") Integer slot){
        return videoService.getSupportedResolutions(slot);
    }
}
