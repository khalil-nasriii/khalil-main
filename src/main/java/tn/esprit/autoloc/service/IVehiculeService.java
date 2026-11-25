package tn.esprit.autoloc.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.web.dto.VehiculeDTO;

public interface IVehiculeService {

    VehiculeDTO create(VehiculeDTO dto);

    VehiculeDTO findById(Long id);

    List<VehiculeDTO> findAll();

    VehiculeDTO update(Long id, VehiculeDTO dto);

    void deleteById(Long id);

    /** Atelier 7 : rattache (ou transfère) un véhicule à une agence. */
    VehiculeDTO affecterVehiculeAAgence(Long vehiculeId, Long agenceId);

    /** Atelier 7 : détache un véhicule de son agence (IllegalStateException s'il n'en a pas). */
    VehiculeDTO desaffecterVehiculeDeAgence(Long vehiculeId);


    // ---------- Atelier 8 — Partie I : recherches (méthodes dérivées) ----------

    List<VehiculeDTO> findByAgenceVille(String ville);

    List<VehiculeDTO> findByStatutAndAgence(StatutVehicule statut, Long agenceId);

    Page<VehiculeDTO> findByStatut(StatutVehicule statut, Pageable pageable);

    List<VehiculeDTO> findByVilleAndCategorie(String ville, CategorieVehicule categorie);

    boolean aLeStatut(Long vehiculeId, StatutVehicule statut);

    long countByStatut(StatutVehicule statut);

    List<VehiculeDTO> findByTarifInferieurA(BigDecimal tarif);

    List<VehiculeDTO> findByTarifSuperieurA(BigDecimal tarif);

    List<VehiculeDTO> findAllTriesParTarif();

    // ---------- Atelier 8 — Partie II : JPQL ----------

    List<VehiculeDTO> rechercherVehiculesDisponibles(String ville, CategorieVehicule categorie,
                                                     LocalDate dateDebut, LocalDate dateFin);

    List<VehiculeDTO> trouverVehiculesAReleaser(LocalDate aujourdhui);
}
