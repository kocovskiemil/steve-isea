package de.rwth.idsg.steve.web.api;

import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.repository.dto.ChargingProfile;
import de.rwth.idsg.steve.repository.dto.OcppTag;
import de.rwth.idsg.steve.service.ChargingProfileService;
import de.rwth.idsg.steve.web.dto.ChargingProfileForm;
import de.rwth.idsg.steve.web.dto.ChargingProfileQueryForm;
import de.rwth.idsg.steve.web.dto.OcppTagForm;
import de.rwth.idsg.steve.web.dto.OcppTagQueryForm;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/chargingProfiles", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ChargingProfilesRestController {

    private final ChargingProfileService chargingProfileService;

    // -------------------------------------------------------------------------
    // OCPP operations
    // -------------------------------------------------------------------------

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiControllerAdvice.ApiErrorResponse.class)}
    )
    @PutMapping("/{chargingProfilePk}/{chargeBoxId}/{connectorId}")
    @ResponseBody
    public ChargingProfile.Overview setProfile(@PathVariable("chargingProfilePk") Integer chargingProfilePk, @PathVariable("chargeBoxId")String chargeBoxId, @PathVariable("connectorId") Integer connectorId){
        log.debug("SetProfile request for Profile Pk: {}, on box {}, connector {}", chargingProfilePk, chargeBoxId, connectorId);

        chargingProfileService.setProfile(chargingProfilePk, chargeBoxId, connectorId);
        var response = getOneInternal(chargingProfilePk);
        log.debug("SetProfile response: {}", response);

        return response;
    }

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
    public List<ChargingProfile.Overview> get(ChargingProfileQueryForm params){
        log.debug("Read request for query: {}",params);

        var response = chargingProfileService.getOverview(params);
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
    @GetMapping("/{chargingProfilePk}")
    @ResponseBody
    public ChargingProfile.Overview getOne(@PathVariable("chargingProfilePk") Integer chargingProfilePk) {
        log.debug("Read request for chargingProfilePk: {}", chargingProfilePk);

        var response = getOneInternal(chargingProfilePk);
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
    public ChargingProfile.Overview create(@RequestBody @Valid ChargingProfileForm params) {
        log.debug("Create request: {}", params);

        int chargingProfilePk = chargingProfileService.addChargingProfile(params);

        var response = getOneInternal(chargingProfilePk);
        log.debug("Create response: {}", response);
        return response;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiControllerAdvice.ApiErrorResponse.class)}
    )
    @PutMapping("/{chargingProfilePk}")
    @ResponseBody
    public ChargingProfile.Overview update(@PathVariable("chargingProfilePk") Integer chargingProfilePk, @RequestBody @Valid ChargingProfileForm params) {
        params.setChargingProfilePk(chargingProfilePk); // the one from incoming params does not matter
        log.debug("Update request: {}", params);

        chargingProfileService.updateChargingProfile(params);

        var response = getOneInternal(chargingProfilePk);
        log.debug("Update response: {}", response);
        return response;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiControllerAdvice.ApiErrorResponse.class)}
    )
    @DeleteMapping("/{chargingProfilePk}")
    @ResponseBody
    public ChargingProfile.Overview delete(@PathVariable("chargingProfilePk") Integer chargingProfilePk) {
        log.debug("Delete request for ocppTagPk: {}", chargingProfilePk);

        var response = getOneInternal(chargingProfilePk);
        chargingProfileService.deleteChargingProfile(chargingProfilePk);

        log.debug("Delete response: {}", response);
        return response;
    }
    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------
    private ChargingProfile.Overview getOneInternal(int chargingProfilePk) {
        ChargingProfileQueryForm params = new ChargingProfileQueryForm();
        params.setChargingProfilePk(chargingProfilePk);

        List<ChargingProfile.Overview> results = chargingProfileService.getOverview(params);
        if (results.isEmpty()) {
            throw new SteveException.NotFound("Could not find this Charging Profile");
        }
        return results.get(0);
    }
}
