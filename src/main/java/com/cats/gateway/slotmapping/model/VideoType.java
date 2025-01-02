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

import lombok.Getter;

@Getter
public enum VideoType {
    AXIS_P7216("Axis.P7216"),
    AXIS_FA54("Axis.FA54"),
    HANWHA_SPE_1620("Hanwha.SPE-1620");

    private String type;

    VideoType(String type) {
        this.type = type;
    }

    public static VideoType findType(String scheme){
        VideoType retVal = null;

        for(VideoType type : VideoType.values()){
            if(type.getType().equals(scheme)){
                retVal = type;
                break;
            }
        }

        return retVal;
    }
}
