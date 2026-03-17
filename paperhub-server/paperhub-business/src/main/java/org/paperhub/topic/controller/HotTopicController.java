package org.paperhub.topic.controller;

import org.paperhub.result.Result;
import org.paperhub.topic.service.HotTopicService;
import org.paperhub.topic.vo.HotTopicPageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HotTopicController {
    private final HotTopicService hotTopicService;

    public HotTopicController(HotTopicService hotTopicService) {
        this.hotTopicService = hotTopicService;
    }

    /**
     * hot_topic_page: query all hot topic records.
     */
    @GetMapping("/api/hot_topic_page")
    public Result<List<HotTopicPageVO>> hotTopicPage() {
        return Result.ok(hotTopicService.listAll());
    }

    /**
     * Increase hot topic view count by id.
     */
    @PostMapping("/api/hot_topic_view")
    public Result<Void> hotTopicView(@RequestParam("id") Long id) {
        hotTopicService.increaseViewCount(id);
        return Result.ok(null);
    }
}
