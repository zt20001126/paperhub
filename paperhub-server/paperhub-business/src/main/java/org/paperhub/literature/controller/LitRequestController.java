package org.paperhub.literature.controller;

import org.paperhub.auth.dto.CreateLitRequestRequest;
import org.paperhub.auth.entity.LitRequest;
import org.paperhub.auth.vo.PageResult;
import org.paperhub.literature.service.LitRequestService;
import org.paperhub.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/lit-request")
public class LitRequestController {
    private final LitRequestService litRequestService;

    public LitRequestController(LitRequestService litRequestService) {
        this.litRequestService = litRequestService;
    }

    /**
     * Query literature requests by page.
     */
    @GetMapping("/page")
    public Result<PageResult<LitRequest>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "5") Long size) {
        return Result.ok(litRequestService.page(current, size));
    }

    /**
     * sub_lit: submit a literature request and persist it to database.
     */
    @PostMapping("/sub_lit")
    public Result<Void> subLit(@Valid @RequestBody CreateLitRequestRequest request) {
        litRequestService.subLit(request);
        return Result.ok(null);
    }
}
