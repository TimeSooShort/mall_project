package com.mall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.IOrderService;
import com.mall.service.IUserService;
import com.mall.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 后台管理系统
 */
@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private IUserService iUserService;

    /**
     * 订单列表
     * @param session 判断登录
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderList(HttpSession session,
                                              @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                              @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("你无权这样做");
        }
    }

    /**
     * 按订单号查询
     * @param session 判断登录
     * @param orderNo 订单号
     * @param pageNum 第几页
     * @param pageSize 一页包含数量
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> searchList(HttpSession session,
                                              Long orderNo,
                                              @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                              @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageSearch(pageNum, pageSize, orderNo);
        } else {
            return ServerResponse.createByErrorMessage("你无权这样做");
        }
    }

    /**
     * 订单详情
     * @param session 判断登录
     * @param orderNo 订单号
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVO> getDetail(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageDetail(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("你无权这样做");
        }
    }

    /**
     * 订单发货
     * @param session 判断登录
     * @param orderNo 订单号
     * @return
     */
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse sendsGoods(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageSendGoods(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("你无权这样做");
        }
    }
}
