package tn.esprit.autoloc.service;

import java.util.List;
import tn.esprit.autoloc.web.dto.VehiculeDTO;

public interface IVehiculeService {

    VehiculeDTO create(VehiculeDTO dto);

    VehiculeDTO findById(Long id);

    List<VehiculeDTO> findAll();

    VehiculeDTO update(Long id, VehiculeDTO dto);

    void deleteById(Long id);

    /** Atelier 7 : rattache (ou transfère) un véhicule à une agence. */
    VehiculeDTO affecterVehiculeAAgence(Long vehiculeId, Long agenceId);

    /** Atelier 7 : détache un véhicule de son agence (IllegalStateException s'il n'en a pas). */
    VehiculeDTO desaffecterVehiculeDeAgence(Long vehiculeId);
}
