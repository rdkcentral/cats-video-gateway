package com.cats.gateway.slotmapping.service;

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

import com.cats.gateway.config.Configuration;
import com.cats.gateway.slotmapping.model.Device;
import com.cats.gateway.slotmapping.model.SlotToPortMappings;
import com.cats.gateway.exceptions.SlotMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SlotMappingService {

    private SlotToPortMappings slotToPortMappings;

    @Autowired
    private Configuration config;
    private final ObjectMapper mapper = new ObjectMapper();
    private String MAPPING_FILEPATH;

    @PostConstruct
    private void initializePortMapping() {
        try {
            FileInputStream in = new FileInputStream(new File(config.getSlotMappingFilePath()));
            slotToPortMappings = mapper.readValue(in, SlotToPortMappings.class);
            if (slotToPortMappings.getMappings().isEmpty()) {
                return;
            }
        } catch (IOException ex) {
            log.error("Could not process slot mappings file, using default values: " + ex.getLocalizedMessage());
            slotToPortMappings = new SlotToPortMappings();
        }
    }

    public SlotToPortMappings getMappings() {
        initializePortMapping();
        return slotToPortMappings;
    }

    public void setMappings(Map<String, String> mappings) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(config.getSlotMappingFilePath(),false))) {
            log.info("Setting new mapping: " + mapper.writeValueAsString(mappings));
            this.slotToPortMappings.setMappings(mappings);
            writer.write(mapper.writeValueAsString(this.slotToPortMappings));
            writer.close();
            log.info("Slot to port mappings file updated");

        } catch (IOException ex) {
            log.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
    }

    public void removeMappings() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(config.getSlotMappingFilePath()))) {
            log.info("Removing slot to port mappings");
            this.slotToPortMappings.removeMappings();
            log.info("Slot to port mappings have been removed");
            writer.write(mapper.writeValueAsString(this.slotToPortMappings));
        } catch (IOException ex) {
            log.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
    }

    public String getMapping(String slot) {
        try {
            log.info("slots {}",slotToPortMappings.getMappings());
            return slotToPortMappings.getMapping(slot);
        } catch (SlotMappingException ex) {
            log.error("Could not locate mapping for slot: " + slot);
            throw ex;
        }
    }

    public SlotToPortMappings updateMapping(Map<String, String> mapping) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(config.getSlotMappingFilePath(),false))) {
            log.info("Setting new mapping: " + mapper.writeValueAsString(mapping));
            mapping.forEach((key, value) -> this.slotToPortMappings.addMapping(key,value));
            writer.write(mapper.writeValueAsString(this.slotToPortMappings));
            writer.close();
            log.info("Slot to port mappings file updated");
        } catch (IOException ex) {
            log.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
        return this.slotToPortMappings;
    }

    public void removeMapping(String slot) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(config.getSlotMappingFilePath(),false))) {
            log.info("Removing mapping on slot " + slot);
            this.slotToPortMappings.removeMapping(slot);
            writer.write(mapper.writeValueAsString(this.slotToPortMappings));
            log.info("Slot " + slot + " mapping removed");

        } catch (IOException | SlotMappingException ex) {
            log.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
    }

    /**
     * Method to validate the mapping for a slot. The mapping should be in the format of "device:outlet".
     *
     * @param mappings
     *      The mapping to validate.
     * */
    private boolean isValidMapping(@Valid @NotNull(message = "Mappings cannot be null.") String mappings) {
        boolean valid = false;
        if (mappings.equals("N/A")) {
            return true;
        }
        try {
            String[] deviceAndOutlet = mappings.split(":");
            for (Device device : this.slotToPortMappings.getDevices()) {
                if (device.getId().equals(Integer.valueOf(deviceAndOutlet[0]))) {
                    if (device.getMaxPort() >= Integer.parseInt(deviceAndOutlet[1])) {
                        valid = true;
                        break;
                    }
                }
            }
        } catch (NumberFormatException ex) {
            log.error("Invalid device info: " + mappings);
        }
        return valid;
    }

    /**
     * Method to update slot mapping. If the slot already has a mapping, it will be removed and replaced with the new mapping.
     *
     * @param slot
     *      The slot to update the mapping for.
     * @param mapping
     *      The new mapping to set for the slot.
     * */
    public void updateMapping(@Valid @NotNull(message = "Slot cannot be null.") String slot, @Valid @NotNull(message = "Mapping cannot be null") String mapping) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(config.getSlotMappingFilePath()))) {
            // added mapping.replaceAll("^\"|\"$", "") to remove quotes from the mapping. This is integrated to make
            // the service backward compatible as the legacy video ms is accepting the mapping with quotes.
            String newMappings = mapping.replaceAll("^\"|\"$", "");
            if (!isValidMapping(newMappings)) {
                log.error("Invalid mapping for slot " + slot + ": " + newMappings);
                writer.write(mapper.writeValueAsString(this.slotToPortMappings));
                throw new IllegalArgumentException("Invalid mapping for slot " + slot + ": " + newMappings);
            }
            if (this.slotToPortMappings.getMappings().containsKey(slot)) {
                this.slotToPortMappings.removeMapping(slot);
            }
            log.info("Setting mapping on slot " + slot + " to " + newMappings);
            log.info("Updating mapping for slot " + slot + " with " + newMappings);
            this.slotToPortMappings.addMapping(slot, newMappings);
            writer.write(mapper.writeValueAsString(this.slotToPortMappings));
            log.info("Slot " + slot + " mapping updated");
        } catch (IOException | SlotMappingException ex) {
            log.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
    }

    public List<Device> getVideoDevices() {
        return this.slotToPortMappings.getDevices();
    }
}
