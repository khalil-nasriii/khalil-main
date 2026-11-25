package tn.esprit.autoloc.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.RoleEmploye;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IAgenceRepository;
import tn.esprit.autoloc.repository.IEmployeRepository;
import tn.esprit.autoloc.service.IEmployeService;
import tn.esprit.autoloc.web.dto.EmployeDTO;
import tn.esprit.autoloc.web.mapper.EmployeMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeServiceImpl implements IEmployeService {

    private static final String RESSOURCE = "Employe";

    private final IEmployeRepository employeRepository;
    private final IAgenceRepository agenceRepository;
    private final EmployeMapper employeMapper;

    @Override
    public EmployeDTO create(EmployeDTO dto) {
        Regles.exigerNouveau(dto.getId(), RESSOURCE);
        valider(dto);
        Employe employe = employeMapper.toEntity(dto);
        if (dto.getAgenceId() != null) {
            employe.setAgence(chargerAgence(dto.getAgenceId()));
        }
        return employeMapper.toDto(employeRepository.save(employe));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeDTO findById(Long id) {
        return employeMapper.toDto(charger(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeDTO> findAll() {
        return employeRepository.findAll().stream()
                .map(employeMapper::toDto)
                .toList();
    }

    @Override
    public EmployeDTO update(Long id, EmployeDTO dto) {
        Employe existant = charger(id);
        valider(dto);
        employeMapper.updateEntity(dto, existant);
        if (dto.getAgenceId() != null) {
            existant.setAgence(chargerAgence(dto.getAgenceId()));
        }
        return employeMapper.toDto(employeRepository.save(existant));
    }

    @Override
    public void deleteById(Long id) {
        if (!employeRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        employeRepository.deleteById(id);
    }

    @Override
    public EmployeDTO affecterEmployeAAgence(Long employeId, Long agenceId) {
        Employe employe = charger(employeId);
        Agence agence = chargerAgence(agenceId);
        employe.setAgence(agence); // côté propriétaire : met à jour employe.agence_id
        return employeMapper.toDto(employeRepository.save(employe));
    }

    @Override
    public EmployeDTO desaffecterEmployeDeAgence(Long employeId) {
        Employe employe = charger(employeId);
        if (employe.getAgence() == null) {
            throw new IllegalStateException(
                    "L'employé " + employeId + " n'est rattaché à aucune agence");
        }
        employe.setAgence(null);
        return employeMapper.toDto(employeRepository.save(employe));
    }

    private Employe charger(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    private Agence chargerAgence(Long agenceId) {
        return agenceRepository.findById(agenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", agenceId));
    }

    private void valider(EmployeDTO dto) {
        Regles.exigerNonVide(dto.getNom(), "Le nom de l'employé est obligatoire");
        Regles.exigerNonVide(dto.getPrenom(), "Le prénom de l'employé est obligatoire");
        Regles.exigerNonNul(dto.getRole(), "Le rôle de l'employé est obligatoire");
    }

    // ---------- Atelier 8 ----------

    @Override
    @Transactional(readOnly = true)
    public List<EmployeDTO> findByAgenceNom(String nomAgence) {
        return employeRepository.findByAgenceNom(nomAgence).stream().map(employeMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeDTO> findByAgenceAndRole(Long agenceId, RoleEmploye role) {
        return employeRepository.findByAgence_IdAgenceAndRole(agenceId, role).stream()
                .map(employeMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeDTO> findByNomContaining(String nom) {
        return employeRepository.findByNomContaining(nom).stream().map(employeMapper::toDto).toList();
    }
}
