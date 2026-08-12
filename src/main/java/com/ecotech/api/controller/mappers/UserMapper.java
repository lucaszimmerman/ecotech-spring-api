package com.ecotech.api.controller.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.ecotech.api.controller.dto.CreateUserDTO;
import com.ecotech.api.controller.dto.UpdateUserDTO;
import com.ecotech.api.controller.dto.UserResponseDTO;
import com.ecotech.api.controller.dto.UserSummaryDTO;
import com.ecotech.api.model.User;


@Mapper(componentModel = "spring", uses = ImageUrlMapper.class)
public interface UserMapper {

    @Mapping(target = "active", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "coverImageUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileImageUrl", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "website", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    User toEntity(CreateUserDTO createUserDTO);

    @Mapping(target = "profileImageUrl", source = "profileImageUrl", qualifiedByName = "toPublicImageUrl")
    @Mapping(target = "coverImageUrl", source = "coverImageUrl", qualifiedByName = "toPublicImageUrl")
    UserResponseDTO toResponseDTO(User user);

    @Mapping(target = "profileImageUrl", source = "profileImageUrl", qualifiedByName = "toPublicImageUrl")
    UserSummaryDTO toSummaryDTO(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    void updateEntity(
        UpdateUserDTO dto,
        @MappingTarget User user
    );
}
