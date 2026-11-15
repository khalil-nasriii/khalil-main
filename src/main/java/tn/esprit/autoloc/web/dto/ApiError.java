package tn.esprit.autoloc.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Corps JSON des réponses d'erreur : statut HTTP, message, horodatage
 * et, pour les erreurs de validation, la liste des champs refusés.
 */
public record ApiError(int statut, String message, LocalDateTime horodatage, List<String> details) {

    public static ApiError of(int statut, String message) {
        return new ApiError(statut, message, LocalDateTime.now(), List.of());
    }

    public static ApiError of(int statut, String message, List<String> details) {
        return new ApiError(statut, message, LocalDateTime.now(), details);
    }
}
