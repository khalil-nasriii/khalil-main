package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import tn.esprit.autoloc.service.IAgenceService;
import tn.esprit.autoloc.web.dto.AgenceDTO;

@Tag(name = "Agences", description = "Gestion des agences")
@RestController
@RequestMapping("/agences")
@RequiredArgsConstructor
public class AgenceController {

    private final IAgenceService agenceService;

    @Operation(summary = "Lister les agences")
    @ApiResponse(responseCode = "200", description = "Liste renvoyée")
    @GetMapping
    public ResponseEntity<List<AgenceDTO>> findAll() {
        return ResponseEntity.ok(agenceService.findAll());
    }

    @Operation(summary = "Consulter une agence")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agence trouvé(e)"),
            @ApiResponse(responseCode = "404", description = "Agence introuvable")})
    @GetMapping("/{id}")
    public ResponseEntity<AgenceDTO> findById(@Parameter(description = "Identifiant") @PathVariable Long id) {
        return ResponseEntity.ok(agenceService.findById(id));
    }

    @Operation(summary = "Créer une agence")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agence créé(e)"),
            @ApiResponse(responseCode = "400", description = "Données invalides")})
    @PostMapping
    public ResponseEntity<AgenceDTO> create(@Valid @RequestBody AgenceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agenceService.create(dto));
    }

    @Operation(summary = "Modifier une agence")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agence modifié(e)"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Agence introuvable")})
    @PutMapping("/{id}")
    public ResponseEntity<AgenceDTO> update(@Parameter(description = "Identifiant") @PathVariable Long id,
                                        @Valid @RequestBody AgenceDTO dto) {
        return ResponseEntity.ok(agenceService.update(id, dto));
    }

    @Operation(summary = "Supprimer une agence")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agence supprimé(e)"),
            @ApiResponse(responseCode = "404", description = "Agence introuvable")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@Parameter(description = "Identifiant") @PathVariable Long id) {
        agenceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ====================== Atelier 8 — recherches ======================

    @Operation(summary = "Première agence d'une ville (findFirstByVille...)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agence trouvée"),
            @ApiResponse(responseCode = "404", description = "Aucune agence dans cette ville")})
    @GetMapping("/ville")
    public ResponseEntity<AgenceDTO> findByVille(@RequestParam String ville) {
        return ResponseEntity.ok(agenceService.findByVille(ville));
    }

    @Operation(summary = "Toutes les agences d'une ville (findByVille)")
    @GetMapping("/ville/toutes")
    public ResponseEntity<List<AgenceDTO>> findAllByVille(@RequestParam String ville) {
        return ResponseEntity.ok(agenceService.findAllByVille(ville));
    }

    @Operation(summary = "Agences dont le nom contient une chaîne (findByNomContaining)")
    @GetMapping("/recherche")
    public ResponseEntity<List<AgenceDTO>> findByNom(@RequestParam String nom) {
        return ResponseEntity.ok(agenceService.findByNomContaining(nom));
    }
}
