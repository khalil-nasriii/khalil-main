package tn.esprit.autoloc.service;

import java.time.LocalDate;
import java.util.List;
import tn.esprit.autoloc.web.dto.PaiementDTO;

/**
 * Lecture seule : la création et la suppression d'un paiement passent par
 * le Contrat (composition, voir IContratService).
 */
public interface IPaiementService {

    PaiementDTO findById(Long id);

    List<PaiementDTO> findAll();


    // ---------- Atelier 8 — recherches ----------

    List<PaiementDTO> findByContrat(Long contratId);

    List<PaiementDTO> findByDatePaiementEntre(LocalDate debut, LocalDate fin);
}
