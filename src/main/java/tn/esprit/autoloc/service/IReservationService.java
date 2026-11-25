package tn.esprit.autoloc.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.web.dto.ReservationRequestDTO;
import tn.esprit.autoloc.web.dto.ReservationResponseDTO;
import tn.esprit.autoloc.web.dto.TopClientDTO;

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


    // ---------- Atelier 8 — recherches (méthodes dérivées) ----------

    List<ReservationResponseDTO> findByClient(Long clientId);

    List<ReservationResponseDTO> findByVehiculeHorsStatut(Long vehiculeId, StatutReservation statutExclu);

    List<ReservationResponseDTO> findByStatut(StatutReservation statut);

    List<ReservationResponseDTO> findByDateDebutEntre(LocalDate debut, LocalDate fin);

    List<ReservationResponseDTO> findByDateFinApres(LocalDate date);

    List<ReservationResponseDTO> findByStatutAndAgence(StatutReservation statut, Long agenceId);

    boolean existsByVehiculeAndStatut(Long vehiculeId, StatutReservation statut);

    // ---------- Atelier 8 — agrégation JPQL ----------

    List<TopClientDTO> topClients(Pageable pageable);
}
