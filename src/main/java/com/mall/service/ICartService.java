package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.vo.CartProductListVO;

/**
 * Created by Administrator on 2018/3/1.
 */
public interface ICartService {

    ServerResponse<CartProductListVO> addProduct(Integer userId, Integer productId, Integer count);

    ServerResponse<CartProductListVO> getList(Integer userId);
}
