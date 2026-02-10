package top.fblue.log.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import top.fblue.common.utils.StringUtil;
import top.fblue.log.constant.TraceConst;
import top.fblue.log.context.TraceContext;

/**
 * 全链路追踪工具类
 * 提供追踪相关的实用方法
 *
 * @author system
 */
@Slf4j
public class TracingUtils {

    /**
     * 从 traceparent 字符串提取追踪上下文并设置到 MDC
     *
     * @param traceParent traceparent 头或 attachment 值，可为 null
     */
    public static void extractAndSetTraceContext(String traceParent) {
        TraceContext traceContext = fromTraceParent(traceParent);
        traceContext.setToMDC();
    }

    /**
     * 根据当前 MDC 中的追踪上下文生成子 span 的 traceparent，用于调用方传递
     * 若 MDC 无追踪信息则生成新的根 span
     *
     * @return traceparent 字符串，格式 00-{traceId}-{spanId}-{flags}
     */
    public static String getTraceParentFor() {
        String traceId = MDC.get(TraceConst.TRACE_ID_KEY);
        String spanId = MDC.get(TraceConst.SPAN_ID_KEY);
        String sampled = MDC.get(TraceConst.SAMPLED_KEY);
        TraceContext child = new TraceContext(traceId, spanId, sampled);
        return child.toTraceParent();
    }

    /**
     * 从 traceparent header 解析追踪上下文，并生成新的 TraceContext
     * 格式: 00-<trace-id>-<span-id>-<trace-flags>
     *
     * @param traceParent traceParent header值
     * @return TraceContext实例
     */
    private static TraceContext fromTraceParent(String traceParent) {
        if (StringUtil.isEmpty(traceParent)) {
            // 没有则去生成
            return new TraceContext();
        }
        String[] parts = traceParent.split("-");
        if (parts.length >= 4) {
            String version = parts[0];
            String traceId = parts[1];
            String spanId = parts[2];
            String traceFlags = parts[3];

            // 验证版本号
            if (!TraceConst.TRACE_VERSION.equals(version)) {
                log.warn("Unsupported traceParent version: {}, using default", version);
                return new TraceContext();
            }

            return new TraceContext(traceId, spanId, traceFlags);
        }
        return new TraceContext();
    }


    /**
     * 清除当前线程的追踪上下文
     */
    public static void clearTraceContext() {
        TraceContext.clearFromMDC();
    }
}