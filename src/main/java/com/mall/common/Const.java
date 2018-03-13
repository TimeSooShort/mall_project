package com.mall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by Administrator on 2018/1/1.
 */
public class Const {

    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    public interface Role{
        int ROLE_CUSTOMER = 0; //普通用户
        int ROLE_ADMIN = 1; //管理员
    }

    public enum ProductStatusEnum{
        ON_SALE(1, "在售");
        private String value;
        private int code;

        ProductStatusEnum(int code, String value) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_asc", "price_desc");
    }

    public interface Cart{
        int CHECKED = 1;  //选中状态
        int UN_CHECKED = 0;  //未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public enum OrderStateEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50, "订单完成"),
        ORDER_CLOSE(60, "订单关闭");

        private int code;
        private String desc;

        OrderStateEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode(){
            return this.code;
        }
        public String getValue(){
            return this.desc;
        }

        public static OrderStateEnum codeOf(int code){
            for (OrderStateEnum orderStateEnum : values()){
                if (orderStateEnum.getCode() == code){
                    return orderStateEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }

    public interface AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum PayPlatform{
        ALIPAY(1, "支付宝");

        private int code;
        private String desc;

        PayPlatform(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
