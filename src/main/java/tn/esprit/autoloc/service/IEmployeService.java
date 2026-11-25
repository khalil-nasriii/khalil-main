package tn.esprit.autoloc.service;

import java.util.List;
import tn.esprit.autoloc.domain.RoleEmploye;
import tn.esprit.autoloc.web.dto.EmployeDTO;

public interface IEmployeService {

    EmployeDTO create(EmployeDTO dto);

    EmployeDTO findById(Long id);

    List<EmployeDTO> findAll();

    EmployeDTO update(Long id, EmployeDTO dto);

    void deleteById(Long id);

    /** Atelier 7 : rattache (ou transfère) un employé à une agence. */
    EmployeDTO affecterEmployeAAgence(Long employeId, Long agenceId);

    /** Atelier 7 : détache un employé de son agence (IllegalStateException s'il n'en a pas). */
    EmployeDTO desaffecterEmployeDeAgence(Long employeId);


    // ---------- Atelier 8 — recherches ----------

    List<EmployeDTO> findByAgenceNom(String nomAgence);

    List<EmployeDTO> findByAgenceAndRole(Long agenceId, RoleEmploye role);

    List<EmployeDTO> findByNomContaining(String nom);
}
