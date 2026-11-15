package tn.esprit.autoloc.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IMaintenanceRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;
import tn.esprit.autoloc.service.IMaintenanceService;
import tn.esprit.autoloc.web.dto.MaintenanceDTO;
import tn.esprit.autoloc.web.mapper.MaintenanceMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceServiceImpl implements IMaintenanceService {

    private static final String RESSOURCE = "Maintenance";

    private final IMaintenanceRepository maintenanceRepository;
    private final IVehiculeRepository vehiculeRepository;
    private final MaintenanceMapper maintenanceMapper;

    @Override
    public MaintenanceDTO create(MaintenanceDTO dto) {
        Regles.exigerNouveau(dto.getId(), RESSOURCE);
        valider(dto);
        Maintenance maintenance = maintenanceMapper.toEntity(dto);
        maintenance.setVehicule(chargerVehicule(dto.getVehiculeId()));
        return maintenanceMapper.toDto(maintenanceRepository.save(maintenance));
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceDTO findById(Long id) {
        return maintenanceMapper.toDto(charger(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceDTO> findAll() {
        return maintenanceRepository.findAll().stream()
                .map(maintenanceMapper::toDto)
                .toList();
    }

    @Override
    public MaintenanceDTO update(Long id, MaintenanceDTO dto) {
        Maintenance existant = charger(id);
        valider(dto);
        maintenanceMapper.updateEntity(dto, existant);
        if (dto.getVehiculeId() != null) {
            existant.setVehicule(chargerVehicule(dto.getVehiculeId()));
        }
        return maintenanceMapper.toDto(maintenanceRepository.save(existant));
    }

    @Override
    public void deleteById(Long id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        maintenanceRepository.deleteById(id);
    }

    private Maintenance charger(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    private Vehicule chargerVehicule(Long vehiculeId) {
        if (vehiculeId == null) {
            return null;
        }
        return vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", vehiculeId));
    }

    private void valider(MaintenanceDTO dto) {
        Regles.exigerNonNul(dto.getDateDebut(), "La date de début de la maintenance est obligatoire");
        Regles.exigerFinApresDebut(dto.getDateDebut(), dto.getDateFin(),
                "La date de fin ne peut pas être antérieure à la date de début");
    }
}
