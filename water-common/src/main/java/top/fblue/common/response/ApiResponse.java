package top.fblue.common.response;

import lombok.Data;
import top.fblue.common.enums.ApiCodeEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一API响应格式
 */
@Data
public class ApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     */
    private ApiCodeEnum code;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 私有构造函数
     */
    private ApiResponse() {
    }

    /**
     * 获取响应消息
     */
    public static <T> String getMessage(ApiResponse<T> response) {
        if (response == null) {
            return null;
        }
        return response.getMessage();
    }

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ApiCodeEnum.SUCCESS);
        response.setSuccess(true);
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ApiCodeEnum.SUCCESS);
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(ApiCodeEnum code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    /**
     * 失败响应（带数据）
     */
    public static <T> ApiResponse<T> error(ApiCodeEnum code, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 失败响应（带数据）
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ApiCodeEnum.BAD_REQUEST);
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
} 