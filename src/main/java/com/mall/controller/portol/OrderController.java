package com.mall.controller.portol;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Order;
import com.mall.pojo.User;
import com.mall.service.IOrderService;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * 订单：分为支付与订单两个模块
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNum, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(orderNum, user.getId(), path);
    }

    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallBack(HttpServletRequest request){
        Map<String, String> params = Maps.newHashMap();

        Map<String, String[]> requestMap = request.getParameterMap();
        Iterator iterator = requestMap.keySet().iterator();
        while (iterator.hasNext()){
            String key = (String) iterator.next();
            String[] values = requestMap.get(key);
            String valueString = "";
            for (int i = 0; i < values.length; i++){
                valueString = (i == values.length-1) ? valueString+values[i] : valueString+values[i]+",";
            }
            params.put(key, valueString);
        }

        logger.info("支付宝回调，sign:{},trade_status:{},参数:{}", params.get("sign"), params.get("trade_status"), params.toString());

        //AlipaySignature.rsaCheckV2源码里将会remove“sign”
        params.remove("sign_type");

        try {
            //这里所调用的是四个参数的rsaCheckV2，因为我们采用的是SHA256WithRSA
            boolean checkResult = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),
                    "utf-8", Configs.getSignType());
            if (!checkResult){
                return ServerResponse.createByErrorMessage("非法请求，验证不通过");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝验证异常", e);
        }

        //out_trade_no,total_amount,seller_id
        ServerResponse response = iOrderService.aliCallback(params);
        if (response.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    //前端需要获取订单支付状态，一边决定是否跳转到相应页面
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse response = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if (response.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }




    //订单模块

    /**
     * 生成订单。对应前端订单确认页的提交订单的按钮点击。
     * @param session 判断登录
     * @param shippingId 收货地址id
     * @return OrderVO对象
     */
    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.createOrder(user.getId(), shippingId);
    }

    /**
     * 取消订单。对应前端订单详情页。
     * 只有未支付状态的订单才能取消
     * @param session 判断登录
     * @param orderNo 订单号
     * @return 返回取消结果
     */
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancelOrder(user.getId(), orderNo);
    }

    /**
     * 对应前端“我的购物车”页面里去结算按钮的点击，会跳到“订单确认”页面，在页面加载商品过程中调用该方法，获取商品信息。
     * 获取订单的商品信息，订单模块有两张表，order表与order_item表，对应Order类与OrderItem类
     * 其中Order信息多与商品相关，如商品名字，图片，购买数量，单价...;
     * Order信息是订单的信息，如订单状态，收货地址，支付方式，支付时间，交易完成时间等....
     * 这里要的就是OrderItem对象。
     * @param session 判断登录
     * @return
     */
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }

    /**
     * 订单详情。对应前端的“订单详情”页。
     * 返回前端OrderVO，所有信息都在里面。
     * @param session 判断登录
     * @param orderNo 订单号
     * @return OrderVO
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getOrderDetail(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(), orderNo);
    }

    /**
     * 订单列表。对应前端的”订单列表“页
     * 返回给前端OrderVO列表，OrderVO对象包含所有需要信息。
     * @param session 判断登录
     * @param pageSie 一页多大
     * @param pageNum 第几页
     * @return OrderVO列表
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getOrderList(HttpSession session,
                                       @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSie,
                                       @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderList(user.getId(), pageSie, pageNum);
    }
}
