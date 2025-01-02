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

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Custom global exception handler Spring Cloud Gateway.
 * This component is responsible for handling exceptions that occur during request processing
 * and providing customized error responses based on the type of exceptions.
 * */
@Component
@Order(-2)
public class ReactiveGlobalExceptionHandler implements ErrorWebExceptionHandler {

    /**
     * This method is invoked when an exception is occurred while proxying the api calls.
     *
     * @param serverWebExchange
     *      -- Instance of ServerWebExchange
     *
     * @param throwable
     *      -- Throwable
     * */
    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        ServerHttpResponse response = serverWebExchange.getResponse();;

        if (throwable instanceof ResponseStatusException responseStatusException) {
            HttpStatusCode httpStatus = responseStatusException.getStatusCode();
            String errorMessage = responseStatusException.getReason();

            return handleErrorResponse(response, httpStatus, errorMessage);
        }

        if (throwable instanceof IllegalArgumentException) {
            return handleIllegalArgumentException(response, (IllegalArgumentException) throwable);
        }

        if (throwable instanceof UnknownHostException) {
            return handleUnknownHostException(response, (UnknownHostException) throwable);
        }

        if (throwable instanceof UnsupportedOperationException) {
            return handleUnsupportedOperationException(response, (UnsupportedOperationException) throwable);
        }

        if (throwable instanceof IOException) {
            return handleIOException(response, (IOException) throwable);
        }

        return handleErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server error");
    }

    /**
     * This method is responsible for handling the error response.
     *
     * @param response
     *      -- Instance of ServerHttpResponse
     *
     * @param httpStatus
     *      -- HttpStatus
     *
     * @param errorMessage
     *      -- String
     * */
    private Mono<Void> handleErrorResponse(ServerHttpResponse response, HttpStatus httpStatus, String errorMessage) {
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.TEXT_PLAIN);

        DataBufferFactory dataBufferFactory = response.bufferFactory();

        String errorResponse = String.format("%s", errorMessage);

        return response.writeWith(Mono.just(dataBufferFactory.wrap(errorResponse.getBytes())));
    }

    /**
     * This method is responsible for handling the error response for IllegalArgumentException.
     *
     * @param response
     *      -- Instance of ServerHttpResponse
     *
     * @param httpStatus
     *      -- HttpStatus
     *
     * @param errorMessage
     *     -- String
     * */
    private Mono<Void> handleErrorResponse(ServerHttpResponse response, HttpStatusCode httpStatus, String errorMessage) {
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.TEXT_PLAIN);

        DataBufferFactory dataBufferFactory = response.bufferFactory();

        String errorResponse = String.format("%s", errorMessage);

        return response.writeWith(Mono.just(dataBufferFactory.wrap(errorResponse.getBytes())));
    }

    /**
     * This method is responsible for handling the error response for IllegalArgumentException.
     *
     * @param response
     *      -- Instance of ServerHttpResponse
     *
     * @param ex
     *      -- IllegalArgumentException
     * */
    private Mono<Void> handleIllegalArgumentException(ServerHttpResponse response, IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorMessage = ex.getMessage();


        return handleErrorResponse(response, status, errorMessage);
    }

    /**
     * This method is responsible for handling the error response for UnknownHostException.
     *
     * @param response
     *      -- Instance of ServerHttpResponse
     *
     * @param ex
     *      -- UnknownHostException
     * */
    private Mono<Void> handleUnknownHostException(ServerHttpResponse response, UnknownHostException ex) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        String errorMessage = ex.getMessage();


        return handleErrorResponse(response, status, errorMessage);
    }

    /**
     * This method is responsible for handling the error response for UnsupportedOperationException.
     *
     * @param response
     *      -- Instance of ServerHttpResponse
     *
     * @param ex
     *      -- UnsupportedOperationException
     * */
    private Mono<Void> handleUnsupportedOperationException(ServerHttpResponse response, UnsupportedOperationException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorMessage = ex.getMessage();


        return handleErrorResponse(response, status, errorMessage);
    }

    /**
     * This method is responsible for handling the error response for IOException.
     *
     * @param response
     *      -- Instance of ServerHttpResponse
     *
     * @param ex
     *      -- IOException
     * */
    private Mono<Void> handleIOException(ServerHttpResponse response, IOException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorMessage = ex.getMessage();


        return handleErrorResponse(response, status, errorMessage);
    }
}
