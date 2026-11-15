package tn.esprit.autoloc.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.web.dto.ContratDTO;

@Mapper(componentModel = "spring")
public interface ContratMapper {

    @Mapping(target = "id", source = "idContrat")
    @Mapping(target = "reservationId", source = "reservation.idReservation")
    ContratDTO toDto(Contrat contrat);

    // la réservation est rattachée par le service (côté propriétaire = Reservation)
    @Mapping(target = "idContrat", source = "id")
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "paiements", ignore = true)
    Contrat toEntity(ContratDTO dto);

    @Mapping(target = "idContrat", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "paiements", ignore = true)
    void updateEntity(ContratDTO dto, @MappingTarget Contrat contrat);
}
