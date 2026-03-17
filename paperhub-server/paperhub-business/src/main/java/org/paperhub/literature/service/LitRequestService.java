package org.paperhub.literature.service;

import org.paperhub.auth.entity.LitRequest;
import org.paperhub.auth.vo.PageResult;

public interface LitRequestService {
    PageResult<LitRequest> page(long current, long size);
}
