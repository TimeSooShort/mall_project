package com.mall.controller.portol;

import com.mall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import com.github.pagehelper.PageInfo;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Shipping;
import com.mall.pojo.User;
import com.mall.service.IShippingService;
import com.mall.util.CooKieUtil;
import com.mall.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/4.
 */
@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;

    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse<Map> addAdress(HttpServletRequest request, Shipping shipping){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.add(user.getId(), shipping);
    }

    @RequestMapping("del.do")
    @ResponseBody
    public ServerResponse<String> delAdress(HttpServletRequest request, Integer shippingId){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.del(user.getId(), shippingId);
    }

    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse<String> updateAdress(HttpServletRequest request, Shipping shipping){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        shipping.setUserId(user.getId());
        return iShippingService.update(shipping);
    }

    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<Shipping> selectAdress(HttpServletRequest request, Integer shippingId){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.select(shippingId, user.getId());
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> listAdress(HttpServletRequest request,
                                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                               @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.list(pageNum,pageSize,user.getId());
    }
}
