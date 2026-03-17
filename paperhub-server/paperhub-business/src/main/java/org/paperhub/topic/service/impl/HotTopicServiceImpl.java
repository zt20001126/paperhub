package org.paperhub.topic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.paperhub.exception.BizException;
import org.paperhub.topic.entity.HotTopic;
import org.paperhub.topic.mapper.HotTopicMapper;
import org.paperhub.topic.service.HotTopicService;
import org.paperhub.topic.vo.HotTopicPageVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotTopicServiceImpl implements HotTopicService {
    private final HotTopicMapper hotTopicMapper;

    public HotTopicServiceImpl(HotTopicMapper hotTopicMapper) {
        this.hotTopicMapper = hotTopicMapper;
    }

    @Override
    public List<HotTopicPageVO> listAll() {
        LambdaQueryWrapper<HotTopic> query = new LambdaQueryWrapper<>();
        query.orderByDesc(HotTopic::getViewCount).orderByDesc(HotTopic::getPublishTime).orderByDesc(HotTopic::getId);

        return hotTopicMapper.selectList(query).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public void increaseViewCount(Long id) {
        HotTopic hotTopic = hotTopicMapper.selectById(id);
        if (hotTopic == null) {
            throw new BizException("热点信息不存在");
        }
        int current = hotTopic.getViewCount() == null ? 0 : hotTopic.getViewCount();
        hotTopic.setViewCount(current + 1);
        hotTopicMapper.updateById(hotTopic);
    }

    private HotTopicPageVO toVO(HotTopic hotTopic) {
        HotTopicPageVO vo = new HotTopicPageVO();
        vo.setId(hotTopic.getId());
        vo.setTitle(hotTopic.getTitle());
        vo.setContent(hotTopic.getContent());
        vo.setCoverUrl(hotTopic.getCoverUrl());
        vo.setViewCount(hotTopic.getViewCount());
        vo.setPublishTime(hotTopic.getPublishTime());
        vo.setUpdateTime(hotTopic.getUpdateTime());
        return vo;
    }
}
