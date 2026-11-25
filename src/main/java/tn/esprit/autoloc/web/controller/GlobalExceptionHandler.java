package tn.esprit.autoloc.web.controller;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.web.dto.ApiError;

/**
 * Traduit les exceptions métier en réponses HTTP lisibles (statut, message, horodatage).
 * Les contrôleurs ne produisent jamais eux-mêmes les 400 / 404 / 409.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex) {
        return erreur(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException ex) {
        return erreur(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    /** Bean Validation (@Valid) : liste des champs refusés. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " : " + e.getDefaultMessage())
                .toList();
        return erreur(HttpStatus.BAD_REQUEST, "Données invalides", details);
    }

    /** JSON mal formé, date hors format AAAA-MM-JJ, valeur d'énumération inconnue... */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> jsonIllisible(HttpMessageNotReadableException ex) {
        return erreur(HttpStatus.BAD_REQUEST, "Corps de requête JSON invalide", List.of());
    }

    /** Atelier 8 : paramètre de recherche manquant (?ville=, ?statut=...). */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> parametreManquant(MissingServletRequestParameterException ex) {
        return erreur(HttpStatus.BAD_REQUEST, "Paramètre obligatoire manquant : " + ex.getParameterName(), List.of());
    }

    /** Atelier 8 : valeur de paramètre invalide (statut inconnu, date mal formée, id non numérique). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> parametreInvalide(MethodArgumentTypeMismatchException ex) {
        return erreur(HttpStatus.BAD_REQUEST, "Valeur invalide pour le paramètre : " + ex.getName(), List.of());
    }

    /** État incohérent : désaffecter un véhicule sans agence, annuler deux fois... */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> conflit(IllegalStateException ex) {
        return erreur(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    /** Contrainte de clé étrangère : ex. supprimer un véhicule encore réservé. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integrite(DataIntegrityViolationException ex) {
        return erreur(HttpStatus.CONFLICT,
                "Opération impossible : la ressource est encore référencée par d'autres données",
                List.of());
    }

    private static ResponseEntity<ApiError> erreur(HttpStatus statut, String message, List<String> details) {
        return ResponseEntity.status(statut).body(ApiError.of(statut.value(), message, details));
    }
}
