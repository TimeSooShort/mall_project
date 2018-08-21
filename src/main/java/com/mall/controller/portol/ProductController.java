package com.mall.controller.portol;

import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.service.IProductService;
import com.mall.vo.ProductDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Administrator on 2018/3/1.
 */
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;

    /**
     * 获取产品详情，对应前端的商品详情页
     * @param productId 产品id
     * @return 返回封装后的Product对象ProductDetailVO
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVO> getDetail(Integer productId){
        return iProductService.getDetail(productId);
    }

    /**
     * 通过关键字或品类id来获取相关商品列表
     * @param keyword 关键字
     * @param categoryId  商品信息
     * @param pageNum 第几页
     * @param pageSize 一页显示几个商品
     * @param orderBy 商品的排列顺寻
     * @return 返回PageInfo对象
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> getProductList(@RequestParam(value = "keyword", required = false) String keyword,
                                                   @RequestParam(value = "categoryId", required = false) Integer categoryId,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                   @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                                   @RequestParam(value = "orderBy", defaultValue = "") String orderBy){
        return iProductService.getProductByKeywordAndCategoryId(keyword, categoryId, pageNum, pageSize, orderBy);
    }
}
