package top.fblue.dubbo.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import top.fblue.log.constant.TraceConst;
import top.fblue.log.utils.TracingUtils;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;
import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

/**
 * Dubbo 全链路追踪 Filter
 * 服务使用者/消费者：设置 traceparent 到 attachment，打印入参和出参
 * 服务提供者/生产者：从 attachment 解析 traceparent 并设置到 MDC，打印入参和出参
 */
@Slf4j
@Activate(group = {PROVIDER, CONSUMER}, order = Integer.MIN_VALUE)
public class DubboTraceFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        boolean isConsumer = RpcContext.getServiceContext().isConsumerSide();
        if (isConsumer) {
            return invokeConsumer(invoker, invocation);
        } else {
            return invokeProvider(invoker, invocation);
        }
    }

    /**
     * 服务使用者/消费者侧：传递 traceparent，记录入参和出参
     */
    private Result invokeConsumer(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String traceParent = TracingUtils.getTraceParentFor();
        RpcContext.getClientAttachment().setAttachment(TraceConst.TRACE_PARENT_HEADER, traceParent);

        String serviceMethod = invoker.getInterface().getSimpleName() + "#" + invocation.getMethodName();
        log.info("dubbo consumer request serviceMethod={} traceparent={}",
                serviceMethod, traceParent);

        long startTime = System.currentTimeMillis();
        try {
            Result result = invoker.invoke(invocation);
            long latency = System.currentTimeMillis() - startTime;
            if (result.hasException()) {
                log.error("dubbo consumer response serviceMethod={} latency={}ms exception={}",
                        serviceMethod, latency, result.getException().getMessage());
            } else {
                log.info("dubbo consumer response serviceMethod={} latency={}ms",
                        serviceMethod, latency);
            }
            return result;
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            log.error("dubbo consumer response serviceMethod={} latency={}ms throwable={}",
                    serviceMethod, latency, e.getMessage());
            throw e;
        } finally {
            RpcContext.getClientAttachment().removeAttachment(TraceConst.TRACE_PARENT_HEADER);
        }
    }

    /**
     * 服务提供者/生产者侧：从 attachment 解析 traceparent 并设置 MDC，记录入参和出参
     */
    private Result invokeProvider(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String traceParent = RpcContext.getServerAttachment().getAttachment(TraceConst.TRACE_PARENT_HEADER);
        TracingUtils.extractAndSetTraceContext(traceParent);

        long startTime = System.currentTimeMillis();
        String serviceMethod = invoker.getInterface().getSimpleName() + "#" + invocation.getMethodName();
        log.info("dubbo provider request serviceMethod={} traceparent={}",
                serviceMethod, traceParent);

        try {
            Result result = invoker.invoke(invocation);
            long latency = System.currentTimeMillis() - startTime;
            if (result.hasException()) {
                log.error("dubbo provider response serviceMethod={} latency={}ms exception={}",
                        serviceMethod, latency, result.getException().getMessage());
            } else {
                log.info("dubbo provider response serviceMethod={} latency={}ms",
                        serviceMethod, latency);
            }
            return result;
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            log.error("dubbo provider response serviceMethod={} latency={}ms throwable={}",
                    serviceMethod, latency, e.getMessage());
            throw e;
        } finally {
            TracingUtils.clearTraceContext();
        }
    }
}
