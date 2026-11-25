package tn.esprit.autoloc;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.ModePaiement;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.RoleEmploye;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.IAgenceRepository;
import tn.esprit.autoloc.repository.IClientRepository;
import tn.esprit.autoloc.repository.IContratRepository;
import tn.esprit.autoloc.repository.IEmployeRepository;
import tn.esprit.autoloc.repository.IPaiementRepository;
import tn.esprit.autoloc.repository.IReservationRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;
import tn.esprit.autoloc.web.dto.ChiffreAffaireAgenceDTO;
import tn.esprit.autoloc.web.dto.TopClientDTO;

/**
 * Atelier 8 — Query Methods et JPQL.
 * Chaque test crée son propre jeu de données (valeurs uniques : villes "…8", année 2030)
 * puis @Transactional annule tout à la fin : la base reste propre, données de démo comprises.
 */
@SpringBootTest
@Transactional
class RequetesTests {

    private static final String VILLE_A = "Nabeul8";
    private static final String VILLE_B = "Kelibia8";

    @Autowired private IAgenceRepository agenceRepository;
    @Autowired private IVehiculeRepository vehiculeRepository;
    @Autowired private IClientRepository clientRepository;
    @Autowired private IReservationRepository reservationRepository;
    @Autowired private IEmployeRepository employeRepository;
    @Autowired private IContratRepository contratRepository;
    @Autowired private IPaiementRepository paiementRepository;

    private Agence agenceA;
    private Agence agenceA2;
    private Agence agenceB;
    private Vehicule v1Berline100;
    private Vehicule v2Berline200;
    private Vehicule v3BerlineMaintenance;
    private Vehicule v4SuvLoue;
    private Client c1;
    private Client c2;
    private Reservation r1;
    private Contrat k1;
    private Contrat k2;
    private Contrat k3Invalide;
    private Contrat k4;
    private Paiement p2;

    // ------------------------------------------------------------------ jeu de données

    @BeforeEach
    void jeuDeDonnees() {
        agenceA = agence("Agence Test8 Nabeul", VILLE_A);
        agenceA2 = agence("Agence Test8 Nabeul Gare", VILLE_A);
        agenceB = agence("Agence Test8 Kelibia", VILLE_B);

        v1Berline100 = vehicule("801 TUN 0001", CategorieVehicule.BERLINE, "100", StatutVehicule.DISPONIBLE, agenceA);
        v2Berline200 = vehicule("801 TUN 0002", CategorieVehicule.BERLINE, "200", StatutVehicule.DISPONIBLE, agenceA);
        v3BerlineMaintenance = vehicule("801 TUN 0003", CategorieVehicule.BERLINE, "150", StatutVehicule.MAINTENANCE, agenceA);
        v4SuvLoue = vehicule("801 TUN 0004", CategorieVehicule.SUV, "300", StatutVehicule.LOUE, agenceB);

        c1 = client("Testnom8Alpha", "c1.atelier8@test.tn", "PERMIS-8-001");
        c2 = client("Testnom8Beta", "c2.atelier8@test.tn", "PERMIS-8-002");

        employe("Empnom8Un", RoleEmploye.AGENT, agenceA);
        employe("Empnom8Deux", RoleEmploye.MANAGER, agenceA);
        employe("Empnom8Trois", RoleEmploye.AGENT, agenceB);

        k1 = contrat("300", true);
        k2 = contrat("200", true);
        k3Invalide = contrat("1000", false);
        k4 = contrat("50", true);
        k1.ajouterPaiement(paiement("100", "2030-01-10"));
        p2 = paiement("200", "2030-01-12");
        k1.ajouterPaiement(p2);
        contratRepository.flush(); // cascade PERSIST : les paiements reçoivent leur identifiant

        // c1 : 3 réservations ; c2 : 2 réservations
        r1 = reservation(c1, v1Berline100, "2030-01-10", "2030-01-15", StatutReservation.CONFIRMEE, k1);
        reservation(c1, v4SuvLoue, "2029-12-20", "2030-01-01", StatutReservation.CONFIRMEE, k3Invalide);
        reservation(c1, v2Berline200, "2030-01-10", "2030-01-15", StatutReservation.ANNULEE, k2);
        reservation(c2, v4SuvLoue, "2030-02-01", "2030-02-05", StatutReservation.EN_ATTENTE, k4);
        reservation(c2, v4SuvLoue, "2030-02-10", "2030-02-12", StatutReservation.EN_ATTENTE, null);
    }

