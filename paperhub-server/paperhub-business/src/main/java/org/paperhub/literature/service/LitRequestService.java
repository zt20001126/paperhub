package org.paperhub.literature.service;

import org.paperhub.literature.dto.CreateLitRequestRequest;
import org.paperhub.literature.po.LitRequest;
import org.paperhub.auth.vo.PageResult;
import org.springframework.web.multipart.MultipartFile;

public interface LitRequestService {
    /**
     * Query literature requests by page.
     */
    PageResult<LitRequest> page(long current, long size);

    /**
     * sub_lit business entry: validate and submit a literature request.
     */
    void subLit(CreateLitRequestRequest request);

    /**
     * Upload a PDF answer for an existing literature request.
     */
    void assist(String token, Long litRequestId, MultipartFile file);
}
