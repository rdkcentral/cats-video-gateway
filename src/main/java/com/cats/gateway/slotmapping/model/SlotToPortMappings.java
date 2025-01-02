package com.cats.gateway.slotmapping.model;

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

import com.cats.gateway.exceptions.SlotMappingException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Schema(description = "Represents the slot to port mappings.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlotToPortMappings {

    Map<String, String> slots = new HashMap<>();

    @Getter
    @Setter
    List<Device> devices;

    @Getter
    @Setter
    private String rackHost;

    @Getter
    @Setter
    private String rackIp;

    @Getter
    @Setter
    private Boolean useProxy = false;

    @Getter
    @Setter
    private String proxyBaseUrl;

    public Map<String, String> getMappings(){
        return slots;
    }

    @JsonProperty("slots")
    public void setMappings(Map<String, String> slots){
        this.slots = slots;
    }

    public void removeMappings(){
        this.slots = new HashMap<>();
    }

    public void addMapping(String slot, String deviceInfo){
        slots.put(slot, deviceInfo);
    }

    public void removeMapping(String slot) throws SlotMappingException {
        if(slots.containsKey(slot)){
            slots.put(slot,"N/A");
        }
        else{
            throw new SlotMappingException(HttpStatus.NOT_FOUND, "Slot " + slot + " is not mapped");
        }
    }

    public String getMapping(String slot) throws SlotMappingException{
        if(slots.containsKey(slot) && !slots.get(slot).equals("N/A")){
            return slots.get(slot);
        }
        throw new SlotMappingException(HttpStatus.NOT_FOUND, "Slot " + slot + " is not mapped");
    }
}
