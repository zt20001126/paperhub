package org.paperhub.literature.service;

import org.paperhub.auth.dto.CreateLitRequestRequest;
import org.paperhub.auth.entity.LitRequest;
import org.paperhub.auth.vo.PageResult;

public interface LitRequestService {
    /**
     * Query literature requests by page.
     */
    PageResult<LitRequest> page(long current, long size);

    /**
     * sub_lit business entry: validate and submit a literature request.
     */
    void subLit(CreateLitRequestRequest request);
}
