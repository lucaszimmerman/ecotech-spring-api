package com.ecotech.api.controller.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.ecotech.api.controller.dto.CommentResponseDTO;
import com.ecotech.api.controller.dto.CreateCommentDTO;
import com.ecotech.api.controller.dto.UpdateCommentDTO;
import com.ecotech.api.model.Comment;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toEntity(CreateCommentDTO createCommentDTO);

    CommentResponseDTO toResponseDTO(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(
            UpdateCommentDTO dto,
            @MappingTarget Comment comment);

}
