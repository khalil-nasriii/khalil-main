package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.service.IVehiculeService;
import tn.esprit.autoloc.web.dto.VehiculeDTO;

/** Atelier 8 — Partie II : requêtes JPQL sur les véhicules (route du support : /api/jpql/vehicules). */
@Tag(name = "JPQL — Véhicules", description = "Recherches personnalisées @Query")
@RestController
@RequestMapping("/jpql/vehicules")
@RequiredArgsConstructor
public class JpqlVehiculeController {

    private final IVehiculeService vehiculeService;

    @Operation(summary = "Véhicules disponibles par ville, catégorie et période")
    @GetMapping("/disponibles")
    public ResponseEntity<List<VehiculeDTO>> rechercherDisponibles(
            @RequestParam String ville,
            @RequestParam CategorieVehicule categorie,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return ResponseEntity.ok(vehiculeService.rechercherVehiculesDisponibles(ville, categorie, dateDebut, dateFin));
    }

    @Operation(summary = "Véhicules à libérer à une date donnée")
    @GetMapping("/a-liberer")
    public ResponseEntity<List<VehiculeDTO>> aLiberer(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate aujourdhui) {
        return ResponseEntity.ok(vehiculeService.trouverVehiculesAReleaser(
                aujourdhui == null ? LocalDate.now() : aujourdhui));
    }
}
