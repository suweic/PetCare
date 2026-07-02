package com.petcare.system.converter;

import com.petcare.system.dto.UserInfoDTO;
import com.petcare.system.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    UserInfoDTO toUserInfoDTO(User user);
}
