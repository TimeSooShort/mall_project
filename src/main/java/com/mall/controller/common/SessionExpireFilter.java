package com.mall.controller.common;

import com.mall.common.Const;
import com.mall.pojo.User;
import com.mall.util.CooKieUtil;
import com.mall.util.JsonUtil;
import com.mall.util.RedisPoolUtil;
import com.mall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class SessionExpireFilter implements Filter{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isNotEmpty(token)){
            String userJsonStr = RedisShardedPoolUtil.get(token);
            User user = JsonUtil.str2Obj(userJsonStr, User.class);
            if (user != null){
                RedisShardedPoolUtil.expire(token, Const.RedisCacheExTime.REDIS_SESSION_EXTIME);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
