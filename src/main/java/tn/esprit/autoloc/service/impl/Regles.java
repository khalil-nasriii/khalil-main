package tn.esprit.autoloc.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Règles de gestion communes aux services (évite la duplication et les
 * conditions trop complexes signalées par SonarQube for IDE).
 * Une règle violée lève IllegalArgumentException, traduite en 400.
 */
final class Regles {

    private Regles() {
    }

    /** Une création ne doit jamais porter d'identifiant (sinon save() ferait un merge). */
    static void exigerNouveau(Long id, String ressource) {
        if (id != null) {
            throw new IllegalArgumentException(
                    ressource + " : une création ne doit pas contenir d'identifiant");
        }
    }

    static void exigerNonVide(String valeur, String message) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    static void exigerNonNul(Object valeur, String message) {
        if (valeur == null) {
            throw new IllegalArgumentException(message);
        }
    }

    static void exigerPositifOuNul(BigDecimal valeur, String message) {
        if (valeur == null || valeur.signum() < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    static void exigerStrictementPositif(BigDecimal valeur, String message) {
        if (valeur == null || valeur.signum() <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /** La date de fin, si elle est renseignée, ne peut pas précéder la date de début. */
    static void exigerFinApresDebut(LocalDate debut, LocalDate fin, String message) {
        if (debut != null && fin != null && fin.isBefore(debut)) {
            throw new IllegalArgumentException(message);
        }
    }
}
