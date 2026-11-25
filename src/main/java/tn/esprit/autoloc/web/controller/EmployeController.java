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
import tn.esprit.autoloc.domain.RoleEmploye;
import tn.esprit.autoloc.service.IEmployeService;
import tn.esprit.autoloc.web.dto.EmployeDTO;

@Tag(name = "Employés", description = "Gestion des employés")
@RestController
@RequestMapping("/employes")
@RequiredArgsConstructor
public class EmployeController {

    private final IEmployeService employeService;

    @Operation(summary = "Lister les employés")
    @ApiResponse(responseCode = "200", description = "Liste renvoyée")
    @GetMapping
    public ResponseEntity<List<EmployeDTO>> findAll() {
        return ResponseEntity.ok(employeService.findAll());
    }

    @Operation(summary = "Consulter un employé")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employé trouvé(e)"),
            @ApiResponse(responseCode = "404", description = "Employé introuvable")})
    @GetMapping("/{id}")
    public ResponseEntity<EmployeDTO> findById(@Parameter(description = "Identifiant") @PathVariable Long id) {
        return ResponseEntity.ok(employeService.findById(id));
    }

    @Operation(summary = "Créer un employé")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employé créé(e)"),
            @ApiResponse(responseCode = "400", description = "Données invalides")})
    @PostMapping
    public ResponseEntity<EmployeDTO> create(@Valid @RequestBody EmployeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeService.create(dto));
    }

    @Operation(summary = "Modifier un employé")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employé modifié(e)"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Employé introuvable")})
    @PutMapping("/{id}")
    public ResponseEntity<EmployeDTO> update(@Parameter(description = "Identifiant") @PathVariable Long id,
                                        @Valid @RequestBody EmployeDTO dto) {
        return ResponseEntity.ok(employeService.update(id, dto));
    }

    @Operation(summary = "Supprimer un employé")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Employé supprimé(e)"),
            @ApiResponse(responseCode = "404", description = "Employé introuvable")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@Parameter(description = "Identifiant") @PathVariable Long id) {
        employeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Affecter un employé à une agence (ou le transférer)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employé affecté"),
            @ApiResponse(responseCode = "404", description = "Employé ou agence introuvable")})
    @PutMapping("/{employeId}/agence/{agenceId}")
    public ResponseEntity<EmployeDTO> affecterAAgence(
            @Parameter(description = "Identifiant de l'employé") @PathVariable Long employeId,
            @Parameter(description = "Identifiant de l'agence") @PathVariable Long agenceId) {
        return ResponseEntity.ok(employeService.affecterEmployeAAgence(employeId, agenceId));
    }

    @Operation(summary = "Désaffecter un employé de son agence")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employé désaffecté"),
            @ApiResponse(responseCode = "404", description = "Employé introuvable"),
            @ApiResponse(responseCode = "409", description = "L'employé n'est rattaché à aucune agence")})
    @PutMapping("/{employeId}/desaffectation")
    public ResponseEntity<EmployeDTO> desaffecterDeAgence(
            @Parameter(description = "Identifiant de l'employé") @PathVariable Long employeId) {
        return ResponseEntity.ok(employeService.desaffecterEmployeDeAgence(employeId));
    }

    // ====================== Atelier 8 — recherches ======================

    @Operation(summary = "Employés d'une agence désignée par son nom (findByAgenceNom)")
    @GetMapping("/agence")
    public ResponseEntity<List<EmployeDTO>> findByAgenceNom(@RequestParam String nom) {
        return ResponseEntity.ok(employeService.findByAgenceNom(nom));
    }

    @Operation(summary = "Employés d'une agence ayant un rôle (findByAgence_IdAgenceAndRole)")
    @GetMapping("/agence/{agenceId}")
    public ResponseEntity<List<EmployeDTO>> findByAgenceAndRole(@PathVariable Long agenceId,
                                                                @RequestParam RoleEmploye role) {
        return ResponseEntity.ok(employeService.findByAgenceAndRole(agenceId, role));
    }

    @Operation(summary = "Employés dont le nom contient une chaîne (findByNomContaining)")
    @GetMapping("/recherche")
    public ResponseEntity<List<EmployeDTO>> findByNom(@RequestParam String nom) {
        return ResponseEntity.ok(employeService.findByNomContaining(nom));
    }
}
