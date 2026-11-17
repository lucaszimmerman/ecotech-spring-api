package com.ecotech.api.controller.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.ecotech.api.controller.dto.CreatePostDTO;
import com.ecotech.api.controller.dto.PostResponseDTO;
import com.ecotech.api.controller.dto.PostInteractionSummaryDTO;
import com.ecotech.api.controller.dto.UpdatePostDTO;
import com.ecotech.api.model.Post;
import com.ecotech.api.service.ImageUrlService;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ImageUrlService.class})
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Post toEntity(CreatePostDTO createPostDTO);

    @Mapping(target = "likeCount", source = "interactions.likeCount")
    @Mapping(target = "commentCount", source = "interactions.commentCount")
    @Mapping(target = "likedByCurrentUser", source = "interactions.likedByCurrentUser")
    @Mapping(target = "imageUrl", source = "post.imageUrl", qualifiedByName = "imageUrl")
    PostResponseDTO toResponseDTO(Post post, PostInteractionSummaryDTO interactions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(
            UpdatePostDTO dto,
            @MappingTarget Post post);
}
