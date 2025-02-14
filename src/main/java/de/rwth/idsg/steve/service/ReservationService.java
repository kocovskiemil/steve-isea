package de.rwth.idsg.steve.service;

import de.rwth.idsg.steve.repository.ReservationRepository;
import de.rwth.idsg.steve.repository.dto.InsertReservationParams;
import de.rwth.idsg.steve.repository.dto.Reservation;
import de.rwth.idsg.steve.web.dto.ReservationQueryForm;
import de.rwth.idsg.steve.web.dto.ocpp.ReserveNowParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;


    // -------------------------------------------------------------------------
    // Ocpp operations
    // -------------------------------------------------------------------------

    // TODO: accepted, cancelled, used

    // -------------------------------------------------------------------------
    // CRUD operations
    // -------------------------------------------------------------------------

    public List<Reservation> getReservations(ReservationQueryForm form){
        return reservationRepository.getReservations(form);
    }

    public List<Integer> getActiveReservationIds(String chargeBoxId){
        return reservationRepository.getActiveReservationIds(chargeBoxId);
    }
    public int insertReservation(InsertReservationParams params){

        return reservationRepository.insert(params);
    }
    public void deleteReservation(int reservationId) {
        reservationRepository.delete(reservationId);
    }


}
