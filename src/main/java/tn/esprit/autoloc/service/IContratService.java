package tn.esprit.autoloc.service;

import java.time.LocalDate;
import java.util.List;
import tn.esprit.autoloc.web.dto.ChiffreAffaireAgenceDTO;
import tn.esprit.autoloc.web.dto.ContratDTO;
import tn.esprit.autoloc.web.dto.PaiementDTO;

public interface IContratService {

    ContratDTO create(ContratDTO dto);

    ContratDTO findById(Long id);

    List<ContratDTO> findAll();

    ContratDTO update(Long id, ContratDTO dto);

    void deleteById(Long id);

    /** Composition : un paiement se crée toujours à travers son contrat. */
    PaiementDTO ajouterPaiement(Long contratId, PaiementDTO dto);

    /** Composition : retirer un paiement le supprime (orphanRemoval). */
    void retirerPaiement(Long contratId, Long paiementId);


    // ---------- Atelier 8 — JPQL ----------

    List<ChiffreAffaireAgenceDTO> chiffreAffaireParAgence();

    List<ContratDTO> trouverContratsAEcheanceProche(LocalDate aujourdhui, LocalDate dateLimite);
}
