package org.paperhub.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.paperhub.auth.entity.SysUser;
import org.paperhub.auth.mapper.AuthMapper;
import org.paperhub.exception.BizException;
import org.paperhub.group.dto.CreatePostRequest;
import org.paperhub.group.entity.Post;
import org.paperhub.group.mapper.PostMapper;
import org.paperhub.group.service.PostService;
import org.paperhub.group.vo.PostListVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    private final PostMapper postMapper;
    private final AuthMapper authMapper;

    public PostServiceImpl(PostMapper postMapper, AuthMapper authMapper) {
        this.postMapper = postMapper;
        this.authMapper = authMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(CreatePostRequest request) {
        SysUser user = authMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        Post post = new Post();
        post.setUserId(request.getUserId());
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent().trim());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setPublishTime(now);
        post.setUpdateTime(now);
        post.setStatus(1);
        postMapper.insert(post);
    }

    @Override
    public List<PostListVO> list() {
        LambdaQueryWrapper<Post> query = new LambdaQueryWrapper<>();
        query.eq(Post::getStatus, 1).orderByDesc(Post::getPublishTime).orderByDesc(Post::getId);
        List<Post> posts = postMapper.selectList(query);
        if (posts.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> userIds = posts.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = authMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));

        return posts.stream().map(post -> toVO(post, userMap.get(post.getUserId()))).collect(Collectors.toList());
    }

    private PostListVO toVO(Post post, SysUser user) {
        PostListVO vo = new PostListVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setUserNickname(user == null ? "--" : user.getNickname());
        vo.setUserAvatarUrl(user == null ? "" : user.getAvatarUrl());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setLikeCount(post.getLikeCount() == null ? 0 : post.getLikeCount());
        vo.setViewCount(post.getViewCount() == null ? 0 : post.getViewCount());
        vo.setPublishTime(post.getPublishTime());
        return vo;
    }
}
