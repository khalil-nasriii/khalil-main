package tn.esprit.autoloc.legacy;

/**
 * Atelier 6 — Partie 1 : AutoLoc v0, conservée volontairement pour l'analyse.
 *
 * Cette classe concentre toutes les responsabilités (calcul du prix, création du contrat,
 * notification, paiement, persistance) et choisit le tarif par une cascade de if/else.
 * Elle viole SRP, OCP et DIP. Elle n'est PAS un bean Spring et n'est utilisée nulle part :
 * la version refactorée se trouve dans service.strategy, service.factory et service.observer.
 * Voir docs/gof-solid-notes.md.
 *
 * @deprecated remplacée par ITarificationStrategy / TarificationStrategyFactory / ReservationSubject.
 */
@Deprecated(since = "Atelier 6")
@SuppressWarnings({"java:S106", "java:S1144", "unused"}) // System.out et méthode inutilisée gardés pour l'analyse
public class AutoLocManager {

    public void louerVehicule(String typeReservation, int nombreJours, String clientEmail) {
        double prix = 0;
        if (typeReservation.equals("LongDuree")) {
            prix = nombreJours * 120;
        } else if (typeReservation.equals("Weekend")) {
            prix = nombreJours * 180;
        } else if (typeReservation.equals("Standard")) {
            prix = nombreJours * 150;
        }
        System.out.println("Contrat créé");
        System.out.println("Prix total : " + prix);

        envoyerEmail(clientEmail);
        enregistrerPaiement();
        sauvegarderContrat();
    }

    private void envoyerEmail(String email) {
        System.out.println("Email envoyé à " + email);
    }

    private void envoyerSMS(String sms) {
        System.out.println("SMS envoyé à " + sms);
    }

    private void enregistrerPaiement() {
        System.out.println("Paiement enregistré");
    }

    private void sauvegarderContrat() {
        System.out.println("Contrat sauvegardé");
    }
}
