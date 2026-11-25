package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.service.IVehiculeService;
import tn.esprit.autoloc.web.dto.VehiculeDTO;

@Tag(name = "Véhicules", description = "Gestion des véhicules")
@RestController
@RequestMapping("/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    private final IVehiculeService vehiculeService;

    @Operation(summary = "Lister les véhicules")
    @ApiResponse(responseCode = "200", description = "Liste renvoyée")
    @GetMapping
    public ResponseEntity<List<VehiculeDTO>> findAll() {
        return ResponseEntity.ok(vehiculeService.findAll());
    }

    @Operation(summary = "Consulter un véhicule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Véhicule trouvé(e)"),
            @ApiResponse(responseCode = "404", description = "Véhicule introuvable")})
    @GetMapping("/{id}")
    public ResponseEntity<VehiculeDTO> findById(@Parameter(description = "Identifiant") @PathVariable Long id) {
        return ResponseEntity.ok(vehiculeService.findById(id));
    }

    @Operation(summary = "Créer un véhicule")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Véhicule créé(e)"),
            @ApiResponse(responseCode = "400", description = "Données invalides")})
    @PostMapping
    public ResponseEntity<VehiculeDTO> create(@Valid @RequestBody VehiculeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculeService.create(dto));
    }

    @Operation(summary = "Modifier un véhicule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Véhicule modifié(e)"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Véhicule introuvable")})
    @PutMapping("/{id}")
    public ResponseEntity<VehiculeDTO> update(@Parameter(description = "Identifiant") @PathVariable Long id,
                                        @Valid @RequestBody VehiculeDTO dto) {
        return ResponseEntity.ok(vehiculeService.update(id, dto));
    }

    @Operation(summary = "Supprimer un véhicule")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Véhicule supprimé(e)"),
            @ApiResponse(responseCode = "404", description = "Véhicule introuvable")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@Parameter(description = "Identifiant") @PathVariable Long id) {
        vehiculeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Affecter un véhicule à une agence (ou le transférer)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Véhicule affecté"),
            @ApiResponse(responseCode = "404", description = "Véhicule ou agence introuvable")})
    @PutMapping("/{vehiculeId}/agence/{agenceId}")
    public ResponseEntity<VehiculeDTO> affecterAAgence(
            @Parameter(description = "Identifiant du véhicule") @PathVariable Long vehiculeId,
            @Parameter(description = "Identifiant de l'agence") @PathVariable Long agenceId) {
        return ResponseEntity.ok(vehiculeService.affecterVehiculeAAgence(vehiculeId, agenceId));
    }

    @Operation(summary = "Désaffecter un véhicule de son agence")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Véhicule désaffecté"),
            @ApiResponse(responseCode = "404", description = "Véhicule introuvable"),
            @ApiResponse(responseCode = "409", description = "Le véhicule n'est rattaché à aucune agence")})
    @PutMapping("/{vehiculeId}/desaffectation")
    public ResponseEntity<VehiculeDTO> desaffecterDeAgence(
            @Parameter(description = "Identifiant du véhicule") @PathVariable Long vehiculeId) {
        return ResponseEntity.ok(vehiculeService.desaffecterVehiculeDeAgence(vehiculeId));
    }

    // ====================== Atelier 8 — Partie I : méthodes dérivées ======================

    @Operation(summary = "Véhicules des agences d'une ville (findByAgenceVille)")
    @GetMapping("/ville")
    public ResponseEntity<List<VehiculeDTO>> findByAgenceVille(@RequestParam String ville) {
        return ResponseEntity.ok(vehiculeService.findByAgenceVille(ville));
    }

    @Operation(summary = "Véhicules d'un statut, paginés (findByStatut + Pageable)")
    @GetMapping({"/statut", "/recherche/statut"})
    public ResponseEntity<Page<VehiculeDTO>> findByStatut(@RequestParam StatutVehicule statut,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(vehiculeService.findByStatut(statut, PageRequest.of(page, size)));
    }

    @Operation(summary = "Véhicules d'une ville et d'une catégorie (findByAgence_VilleAndCategorie)")
    @GetMapping("/recherche")
    public ResponseEntity<List<VehiculeDTO>> findByVilleAndCategorie(@RequestParam String ville,
                                                                     @RequestParam CategorieVehicule categorie) {
        return ResponseEntity.ok(vehiculeService.findByVilleAndCategorie(ville, categorie));
    }

    @Operation(summary = "Véhicules d'un statut dans une agence (findByStatutAndAgence_IdAgence)")
    @GetMapping("/agence/{agenceId}")
    public ResponseEntity<List<VehiculeDTO>> findByStatutAndAgence(@PathVariable Long agenceId,
                                                                   @RequestParam StatutVehicule statut) {
        return ResponseEntity.ok(vehiculeService.findByStatutAndAgence(statut, agenceId));
    }

    @Operation(summary = "Le véhicule a-t-il ce statut ? (existsByIdVehiculeAndStatut)")
    @GetMapping("/{id}/a-le-statut")
    public ResponseEntity<Boolean> aLeStatut(@PathVariable Long id, @RequestParam StatutVehicule statut) {
        return ResponseEntity.ok(vehiculeService.aLeStatut(id, statut));
    }

    @Operation(summary = "Nombre de véhicules d'un statut (countByStatut)")
    @GetMapping("/compteur")
    public ResponseEntity<Long> countByStatut(@RequestParam StatutVehicule statut) {
        return ResponseEntity.ok(vehiculeService.countByStatut(statut));
    }

    @Operation(summary = "Véhicules dont le tarif est inférieur à une valeur (LessThan)")
    @GetMapping("/tarif-inferieur")
    public ResponseEntity<List<VehiculeDTO>> tarifInferieur(@RequestParam BigDecimal tarif) {
        return ResponseEntity.ok(vehiculeService.findByTarifInferieurA(tarif));
    }

    @Operation(summary = "Véhicules dont le tarif est supérieur à une valeur (GreaterThan)")
    @GetMapping("/tarif-superieur")
    public ResponseEntity<List<VehiculeDTO>> tarifSuperieur(@RequestParam BigDecimal tarif) {
        return ResponseEntity.ok(vehiculeService.findByTarifSuperieurA(tarif));
    }

    @Operation(summary = "Véhicules triés par tarif croissant (findAllByOrderByTarifJournalierAsc)")
    @GetMapping("/tries-par-tarif")
    public ResponseEntity<List<VehiculeDTO>> triesParTarif() {
        return ResponseEntity.ok(vehiculeService.findAllTriesParTarif());
    }

    // ====================== Atelier 8 — Partie II : JPQL ======================

    @Operation(summary = "Véhicules disponibles par ville, catégorie et période (JPQL)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste, éventuellement vide"),
            @ApiResponse(responseCode = "400", description = "Paramètre manquant ou période incohérente")})
    @GetMapping("/disponibles")
    public ResponseEntity<List<VehiculeDTO>> rechercherDisponibles(
            @RequestParam String ville,
            @RequestParam CategorieVehicule categorie,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return ResponseEntity.ok(vehiculeService.rechercherVehiculesDisponibles(ville, categorie, dateDebut, dateFin));
    }

    @Operation(summary = "Véhicules à libérer : réservation confirmée arrivée à son terme (JPQL)")
    @GetMapping("/a-liberer")
    public ResponseEntity<List<VehiculeDTO>> aLiberer(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate aujourdhui) {
        LocalDate reference = aujourdhui == null ? LocalDate.now() : aujourdhui;
        return ResponseEntity.ok(vehiculeService.trouverVehiculesAReleaser(reference));
    }
}