    // ------------------------------------------------------------------ Partie I

    @Test
    void exercice1_vehicules() {
        assertThat(ids(vehiculeRepository.findByAgenceVille(VILLE_A)))
                .containsExactlyInAnyOrder(id(v1Berline100), id(v2Berline200), id(v3BerlineMaintenance));
        assertThat(vehiculeRepository.findByAgenceVille("VilleInexistante8")).isEmpty();

        assertThat(ids(vehiculeRepository.findByStatutAndAgence_IdAgence(StatutVehicule.DISPONIBLE, agenceA.getIdAgence())))
                .containsExactlyInAnyOrder(id(v1Berline100), id(v2Berline200));
        assertThat(vehiculeRepository.findByStatutAndAgence_IdAgence(StatutVehicule.DISPONIBLE, agenceA2.getIdAgence())).isEmpty();

        // pagination : 1 élément par page, total = countByStatut
        Page<Vehicule> page = vehiculeRepository.findByStatut(StatutVehicule.MAINTENANCE, PageRequest.of(0, 1));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getSize()).isEqualTo(1);
        assertThat(page.getTotalElements()).isEqualTo(vehiculeRepository.countByStatut(StatutVehicule.MAINTENANCE));
        assertThat(vehiculeRepository.countByStatut(StatutVehicule.MAINTENANCE)).isPositive();

        assertThat(ids(vehiculeRepository.findByAgence_VilleAndCategorie(VILLE_A, CategorieVehicule.BERLINE)))
                .containsExactlyInAnyOrder(id(v1Berline100), id(v2Berline200), id(v3BerlineMaintenance));
        assertThat(vehiculeRepository.findByAgence_VilleAndCategorie(VILLE_A, CategorieVehicule.SUV)).isEmpty();

        assertThat(vehiculeRepository.existsByIdVehiculeAndStatut(id(v3BerlineMaintenance), StatutVehicule.MAINTENANCE)).isTrue();
        assertThat(vehiculeRepository.existsByIdVehiculeAndStatut(id(v3BerlineMaintenance), StatutVehicule.DISPONIBLE)).isFalse();

        // comparaisons strictes : 150 n'est ni < 150 ni > 150
        assertThat(ids(vehiculeRepository.findByTarifJournalierLessThan(new BigDecimal("150"))))
                .contains(id(v1Berline100)).doesNotContain(id(v2Berline200), id(v3BerlineMaintenance));
        assertThat(ids(vehiculeRepository.findByTarifJournalierGreaterThan(new BigDecimal("150"))))
                .contains(id(v2Berline200), id(v4SuvLoue)).doesNotContain(id(v1Berline100), id(v3BerlineMaintenance));

