package tn.esprit.autoloc.service;

import java.util.List;
import tn.esprit.autoloc.web.dto.ClientDTO;

public interface IClientService {

    ClientDTO create(ClientDTO dto);

    ClientDTO findById(Long id);

    List<ClientDTO> findAll();

    ClientDTO update(Long id, ClientDTO dto);

    void deleteById(Long id);


    // ---------- Atelier 8 — recherches ----------

    ClientDTO findByEmail(String email);

    ClientDTO findByNumPermis(String numPermis);

    List<ClientDTO> findByNomContaining(String nom);

    boolean existsByEmail(String email);
}
