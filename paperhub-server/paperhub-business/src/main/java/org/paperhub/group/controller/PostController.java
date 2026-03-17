package org.paperhub.group.controller;

import org.paperhub.group.dto.CreatePostRequest;
import org.paperhub.group.service.PostService;
import org.paperhub.group.vo.PostListVO;
import org.paperhub.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/group/post")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Publish a community post.
     */
    @PostMapping("/publish")
    public Result<Void> publish(@Valid @RequestBody CreatePostRequest request) {
        postService.publish(request);
        return Result.ok(null);
    }

    /**
     * Query community post list.
     */
    @GetMapping("/list")
    public Result<List<PostListVO>> list() {
        return Result.ok(postService.list());
    }
}
