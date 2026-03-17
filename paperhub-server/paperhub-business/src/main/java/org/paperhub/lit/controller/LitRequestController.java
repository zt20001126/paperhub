package org.paperhub.lit.controller;

import org.paperhub.auth.entity.LitRequest;
import org.paperhub.auth.vo.PageResult;
import org.paperhub.lit.service.LitRequestService;
import org.paperhub.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lit-request")
public class LitRequestController {
    private final LitRequestService litRequestService;

    public LitRequestController(LitRequestService litRequestService) {
        this.litRequestService = litRequestService;
    }

    @GetMapping("/page")
    public Result<PageResult<LitRequest>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "5") Long size) {
        return Result.ok(litRequestService.page(current, size));
    }
}
