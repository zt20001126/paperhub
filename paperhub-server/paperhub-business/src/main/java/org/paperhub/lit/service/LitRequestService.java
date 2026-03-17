package org.paperhub.lit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.paperhub.auth.entity.LitRequest;
import org.paperhub.auth.mapper.LitRequestMapper;
import org.paperhub.auth.vo.PageResult;
import org.springframework.stereotype.Service;

@Service
public class LitRequestService {
    private final LitRequestMapper litRequestMapper;

    public LitRequestService(LitRequestMapper litRequestMapper) {
        this.litRequestMapper = litRequestMapper;
    }

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
