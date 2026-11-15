package tn.esprit.autoloc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.ModePaiement;
import tn.esprit.autoloc.domain.RoleEmploye;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IAgenceRepository;
import tn.esprit.autoloc.repository.IClientRepository;
import tn.esprit.autoloc.repository.IContratRepository;
import tn.esprit.autoloc.repository.IEmployeRepository;
import tn.esprit.autoloc.repository.IEquipementRepository;
import tn.esprit.autoloc.repository.IMaintenanceRepository;
import tn.esprit.autoloc.repository.IPaiementRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;
import tn.esprit.autoloc.service.IAgenceService;
import tn.esprit.autoloc.service.IClientService;
import tn.esprit.autoloc.service.IContratService;
import tn.esprit.autoloc.service.IEmployeService;
import tn.esprit.autoloc.service.IEquipementService;
import tn.esprit.autoloc.service.IMaintenanceService;
import tn.esprit.autoloc.service.IPaiementService;
import tn.esprit.autoloc.service.IVehiculeService;
import tn.esprit.autoloc.web.dto.AgenceDTO;
import tn.esprit.autoloc.web.dto.ClientDTO;
import tn.esprit.autoloc.web.dto.ContratDTO;
import tn.esprit.autoloc.web.dto.EmployeDTO;
import tn.esprit.autoloc.web.dto.EquipementDTO;
import tn.esprit.autoloc.web.dto.MaintenanceDTO;
import tn.esprit.autoloc.web.dto.PaiementDTO;
import tn.esprit.autoloc.web.dto.VehiculeDTO;

/**
 * Atelier 4 - Part 2 : tests de la couche Service (adaptés aux DTO de l'Atelier 7).
 * Nécessite MySQL (XAMPP) démarré sur localhost:3306.
 */
@SpringBootTest
class ServiceTests {

    @Autowired private IAgenceService agenceService;
    @Autowired private IClientService clientService;
    @Autowired private IVehiculeService vehiculeService;
    @Autowired private IEmployeService employeService;
    @Autowired private IEquipementService equipementService;
    @Autowired private IMaintenanceService maintenanceService;
    @Autowired private IContratService contratService;
    @Autowired private IPaiementService paiementService;

    // Les repositories servent uniquement à count() et existsById() (absents des interfaces Service)
    @Autowired private IAgenceRepository agenceRepository;
    @Autowired private IClientRepository clientRepository;
    @Autowired private IVehiculeRepository vehiculeRepository;
    @Autowired private IEmployeRepository employeRepository;
    @Autowired private IEquipementRepository equipementRepository;
    @Autowired private IMaintenanceRepository maintenanceRepository;
    @Autowired private IContratRepository contratRepository;
    @Autowired private IPaiementRepository paiementRepository;

    // ---------- Partie 2 : AgenceService ----------
    @Test
    void testAgenceService() {
        long avant = agenceRepository.count();

        AgenceDTO saved = agenceService.create(AgenceDTO.builder()
                .nom("Agence Tunis Centre").ville("Tunis")
                .adresse("10 Avenue Habib Bourguiba").telephone("71000000").build());
        assertNotNull(saved.getId());
        Long idAgence = saved.getId();
        System.out.println("Agence enregistrée : id=" + idAgence + ", nom=" + saved.getNom());

        System.out.println("--- findAll ---");
        agenceService.findAll().forEach(a ->
                System.out.println(a.getId() + " | " + a.getNom() + " | " + a.getVille()));

        AgenceDTO trouvee = agenceService.findById(idAgence);
        assertEquals("Agence Tunis Centre", trouvee.getNom());
        System.out.println("findById : " + trouvee.getNom());

        AgenceDTO maj = agenceService.update(idAgence, AgenceDTO.builder()
                .nom("Agence Tunis Lac").ville("Tunis").adresse("Les Berges du Lac").telephone("71111111").build());
        assertEquals("Agence Tunis Lac", maj.getNom());
        assertEquals(idAgence, maj.getId());
        System.out.println("update : " + maj.getNom());

        assertEquals(avant + 1, agenceRepository.count());
        System.out.println("Nombre d'agences : " + agenceRepository.count());

        // règle de gestion : nom obligatoire / création sans identifiant
        AgenceDTO sansNom = AgenceDTO.builder().ville("Tunis").build();
        assertThrows(IllegalArgumentException.class, () -> agenceService.create(sansNom));
        AgenceDTO avecId = AgenceDTO.builder().id(5L).nom("X").ville("Y").build();
        assertThrows(IllegalArgumentException.class, () -> agenceService.create(avecId));

        agenceService.deleteById(idAgence);
        assertFalse(agenceRepository.existsById(idAgence));
        assertThrows(ResourceNotFoundException.class, () -> agenceService.findById(idAgence));
        assertThrows(ResourceNotFoundException.class, () -> agenceService.deleteById(idAgence));
        assertEquals(avant, agenceRepository.count());
        System.out.println("Agence supprimée, elle n'existe plus.");
    }

