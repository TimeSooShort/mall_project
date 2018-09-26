package com.mall.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Integer id;

    private Long orderNo;

    private Integer userId;

    private Integer shippingId;

    private BigDecimal payment; // 订单所要支付的金额

    private Integer paymentType; // 支付类型：1代表在线支付

    private Integer postage; // 运费

    private Integer status; // 订单状态：0取消，10未支付，20已支付，40已发货，50订单完成，60订单关闭

    private Date paymentTime; //支付时间

    private Date sendTime; // 发货时间

    private Date endTime; // 交易完成时间

    private Date closeTime; //交易关闭时间

    private Date createTime;

    private Date updateTime;
}