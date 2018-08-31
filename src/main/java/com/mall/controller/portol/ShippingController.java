package com.mall.controller.portol;

import com.github.pagehelper.PageInfo;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Shipping;
import com.mall.pojo.User;
import com.mall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Controller：收货地址实现类
 */
@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;

    /**
     * 添加收货地址
     * @param session 确认登录状态
     * @param shipping 信息
     * @return 这里返回的是个map：{shippingId ： xx}
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse<Map> addAddress(HttpSession session, Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.add(user.getId(), shipping);
    }

    /**
     * 收货地址删除
     * @param session 确认登录状态
     * @param shippingId 收货地址id
     * @return
     */
    @RequestMapping("del.do")
    @ResponseBody
    public ServerResponse<String> delAddress(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.del(user.getId(), shippingId);
    }

    /**
     * 跟新收货地址
     * @param session 确认登录状态
     * @param shipping 信息
     * @return
     */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse<String> updateAddress(HttpSession session, Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        shipping.setUserId(user.getId());
        return iShippingService.update(shipping);
    }

    /**
     * 选中查看具体的地址信息，即前端编辑按钮的点击
     * @param session 确认登录状态
     * @param shippingId 收货地址id
     * @return
     */
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<Shipping> selectAddress(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.select(shippingId, user.getId());
    }

    /**
     * 地址的编辑，删除，添加完车后，前端都会调用该接口，重新加载订单确认页的收货地址显示
     * 前端有一个功能，是在对其它地址进行操作后仍能维持之前所选地址框的选中状态（呈红色），这就
     * 需要得到所有收货地址id然后遍历与之前被存储的地址框id进行比较。
     *
     * @param session 确认登录状态
     * @param pageNum 第几页
     * @param pageSize 一页几个
     * @return PageInfo
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> listAddress(HttpSession session,
                                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                               @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.list(pageNum,pageSize,user.getId());
    }
}
