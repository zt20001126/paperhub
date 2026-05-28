package org.paperhub.literature.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.paperhub.auth.mapper.AuthMapper;
import org.paperhub.auth.po.SysUser;
import org.paperhub.auth.service.AuthService;
import org.paperhub.auth.vo.PageResult;
import org.paperhub.exception.BizException;
import org.paperhub.literature.dto.CreateLitRequestRequest;
import org.paperhub.literature.mapper.LitAssistMapper;
import org.paperhub.literature.mapper.LitRequestMapper;
import org.paperhub.literature.po.LitAssist;
import org.paperhub.literature.po.LitRequest;
import org.paperhub.literature.service.LitRequestService;
import org.paperhub.storage.service.MinioStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class LitRequestServiceImpl implements LitRequestService {
    private final LitRequestMapper litRequestMapper;
    private final LitAssistMapper litAssistMapper;
    private final AuthMapper authMapper;
    private final AuthService authService;
    private final MinioStorageService minioStorageService;

    public LitRequestServiceImpl(
            LitRequestMapper litRequestMapper,
            LitAssistMapper litAssistMapper,
            AuthMapper authMapper,
            AuthService authService,
            MinioStorageService minioStorageService) {
        this.litRequestMapper = litRequestMapper;
        this.litAssistMapper = litAssistMapper;
        this.authMapper = authMapper;
        this.authService = authService;
        this.minioStorageService = minioStorageService;
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

    /**
     * Store a helper's PDF in MinIO and persist metadata for later review/download.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assist(String token, Long litRequestId, MultipartFile file) {
        SysUser currentUser = authService.getCurrentUser(token);
        LitRequest litRequest = litRequestMapper.selectById(litRequestId);
        if (litRequest == null) {
            throw new BizException("文献求助不存在");
        }
        if (file == null || file.isEmpty()) {
            throw new BizException("请上传 PDF 文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new BizException("只能上传 pdf 结尾的文件");
        }
        if (!isPdf(file)) {
            throw new BizException("文件内容不是有效的 PDF");
        }

        String objectName = minioStorageService.uploadPdf(litRequestId, currentUser.getId(), file);

        LitAssist assist = new LitAssist();
        assist.setLitRequestId(litRequestId);
        assist.setUserId(currentUser.getId());
        assist.setOriginalFilename(originalFilename);
        assist.setObjectName(objectName);
        assist.setFileSize(file.getSize());
        assist.setStatus(0);
        litAssistMapper.insert(assist);

        if (Integer.valueOf(0).equals(litRequest.getStatus())) {
            litRequest.setStatus(1);
            litRequestMapper.updateById(litRequest);
        }
    }

    private boolean isPdf(MultipartFile file) {
        String contentType = file.getContentType();
        if ("application/pdf".equalsIgnoreCase(contentType)) {
            return true;
        }

        byte[] header = new byte[4];
        try (InputStream inputStream = file.getInputStream()) {
            return inputStream.read(header) == 4
                    && header[0] == '%'
                    && header[1] == 'P'
                    && header[2] == 'D'
                    && header[3] == 'F';
        } catch (IOException ex) {
            throw new BizException("读取上传文件失败");
        }
    }
}
