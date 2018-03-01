package com.mall.controller.portol;

import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.service.IProductService;
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

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(Integer productId){
        return iProductService.getDetail(productId);
    }

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
