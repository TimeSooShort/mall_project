package com.mall.controller.backend;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Product;
import com.mall.pojo.User;
import com.mall.service.IProductService;
import com.mall.service.IUserService;
import com.mall.vo.ProductDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by Administrator on 2018/2/25.
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IUserService iUserService;

    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse<String> productSaveOrUpdate(HttpSession session, Product product){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            return  iProductService.saveOrUpdateProduct(product);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能执行此操作");
        }
    }

    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse<String> setSaleStatus(HttpSession session, Integer productId, Integer status){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            return  iProductService.setSaleStatus(productId, status);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能执行此操作");
        }
    }

    public ServerResponse<ProductDetailVO> productDetail(HttpSession session, Integer productId){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            return iProductService.manageProductDetail(productId);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能执行此操作");
        }
    }
}
