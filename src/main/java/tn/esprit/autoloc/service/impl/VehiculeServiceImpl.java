package tn.esprit.autoloc.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IAgenceRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;
import tn.esprit.autoloc.service.IVehiculeService;
import tn.esprit.autoloc.web.dto.VehiculeDTO;
import tn.esprit.autoloc.web.mapper.VehiculeMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class VehiculeServiceImpl implements IVehiculeService {

    private static final String RESSOURCE = "Vehicule";

    private final IVehiculeRepository vehiculeRepository;
    private final IAgenceRepository agenceRepository;
    private final VehiculeMapper vehiculeMapper;

    @Override
    public VehiculeDTO create(VehiculeDTO dto) {
        Regles.exigerNouveau(dto.getId(), RESSOURCE);
        valider(dto);
        Vehicule vehicule = vehiculeMapper.toEntity(dto);
        if (vehicule.getStatut() == null) {
            vehicule.setStatut(StatutVehicule.DISPONIBLE);
        }
        if (dto.getAgenceId() != null) {
            vehicule.setAgence(chargerAgence(dto.getAgenceId()));
        }
        return vehiculeMapper.toDto(vehiculeRepository.save(vehicule));
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculeDTO findById(Long id) {
        return vehiculeMapper.toDto(charger(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeDTO> findAll() {
        return vehiculeRepository.findAll().stream()
                .map(vehiculeMapper::toDto)
                .toList();
    }

    @Override
    public VehiculeDTO update(Long id, VehiculeDTO dto) {
        Vehicule existant = charger(id);
        valider(dto);
        StatutVehicule statut = existant.getStatut();
        vehiculeMapper.updateEntity(dto, existant);
        if (existant.getStatut() == null) {
            existant.setStatut(statut);
        }
        // un agenceId absent conserve l'agence actuelle : la désaffectation est une opération explicite
        if (dto.getAgenceId() != null) {
            existant.setAgence(chargerAgence(dto.getAgenceId()));
        }
        return vehiculeMapper.toDto(vehiculeRepository.save(existant));
    }

    @Override
    public void deleteById(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        vehiculeRepository.deleteById(id);
    }

    @Override
    public VehiculeDTO affecterVehiculeAAgence(Long vehiculeId, Long agenceId) {
        Vehicule vehicule = charger(vehiculeId);
        Agence agence = chargerAgence(agenceId);
        vehicule.setAgence(agence); // côté propriétaire : met à jour vehicule.agence_id
        return vehiculeMapper.toDto(vehiculeRepository.save(vehicule));
    }

    @Override
    public VehiculeDTO desaffecterVehiculeDeAgence(Long vehiculeId) {
        Vehicule vehicule = charger(vehiculeId);
        if (vehicule.getAgence() == null) {
            throw new IllegalStateException(
                    "Le véhicule " + vehiculeId + " n'est rattaché à aucune agence");
        }
        vehicule.setAgence(null);
        return vehiculeMapper.toDto(vehiculeRepository.save(vehicule));
    }

    private Vehicule charger(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    private Agence chargerAgence(Long agenceId) {
        return agenceRepository.findById(agenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", agenceId));
    }

    private void valider(VehiculeDTO dto) {
        Regles.exigerPositifOuNul(dto.getTarifJournalier(),
                "Le tarif journalier est obligatoire et ne peut pas être négatif");
    }

    // ---------- Atelier 8 — Partie I ----------

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeDTO> findByAgenceVille(String ville) {
        return versDto(vehiculeRepository.findByAgenceVille(ville));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeDTO> findByStatutAndAgence(StatutVehicule statut, Long agenceId) {
        return versDto(vehiculeRepository.findByStatutAndAgence_IdAgence(statut, agenceId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehiculeDTO> findByStatut(StatutVehicule statut, Pageable pageable) {
        return vehiculeRepository.findByStatut(statut, pageable).map(vehiculeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeDTO> findByVilleAndCategorie(String ville, CategorieVehicule categorie) {
        return versDto(vehiculeRepository.findByAgence_VilleAndCategorie(ville, categorie));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean aLeStatut(Long vehiculeId, StatutVehicule statut) {
        return vehiculeRepository.existsByIdVehiculeAndStatut(vehiculeId, statut);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatut(StatutVehicule statut) {
        return vehiculeRepository.countByStatut(statut);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeDTO> findByTarifInferieurA(BigDecimal tarif) {
        Regles.exigerNonNul(tarif, "Le tarif de comparaison est obligatoire");
        return versDto(vehiculeRepository.findByTarifJournalierLessThan(tarif));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeDTO> findByTarifSuperieurA(BigDecimal tarif) {
        Regles.exigerNonNul(tarif, "Le tarif de comparaison est obligatoire");
        return versDto(vehiculeRepository.findByTarifJournalierGreaterThan(tarif));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeDTO> findAllTriesParTarif() {
        return versDto(vehiculeRepository.findAllByOrderByTarifJournalierAsc());
    }

    // ---------- Atelier 8 — Partie II (JPQL) ----------

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeDTO> rechercherVehiculesDisponibles(String ville, CategorieVehicule categorie,
                                                            LocalDate dateDebut, LocalDate dateFin) {
        Regles.exigerNonVide(ville, "La ville est obligatoire");
        Regles.exigerNonNul(categorie, "La catégorie est obligatoire");
        Regles.exigerPeriode(dateDebut, dateFin);
        return versDto(vehiculeRepository.rechercherVehiculesDisponibles(ville, categorie, dateDebut, dateFin));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeDTO> trouverVehiculesAReleaser(LocalDate aujourdhui) {
        Regles.exigerNonNul(aujourdhui, "La date de référence est obligatoire");
        return versDto(vehiculeRepository.trouverVehiculesAReleaser(aujourdhui));
    }

    private List<VehiculeDTO> versDto(List<Vehicule> vehicules) {
        return vehicules.stream().map(vehiculeMapper::toDto).toList();
    }
}
