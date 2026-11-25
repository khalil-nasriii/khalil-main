package tn.esprit.autoloc.web.dto;

/** Atelier 8 — projection JPQL : un client et son nombre de réservations. */
public record TopClientDTO(Long clientId, String nom, String prenom, Long nombreReservations) {
}
