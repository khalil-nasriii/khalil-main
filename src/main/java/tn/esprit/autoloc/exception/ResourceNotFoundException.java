package tn.esprit.autoloc.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String ressource, Long id) {
        super(ressource + " introuvable avec l'identifiant " + id);
    }

    /** Atelier 8 : recherche par critère (email, ville, permis...). */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
