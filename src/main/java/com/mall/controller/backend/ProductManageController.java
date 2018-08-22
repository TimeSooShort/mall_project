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
import com.mall.util.PropertiesUtil;
import com.mall.vo.ProductDetailVO;
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
 * Controller：后台产品实现类
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

    /**
     * 新增OR更新产品
     * @param session 确保登录及权限
     * @param product 产品
     * @return
     */
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

    /**
     * 产品上下架
     * @param session 确保登录及权限
     * @param productId 产品id
     * @param status 产品状态：1-在售
     * @return 返回结果
     */
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

    /**
     * 产品详情
     * @param session 确保登录及权限
     * @param productId 产品id
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
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

    /**
     * 产品列表
     * @param session 确保登录及权限
     * @param pageNum 第几页
     * @param pageSize 一页包含几个
     * @return PageInfo
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> getProductList(HttpSession session,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                   @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            return iProductService.getProductList(pageNum, pageSize);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能执行此操作");
        }
    }

    /**
     * 产品搜索
     * @param session 确保登录及权限
     * @param productName 产品名
     * @param productId 产品id
     * @param pageNum 第几页
     * @param pageSize 一页包含几个
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> productSearch(HttpSession session, String productName, Integer productId,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                   @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能执行此操作");
        }
    }

    /**
     * 文件上传
     * @param session 确保登录及权限
     * @param file MultipartFile
     * @param request HttpServletRequest
     * @return 返回一个包含上传文件的url与uri的map
     */
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }
        if (iUserService.checkAdminRole(currentUser).isSuccess()){
            // 发布后upload文件夹会被创建到webapp下
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetName = iFileService.upload(file, path);
            // 组成文件的url返回给前端
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetName;

            Map<String, String> fileMap = Maps.newHashMap();
            fileMap.put("uri", targetName); // uri代表统一资源标识符
            fileMap.put("url", url); // url代表统一资源定位符
            return ServerResponse.createBySuccess(fileMap);
        }else {
            return ServerResponse.createByErrorMessage("只有管理员才能执行此操作");
        }
    }

    /**
     *  富文本图片上传
     *       富文本中对于返回值有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
     *       {
     *       "success": true/false,
     *       "msg": "error message", # optional
     *       "file_path": "[real file path]"
     *       }
     * @param session 确保登录及权限
     * @param file MultipartFile
     * @param request HttpServletRequest
     * @param response HttpServletResponse，用于成功后修改header
     * @return 返回一个map
     */
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session,@RequestParam(value = "upload_file",
            required = false) MultipartFile file, HttpServletRequest request,
                                 HttpServletResponse response){
        Map<String, Object> resultMap = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
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
            // 注意需要修改response的header
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        } else {
            resultMap.put("success", false);
            resultMap.put("msg", "无权限操作");
            return resultMap;
        }
    }
}
