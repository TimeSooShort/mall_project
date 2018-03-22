package com.mall.service;

import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.vo.OrderVO;

import java.util.Map;

/**
 * Created by Administrator on 2018/3/8.
 */
public interface IOrderService {

    ServerResponse pay(Long orderNum, Integer userId, String path);

    ServerResponse aliCallback(Map<String, String> params);

    ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);

    ServerResponse createOrder(Integer userId, Integer shippingId);

    ServerResponse cancelOrder(Integer userId, Long orderNo);

    ServerResponse getOrderCartProduct(Integer userId);

    ServerResponse getOrderDetail(Integer userId, Long orderNo);

    ServerResponse getOrderList(Integer userId, Integer pageSize, Integer pageNum);

    //后台
    ServerResponse<PageInfo> manageList(Integer pageNum, Integer pageSize);

    ServerResponse<PageInfo> manageSearch(Integer pageNum, Integer pageSize, Long orderNo);

    ServerResponse<OrderVO> manageDetail(Long orderNo);
}
