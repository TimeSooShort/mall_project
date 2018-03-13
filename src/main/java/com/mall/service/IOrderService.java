package com.mall.service;

import com.mall.common.ServerResponse;

import java.util.Map;

/**
 * Created by Administrator on 2018/3/8.
 */
public interface IOrderService {

    ServerResponse pay(Long orderNum, Integer userId, String path);

    ServerResponse aliCallback(Map<String, String> params);

    ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);
}
