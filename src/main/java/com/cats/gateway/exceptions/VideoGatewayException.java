package com.cats.gateway.exceptions;

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

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class VideoGatewayException extends ResponseStatusException {

    /**
     * Constructor to create an instance of VideoGatewayException
     *
     * @param status
     *      -- HttpStatus
     * */
    public VideoGatewayException(HttpStatus status) {
        super(status);
    }

    /**
     * Constructor to create an instance of VideoGatewayException
     *
     * @param status
     *      -- HttpStatus
     *
     * @param reason
     *      -- String
     * */
    public VideoGatewayException(HttpStatus status, String reason) {
        super(status, reason);
    }

    /**
     * Constructor to create an instance of VideoGatewayException
     *
     * @param status
     *      -- HttpStatus
     *
     * @param reason
     *      -- String
     *
     * @param cause
     *      -- Throwable
     * */
    public VideoGatewayException(HttpStatus status, String reason, Throwable cause) {
        super(status, reason, cause);
    }
}