    // ---------- Partie 3 : ClientService ----------
    @Test
    void testClientService() {
        long avant = clientRepository.count();

        ClientDTO s1 = clientService.create(ClientDTO.builder()
                .nom("Ben Ali").prenom("Ahmed").email("ahmed.benali@test.tn")
                .telephone("20111111").numPermis("P-1001").dateInscription(LocalDate.now()).build());
        ClientDTO s2 = clientService.create(ClientDTO.builder()
                .nom("Trabelsi").prenom("Sarra").email("sarra.trabelsi@test.tn")
                .telephone("20222222").numPermis("P-1002").build());
        assertNotNull(s1.getId());
        assertNotNull(s2.getId());
        assertNotEquals(s1.getId(), s2.getId());
        assertNotNull(s2.getDateInscription(), "date d'inscription renseignée par défaut");
        Long idClient1 = s1.getId();
        Long idClient2 = s2.getId();
        System.out.println("Clients enregistrés : " + idClient1 + " et " + idClient2);

        System.out.println("--- findAll ---");
        clientService.findAll().forEach(c ->
                System.out.println(c.getId() + " | " + c.getNom() + " " + c.getPrenom() + " | " + c.getEmail()));

        ClientDTO trouve = clientService.findById(idClient1);
        assertEquals("Ben Ali", trouve.getNom());
        System.out.println("findById : " + trouve.getNom() + " " + trouve.getPrenom());

        assertEquals(avant + 2, clientRepository.count());
        System.out.println("Nombre de clients : " + clientRepository.count());

        clientService.deleteById(idClient1);
        assertFalse(clientRepository.existsById(idClient1));
        assertThrows(ResourceNotFoundException.class, () -> clientService.findById(idClient1));
        System.out.println("Client " + idClient1 + " supprimé, il n'existe plus.");

        clientService.deleteById(idClient2);
        assertEquals(avant, clientRepository.count());
    }

    // ---------- Partie 4 : VehiculeService ----------
    @Test
    void testVehiculeService() {
        long avant = vehiculeRepository.count();

        VehiculeDTO saved = vehiculeService.create(VehiculeDTO.builder()
                .immatriculation("123 TUN 4567").marque("Renault").modele("Clio")
                .categorie(CategorieVehicule.CITADINE).tarifJournalier(new BigDecimal("80.00")).build());
        assertNotNull(saved.getId());
        assertNotNull(saved.getStatut(), "statut DISPONIBLE par défaut");
        System.out.println("Véhicule enregistré : id=" + saved.getId() + ", " + saved.getMarque() + " " + saved.getModele());

        System.out.println("--- findAll ---");
        vehiculeService.findAll().forEach(x ->
                System.out.println(x.getId() + " | " + x.getImmatriculation() + " | "
                        + x.getMarque() + " " + x.getModele() + " | " + x.getTarifJournalier()));

        VehiculeDTO trouve = vehiculeService.findById(saved.getId());
        assertEquals("Renault", trouve.getMarque());
        System.out.println("findById : " + trouve.getMarque() + " " + trouve.getModele());

        assertEquals(avant + 1, vehiculeRepository.count());
        System.out.println("Nombre de véhicules : " + vehiculeRepository.count());

        // règle de gestion : tarif négatif refusé
        VehiculeDTO invalide = VehiculeDTO.builder().tarifJournalier(new BigDecimal("-5")).build();
        assertThrows(IllegalArgumentException.class, () -> vehiculeService.create(invalide));

        vehiculeService.deleteById(saved.getId());
        assertFalse(vehiculeRepository.existsById(saved.getId()));
        assertEquals(avant, vehiculeRepository.count());
        System.out.println("Véhicule supprimé.");
    }

    // ---------- Partie 5 : exercice autonome ----------
    @Test
    void testEmployeService() {
        EmployeDTO saved = employeService.create(EmployeDTO.builder()
                .nom("Gharbi").prenom("Karim").role(RoleEmploye.AGENT).build());
        assertNotNull(saved.getId());
        System.out.println("Employé créé : " + saved.getId() + " " + saved.getNom());

        assertEquals("Gharbi", employeService.findById(saved.getId()).getNom());
        System.out.println("findAll : " + employeService.findAll().size() + " employé(s)");

        assertTrue(employeRepository.existsById(saved.getId()));
        employeService.deleteById(saved.getId());
        assertFalse(employeRepository.existsById(saved.getId()));
        System.out.println("Employé supprimé, existsById = " + employeRepository.existsById(saved.getId()));
    }

