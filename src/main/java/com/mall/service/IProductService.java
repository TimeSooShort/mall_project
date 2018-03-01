package com.mall.service;

import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.pojo.Product;
import com.mall.vo.ProductDetailVO;

/**
 * Created by Administrator on 2018/2/25.
 */
public interface IProductService {

    ServerResponse<String> saveOrUpdateProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVO> manageProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize);

    ServerResponse<PageInfo> searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize);

    ServerResponse<ProductDetailVO> getDetail(Integer productId);

    ServerResponse<PageInfo> getProductByKeywordAndCategoryId(String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy);
}
