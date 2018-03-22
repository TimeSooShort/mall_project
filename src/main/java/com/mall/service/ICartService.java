package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.vo.CartProductListVO;

/**
 * Created by Administrator on 2018/3/1.
 */
public interface ICartService {

    ServerResponse<CartProductListVO> addProduct(Integer userId, Integer productId, Integer count);

    ServerResponse<CartProductListVO> getList(Integer userId);

    ServerResponse<CartProductListVO> updateCount(Integer userId, Integer productId, Integer count);

    ServerResponse<CartProductListVO> deleteProductInCart(Integer userId, String ids);

    ServerResponse<CartProductListVO> selectOrUnSelect(Integer userId, Integer productId, Integer checked);

    ServerResponse<Integer> getCartProductCount(Integer userId);
}