    @Test
    void testEquipementService() {
        EquipementDTO saved = equipementService.create(EquipementDTO.builder().libelle("GPS").build());
        assertNotNull(saved.getId());
        System.out.println("Équipement créé : " + saved.getId() + " " + saved.getLibelle());

        assertEquals("GPS", equipementService.findById(saved.getId()).getLibelle());
        System.out.println("findAll : " + equipementService.findAll().size() + " équipement(s)");

        EquipementDTO sansLibelle = EquipementDTO.builder().libelle(" ").build();
        assertThrows(IllegalArgumentException.class, () -> equipementService.create(sansLibelle));

        assertTrue(equipementRepository.existsById(saved.getId()));
        equipementService.deleteById(saved.getId());
        assertFalse(equipementRepository.existsById(saved.getId()));
        System.out.println("Équipement supprimé, existsById = " + equipementRepository.existsById(saved.getId()));
    }

    @Test
    void testMaintenanceService() {
        MaintenanceDTO saved = maintenanceService.create(MaintenanceDTO.builder()
                .dateDebut(LocalDate.now()).dateFin(LocalDate.now().plusDays(2))
                .description("Vidange et contrôle des freins").build());
        assertNotNull(saved.getId());
        System.out.println("Maintenance créée : " + saved.getId() + " " + saved.getDescription());

        assertEquals("Vidange et contrôle des freins", maintenanceService.findById(saved.getId()).getDescription());
        System.out.println("findAll : " + maintenanceService.findAll().size() + " maintenance(s)");

        // règle de gestion : dateFin avant dateDebut refusée
        MaintenanceDTO invalide = MaintenanceDTO.builder()
                .dateDebut(LocalDate.now()).dateFin(LocalDate.now().minusDays(1)).build();
        assertThrows(IllegalArgumentException.class, () -> maintenanceService.create(invalide));

        assertTrue(maintenanceRepository.existsById(saved.getId()));
        maintenanceService.deleteById(saved.getId());
        assertFalse(maintenanceRepository.existsById(saved.getId()));
        System.out.println("Maintenance supprimée, existsById = " + maintenanceRepository.existsById(saved.getId()));
    }

    @Test
    void testContratServiceEtCascadePaiements() {
        long paiementsAvant = paiementRepository.count();

        ContratDTO contrat = contratService.create(ContratDTO.builder()
                .dateSignature(LocalDate.now()).montantTotal(new BigDecimal("480.00")).valide(true).build());
        assertNotNull(contrat.getId());
        Long idContrat = contrat.getId();
        System.out.println("Contrat créé : id=" + idContrat + ", montant=" + contrat.getMontantTotal());

        PaiementDTO p1 = contratService.ajouterPaiement(idContrat, PaiementDTO.builder()
                .montant(new BigDecimal("150.00")).modePaiement(ModePaiement.CARTE).contratId(idContrat).build());
        contratService.ajouterPaiement(idContrat, PaiementDTO.builder()
                .montant(new BigDecimal("330.00")).modePaiement(ModePaiement.VIREMENT).build());
        assertNotNull(p1.getId());
        assertEquals(idContrat, p1.getContratId());
        assertEquals(paiementsAvant + 2, paiementRepository.count());
        assertEquals(idContrat, paiementService.findById(p1.getId()).getContratId());
        System.out.println("2 paiements ajoutés via le contrat (cascade PERSIST)");

        // montant négatif refusé
        PaiementDTO negatif = PaiementDTO.builder().montant(new BigDecimal("-1")).modePaiement(ModePaiement.ESPECES).build();
        assertThrows(IllegalArgumentException.class, () -> contratService.ajouterPaiement(idContrat, negatif));

        // la mise à jour ne touche pas aux paiements
        ContratDTO maj = contratService.update(idContrat, ContratDTO.builder()
                .montantTotal(new BigDecimal("520.00")).valide(true).build());
        assertEquals(0, new BigDecimal("520.00").compareTo(maj.getMontantTotal()));
        assertEquals(paiementsAvant + 2, paiementRepository.count());

        // orphanRemoval
        contratService.retirerPaiement(idContrat, p1.getId());
        assertFalse(paiementRepository.existsById(p1.getId()));

        // cascade REMOVE
        contratService.deleteById(idContrat);
        assertFalse(contratRepository.existsById(idContrat));
        assertEquals(paiementsAvant, paiementRepository.count());
        System.out.println("Contrat supprimé avec ses paiements (cascade REMOVE)");
    }
}
