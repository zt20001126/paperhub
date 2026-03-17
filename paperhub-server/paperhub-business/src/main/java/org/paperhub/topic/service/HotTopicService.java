package org.paperhub.topic.service;

import org.paperhub.topic.vo.HotTopicPageVO;

import java.util.List;

public interface HotTopicService {
    /**
     * Query all hot topic records.
     */
    List<HotTopicPageVO> listAll();

    /**
     * Increase view count by 1.
     */
    void increaseViewCount(Long id);
}
