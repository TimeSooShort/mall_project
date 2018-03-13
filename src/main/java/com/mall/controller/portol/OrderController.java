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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/7.
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
}
