package com.mall.controller.backend;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Category;
import com.mall.pojo.User;
import com.mall.service.ICategoryService;
import com.mall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by Administrator on 2018/2/24.
 */
@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            return iCategoryService.addCategory(categoryName, parentId);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能拥有此项权限");
        }
    }

    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能拥有此项权限");
        }
    }

    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse<List<Category>> getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            return iCategoryService.getChildrenParallelCategory(categoryId);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能拥有此项权限");
        }
    }

    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse<List<Integer>> getCategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能拥有此项权限");
        }
    }
}
