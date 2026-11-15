package tn.esprit.autoloc.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.web.dto.VehiculeDTO;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {

    @Mapping(target = "id", source = "idVehicule")
    @Mapping(target = "agenceId", source = "agence.idAgence")
    VehiculeDTO toDto(Vehicule vehicule);

    // l'agence est rattachée par le service à partir de agenceId
    @Mapping(target = "idVehicule", source = "id")
    @Mapping(target = "agence", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "maintenances", ignore = true)
    @Mapping(target = "equipements", ignore = true)
    Vehicule toEntity(VehiculeDTO dto);

    @Mapping(target = "idVehicule", ignore = true)
    @Mapping(target = "agence", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "maintenances", ignore = true)
    @Mapping(target = "equipements", ignore = true)
    void updateEntity(VehiculeDTO dto, @MappingTarget Vehicule vehicule);
}
