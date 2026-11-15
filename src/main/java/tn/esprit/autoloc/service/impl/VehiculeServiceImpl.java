package tn.esprit.autoloc.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Agence;
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
}
