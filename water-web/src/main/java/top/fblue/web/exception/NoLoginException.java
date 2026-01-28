package top.fblue.web.exception;

/**
 * 未登录异常
 */
public class NoLoginException extends RuntimeException {

    public NoLoginException() {
        super();
    }


    public NoLoginException(String message) {
        super(message);
    }
}
