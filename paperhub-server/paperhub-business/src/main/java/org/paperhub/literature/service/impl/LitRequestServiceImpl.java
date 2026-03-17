package org.paperhub.literature.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.paperhub.auth.dto.CreateLitRequestRequest;
import org.paperhub.auth.entity.LitRequest;
import org.paperhub.auth.entity.SysUser;
import org.paperhub.auth.mapper.AuthMapper;
import org.paperhub.auth.vo.PageResult;
import org.paperhub.exception.BizException;
import org.paperhub.literature.mapper.LitRequestMapper;
import org.paperhub.literature.service.LitRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LitRequestServiceImpl implements LitRequestService {
    private final LitRequestMapper litRequestMapper;
    private final AuthMapper authMapper;

    public LitRequestServiceImpl(LitRequestMapper litRequestMapper, AuthMapper authMapper) {
        this.litRequestMapper = litRequestMapper;
        this.authMapper = authMapper;
    }

    /**
     * Query literature requests by page and order by create time desc.
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
     * sub_lit core flow:
     * 1) verify user exists
     * 2) verify user points are enough
     * 3) deduct points
     * 4) insert literature request
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void subLit(CreateLitRequestRequest request) {
        SysUser user = authMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }

        int rewardPoints = request.getRewardPoints() == null ? 0 : request.getRewardPoints();
        int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        if (currentPoints < rewardPoints) {
            throw new BizException("积分不足，无法发布求助");
        }

        user.setPoints(currentPoints - rewardPoints);
        authMapper.updateById(user);

        LitRequest litRequest = new LitRequest();
        litRequest.setUserId(user.getId());
        litRequest.setTitle(request.getTitle());
        litRequest.setJournal(request.getJournal());
        litRequest.setDoi(request.getDoi());
        litRequest.setRewardPoints(rewardPoints);
        litRequest.setStatus(0);
        litRequestMapper.insert(litRequest);
    }
}
