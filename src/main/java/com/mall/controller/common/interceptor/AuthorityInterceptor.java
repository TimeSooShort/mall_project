package com.mall.controller.common.interceptor;

import com.google.common.collect.Maps;
import com.mall.common.Const;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.util.CooKieUtil;
import com.mall.util.JsonUtil;
import com.mall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {
    /**
     * 这里的逻辑是：取出请求的类名，方法名，参数，打印到日志
     * login登陆处理
     * 判断权限，是否登陆，是否是管理员，进行验证拦截；
     * 注意ProductManageController.richtextImgUpload方法，要求的返回数据格式与其他不同
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        log.info("preHandle");
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String methodName = handlerMethod.getMethod().getName(); //方法名
        String className = handlerMethod.getBean().getClass().getName();//类名

        StringBuilder paramsBuilder = new StringBuilder();
        Map paramMap = request.getParameterMap();
        Iterator iterator = paramMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
            String mapKey = (String) entry.getKey();
            String mapValue = StringUtils.EMPTY;

            //返回的是一个String[]
            Object obj = entry.getValue();
            if (obj instanceof String[]){
                mapValue = Arrays.toString((String[]) obj);
            }
            paramsBuilder.append(mapKey).append("=").append(mapValue);
        }
        if (StringUtils.equals("UserManageController", className) &&
                StringUtils.equals("login", methodName)){
            //这里不能将登陆信息打印到日志上
            log.info("权限拦截器拦截到请求，className：{}，methodName：{}",className, methodName);
            return true;
        }
        log.info("权限拦截器拦截到请求，className：{}，methodName：{}, param:{}",className,
                methodName, paramsBuilder.toString());

        User user = null;
        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isNotEmpty(token)){
            String userJsonStr = RedisShardedPoolUtil.get(token);
            user = JsonUtil.str2Obj(userJsonStr, User.class);
        }

        if (user == null || user.getRole() != Const.Role.ROLE_ADMIN){
            //这里要添加reset，否则报异常 getWriter() has already been called for this response.
            response.reset();
            //乱码
            response.setCharacterEncoding("UTF-8");
            //这里要设置返回值的类型，因为全部是json接口
            response.setContentType("application/json;charset=UTF-8");

            PrintWriter out = response.getWriter();

            if (user == null){
                //上传由于富文本的控件要求，要特殊处理返回值，这里面区分是否登录以及是否有权限
                if (StringUtils.equals("ProductManageController", className) &&
                        StringUtils.equals("richtextImgUpload", methodName)){
                    Map<String, Object> resultMap = Maps.newHashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "请登陆管理员");
                    out.print(JsonUtil.obj2String(resultMap));
                } else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage(
                            "拦截器拦截，用户未登陆")));
                }
            } else {
                if (StringUtils.equals("ProductManageController", className) &&
                        StringUtils.equals("richtextImgUpload", methodName)){
                    Map<String, Object> resultMap = Maps.newHashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "无权限操作");
                    out.print(JsonUtil.obj2String(resultMap));
                }else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage(
                            "拦截器拦截，无权限操作")));
                }
            }
            out.flush();//强制内存中数据存储到存储器里
            out.close();//记得要关闭
            return false;//返回false.即不会调用controller里的方法
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        log.info("afterCompletion");
    }
}
