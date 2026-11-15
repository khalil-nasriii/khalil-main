package tn.esprit.autoloc.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IEquipementRepository;
import tn.esprit.autoloc.service.IEquipementService;
import tn.esprit.autoloc.web.dto.EquipementDTO;
import tn.esprit.autoloc.web.mapper.EquipementMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipementServiceImpl implements IEquipementService {

    private static final String RESSOURCE = "Equipement";

    private final IEquipementRepository equipementRepository;
    private final EquipementMapper equipementMapper;

    @Override
    public EquipementDTO create(EquipementDTO dto) {
        Regles.exigerNouveau(dto.getId(), RESSOURCE);
        valider(dto);
        return equipementMapper.toDto(equipementRepository.save(equipementMapper.toEntity(dto)));
    }

    @Override
    @Transactional(readOnly = true)
    public EquipementDTO findById(Long id) {
        return equipementMapper.toDto(charger(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipementDTO> findAll() {
        return equipementRepository.findAll().stream()
                .map(equipementMapper::toDto)
                .toList();
    }

    @Override
    public EquipementDTO update(Long id, EquipementDTO dto) {
        Equipement existant = charger(id);
        valider(dto);
        equipementMapper.updateEntity(dto, existant);
        return equipementMapper.toDto(equipementRepository.save(existant));
    }

    @Override
    public void deleteById(Long id) {
        Equipement equipement = charger(id);
        // retirer l'équipement des véhicules (côté propriétaire de la table de jointure)
        equipement.getVehicules().forEach(v -> v.getEquipements().remove(equipement));
        equipementRepository.delete(equipement);
    }

    private Equipement charger(Long id) {
        return equipementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    private void valider(EquipementDTO dto) {
        Regles.exigerNonVide(dto.getLibelle(), "Le libellé de l'équipement est obligatoire");
    }
}
