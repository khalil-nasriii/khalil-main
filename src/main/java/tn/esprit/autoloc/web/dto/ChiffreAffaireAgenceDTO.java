package tn.esprit.autoloc.web.dto;

import java.math.BigDecimal;

/**
 * Atelier 8 — projection JPQL : chiffre d'affaires d'une agence.
 * Règle de calcul : somme des montantTotal des contrats VALIDES rattachés à une
 * réservation dont le véhicule appartient à l'agence.
 */
public record ChiffreAffaireAgenceDTO(Long agenceId, String nomAgence, BigDecimal chiffreAffaires) {
}
