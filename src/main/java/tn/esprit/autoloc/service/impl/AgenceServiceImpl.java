package tn.esprit.autoloc.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IAgenceRepository;
import tn.esprit.autoloc.service.IAgenceService;
import tn.esprit.autoloc.web.dto.AgenceDTO;
import tn.esprit.autoloc.web.mapper.AgenceMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class AgenceServiceImpl implements IAgenceService {

    private static final String RESSOURCE = "Agence";

    private final IAgenceRepository agenceRepository;
    private final AgenceMapper agenceMapper;

    @Override
    public AgenceDTO create(AgenceDTO dto) {
        Regles.exigerNouveau(dto.getId(), RESSOURCE);
        valider(dto);
        Agence saved = agenceRepository.save(agenceMapper.toEntity(dto));
        return agenceMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AgenceDTO findById(Long id) {
        return agenceMapper.toDto(charger(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgenceDTO> findAll() {
        return agenceRepository.findAll().stream()
                .map(agenceMapper::toDto)
                .toList();
    }

    @Override
    public AgenceDTO update(Long id, AgenceDTO dto) {
        Agence existant = charger(id);
        valider(dto);
        agenceMapper.updateEntity(dto, existant);
        return agenceMapper.toDto(agenceRepository.save(existant));
    }

    @Override
    public void deleteById(Long id) {
        if (!agenceRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        agenceRepository.deleteById(id);
    }

    private Agence charger(Long id) {
        return agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    private void valider(AgenceDTO dto) {
        Regles.exigerNonVide(dto.getNom(), "Le nom de l'agence est obligatoire");
        Regles.exigerNonVide(dto.getVille(), "La ville de l'agence est obligatoire");
    }
}
