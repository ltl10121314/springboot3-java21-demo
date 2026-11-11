package org.example.springboot3java21demo.common.trace;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

/**
 * 将 traceId 放入 Dubbo attachment，Provider 线程使用同一个 traceId 写日志。
 */
@Activate(
        group = {CommonConstants.CONSUMER, CommonConstants.PROVIDER},
        order = -10_000)
public class TraceIdDubboFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String side = invoker.getUrl().getParameter(CommonConstants.SIDE_KEY);
        if (!CommonConstants.PROVIDER_SIDE.equals(side)) {
            String traceId = TraceContext.current();
            if (traceId == null || traceId.isBlank()) {
                return invoker.invoke(invocation);
            }
            try (TraceContext.Scope ignored = TraceContext.open(traceId)) {
                invocation.setAttachment(TraceConstants.TRACE_ID_MDC_KEY, traceId);
                RpcContext.getClientAttachment()
                        .setAttachment(TraceConstants.TRACE_ID_MDC_KEY, traceId);
                return invoker.invoke(invocation);
            }
        }

        String traceId = TraceContext.sanitize(
                invocation.getAttachment(TraceConstants.TRACE_ID_MDC_KEY));
        if (traceId == null) {
            return invoker.invoke(invocation);
        }
        try (TraceContext.Scope ignored = TraceContext.open(traceId)) {
            return invoker.invoke(invocation);
        }
    }
}
