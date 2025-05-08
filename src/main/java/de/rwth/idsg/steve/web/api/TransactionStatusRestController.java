/*
 * SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
 * Copyright (C) 2013-2024 SteVe Community Team
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.rwth.idsg.steve.web.api;

import de.rwth.idsg.steve.web.dto.ocpp.RemoteStartTransactionParams;
import de.rwth.idsg.steve.web.dto.ocpp.RemoteStopTransactionParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import de.rwth.idsg.steve.service.ChargePointService16_Client;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TransactionStatusRestController {

    private final ChargePointService16_Client v16Client;

    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Bad Request", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 422, message = "Unprocessable Entity", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiControllerAdvice.ApiErrorResponse.class)}
    )
    @PostMapping(value = "/start")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public int start(@RequestBody @Valid RemoteStartTransactionParams params) {
        log.debug("Create request: {}", params);

        int response = v16Client.remoteStartTransaction(params);

        log.debug("Create response: {}", response);
        log.debug("SENT PAYLOAD: {}", params);
        return response;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Bad Request", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 422, message = "Unprocessable Entity", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiControllerAdvice.ApiErrorResponse.class)}
    )
    @PostMapping(value = "/stop")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public int stop(@RequestBody @Valid RemoteStopTransactionParams params) {
        //params: Integer transactionId
        log.debug("Create request: {}", params);

        int response = v16Client.remoteStopTransaction(params);

        log.debug("Create response: {}", response);
        log.debug("SENT PAYLOAD: {}", params);
        return response;
    }
}
