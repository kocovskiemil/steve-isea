package de.rwth.idsg.steve.service;

import de.rwth.idsg.steve.repository.ChargingProfileRepository;
import de.rwth.idsg.steve.repository.dto.ChargingProfile;
import de.rwth.idsg.steve.web.dto.ChargingProfileForm;
import de.rwth.idsg.steve.web.dto.ChargingProfileQueryForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingProfileService {

    private final ChargingProfileRepository chargingProfileRepository;


    // -------------------------------------------------------------------------
    // Ocpp operations
    // -------------------------------------------------------------------------

    public void setProfile(int chargingProfilePk, String chargeBoxId, int connectorId){
        chargingProfileRepository.setProfile(chargingProfilePk,chargeBoxId,connectorId);
    }

    // -------------------------------------------------------------------------
    // CRUD operations
    // -------------------------------------------------------------------------

    public List<ChargingProfile.Overview> getOverview(ChargingProfileQueryForm form){
        return chargingProfileRepository.getOverview(form);
    }
    public int addChargingProfile(ChargingProfileForm form){
        return chargingProfileRepository.add(form);
    }

    public void updateChargingProfile(ChargingProfileForm form) {
        chargingProfileRepository.update(form);
    }

    public void deleteChargingProfile(int chargingProfilePk) {
        chargingProfileRepository.delete(chargingProfilePk);
    }


}
