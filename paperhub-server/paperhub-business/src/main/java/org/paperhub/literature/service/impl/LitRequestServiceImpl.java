package org.paperhub.literature.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.paperhub.auth.entity.LitRequest;
import org.paperhub.literature.mapper.LitRequestMapper;
import org.paperhub.auth.vo.PageResult;
import org.paperhub.literature.service.LitRequestService;
import org.springframework.stereotype.Service;

@Service
public class LitRequestServiceImpl implements LitRequestService {
    private final LitRequestMapper litRequestMapper;

    public LitRequestServiceImpl(LitRequestMapper litRequestMapper) {
        this.litRequestMapper = litRequestMapper;
    }

    /**
     * 分页查询文献互助请求列表，并按创建时间倒序返回。
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
}
