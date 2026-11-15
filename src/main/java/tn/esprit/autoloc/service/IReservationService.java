package tn.esprit.autoloc.service;

import java.util.List;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.web.dto.ReservationRequestDTO;
import tn.esprit.autoloc.web.dto.ReservationResponseDTO;

public interface IReservationService {

    /** Crée une réservation confirmée, calcule le tarif estimé et notifie les observateurs. */
    ReservationResponseDTO creer(ReservationRequestDTO dto);

    ReservationResponseDTO findById(Long id);

    List<ReservationResponseDTO> findAll();

    ReservationResponseDTO update(Long id, ReservationRequestDTO dto);

    /** Passe la réservation au statut ANNULEE et notifie les observateurs. */
    ReservationResponseDTO annuler(Long id);

    void deleteById(Long id);

    // ---- Atelier 6 (patron Observer) : opérations métier sur l'entité ----

    /** Crée une réservation (non persistée) pour un client et notifie tous les observateurs. */
    Reservation creerReservation(Client client);

    /** Annule une réservation existante et notifie tous les observateurs. */
    Reservation annulerReservation(Reservation reservation);
}
