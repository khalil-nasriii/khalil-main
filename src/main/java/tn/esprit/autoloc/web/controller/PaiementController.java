package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.service.IPaiementService;
import tn.esprit.autoloc.web.dto.PaiementDTO;

/**
 * Lecture seule : un POST / PUT / DELETE sur /api/paiements renvoie 405.
 * Les paiements se créent via POST /api/contrats/{id}/paiements.
 */
@Tag(name = "Paiements", description = "Consultation des paiements (lecture seule)")
@RestController
@RequestMapping("/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final IPaiementService paiementService;

    @Operation(summary = "Lister les paiements")
    @ApiResponse(responseCode = "200", description = "Liste renvoyée")
    @GetMapping
    public ResponseEntity<List<PaiementDTO>> findAll() {
        return ResponseEntity.ok(paiementService.findAll());
    }

    @Operation(summary = "Consulter un paiement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paiement trouvé"),
            @ApiResponse(responseCode = "404", description = "Paiement introuvable")})
    @GetMapping("/{id}")
    public ResponseEntity<PaiementDTO> findById(@Parameter(description = "Identifiant") @PathVariable Long id) {
        return ResponseEntity.ok(paiementService.findById(id));
    }

    // ====================== Atelier 8 — recherches ======================

    @Operation(summary = "Paiements d'un contrat (findByContrat_IdContrat)")
    @GetMapping("/contrat/{contratId}")
    public ResponseEntity<List<PaiementDTO>> findByContrat(@PathVariable Long contratId) {
        return ResponseEntity.ok(paiementService.findByContrat(contratId));
    }

    @Operation(summary = "Paiements d'une période (findByDatePaiementBetween)")
    @GetMapping("/periode")
    public ResponseEntity<List<PaiementDTO>> findByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(paiementService.findByDatePaiementEntre(debut, fin));
    }
}
