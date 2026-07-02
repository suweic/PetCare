package com.petcare.system.converter;

import com.petcare.system.dto.PetDetailDTO;
import com.petcare.system.dto.PetListItemDTO;
import com.petcare.system.entity.Pet;
import com.petcare.system.enums.Gender;
import com.petcare.system.enums.Species;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PetConverter {

    PetConverter INSTANCE = Mappers.getMapper(PetConverter.class);

    @Mapping(source = "birthday", target = "birthDate")
    @Mapping(source = "sterilized", target = "neutered")
    @Mapping(source = "species", target = "speciesName", qualifiedByName = "toSpeciesName")
    @Mapping(source = "gender", target = "genderName", qualifiedByName = "toGenderName")
    PetDetailDTO toDetailDTO(Pet pet);

    @Mapping(source = "birthday", target = "birthDate")
    @Mapping(source = "sterilized", target = "neutered")
    @Mapping(source = "species", target = "speciesName", qualifiedByName = "toSpeciesName")
    @Mapping(source = "gender", target = "genderName", qualifiedByName = "toGenderName")
    PetListItemDTO toListItemDTO(Pet pet);

    List<PetListItemDTO> toListItemDTOList(List<Pet> pets);

    @Named("toSpeciesName")
    default String toSpeciesName(Integer code) {
        Species s = Species.of(code);
        return s != null ? s.getDescription() : null;
    }

    @Named("toGenderName")
    default String toGenderName(Integer code) {
        Gender g = Gender.of(code);
        return g != null ? g.getDescription() : null;
    }
}
