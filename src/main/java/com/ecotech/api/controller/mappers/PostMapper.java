package com.ecotech.api.controller.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.ecotech.api.controller.dto.CreatePostDTO;
import com.ecotech.api.controller.dto.PostResponseDTO;
import com.ecotech.api.controller.dto.UpdatePostDTO;
import com.ecotech.api.model.Post;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Post toEntity(CreatePostDTO createPostDTO);

    PostResponseDTO toResponseDTO(Post post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(
            UpdatePostDTO dto,
            @MappingTarget Post post);
}
