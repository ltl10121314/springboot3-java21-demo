package org.example.springboot3java21demo.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.springboot3java21demo.payment.domain.ReconcileTask;
import org.example.springboot3java21demo.payment.model.ApiResponse;
import org.example.springboot3java21demo.payment.model.ReconcileReport;
import org.example.springboot3java21demo.payment.repository.ReconcileTaskRepository;
import org.example.springboot3java21demo.payment.service.OutboxRelay;
import org.example.springboot3java21demo.payment.service.PaymentTimeoutCompensator;
import org.example.springboot3java21demo.payment.service.ReconcileService;
import org.example.springboot3java21demo.payment.support.PaymentMetrics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 运维/复盘接口。
 *
 * <p>这几个接口把「闭环」做成了可操作的动作，而不是只能看日志：
 * 手工触发消息补投、手工触发未决单补偿、手工触发对账、查看指标与待处理工单。
 * 生产环境必须加权限控制和审计日志。</p>
 */
@Tag(name = "支付运维", description = "消息补投、未决单补偿、对账与指标查询")
@RestController
@RequestMapping("/api/payment/admin")
@RequiredArgsConstructor
public class PaymentAdminController {

    private static final DateTimeFormatter BIZ_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final OutboxRelay outboxRelay;
    private final PaymentTimeoutCompensator compensator;
    private final ReconcileService reconcileService;
    private final ReconcileTaskRepository reconcileTaskRepository;
    private final PaymentMetrics paymentMetrics;

    @Operation(summary = "手工投递本地消息表", description = "返回本轮成功投递条数，用于 MQ 故障恢复后补投")
    @PostMapping("/outbox/relay")
    public ApiResponse<Integer> relayOutbox() {
        return ApiResponse.success(outboxRelay.relayOnce());
    }

    @Operation(summary = "手工触发未决单补偿", description = "向渠道查询 PAYING/UNKNOWN 的支付单并闭环，返回处理条数")
    @PostMapping("/compensate")
    public ApiResponse<Integer> compensate() {
        return ApiResponse.success(compensator.compensateOnce());
    }

    @Operation(summary = "触发对账", description = "按账期比对本地流水与渠道账单，差异自动生成工单")
    @PostMapping("/reconcile")
    public ApiResponse<ReconcileReport> reconcile(@RequestParam(required = false) String bizDate) {
        String date = bizDate == null || bizDate.isBlank()
                ? LocalDate.now().format(BIZ_DATE_FORMATTER)
                : bizDate;
        return ApiResponse.success(reconcileService.reconcile(date));
    }

    @Operation(summary = "查询待处理差异工单")
    @GetMapping("/reconcile/tasks")
    public ApiResponse<List<ReconcileTask>> reconcileTasks(@RequestParam(required = false) String bizDate) {
        if (bizDate == null || bizDate.isBlank()) {
            return ApiResponse.success(reconcileTaskRepository.findOpen());
        }
        return ApiResponse.success(reconcileTaskRepository.findByBizDate(bizDate));
    }

    @Operation(summary = "支付链路指标", description = "成功率、P99 耗时、未知结果数、限流/熔断次数等")
    @GetMapping("/metrics")
    public ApiResponse<Map<String, Long>> metrics() {
        return ApiResponse.success(paymentMetrics.snapshot());
    }
}
