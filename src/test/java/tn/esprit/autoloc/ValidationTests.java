package tn.esprit.autoloc;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.ModePaiement;
import tn.esprit.autoloc.web.dto.ClientDTO;
import tn.esprit.autoloc.web.dto.PaiementDTO;
import tn.esprit.autoloc.web.dto.ReservationRequestDTO;
import tn.esprit.autoloc.web.dto.VehiculeDTO;
import tn.esprit.autoloc.web.validator.ReservationRequestValidator;

/**
 * Atelier 7 - Partie 5.6 : Bean Validation sur les DTO et validateur personnalisé.
 * Tests unitaires : pas de base de données nécessaire.
 */
class ValidationTests {

    private static ValidatorFactory factory;
    private static Validator validator;
    private final ReservationRequestValidator reservationValidator = new ReservationRequestValidator();

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    private static <T> Set<String> champsInvalides(T dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(java.util.stream.Collectors.toSet());
    }

    private static ReservationRequestDTO reservation(LocalDate debut, LocalDate fin) {
        return ReservationRequestDTO.builder().clientId(1L).vehiculeId(1L).dateDebut(debut).dateFin(fin).build();
    }

    private Errors validerAvecLeValidateur(ReservationRequestDTO dto) {
        Errors errors = new BeanPropertyBindingResult(dto, "reservationRequestDTO");
        reservationValidator.validate(dto, errors);
        return errors;
    }

    @Test
    void clientAvecEmailInvalideRejete() {
        ClientDTO client = ClientDTO.builder().nom("Ben Ali").prenom("Ahmed").email("pas-un-email").build();
        assertThat(champsInvalides(client)).containsExactly("email");
    }

    @Test
    void vehiculeSansImmatriculationRejete() {
        VehiculeDTO vehicule = VehiculeDTO.builder().marque("Renault").modele("Clio")
                .categorie(CategorieVehicule.CITADINE).tarifJournalier(new BigDecimal("80")).agenceId(1L).build();
        assertThat(champsInvalides(vehicule)).containsExactly("immatriculation");
    }

    @Test
    void paiementAvecMontantNulOuNegatifRejete() {
        PaiementDTO zero = PaiementDTO.builder().montant(BigDecimal.ZERO).modePaiement(ModePaiement.CARTE).contratId(1L).build();
        PaiementDTO negatif = PaiementDTO.builder().montant(new BigDecimal("-10")).modePaiement(ModePaiement.CARTE).contratId(1L).build();
        assertThat(champsInvalides(zero)).containsExactly("montant");
        assertThat(champsInvalides(negatif)).containsExactly("montant");
    }

    @Test
    void reservationSansDatesRejetee() {
        ReservationRequestDTO dto = reservation(null, null);
        assertThat(champsInvalides(dto)).containsExactlyInAnyOrder("dateDebut", "dateFin");
        // le validateur personnalisé laisse Bean Validation traiter les champs manquants
        assertThat(validerAvecLeValidateur(dto).hasErrors()).isFalse();
    }

    @Test
    void reservationAvecDateFinAvantOuEgaleDateDebutRejeteeParLeValidateur() {
        LocalDate debut = LocalDate.now().plusDays(5);
        Errors avant = validerAvecLeValidateur(reservation(debut, debut.minusDays(1)));
        Errors egale = validerAvecLeValidateur(reservation(debut, debut));

        assertThat(avant.hasFieldErrors("dateFin")).isTrue();
        assertThat(egale.hasFieldErrors("dateFin")).isTrue();
        assertThat(egale.getFieldError("dateFin").getDefaultMessage())
                .isEqualTo("La date de fin doit être postérieure à la date de début");
    }

    @Test
    void reservationAvecDateDebutPasseeRejeteeParLeValidateur() {
        Errors errors = validerAvecLeValidateur(reservation(LocalDate.now().minusDays(1), LocalDate.now().plusDays(3)));
        assertThat(errors.hasFieldErrors("dateDebut")).isTrue();
    }

    @Test
    void reservationAvecDatesCoherentesAcceptee() {
        ReservationRequestDTO dto = reservation(LocalDate.now().plusDays(1), LocalDate.now().plusDays(4));
        assertThat(champsInvalides(dto)).isEmpty();
        assertThat(validerAvecLeValidateur(dto).hasErrors()).isFalse();
    }
}
