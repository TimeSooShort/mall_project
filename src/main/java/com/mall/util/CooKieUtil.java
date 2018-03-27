package com.mall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CooKieUtil {

    private static final String COOKIE_DOMAIN = "mall.com"; //设置一级域名
    private static final String COOKIE_NAME = "mall_login_token";

    //domain与path：同级之间不能互访对方的cookie，子级共享父级的cookie
    //X:domain=".mall.com"  //一级域名
    //a:A.mall.com            cookie:domain=A.mall.com;path="/" 二级域名
    //b:B.mall.com            cookie:domain=B.mall.com;path="/"  二级域名
    //c:A.mall.com/test/cc    cookie:domain=A.mall.com;path="/test/cc"
    //d:A.mall.com/test/dd    cookie:domain=A.mall.com;path="/test/dd"
    //e:A.mall.com/test       cookie:domain=A.mall.com;path="/test"
    public static void writeLoginToken(HttpServletResponse response, String token){
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath("/");  //代表设置在根目录
        cookie.setHttpOnly(true);//不允许使用脚本来获取cookie，也不会发送给第三方
        //单位是秒；如果不设置setMaxAge，cookie就不会写入硬盘，而是在内存，只有当前页面有效
        cookie.setMaxAge(60 * 60 * 24 * 365);//-1 代表永久
        log.info("write cookieName:{},cookieValue:{}",cookie.getName(), cookie.getValue());
        response.addCookie(cookie);
    }

    public static String readLoginCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (cookies != null){
            for (Cookie cookie : cookies){
                log.info("read cookieName:{} cookieValue:{}", cookie.getName(), cookie.getValue());
                if (StringUtils.equals(cookie.getName(),COOKIE_NAME)){
                    log.info("read cookieName:{} cookieValue:{}", cookie.getName(), cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void delLoginCookie(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        if (cookies != null){
            for (Cookie cookie : cookies){
                if (StringUtils.equals(cookie.getName(), COOKIE_NAME)){
                    cookie.setMaxAge(0);//设置成0，代表删除此cookie
                    cookie.setPath("/");
                    cookie.setDomain(COOKIE_DOMAIN);
                    log.info("del CookieName:{} CookieValue:{}", cookie.getName(), cookie.getValue());
                    response.addCookie(cookie);
                    return;
                }
            }
        }
    }
}
