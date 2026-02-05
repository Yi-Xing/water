package top.fblue.common.exception;

/**
 * RPC异常基类
 * 所有RPC相关异常的父类
 */
public class RpcException extends RuntimeException {

    public RpcException() {
        super();
    }


    public RpcException(String message) {
        super(message);
    }
}
