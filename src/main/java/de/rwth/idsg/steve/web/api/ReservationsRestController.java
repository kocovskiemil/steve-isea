package de.rwth.idsg.steve.web.api;

import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.repository.dto.ChargePointSelect;
import de.rwth.idsg.steve.repository.dto.InsertReservationParams;
import de.rwth.idsg.steve.repository.dto.Reservation;
import de.rwth.idsg.steve.service.ChargePointService15_Client;
import de.rwth.idsg.steve.service.ChargePointService16_Client;
import de.rwth.idsg.steve.service.ReservationService;
import de.rwth.idsg.steve.web.api.exception.BadRequestException;
import de.rwth.idsg.steve.web.dto.ReservationQueryForm;
import de.rwth.idsg.steve.web.dto.ocpp.CancelReservationParams;
import de.rwth.idsg.steve.web.dto.ocpp.ReserveNowParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Dictionary;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/reservations", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ReservationsRestController {

    private final ReservationService reservationService;
    private final ChargePointService16_Client v16Client;


    // -------------------------------------------------------------------------
    // OCPP operations
    // -------------------------------------------------------------------------

    // TODO: accepted, cancelled, used

    // -------------------------------------------------------------------------
    // CRUD operations
    // -------------------------------------------------------------------------

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiControllerAdvice.ApiErrorResponse.class)}
    )
    @GetMapping(value = "")
    @ResponseBody
    public List<Reservation> get(ReservationQueryForm params){
        log.debug("Read request for query: {}",params);

        var response = reservationService.getReservations(params);
        log.debug("Read response for query: {}", response);
        return response;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiControllerAdvice.ApiErrorResponse.class)}
    )
    @GetMapping("/{chargeBoxId}")
    @ResponseBody
    public List<Integer> getReservationsActiveForChargeBox(@PathVariable("chargeBoxId") String chargeBoxId) {
        log.debug("Read request for chargingProfilePk: {}", chargeBoxId);

        var response = getReservationsActiveForChargeBoxInternal(chargeBoxId);
        log.debug("Read response: {}", response);
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
    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public int create(@RequestBody @Valid ReserveNowParams params) {
        log.debug("Create request: {}", params);

        ChargePointService16_Client.enableReturnReservationId();
        int response = v16Client.reserveNow(params);

        log.debug("Create response: {}", response);
        log.debug("SENT PAYLOAD: {}", params);
        return response;
    }


    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 422, message = "Unprocessable Entity", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiControllerAdvice.ApiErrorResponse.class)
    })
    @DeleteMapping("/{reservationId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public int delete(@PathVariable("reservationId") int reservationId,
                      @RequestBody @Valid CancelReservationParams params) {
        log.debug("Delete request for ocppTagPk: {}", params.getReservationId());

        if (reservationId != params.getReservationId()) {
            throw new BadRequestException("Reservation ID in path and body must match");
        }

        v16Client.cancelReservation(params);

        log.debug("Deleting called for reservation: {}",params.getReservationId());

        return params.getReservationId();
    }
    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------
    private List<Integer> getReservationsActiveForChargeBoxInternal(String chargeBoxId) {
        ReservationQueryForm params = new ReservationQueryForm();
        params.setChargeBoxId(chargeBoxId);

        return reservationService.getActiveReservationIds(chargeBoxId);
    }
}
