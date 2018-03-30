package com.mall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Product;
import com.mall.pojo.User;
import com.mall.service.IFileService;
import com.mall.service.IProductService;
import com.mall.service.IUserService;
import com.mall.util.CooKieUtil;
import com.mall.util.JsonUtil;
import com.mall.util.PropertiesUtil;
import com.mall.util.RedisShardedPoolUtil;
import com.mall.vo.ProductDetailVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

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

    @Autowired
    private IFileService iFileService;

    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse<String> productSaveOrUpdate(HttpServletRequest request, Product product){
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);

        String loginToken = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.str2Obj(userJsonStr, User.class);

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
    public ServerResponse<String> setSaleStatus(HttpServletRequest request, Integer productId, Integer status){
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);

        String loginToken = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.str2Obj(userJsonStr, User.class);

        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            return  iProductService.setSaleStatus(productId, status);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能执行此操作");
        }
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVO> productDetail(HttpServletRequest request, Integer productId){
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);

        String loginToken = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.str2Obj(userJsonStr, User.class);

        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            return iProductService.manageProductDetail(productId);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能执行此操作");
        }
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> getProductList(HttpServletRequest request,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                   @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);

        String loginToken = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.str2Obj(userJsonStr, User.class);

        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            return iProductService.getProductList(pageNum, pageSize);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能执行此操作");
        }
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> productSearch(HttpServletRequest request, String productName, Integer productId,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                   @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);

        String loginToken = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.str2Obj(userJsonStr, User.class);

        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能执行此操作");
        }
    }

    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request){
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);

        String loginToken = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.str2Obj(userJsonStr, User.class);

        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetName = iFileService.upload(file, path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetName;

            Map<String, String> fileMap = Maps.newHashMap();
            fileMap.put("uri", targetName);
            fileMap.put("url", url);
            return ServerResponse.createBySuccess(fileMap);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能执行此操作");
        }
    }

    /**
     *       富文本中对于返回值有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
     *       {
     *       "success": true/false,
     *       "msg": "error message", # optional
     *       "file_path": "[real file path]"
     *       }
     *
     * @param file
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map<String, Object> resultMap = Maps.newHashMap();
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String loginToken = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(loginToken)){
            resultMap.put("success", false);
            resultMap.put("msg", "请以管理员身份登录");
            return resultMap;
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            resultMap.put("success", false);
            resultMap.put("msg", "请以管理员身份登录");
            return resultMap;
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            if (targetFileName == null){
                resultMap.put("success", false);
                resultMap.put("msg", "上传失败");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success", true);
            resultMap.put("msg", "上传成功");
            resultMap.put("file_path", url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        } else {
            resultMap.put("success", false);
            resultMap.put("msg", "无权限操作");
            return resultMap;
        }
    }
}
