package org.paperhub.group.service;

import org.paperhub.group.dto.CreatePostRequest;
import org.paperhub.group.vo.PostListVO;

import java.util.List;

public interface PostService {
    /**
     * Publish a post.
     */
    void publish(CreatePostRequest request);

    /**
     * Query all normal posts.
     */
    List<PostListVO> list();
}
