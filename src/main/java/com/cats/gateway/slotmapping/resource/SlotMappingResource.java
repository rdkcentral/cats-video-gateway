package com.cats.gateway.slotmapping.resource;

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

import com.cats.gateway.slotmapping.model.SlotToPortMappings;
import com.cats.gateway.exceptions.SlotMappingException;
import com.cats.gateway.slotmapping.service.SlotMappingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Slot Mapping Resource", description = "APIs to perform CRUD for slot mappings")
@RestController
@RequestMapping("/mappings/v1/")
@Slf4j
public class SlotMappingResource {

    @Autowired
    private SlotMappingService slotMappingService;

    /**
     * Method to get slot mappings for configured video devices
     *
     * @return SlotToPortMappings
     */
    @Operation(summary = "Get slot Mappings ", description = "Get slot mappings for configured video devices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json" , schema = @Schema(implementation = SlotToPortMappings.class)) }),
            @ApiResponse(responseCode = "404", description = "slot mappings not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "slot mappings not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))})
    })
    @GetMapping("")
    public SlotToPortMappings getMappings() {
        return slotMappingService.getMappings();
    }

    /**
     * Method to add slot mappings for video devices
     *
     * @param slotToPortMappings
     *  -- SlotToPortMappings
     * @throws IOException
     *  -- IOException
     * @throws SlotMappingException
     *  -- SlotMappingException
     */
    @Operation(summary = "Add Slot Mappings for video devices", description = "Adds all slot mappings between slots and video devices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = SlotToPortMappings.class)) }),
            @ApiResponse(responseCode = "400", description = "Request Body is invalid. Please update and retry request.", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))})
    })
    @PostMapping(produces="application/json")
    public void setMappings(@Parameter(description = "Request body to ingest slot mappings") @RequestBody SlotToPortMappings slotToPortMappings) throws IOException, SlotMappingException {
        log.info("slot mapping POST invoked "+ slotToPortMappings.getMappings().keySet().toString());
        slotMappingService.setMappings(slotToPortMappings.getMappings());
    }

    /**
     * Method to delete slot mappings for video devices
     *
     * @throws IOException
     *  -- IOException
     */
    @Operation(summary = "Delete slot mappings", description = "Deletes all slot mappings of configured video devices")
    @ApiResponse(responseCode = "200", description = "Slot mappings deleted successfully")
    @ApiResponse(responseCode = "404", description = "Slot mappings not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))})
    @DeleteMapping()
    public void deleteMappings()throws IOException{
         slotMappingService.removeMappings();
    }

    /**
     * Method to delete slot mapping for a slot
     *
     * @param slot
     *  -- String
     * @throws IOException
     *  -- IOException
     * @throws SlotMappingException
     *  -- SlotMappingException
     */
    @Operation(summary = "Delete slot mapping for a slot", description = "Deletes given slot mappings of video devices")
    @ApiResponse(responseCode = "200", description = "Slot mapping deleted successfully")
    @ApiResponse(responseCode = "404", description = "Slot mapping not found",content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))})
    @DeleteMapping(value="/{slot}",produces="application/json")
    public void removeMapping(@Parameter(description = "slot number to delete mappings") @PathVariable("slot") String slot) throws IOException, SlotMappingException{
         slotMappingService.removeMapping(slot);
    }

    /**
     * Method to get slot mapping for a slot
     *
     * @param slot
     *  -- String
     * @return Map<String, String>
     * @throws SlotMappingException
     *  -- SlotMappingException
     */
    @Operation(summary = "Get slot mapping for a slot", description = "Fetches given slot mappings of video devices")
    @ApiResponse(responseCode = "200", description = "operation successful",
            content = { @Content(mediaType = "application/json" , schema = @Schema(implementation = String.class)) })
    @ApiResponse(responseCode = "404", description = "Slot mapping not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))})
    @GetMapping(value="/{slot}",produces="application/json")
    public Map<String, String> getMapping(@Parameter(description = "slot number to fetch mappings") @PathVariable("slot") String slot) throws SlotMappingException{
        Map<String, String> mapping = new HashMap<>();
        String deviceInfo = slotMappingService.getMapping(slot);
        mapping.put(slot, deviceInfo);
        return mapping;
    }

    /**
     * Method to update slot mappings for video devices
     *
     * @param mapping
     *  -- Map<String, String>
     * @throws IOException
     *  -- IOException
     */
    @Operation(summary = "Update slot mapping", description = "Updates slot mappings of all video devices")
    @ApiResponse(responseCode = "200", description = "Slot mappings updated successfully")
    @ApiResponse(responseCode = "404", description = "Slot mappings not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))})
    @PutMapping(produces = "application/json")
    public void updateMapping(@Parameter(description = "Mappings info to update existing mapping") @RequestParam Map<String, String> mapping) throws IOException {
        slotMappingService.updateMapping(mapping);
    }

    /**
     * Method to update slot mapping for a slot
     *
     * @param slot
     *  -- String
     * @param mapping
     *  -- String
     * @throws IOException
     *  -- IOException
     */
    @Operation(summary = "Update slot mapping for a slot", description = "Updates given slot mappings of video devices")
    @ApiResponse(responseCode = "200", description = "Slot mapping updated successfully")
    @ApiResponse(responseCode = "404", description = "Slot mapping not found", content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))})
    @PutMapping(value="/{slot}",produces = "application/json")
    public void updateMapping(@Parameter(description = "slot number to update mappings") @PathVariable("slot") @Valid @NotNull(message = "Slot cannot be null.") String slot,
                              @Parameter(description = "Request body to update mappings for a slot") @RequestBody @Valid @NotNull(message = "Mapping cannot be null.") String mapping) throws IOException {
        slotMappingService.updateMapping(slot, mapping);
    }

}
