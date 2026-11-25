package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.service.IContratService;
import tn.esprit.autoloc.service.IReservationService;
import tn.esprit.autoloc.web.dto.ApiError;
import tn.esprit.autoloc.web.dto.ChiffreAffaireAgenceDTO;
import tn.esprit.autoloc.web.dto.ReservationRequestDTO;
import tn.esprit.autoloc.web.dto.ReservationResponseDTO;
import tn.esprit.autoloc.web.dto.TopClientDTO;
import tn.esprit.autoloc.web.validator.ReservationRequestValidator;

@Tag(name = "Réservations", description = "Gestion des réservations")
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService reservationService;
    private final ReservationRequestValidator reservationRequestValidator;
    private final IContratService contratService;

    @Operation(summary = "Lister les réservations")
    @ApiResponse(responseCode = "200", description = "Liste renvoyée")
    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> findAll() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @Operation(summary = "Consulter une réservation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réservation trouvée"),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable")})
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> findById(
            @Parameter(description = "Identifiant") @PathVariable Long id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    @Operation(summary = "Créer une réservation (tarif estimé + notifications email/SMS)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Réservation créée",
                    content = @Content(schema = @Schema(implementation = ReservationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Client ou véhicule introuvable")})
    @PostMapping
    public ResponseEntity<Object> creer(@Valid @RequestBody ReservationRequestDTO dto,
                                        BindingResult result) {
        reservationRequestValidator.validate(dto, result);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(erreurs(result));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.creer(dto));
    }

    @Operation(summary = "Modifier une réservation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réservation modifiée",
                    content = @Content(schema = @Schema(implementation = ReservationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Réservation, client ou véhicule introuvable")})
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@Parameter(description = "Identifiant") @PathVariable Long id,
                                         @Valid @RequestBody ReservationRequestDTO dto,
                                         BindingResult result) {
        reservationRequestValidator.validate(dto, result);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(erreurs(result));
        }
        return ResponseEntity.ok(reservationService.update(id, dto));
    }

    @Operation(summary = "Annuler une réservation (notifications email/SMS)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réservation annulée"),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable"),
            @ApiResponse(responseCode = "409", description = "Réservation déjà annulée")})
    @PutMapping("/{id}/annulation")
    public ResponseEntity<ReservationResponseDTO> annuler(
            @Parameter(description = "Identifiant") @PathVariable Long id) {
        return ResponseEntity.ok(reservationService.annuler(id));
    }

    @Operation(summary = "Supprimer une réservation")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Réservation supprimée"),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@Parameter(description = "Identifiant") @PathVariable Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** Erreurs Bean Validation + validateur personnalisé, au même format que GlobalExceptionHandler. */
    private static ApiError erreurs(BindingResult result) {
        List<String> details = result.getFieldErrors().stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .toList();
        return ApiError.of(HttpStatus.BAD_REQUEST.value(), "Données invalides", details);
    }

    // ====================== Atelier 8 — Partie I : méthodes dérivées ======================

    @Operation(summary = "Réservations d'un client (findByClientIdClient)")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ReservationResponseDTO>> findByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(reservationService.findByClient(clientId));
    }

    @Operation(summary = "Réservations d'un véhicule hors d'un statut (findByVehicule_IdVehiculeAndStatutNot)")
    @GetMapping("/vehicule/{vehiculeId}")
    public ResponseEntity<List<ReservationResponseDTO>> findByVehiculeHorsStatut(
            @PathVariable Long vehiculeId,
            @RequestParam(defaultValue = "ANNULEE") StatutReservation statutExclu) {
        return ResponseEntity.ok(reservationService.findByVehiculeHorsStatut(vehiculeId, statutExclu));
    }

    @Operation(summary = "Le véhicule a-t-il une réservation de ce statut ? (existsByVehicule_IdVehiculeAndStatut)")
    @GetMapping("/vehicule/{vehiculeId}/existe")
    public ResponseEntity<Boolean> existsByVehiculeAndStatut(@PathVariable Long vehiculeId,
                                                             @RequestParam StatutReservation statut) {
        return ResponseEntity.ok(reservationService.existsByVehiculeAndStatut(vehiculeId, statut));
    }

    @Operation(summary = "Réservations d'un statut (findByStatut)")
    @GetMapping("/statut")
    public ResponseEntity<List<ReservationResponseDTO>> findByStatut(@RequestParam StatutReservation statut) {
        return ResponseEntity.ok(reservationService.findByStatut(statut));
    }

    @Operation(summary = "Réservations dont la date de début est dans la période (findByDateDebutBetween)")
    @GetMapping("/periode")
    public ResponseEntity<List<ReservationResponseDTO>> findByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(reservationService.findByDateDebutEntre(debut, fin));
    }

    @Operation(summary = "Réservations se terminant après une date (findByDateFinAfter)")
    @GetMapping("/fin-apres")
    public ResponseEntity<List<ReservationResponseDTO>> findByDateFinApres(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reservationService.findByDateFinApres(date));
    }

    @Operation(summary = "Réservations d'un statut dans une agence (findByStatutAndVehiculeAgenceIdAgence)")
    @GetMapping("/agence/{agenceId}")
    public ResponseEntity<List<ReservationResponseDTO>> findByStatutAndAgence(@PathVariable Long agenceId,
                                                                              @RequestParam StatutReservation statut) {
        return ResponseEntity.ok(reservationService.findByStatutAndAgence(statut, agenceId));
    }

    // ====================== Atelier 8 — Partie II : agrégations JPQL ======================

    @Operation(summary = "Top des clients par nombre de réservations (COUNT + GROUP BY)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Au plus 'limit' clients, ordre décroissant"),
            @ApiResponse(responseCode = "400", description = "limit inférieur à 1")})
    @GetMapping("/top-clients")
    public ResponseEntity<List<TopClientDTO>> topClients(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(reservationService.topClients(PageRequest.of(0, limit)));
    }

    @Operation(summary = "Chiffre d'affaires par agence (SUM sur les contrats valides)")
    @GetMapping({"/chiffre-affaires-agences", "/chiffre-affaires/agences"})
    public ResponseEntity<List<ChiffreAffaireAgenceDTO>> chiffreAffaireParAgence() {
        return ResponseEntity.ok(contratService.chiffreAffaireParAgence());
    }
}
