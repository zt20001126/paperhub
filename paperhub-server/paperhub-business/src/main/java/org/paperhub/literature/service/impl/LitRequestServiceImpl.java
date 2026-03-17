package org.paperhub.literature.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.paperhub.auth.dto.CreateLitRequestRequest;
import org.paperhub.auth.entity.LitRequest;
import org.paperhub.auth.vo.PageResult;
import org.paperhub.literature.mapper.LitRequestMapper;
import org.paperhub.literature.service.LitRequestService;
import org.springframework.stereotype.Service;

@Service
public class LitRequestServiceImpl implements LitRequestService {
    private final LitRequestMapper litRequestMapper;

    public LitRequestServiceImpl(LitRequestMapper litRequestMapper) {
        this.litRequestMapper = litRequestMapper;
    }

    /**
     * 分页查询文献求助列表，并按发布时间倒序返回。
     */
    @Override
    public PageResult<LitRequest> page(long current, long size) {
        Page<LitRequest> page = new Page<>(current, size);
        LambdaQueryWrapper<LitRequest> query = new LambdaQueryWrapper<>();
        query.orderByDesc(LitRequest::getCreateTime).orderByDesc(LitRequest::getId);

        IPage<LitRequest> result = litRequestMapper.selectPage(page, query);
        PageResult<LitRequest> pageResult = new PageResult<>();
        pageResult.setRecords(result.getRecords());
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());
        return pageResult;
    }

    /**
     * 将用户提交的求助信息写入 literature_request 表。
     */
    @Override
    public void subLit(CreateLitRequestRequest request) {
        LitRequest litRequest = new LitRequest();
        litRequest.setUserId(request.getUserId());
        litRequest.setTitle(request.getTitle());
        litRequest.setJournal(request.getJournal());
        litRequest.setDoi(request.getDoi());
        litRequest.setRewardPoints(request.getRewardPoints());
        litRequest.setStatus(0);
        litRequestMapper.insert(litRequest);
    }
}
