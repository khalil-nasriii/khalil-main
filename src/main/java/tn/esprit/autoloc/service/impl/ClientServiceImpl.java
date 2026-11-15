package tn.esprit.autoloc.service.impl;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IClientRepository;
import tn.esprit.autoloc.service.IClientService;
import tn.esprit.autoloc.web.dto.ClientDTO;
import tn.esprit.autoloc.web.mapper.ClientMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements IClientService {

    private static final String RESSOURCE = "Client";

    private final IClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    public ClientDTO create(ClientDTO dto) {
        Regles.exigerNouveau(dto.getId(), RESSOURCE);
        valider(dto);
        Client client = clientMapper.toEntity(dto);
        if (client.getDateInscription() == null) {
            client.setDateInscription(LocalDate.now());
        }
        return clientMapper.toDto(clientRepository.save(client));
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDTO findById(Long id) {
        return clientMapper.toDto(charger(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> findAll() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toDto)
                .toList();
    }

    @Override
    public ClientDTO update(Long id, ClientDTO dto) {
        Client existant = charger(id);
        valider(dto);
        LocalDate dateInscription = existant.getDateInscription();
        clientMapper.updateEntity(dto, existant);
        if (existant.getDateInscription() == null) {
            existant.setDateInscription(dateInscription);
        }
        return clientMapper.toDto(clientRepository.save(existant));
    }

    @Override
    public void deleteById(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        clientRepository.deleteById(id);
    }

    private Client charger(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    private void valider(ClientDTO dto) {
        Regles.exigerNonVide(dto.getNom(), "Le nom du client est obligatoire");
        Regles.exigerNonVide(dto.getEmail(), "L'e-mail du client est obligatoire");
    }
}
