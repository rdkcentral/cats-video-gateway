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

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.SocketTimeoutException;

@ControllerAdvice
@RestController
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle IllegalArgumentException
     *
     * @param e
     *  -- IllegalArgumentException
     * @return
     *  -- error message
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Exception caught : {}", e.getMessage());
        return e.getMessage();
    }

    /**
     * Handle SlotMappingException
     *
     * @param e
     *  -- SlotMappingException
     * @return
     *  -- error message
     */
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = SlotMappingException.class)
    public String slotMappingException(SlotMappingException e) {
        log.warn("Exception caught : {}", e.getMessage());
        return e.getMessage();
    }

    /**
     * Handle VideoGatewayException
     *
     * @param e
     *  -- VideoGatewayException
     * @return
     *  -- error message
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = VideoGatewayException.class)
    public String videoGatewayException(VideoGatewayException e) {
        log.warn("Exception caught : {}", e.getMessage());
        return e.getMessage();
    }

    /**
     * Handle SocketTimeoutException
     *
     * @param e
     *  -- SocketTimeoutException
     * @return
     *  -- error message
     */
    @ResponseStatus(code = HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(value = SocketTimeoutException.class)
    public String socketTimeOutException(SocketTimeoutException e) {
        log.warn("Exception caught : {}", e.getMessage());
        return e.getMessage();
    }

    /**
     * Handle UnsupportedOperationException
     *
     * @param e
     *  -- UnsupportedOperationException
     * @return
     *  -- error message
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = UnsupportedOperationException.class)
    public String handleUnSupportedOperationException(UnsupportedOperationException e) {
        log.warn("Exception caught : {}", e.getMessage());
        return e.getMessage();
    }
}
