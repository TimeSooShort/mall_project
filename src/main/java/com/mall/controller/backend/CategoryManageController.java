package com.mall.controller.backend;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Category;
import com.mall.pojo.User;
import com.mall.service.ICategoryService;
import com.mall.service.IUserService;
import com.mall.util.CooKieUtil;
import com.mall.util.JsonUtil;
import com.mall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by Administrator on 2018/2/24.
 */
@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {

//    @Autowired
//    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpServletRequest request, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId){
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);

//        String loginToken = CooKieUtil.readLoginCookie(request);
//        if (StringUtils.isEmpty(loginToken)){
//            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
//        }
//        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
//        User currentUser = JsonUtil.str2Obj(userJsonStr, User.class);
//
//        if (currentUser == null){
//            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
//        }
//        if (iUserService.checkAdminRole(currentUser).isSuccess()){
//            return iCategoryService.addCategory(categoryName, parentId);
//        }else {
//            return ServerResponse.createByErrorMessage("只有管理员才能拥有此项权限");
//        }
        return iCategoryService.addCategory(categoryName, parentId);
    }

    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpServletRequest request, Integer categoryId, String categoryName){
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);

//        String loginToken = CooKieUtil.readLoginCookie(request);
//        if (StringUtils.isEmpty(loginToken)){
//            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
//        }
//        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
//        User currentUser = JsonUtil.str2Obj(userJsonStr, User.class);
//
//        if (currentUser == null){
//            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
//        }
//        if (iUserService.checkAdminRole(currentUser).isSuccess()){
//            return iCategoryService.updateCategoryName(categoryId, categoryName);
//        }else {
//            return ServerResponse.createByErrorMessage("只有管理员才能拥有此项权限");
//        }

        return iCategoryService.updateCategoryName(categoryId, categoryName);
    }

    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse<List<Category>> getChildrenParallelCategory(HttpServletRequest request, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);

//        String loginToken = CooKieUtil.readLoginCookie(request);
//        if (StringUtils.isEmpty(loginToken)){
//            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
//        }
//        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
//        User currentUser = JsonUtil.str2Obj(userJsonStr, User.class);
//
//        if (currentUser == null){
//            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
//        }
//        if (iUserService.checkAdminRole(currentUser).isSuccess()){
//            return iCategoryService.getChildrenParallelCategory(categoryId);
//        }else {
//            return ServerResponse.createByErrorMessage("只有管理员才能拥有此项权限");
//        }
        return iCategoryService.getChildrenParallelCategory(categoryId);
    }

    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse<List<Integer>> getCategoryAndDeepChildrenCategory(HttpServletRequest request,
            @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);

//        String loginToken = CooKieUtil.readLoginCookie(request);
//        if (StringUtils.isEmpty(loginToken)){
//            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
//        }
//        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
//        User currentUser = JsonUtil.str2Obj(userJsonStr, User.class);
//
//        if (currentUser == null){
//            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
//        }
//        if (iUserService.checkAdminRole(currentUser).isSuccess()){
//            return iCategoryService.selectCategoryAndChildrenById(categoryId);
//        }else {
//            return ServerResponse.createByErrorMessage("只有管理员才能拥有此项权限");
//        }
        return iCategoryService.selectCategoryAndChildrenById(categoryId);
    }
}
