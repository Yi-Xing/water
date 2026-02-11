package top.fblue.dubbo.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import top.fblue.log.constant.TraceConst;
import top.fblue.log.utils.TracingUtils;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

/**
 * Dubbo 全链路追踪 Filter（服务使用者/消费者侧）
 * 设置 traceparent 到 attachment，打印入参和出参
 */
@Slf4j
@Activate(group = CONSUMER, order = Integer.MIN_VALUE)
public class DubboTraceConsumerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
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
}
