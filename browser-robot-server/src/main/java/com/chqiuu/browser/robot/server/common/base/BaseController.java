package com.chqiuu.browser.robot.server.common.base;

import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * Controller基类
 *
 * @author chqiu
 */
@Slf4j
public class BaseController {

    /**
     * 获取当前用户请求对象
     *
     * @return 当前用户请求对象
     */
    public HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (null == requestAttributes) {
            return null;
        }
        return requestAttributes.getRequest();
    }

    /**
     * 获取客户端IP地址
     *
     * @return 客户端IP地址
     */
    public String getClientIp() {
        return JakartaServletUtil.getClientIP(getRequest(), "");
    }

    /**
     * 获取当前用户响应对象
     *
     * @return 当前用户响应对象
     */
    public HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (null == requestAttributes) {
            return null;
        }
        return requestAttributes.getResponse();
    }

    /**
     * 获取当前用户Session
     *
     * @return 当前用户Session
     */
    public HttpSession getSession() {
        HttpServletRequest request = getRequest();
        if (null == request) {
            return null;
        }
        return request.getSession();
    }
}
