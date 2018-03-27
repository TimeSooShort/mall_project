package com.mall.controller.portol;

import org.apache.commons.lang3.StringUtils;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.ICartService;
import com.mall.util.CooKieUtil;
import com.mall.util.JsonUtil;
import com.mall.util.RedisPoolUtil;
import com.mall.vo.CartProductListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by Administrator on 2018/3/1.
 */
@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> add(HttpServletRequest request, Integer productId, Integer count){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.addProduct(user.getId(), productId, count);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> list(HttpServletRequest request){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.getList(user.getId());
    }

    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> updateCartCount(HttpServletRequest request, Integer productId, Integer count){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.updateCount(user.getId(), productId, count);
    }

    @RequestMapping("delete_product.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> deleteCart(HttpServletRequest request, String productIds){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.deleteProductInCart(user.getId(), productIds);
    }

    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> select(HttpServletRequest request, Integer productId){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), productId, Const.Cart.CHECKED);
    }

    @RequestMapping("un_select.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> unSelect(HttpServletRequest request, Integer productId){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), productId, Const.Cart.UN_CHECKED);
    }

    @RequestMapping("select_all.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> selectAll(HttpServletRequest request){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), null, Const.Cart.CHECKED);
    }

    @RequestMapping("un_select_all.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> unSelectAll(HttpServletRequest request){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), null, Const.Cart.UN_CHECKED);
    }

    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpServletRequest request){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String token = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(token);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.getCartProductCount(user.getId());
    }
}
