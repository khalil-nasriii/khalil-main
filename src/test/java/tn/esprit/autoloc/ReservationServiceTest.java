package tn.esprit.autoloc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.service.IClientService;
import tn.esprit.autoloc.service.IReservationService;
import tn.esprit.autoloc.service.IVehiculeService;
import tn.esprit.autoloc.service.observer.EmailNotificationObserver;
import tn.esprit.autoloc.service.observer.SmsNotificationObserver;
import tn.esprit.autoloc.web.dto.ClientDTO;
import tn.esprit.autoloc.web.dto.ReservationRequestDTO;
import tn.esprit.autoloc.web.dto.ReservationResponseDTO;
import tn.esprit.autoloc.web.dto.VehiculeDTO;

/**
 * Atelier 6 : test du patron Observer.
 * Observer dans la console les lignes [EMAIL] ... et [SMS] ...
 * Les observateurs sont des "spy" : le vrai code s'exécute (logs) et l'appel est vérifié.
 */
@SpringBootTest
class ReservationServiceTest {

    @Autowired
    private IReservationService reservationService;
    @Autowired
    private IClientService clientService;
    @Autowired
    private IVehiculeService vehiculeService;

    @MockitoSpyBean
    private EmailNotificationObserver emailObserver;
    @MockitoSpyBean
    private SmsNotificationObserver smsObserver;

    private static Client client() {
        Client client = new Client();
        client.setNom("Ben Ali");
        client.setPrenom("Ahmed");
        client.setEmail("ahmed.benali@test.tn");
        client.setTelephone("+216 20 111 111");
        return client;
    }

    @Test
    void creerReservationDoitNotifierLesObservateurs() {
        Reservation reservation = reservationService.creerReservation(client());

        assertEquals(StatutReservation.CONFIRMEE, reservation.getStatut());
        // console : [EMAIL] Confirmation envoyee au client ... / [SMS] Confirmation envoyee au ...
        verify(emailObserver).onReservationConfirmee(reservation);
        verify(smsObserver).onReservationConfirmee(reservation);
    }

    @Test
    void annulerReservationDoitNotifierLesObservateurs() {
        Reservation reservation = reservationService.creerReservation(client());

        Reservation annulee = reservationService.annulerReservation(reservation);

        assertEquals(StatutReservation.ANNULEE, annulee.getStatut());
        // console : [EMAIL] Annulation notifiee au client ... / [SMS] Annulation notifiee au ...
        verify(emailObserver).onReservationAnnulee(reservation);
        verify(smsObserver).onReservationAnnulee(reservation);
    }

    @Test
    void creerPuisAnnulerUneReservationPersistee() {
        ClientDTO client = clientService.create(ClientDTO.builder()
                .nom("Trabelsi").prenom("Sarra").email("sarra.obs@test.tn").telephone("20222222").build());
        VehiculeDTO vehicule = vehiculeService.create(VehiculeDTO.builder()
                .immatriculation("777 TUN 7777").marque("Kia").modele("Picanto")
                .categorie(CategorieVehicule.CITADINE).tarifJournalier(new BigDecimal("100.00")).build());

        // 10 jours => stratégie LONGUE_DUREE : 10 x 100 x 0.80 = 800
        ReservationResponseDTO creee = reservationService.creer(ReservationRequestDTO.builder()
                .clientId(client.getId()).vehiculeId(vehicule.getId())
                .dateDebut(LocalDate.now().plusDays(1)).dateFin(LocalDate.now().plusDays(11)).build());

        assertNotNull(creee.getId());
        assertEquals(StatutReservation.CONFIRMEE, creee.getStatut());
        assertEquals("Sarra Trabelsi", creee.getClientNomComplet());
        assertEquals("777 TUN 7777", creee.getVehiculeImmatriculation());
        assertEquals(0, new BigDecimal("800.00").compareTo(creee.getTarifEstime()));

        ReservationResponseDTO annulee = reservationService.annuler(creee.getId());
        assertEquals(StatutReservation.ANNULEE, annulee.getStatut());

        reservationService.deleteById(creee.getId());
        vehiculeService.deleteById(vehicule.getId());
        clientService.deleteById(client.getId());
    }
}
