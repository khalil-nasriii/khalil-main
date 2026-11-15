package tn.esprit.autoloc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idContrat;

    private LocalDate dateSignature;
    private BigDecimal montantTotal;
    private boolean valide;

    // côté inverse : la clé étrangère contrat_id est portée par Reservation
    @OneToOne(mappedBy = "contrat")
    @JsonIgnore
    private Reservation reservation;

    // composition : les paiements vivent et meurent avec leur contrat
    @OneToMany(mappedBy = "contrat", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("contrat-paiements")
    private List<Paiement> paiements = new ArrayList<>();

    /** Ajoute un paiement en maintenant les deux côtés de l'association. */
    public void ajouterPaiement(Paiement paiement) {
        paiements.add(paiement);
        paiement.setContrat(this);
    }

    /** Retire un paiement ; orphanRemoval le supprime en base. */
    public void retirerPaiement(Paiement paiement) {
        paiements.remove(paiement);
        paiement.setContrat(null);
    }
}
