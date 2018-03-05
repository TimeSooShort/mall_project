package com.mall.service;

import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.pojo.Shipping;

import java.util.Map;

/**
 * Created by Administrator on 2018/3/4.
 */
public interface IShippingService {

    ServerResponse<Map> add(Integer userId, Shipping shipping);

    ServerResponse<String> del(Integer userId, Integer shippingId);

    ServerResponse<String> update(Shipping shipping);

    ServerResponse<Shipping> select(Integer shippingId, Integer userId);

    ServerResponse<PageInfo> list(Integer pageNum, Integer pageSize, Integer userId);

}
