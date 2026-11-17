package com.ecotech.api.controller.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.ecotech.api.controller.dto.PostInteractionSummaryDTO;
import com.ecotech.api.controller.dto.UpdateUserDTO;
import com.ecotech.api.model.Comment;
import com.ecotech.api.model.Post;
import com.ecotech.api.model.User;
import com.ecotech.api.service.ImageUrlService;

@SpringJUnitConfig({UserMapperImpl.class, PostMapperImpl.class, CommentMapperImpl.class})
class ImageResponseMapperTest {

    @Autowired private UserMapper userMapper;
    @Autowired private PostMapper postMapper;
    @Autowired private CommentMapper commentMapper;
    @MockitoBean private ImageUrlService imageUrlService;

    @BeforeEach
    void setUp() {
        when(imageUrlService.resolve(anyString())).thenAnswer(invocation -> signed(invocation.getArgument(0)));
    }

    private String signed(String key) {
        return "https://images.example.com/" + key + "?signature=temporary";
    }

    private User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("eco-user");
        user.setName("Eco User");
        user.setProfileImageUrl("users/owner/profile/image.png");
        user.setCoverImageUrl("users/owner/cover/image.png");
        return user;
    }

    @Test
    void shouldResolveImagesInPrivatePublicAndSummaryDtosWithoutMutatingUser() {
        User user = user();
        var privateProfile = userMapper.toResponseDTO(user);
        var publicProfile = userMapper.toPublicProfileDTO(user);
        var summary = userMapper.toSummaryDTO(user);

        assertThat(privateProfile.profileImageUrl()).isEqualTo(signed(user.getProfileImageUrl()));
        assertThat(privateProfile.coverImageUrl()).isEqualTo(signed(user.getCoverImageUrl()));
        assertThat(publicProfile.profileImageUrl()).isEqualTo(privateProfile.profileImageUrl());
        assertThat(publicProfile.coverImageUrl()).isEqualTo(privateProfile.coverImageUrl());
        assertThat(summary.profileImageUrl()).isEqualTo(privateProfile.profileImageUrl());
        assertThat(summary.username()).isEqualTo("eco-user");
        assertThat(privateProfile.name()).isEqualTo("Eco User");
        assertThat(user.getProfileImageUrl()).isEqualTo("users/owner/profile/image.png");
        assertThat(user.getCoverImageUrl()).isEqualTo("users/owner/cover/image.png");
    }

    @Test
    void shouldResolvePostAndNestedAuthorImagesWithoutChangingStoredKeys() {
        User user = user();
        Post post = new Post();
        post.setUser(user);
        post.setImageUrl("posts/post-id/image.png");
        post.setContent("Post content must remain unchanged.");

        var response = postMapper.toResponseDTO(post, new PostInteractionSummaryDTO(2, 3, true));
        assertThat(response.imageUrl()).isEqualTo(signed("posts/post-id/image.png"));
        assertThat(response.user().profileImageUrl()).isEqualTo(signed(user.getProfileImageUrl()));
        assertThat(response.content()).isEqualTo(post.getContent());
        assertThat(response.likeCount()).isEqualTo(2);
        assertThat(response.commentCount()).isEqualTo(3);
        assertThat(response.likedByCurrentUser()).isTrue();
        assertThat(post.getImageUrl()).isEqualTo("posts/post-id/image.png");
    }

    @Test
    void shouldResolveCommentAuthorImage() {
        Comment comment = new Comment();
        comment.setUser(user());
        comment.setContent("A comment.");
        assertThat(commentMapper.toResponseDTO(comment).user().profileImageUrl())
                .isEqualTo(signed(comment.getUser().getProfileImageUrl()));
    }

    @Test
    void shouldKeepMissingImageNullInResponses() {
        User user = new User();
        assertThat(userMapper.toResponseDTO(user).profileImageUrl()).isNull();
        assertThat(userMapper.toPublicProfileDTO(user).coverImageUrl()).isNull();
        assertThat(userMapper.toSummaryDTO(user).profileImageUrl()).isNull();
    }

    @Test
    void shouldIgnoreImageFieldsDuringTextProfileUpdates() {
        User user = user();
        UpdateUserDTO update = new UpdateUserDTO("updated-user", "Updated User",
                "https://example.com/temporary-cover", "users/another-user/profile.png",
                "Maringa", "https://example.com", "Updated bio");
        userMapper.updateEntity(update, user);
        assertThat(user.getUsername()).isEqualTo("updated-user");
        assertThat(user.getBio()).isEqualTo("Updated bio");
        assertThat(user.getProfileImageUrl()).isEqualTo("users/owner/profile/image.png");
        assertThat(user.getCoverImageUrl()).isEqualTo("users/owner/cover/image.png");
    }
}
