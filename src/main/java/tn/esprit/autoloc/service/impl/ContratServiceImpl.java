package tn.esprit.autoloc.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IContratRepository;
import tn.esprit.autoloc.repository.IReservationRepository;
import tn.esprit.autoloc.service.IContratService;
import tn.esprit.autoloc.web.dto.ContratDTO;
import tn.esprit.autoloc.web.dto.PaiementDTO;
import tn.esprit.autoloc.web.mapper.ContratMapper;
import tn.esprit.autoloc.web.mapper.PaiementMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class ContratServiceImpl implements IContratService {

    private static final String RESSOURCE = "Contrat";

    private final IContratRepository contratRepository;
    private final IReservationRepository reservationRepository;
    private final ContratMapper contratMapper;
    private final PaiementMapper paiementMapper;

    @Override
    public ContratDTO create(ContratDTO dto) {
        Regles.exigerNouveau(dto.getId(), RESSOURCE);
        valider(dto);
        Contrat contrat = contratMapper.toEntity(dto);
        if (contrat.getDateSignature() == null) {
            contrat.setDateSignature(LocalDate.now());
        }
        Contrat saved = contratRepository.save(contrat);
        if (dto.getReservationId() != null) {
            rattacherReservation(saved, dto.getReservationId());
        }
        return contratMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContratDTO findById(Long id) {
        return contratMapper.toDto(charger(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratDTO> findAll() {
        return contratRepository.findAll().stream()
                .map(contratMapper::toDto)
                .toList();
    }

    @Override
    public ContratDTO update(Long id, ContratDTO dto) {
        Contrat existant = charger(id);
        valider(dto);
        LocalDate dateSignature = existant.getDateSignature();
        // ne recopie que les attributs simples : la collection de paiements n'est jamais écrasée
        contratMapper.updateEntity(dto, existant);
        if (existant.getDateSignature() == null) {
            existant.setDateSignature(dateSignature);
        }
        Long reservationActuelle = existant.getReservation() == null
                ? null : existant.getReservation().getIdReservation();
        if (dto.getReservationId() != null && !dto.getReservationId().equals(reservationActuelle)) {
            detacherReservation(existant);
            rattacherReservation(existant, dto.getReservationId());
        }
        return contratMapper.toDto(contratRepository.save(existant));
    }

    @Override
    public void deleteById(Long id) {
        Contrat contrat = charger(id);
        // la clé étrangère contrat_id est dans reservation : on la libère avant de supprimer
        detacherReservation(contrat);
        contratRepository.delete(contrat); // cascade REMOVE sur les paiements
    }

    @Override
    public PaiementDTO ajouterPaiement(Long contratId, PaiementDTO dto) {
        if (dto.getContratId() != null && !dto.getContratId().equals(contratId)) {
            throw new IllegalArgumentException(
                    "Le contratId du paiement (" + dto.getContratId()
                            + ") ne correspond pas au contrat " + contratId);
        }
        Regles.exigerNouveau(dto.getId(), "Paiement");
        Regles.exigerStrictementPositif(dto.getMontant(), "Le montant doit être strictement positif");
        Regles.exigerNonNul(dto.getModePaiement(), "Le mode de paiement est obligatoire");

        Contrat contrat = charger(contratId);
        Paiement paiement = paiementMapper.toEntity(dto);
        if (paiement.getDatePaiement() == null) {
            paiement.setDatePaiement(LocalDate.now());
        }
        contrat.ajouterPaiement(paiement);
        contratRepository.flush(); // cascade PERSIST : le paiement reçoit son identifiant
        return paiementMapper.toDto(paiement);
    }

    @Override
    public void retirerPaiement(Long contratId, Long paiementId) {
        Contrat contrat = charger(contratId);
        Paiement paiement = contrat.getPaiements().stream()
                .filter(p -> Objects.equals(p.getIdPaiement(), paiementId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", paiementId));
        contrat.retirerPaiement(paiement); // orphanRemoval : DELETE du paiement
    }

    private Contrat charger(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    private void rattacherReservation(Contrat contrat, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));
        if (reservation.getContrat() != null && reservation.getContrat() != contrat) {
            throw new IllegalStateException(
                    "La réservation " + reservationId + " possède déjà un contrat");
        }
        reservation.setContrat(contrat); // côté propriétaire
        contrat.setReservation(reservation); // côté inverse, pour la réponse
    }

    private void detacherReservation(Contrat contrat) {
        Reservation reservation = contrat.getReservation();
        if (reservation != null) {
            reservation.setContrat(null);
            contrat.setReservation(null);
        }
    }

    private void valider(ContratDTO dto) {
        Regles.exigerPositifOuNul(dto.getMontantTotal(),
                "Le montant total est obligatoire et ne peut pas être négatif");
    }
}
