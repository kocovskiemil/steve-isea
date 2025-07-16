package de.rwth.idsg.steve.web.api;

import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.repository.dto.ChargingProfile;
import de.rwth.idsg.steve.repository.dto.OcppTag;
import de.rwth.idsg.steve.service.ChargePointService16_Client;
import de.rwth.idsg.steve.web.dto.ocpp.ChangeAvailabilityParams;
import de.rwth.idsg.steve.web.api.ApiControllerAdvice;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
// ...existing code...
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/chargePoint16", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ChargePointRestController16 {
    private final ChargePointService16_Client chargePointService16Client;

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request", response =  ApiControllerAdvice.ApiErrorResponse.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ApiControllerAdvice.ApiErrorResponse.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiControllerAdvice.ApiErrorResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiControllerAdvice.ApiErrorResponse.class)}
    )
    @PutMapping("/changeAvailability")
    @ResponseBody
    public int changeAvailability(@RequestBody @Valid ChangeAvailabilityParams params) {
        int response = chargePointService16Client.changeAvailability(params);
        return response;
    }   
}