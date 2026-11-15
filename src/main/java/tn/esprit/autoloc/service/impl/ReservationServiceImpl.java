package tn.esprit.autoloc.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IClientRepository;
import tn.esprit.autoloc.repository.IReservationRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;
import tn.esprit.autoloc.service.IReservationService;
import tn.esprit.autoloc.service.factory.TarificationStrategyFactory;
import tn.esprit.autoloc.service.observer.ReservationSubject;
import tn.esprit.autoloc.web.dto.ReservationRequestDTO;
import tn.esprit.autoloc.web.dto.ReservationResponseDTO;
import tn.esprit.autoloc.web.mapper.ReservationMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements IReservationService {

    private static final String RESSOURCE = "Reservation";

    private final IReservationRepository reservationRepository;
    private final IClientRepository clientRepository;
    private final IVehiculeRepository vehiculeRepository;
    private final ReservationMapper reservationMapper;
    private final TarificationStrategyFactory tarificationStrategyFactory;
    private final ReservationSubject reservationSubject;

    @Override
    public ReservationResponseDTO creer(ReservationRequestDTO dto) {
        validerDates(dto);
        Reservation reservation = Reservation.builder()
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .client(chargerClient(dto.getClientId()))
                .vehicule(chargerVehicule(dto.getVehiculeId()))
                .statut(StatutReservation.CONFIRMEE)
                .build();
        Reservation saved = reservationRepository.save(reservation);

        // Déclenche le processus métier via le patron Observer (email, SMS...)
        reservationSubject.notifierConfirmation(saved);
        return versReponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDTO findById(Long id) {
        return versReponse(charger(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> findAll() {
        return reservationRepository.findAll().stream()
                .map(this::versReponse)
                .toList();
    }

    @Override
    public ReservationResponseDTO update(Long id, ReservationRequestDTO dto) {
        Reservation existant = charger(id);
        validerDates(dto);
        existant.setDateDebut(dto.getDateDebut());
        existant.setDateFin(dto.getDateFin());
        existant.setClient(chargerClient(dto.getClientId()));
        existant.setVehicule(chargerVehicule(dto.getVehiculeId()));
        return versReponse(reservationRepository.save(existant));
    }

    @Override
    public ReservationResponseDTO annuler(Long id) {
        Reservation reservation = charger(id);
        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new IllegalStateException("La réservation " + id + " est déjà annulée");
        }
        annulerReservation(reservation);
        return versReponse(reservationRepository.save(reservation));
    }

    @Override
    public void deleteById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        reservationRepository.deleteById(id);
    }

    @Override
    public Reservation creerReservation(Client client) {
        Reservation reservation = Reservation.builder()
                .client(client)
                .dateDebut(LocalDate.now())
                .statut(StatutReservation.CONFIRMEE)
                .build();

        // Déclenche le processus métier via le patron Observer
        reservationSubject.notifierConfirmation(reservation);
        return reservation;
    }

    @Override
    public Reservation annulerReservation(Reservation reservation) {
        reservation.setStatut(StatutReservation.ANNULEE);
        log.info("Reservation #{} annulée pour le client {}",
                reservation.getIdReservation(),
                reservation.getClient() == null ? "?" : reservation.getClient().getEmail());

        reservationSubject.notifierAnnulation(reservation);
        return reservation;
    }

    private ReservationResponseDTO versReponse(Reservation reservation) {
        ReservationResponseDTO dto = reservationMapper.toDto(reservation);
        dto.setTarifEstime(estimerTarif(reservation));
        return dto;
    }

    /** Patron Strategy : la factory choisit la stratégie adaptée à la durée du séjour. */
    private BigDecimal estimerTarif(Reservation reservation) {
        Vehicule vehicule = reservation.getVehicule();
        if (vehicule == null || vehicule.getTarifJournalier() == null
                || reservation.getDateDebut() == null || reservation.getDateFin() == null) {
            return null;
        }
        return tarificationStrategyFactory.resoudre(reservation).calculerTarif(vehicule, reservation);
    }

    private Reservation charger(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    private Client chargerClient(Long clientId) {
        Regles.exigerNonNul(clientId, "Le client est obligatoire");
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));
    }

    private Vehicule chargerVehicule(Long vehiculeId) {
        Regles.exigerNonNul(vehiculeId, "Le véhicule est obligatoire");
        return vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", vehiculeId));
    }

    private void validerDates(ReservationRequestDTO dto) {
        Regles.exigerNonNul(dto.getDateDebut(), "La date de début est obligatoire");
        Regles.exigerNonNul(dto.getDateFin(), "La date de fin est obligatoire");
        Regles.exigerFinApresDebut(dto.getDateDebut(), dto.getDateFin(),
                "La date de fin ne peut pas être antérieure à la date de début");
    }
}
