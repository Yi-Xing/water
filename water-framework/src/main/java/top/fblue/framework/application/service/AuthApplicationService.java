package top.fblue.framework.application.service;


import top.fblue.framework.common.dto.UserTokenDTO;

/**
 * 登录应用服务接口
 */
public interface AuthApplicationService {

    /**
     * 退出登录
     */
    void logout(String authHeader);

    /**
     * 刷新token
     */
    String refreshToken(String authHeader);

    /**
     * 验证token有效性并获取 UserToken
     */
    UserTokenDTO validateToken(String token);

    /**
     * 判断当前登录用户是否有指定接口资源的code权限
     */
    boolean hasPermission(String resourceCode);
} 