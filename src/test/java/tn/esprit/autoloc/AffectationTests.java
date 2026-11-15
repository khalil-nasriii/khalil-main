package tn.esprit.autoloc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.RoleEmploye;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IEmployeRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;
import tn.esprit.autoloc.service.IAgenceService;
import tn.esprit.autoloc.service.IEmployeService;
import tn.esprit.autoloc.service.IVehiculeService;
import tn.esprit.autoloc.web.dto.AgenceDTO;
import tn.esprit.autoloc.web.dto.EmployeDTO;
import tn.esprit.autoloc.web.dto.VehiculeDTO;

/** Atelier 7 - Partie 1.5 : affectation / désaffectation Véhicule et Employé <-> Agence. */
@SpringBootTest
class AffectationTests {

    private static final long INEXISTANT = 999_999L;

    @Autowired private IAgenceService agenceService;
    @Autowired private IVehiculeService vehiculeService;
    @Autowired private IEmployeService employeService;
    @Autowired private IVehiculeRepository vehiculeRepository;
    @Autowired private IEmployeRepository employeRepository;

    private Long agenceTunis;
    private Long agenceSousse;
    private Long vehiculeId;
    private Long employeId;

    @BeforeEach
    void setUp() {
        agenceTunis = agenceService.create(AgenceDTO.builder().nom("AutoLoc Tunis").ville("Tunis").build()).getId();
        agenceSousse = agenceService.create(AgenceDTO.builder().nom("AutoLoc Sousse").ville("Sousse").build()).getId();
        vehiculeId = vehiculeService.create(VehiculeDTO.builder()
                .immatriculation("101 TUN 2026").marque("Peugeot").modele("208")
                .categorie(CategorieVehicule.CITADINE).tarifJournalier(new BigDecimal("90")).build()).getId();
        employeId = employeService.create(EmployeDTO.builder()
                .nom("Gharbi").prenom("Karim").role(RoleEmploye.AGENT).build()).getId();
    }

    @AfterEach
    void tearDown() {
        vehiculeRepository.deleteById(vehiculeId);
        employeRepository.deleteById(employeId);
        agenceService.deleteById(agenceTunis);
        agenceService.deleteById(agenceSousse);
    }

    private Long agenceEnBaseDuVehicule() {
        var agence = vehiculeRepository.findById(vehiculeId).orElseThrow().getAgence();
        return agence == null ? null : agence.getIdAgence();
    }

    private Long agenceEnBaseDeLEmploye() {
        var agence = employeRepository.findById(employeId).orElseThrow().getAgence();
        return agence == null ? null : agence.getIdAgence();
    }

    @Test
    void affecterTransfererPuisDesaffecterUnVehicule() {
        VehiculeDTO affecte = vehiculeService.affecterVehiculeAAgence(vehiculeId, agenceTunis);
        assertEquals(agenceTunis, affecte.getAgenceId());
        assertEquals(agenceTunis, agenceEnBaseDuVehicule());
        System.out.println("Véhicule " + vehiculeId + " affecté à l'agence " + agenceTunis);

        // transfert vers une autre agence
        vehiculeService.affecterVehiculeAAgence(vehiculeId, agenceSousse);
        assertEquals(agenceSousse, agenceEnBaseDuVehicule());
        System.out.println("Véhicule transféré vers l'agence " + agenceSousse);

        // désaffectation : agence_id = NULL en base
        VehiculeDTO desaffecte = vehiculeService.desaffecterVehiculeDeAgence(vehiculeId);
        assertNull(desaffecte.getAgenceId());
        assertNull(agenceEnBaseDuVehicule());
        System.out.println("Véhicule désaffecté : agence_id = NULL");

        // désaffecter un véhicule sans agence : erreur métier (409)
        assertThrows(IllegalStateException.class, () -> vehiculeService.desaffecterVehiculeDeAgence(vehiculeId));
    }

    @Test
    void affecterUnVehiculeAvecDesIdentifiantsInexistants() {
        vehiculeService.affecterVehiculeAAgence(vehiculeId, agenceTunis);

        assertThrows(ResourceNotFoundException.class,
                () -> vehiculeService.affecterVehiculeAAgence(INEXISTANT, agenceTunis));
        assertThrows(ResourceNotFoundException.class,
                () -> vehiculeService.affecterVehiculeAAgence(vehiculeId, INEXISTANT));

        // l'échec n'a rien modifié : la base reste cohérente (transaction annulée)
        assertEquals(agenceTunis, agenceEnBaseDuVehicule());
    }

    @Test
    void affecterTransfererPuisDesaffecterUnEmploye() {
        assertEquals(agenceTunis, employeService.affecterEmployeAAgence(employeId, agenceTunis).getAgenceId());
        assertEquals(agenceTunis, agenceEnBaseDeLEmploye());

        employeService.affecterEmployeAAgence(employeId, agenceSousse);
        assertEquals(agenceSousse, agenceEnBaseDeLEmploye());

        assertNull(employeService.desaffecterEmployeDeAgence(employeId).getAgenceId());
        assertNull(agenceEnBaseDeLEmploye());

        assertThrows(IllegalStateException.class, () -> employeService.desaffecterEmployeDeAgence(employeId));
    }

    @Test
    void affecterUnEmployeAvecDesIdentifiantsInexistants() {
        employeService.affecterEmployeAAgence(employeId, agenceSousse);

        assertThrows(ResourceNotFoundException.class,
                () -> employeService.affecterEmployeAAgence(INEXISTANT, agenceTunis));
        assertThrows(ResourceNotFoundException.class,
                () -> employeService.affecterEmployeAAgence(employeId, INEXISTANT));

        assertEquals(agenceSousse, agenceEnBaseDeLEmploye());
    }
}