        List<BigDecimal> tarifs = vehiculeRepository.findAllByOrderByTarifJournalierAsc().stream()
                .map(Vehicule::getTarifJournalier).filter(Objects::nonNull).toList();
        for (int i = 1; i < tarifs.size(); i++) {
            assertThat(tarifs.get(i - 1)).isLessThanOrEqualTo(tarifs.get(i));
        }
    }

    @Test
    void exercice2_clients() {
        assertThat(clientRepository.findByEmail("c1.atelier8@test.tn")).get()
                .extracting(Client::getIdClient).isEqualTo(c1.getIdClient());
        assertThat(clientRepository.findByEmail("personne8@test.tn")).isEmpty();
        assertThat(clientRepository.findByNumPermis("PERMIS-8-002")).get()
                .extracting(Client::getIdClient).isEqualTo(c2.getIdClient());
        assertThat(clientRepository.findByNumPermis("PERMIS-INCONNU")).isEmpty();
        assertThat(clientRepository.findByNomContaining("Testnom8")).hasSize(2);
        assertThat(clientRepository.findByNomContaining("Alpha")).extracting(Client::getIdClient)
                .containsExactly(c1.getIdClient());
        assertThat(clientRepository.existsByEmail("c2.atelier8@test.tn")).isTrue();
        assertThat(clientRepository.existsByEmail("personne8@test.tn")).isFalse();
    }

    @Test
    void exercice3_reservations() {
        assertThat(reservationRepository.findByClientIdClient(c1.getIdClient())).hasSize(3);
        assertThat(reservationRepository.findByClientIdClient(999_999L)).isEmpty();

        assertThat(reservationRepository.findByVehicule_IdVehiculeAndStatutNot(id(v2Berline200), StatutReservation.ANNULEE)).isEmpty();
        assertThat(reservationRepository.findByVehicule_IdVehiculeAndStatutNot(id(v4SuvLoue), StatutReservation.ANNULEE)).hasSize(3);

        assertThat(reservationRepository.findByStatut(StatutReservation.EN_ATTENTE))
                .filteredOn(r -> r.getClient() != null && c2.getIdClient().equals(r.getClient().getIdClient())).hasSize(2);

        // Between : bornes incluses
        assertThat(reservationRepository.findByDateDebutBetween(LocalDate.parse("2030-02-01"), LocalDate.parse("2030-02-10")))
                .hasSize(2);
        assertThat(reservationRepository.findByDateDebutBetween(LocalDate.parse("2031-01-01"), LocalDate.parse("2031-12-31")))
                .isEmpty();

        // After : strictement après
        assertThat(reservationRepository.findByDateFinAfter(LocalDate.parse("2030-02-05")))
                .extracting(Reservation::getDateFin).containsOnly(LocalDate.parse("2030-02-12"));

        assertThat(reservationRepository.findByStatutAndVehiculeAgenceIdAgence(StatutReservation.CONFIRMEE, agenceA.getIdAgence()))
                .extracting(Reservation::getIdReservation).containsExactly(r1.getIdReservation());

        assertThat(reservationRepository.existsByVehicule_IdVehiculeAndStatut(id(v2Berline200), StatutReservation.ANNULEE)).isTrue();
        assertThat(reservationRepository.existsByVehicule_IdVehiculeAndStatut(id(v2Berline200), StatutReservation.CONFIRMEE)).isFalse();
    }

    @Test
    void exercices4a6_employesAgencesPaiements() {
        assertThat(employeRepository.findByAgenceNom("Agence Test8 Nabeul")).hasSize(2);
        assertThat(employeRepository.findByAgence_IdAgenceAndRole(agenceA.getIdAgence(), RoleEmploye.AGENT)).hasSize(1);
        assertThat(employeRepository.findByAgence_IdAgenceAndRole(agenceA2.getIdAgence(), RoleEmploye.AGENT)).isEmpty();
        assertThat(employeRepository.findByNomContaining("Empnom8")).hasSize(3);

        assertThat(agenceRepository.findFirstByVilleOrderByIdAgenceAsc(VILLE_A)).get()
                .extracting(Agence::getIdAgence).isEqualTo(agenceA.getIdAgence());
        assertThat(agenceRepository.findFirstByVilleOrderByIdAgenceAsc("VilleInexistante8")).isEmpty();
        assertThat(agenceRepository.findByVille(VILLE_A)).hasSize(2);
        assertThat(agenceRepository.findByNomContaining("Test8")).hasSize(3);

        assertThat(paiementRepository.findByContrat_IdContrat(k1.getIdContrat())).hasSize(2);
        assertThat(paiementRepository.findByContrat_IdContrat(k4.getIdContrat())).isEmpty();
        assertThat(paiementRepository.findByDatePaiementBetween(LocalDate.parse("2030-01-11"), LocalDate.parse("2030-01-12")))
                .extracting(Paiement::getIdPaiement).containsExactly(p2.getIdPaiement());
        assertThat(paiementRepository.findByDatePaiementBetween(LocalDate.parse("2031-01-01"), LocalDate.parse("2031-01-31")))
                .isEmpty();
    }

    // ------------------------------------------------------------------ Partie II (Exercice 10)

    @Test
    void exercice7_1_vehiculesDisponibles_chevauchementsEtBornes() {
        // chevauchement avec la réservation CONFIRMEE de v1 (10 -> 15) ; v2 a une réservation ANNULEE (ignorée) ;
        // v3 est en maintenance
        assertThat(ids(disponibles("2030-01-12", "2030-01-14"))).containsExactly(id(v2Berline200));
        // chevauchement d'un seul jour
        assertThat(ids(disponibles("2030-01-14", "2030-01-16"))).containsExactly(id(v2Berline200));
        // bornes qui se touchent : la nouvelle location commence le jour du retour -> v1 disponible
        assertThat(ids(disponibles("2030-01-15", "2030-01-20"))).containsExactly(id(v1Berline100), id(v2Berline200));
        // bornes qui se touchent : la nouvelle location se termine le jour du départ
        assertThat(ids(disponibles("2030-01-05", "2030-01-10"))).containsExactly(id(v1Berline100), id(v2Berline200));
        // aucune réservation sur la période : tri par tarif croissant
        assertThat(ids(disponibles("2030-03-01", "2030-03-05"))).containsExactly(id(v1Berline100), id(v2Berline200));
        // aucun résultat
        assertThat(vehiculeRepository.rechercherVehiculesDisponibles("VilleInexistante8", CategorieVehicule.BERLINE,
                LocalDate.parse("2030-01-12"), LocalDate.parse("2030-01-14"))).isEmpty();
        assertThat(vehiculeRepository.rechercherVehiculesDisponibles(VILLE_A, CategorieVehicule.UTILITAIRE,
                LocalDate.parse("2030-01-12"), LocalDate.parse("2030-01-14"))).isEmpty();
    }

    @Test
    void exercice7_2_vehiculesALiberer() {
        // la réservation CONFIRMEE de v4 se termine le 01/01/2030
        assertThat(ids(vehiculeRepository.trouverVehiculesAReleaser(LocalDate.parse("2030-01-01")))).contains(id(v4SuvLoue));
        assertThat(ids(vehiculeRepository.trouverVehiculesAReleaser(LocalDate.parse("2029-12-31")))).doesNotContain(id(v4SuvLoue));
        // les réservations EN_ATTENTE de v4 en février ne la rendent pas "à libérer" plus tard non plus
        assertThat(ids(vehiculeRepository.trouverVehiculesAReleaser(LocalDate.parse("2030-01-15"))))
                .contains(id(v1Berline100), id(v4SuvLoue)).doesNotContain(id(v2Berline200));
    }

    @Test
    void exercice8_topClients() {
        List<TopClientDTO> top = reservationRepository.topClients(PageRequest.of(0, 5));

        assertThat(top).hasSizeLessThanOrEqualTo(5);
        for (int i = 1; i < top.size(); i++) {
            assertThat(top.get(i - 1).nombreReservations()).isGreaterThanOrEqualTo(top.get(i).nombreReservations());
        }
        assertThat(top).contains(
                new TopClientDTO(c1.getIdClient(), "Testnom8Alpha", "Prenom8", 3L),
                new TopClientDTO(c2.getIdClient(), "Testnom8Beta", "Prenom8", 2L));
        assertThat(top.indexOf(new TopClientDTO(c1.getIdClient(), "Testnom8Alpha", "Prenom8", 3L)))
                .isLessThan(top.indexOf(new TopClientDTO(c2.getIdClient(), "Testnom8Beta", "Prenom8", 2L)));

        // la pagination limite le nombre de lignes
        assertThat(reservationRepository.topClients(PageRequest.of(0, 1))).hasSize(1);
    }

    @Test
    void exercice9_1_chiffreAffairesParAgence() {
        List<ChiffreAffaireAgenceDTO> ca = contratRepository.chiffreAffaireParAgence();

        // agence A : k1 (300) + k2 (200) ; agence B : k4 (50), k3 invalide (1000) exclu ; agence A2 : aucun contrat
        assertThat(montant(ca, agenceA)).isEqualByComparingTo("500.00");
        assertThat(montant(ca, agenceB)).isEqualByComparingTo("50.00");
        assertThat(ca).noneMatch(l -> l.agenceId().equals(agenceA2.getIdAgence()));
        assertThat(ca).filteredOn(l -> l.agenceId().equals(agenceA.getIdAgence()))
                .extracting(ChiffreAffaireAgenceDTO::nomAgence).containsExactly("Agence Test8 Nabeul");
        for (int i = 1; i < ca.size(); i++) {
            assertThat(ca.get(i - 1).chiffreAffaires()).isGreaterThanOrEqualTo(ca.get(i).chiffreAffaires());
        }
    }

    @Test
    void exercice9_2_contratsAEcheanceProche() {
        // k1 et k2 : réservations finissant le 15/01/2030 ; k3 (01/01/2030) est invalide ; k4 finit le 05/02/2030
        assertThat(contratRepository.trouverContratsAEcheanceProche(LocalDate.parse("2030-01-01"), LocalDate.parse("2030-01-15")))
                .extracting(Contrat::getIdContrat).containsExactlyInAnyOrder(k1.getIdContrat(), k2.getIdContrat());
        // bornes incluses
        assertThat(contratRepository.trouverContratsAEcheanceProche(LocalDate.parse("2030-01-15"), LocalDate.parse("2030-01-15")))
                .hasSize(2);
        assertThat(contratRepository.trouverContratsAEcheanceProche(LocalDate.parse("2030-01-16"), LocalDate.parse("2030-01-31")))
                .isEmpty();
        assertThat(contratRepository.trouverContratsAEcheanceProche(LocalDate.parse("2030-02-01"), LocalDate.parse("2030-02-28")))
                .extracting(Contrat::getIdContrat).containsExactly(k4.getIdContrat());
    }

    // ------------------------------------------------------------------ outils

    private List<Vehicule> disponibles(String debut, String fin) {
        return vehiculeRepository.rechercherVehiculesDisponibles(VILLE_A, CategorieVehicule.BERLINE,
                LocalDate.parse(debut), LocalDate.parse(fin));
    }

    private static BigDecimal montant(List<ChiffreAffaireAgenceDTO> ca, Agence agence) {
        return ca.stream().filter(l -> l.agenceId().equals(agence.getIdAgence()))
                .map(ChiffreAffaireAgenceDTO::chiffreAffaires).findFirst().orElseThrow();
    }

    private static Long id(Vehicule v) {
        return v.getIdVehicule();
    }

    private static List<Long> ids(List<Vehicule> vehicules) {
        return vehicules.stream().map(Vehicule::getIdVehicule).toList();
    }

    private Agence agence(String nom, String ville) {
        Agence a = new Agence();
        a.setNom(nom);
        a.setVille(ville);
        return agenceRepository.save(a);
    }

    private Vehicule vehicule(String immat, CategorieVehicule categorie, String tarif, StatutVehicule statut, Agence agence) {
        Vehicule v = new Vehicule();
        v.setImmatriculation(immat);
        v.setMarque("Marque8");
        v.setModele("Modele8");
        v.setCategorie(categorie);
        v.setTarifJournalier(new BigDecimal(tarif));
        v.setStatut(statut);
        v.setAgence(agence);
        return vehiculeRepository.save(v);
    }

    private Client client(String nom, String email, String permis) {
        Client c = new Client();
        c.setNom(nom);
        c.setPrenom("Prenom8");
        c.setEmail(email);
        c.setNumPermis(permis);
        c.setDateInscription(LocalDate.now());
        return clientRepository.save(c);
    }

    private void employe(String nom, RoleEmploye role, Agence agence) {
        Employe e = new Employe();
        e.setNom(nom);
        e.setPrenom("Prenom8");
        e.setRole(role);
        e.setAgence(agence);
        employeRepository.save(e);
    }

    private Contrat contrat(String montant, boolean valide) {
        Contrat k = new Contrat();
        k.setDateSignature(LocalDate.parse("2029-12-01"));
        k.setMontantTotal(new BigDecimal(montant));
        k.setValide(valide);
        return contratRepository.save(k);
    }

    private static Paiement paiement(String montant, String date) {
        Paiement p = new Paiement();
        p.setMontant(new BigDecimal(montant));
        p.setDatePaiement(LocalDate.parse(date));
        p.setModePaiement(ModePaiement.CARTE);
        return p;
    }

    private Reservation reservation(Client client, Vehicule vehicule, String debut, String fin,
                                    StatutReservation statut, Contrat contrat) {
        Reservation r = reservationRepository.save(Reservation.builder()
                .client(client).vehicule(vehicule)
                .dateDebut(LocalDate.parse(debut)).dateFin(LocalDate.parse(fin))
                .statut(statut).contrat(contrat).build());
        if (contrat != null) {
            contrat.setReservation(r);
        }
        return r;
    }
}
