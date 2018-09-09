package com.mall.controller.portol;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.ICartService;
import com.mall.vo.CartProductListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Controller：购物车实现类
 */
@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

    /**
     * 加入购物车
     * @param session 确保登录状态
     * @param productId 产品id
     * @param count 购买数量
     * @return 返回CartProductListVO
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> add(HttpSession session, Integer productId, Integer count){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.addProduct(user.getId(), productId, count);
    }

    /**
     * 购物车List列表
     * @param session 确认登录状态
     * @return CartProductListVO
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> list(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.getList(user.getId());
    }

    /**
     * 前端购物车页，用户可以点击增/减按钮来增加/减少购买商品的数量
     * 该页面生成时会渲染来自数据库最新的数据：库存stock，修正后的商品个数quantity，这些确保用户
     * 不会超量购买，但是stock数据仍然可能失效，要确保的是用户不会购买超过库存的商品。
     *
     * 这里点击增/减商品按键，逻辑是：前端调用update.do，用得到的数据重新渲染页面；
     * 在后端则是updateCartCount方法被调用，返回CartProductListVO对象，
     * 该对象的productStock代表此时的库存，quantity代表用户购买的商品数量，
     * 我们确保它是修正后的即<=productStock
     *
     * 举例来说明这样做的原因：用户进入购物车列表页，此时显示A商品用户购买数为8个，前端得到的库存数据
     * 为10，现在用户点击加号按键要增加一个，但是在操作过程中实际商品库存已经变为6，那么用户点击加号后
     * 页面显示的数据是？我这里的设计是，显示6，即当用户要购买的商品数大于库存时，就在后台进行修正
     * 不仅修正cart记录里的信息，还会返回给前端你能购买的最大数量
     * @param session 确认登录
     * @param productId 产品id
     * @param count 数量
     * @return CartProductListVO
     */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> updateCartCount(HttpSession session, Integer productId, Integer count){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.updateCount(user.getId(), productId, count);
    }

    /**
     * 前端删除单个产品cart，与删除选中商品都会调用该方法
     * @param session 确认登录
     * @param productIds 前端将要删除的产品id以逗号分隔转换成字符转传过来
     * @return CartProductListVO
     */
    @RequestMapping("delete_product.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> deleteCart(HttpSession session, String productIds){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.deleteProductInCart(user.getId(), productIds);
    }

    /**
     * 前端购物车页面一个商品的勾选会调用该接口。对应到后端的逻辑就是找到cart里的记录更新其
     * checked为1，update_time为now（）。checked: 1 :代表选中状态。 0：代表未选中状态
     * @param session 确认登录
     * @param productId 产品id
     * @return CartProductListVO
     */
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> select(HttpSession session, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), productId, Const.Cart.CHECKED);
    }

    /**
     * 前端购物车页面一个商品的取消勾选会调用该接口。对应到后端的逻辑就是找到cart里的记录更新其
     * checked为0，update_time为now（）。checked: 1 :代表选中状态。 0：代表未选中状态
     * @param session 确认登录
     * @param productId 产品id
     * @return CartProductListVO
     */
    @RequestMapping("un_select.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> unSelect(HttpSession session, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), productId, Const.Cart.UN_CHECKED);
    }

    /**
     * 前端购物车页面全选框勾选会调用该接口。对应到后端的逻辑就是找到cart里该用户的所有记录更新其
     * checked为1，update_time为now（）。checked: 1 :代表选中状态。 0：代表未选中状态
     * @param session 确认登录
     * @return CartProductListVO
     */
    @RequestMapping("select_all.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> selectAll(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), null, Const.Cart.CHECKED);
    }

    /**
     * 前端购物车页面全选框取消选中状态会调用该接口。对应到后端的逻辑就是找到cart里该用户的所有记录更新其
     * checked为0，update_time为now（）。checked: 1 :代表选中状态。 0：代表未选中状态
     * @param session 确认登录
     * @return CartProductListVO
     */
    @RequestMapping("un_select_all.do")
    @ResponseBody
    public ServerResponse<CartProductListVO> unSelectAll(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), null, Const.Cart.UN_CHECKED);
    }

    /**
     * 获取购物车中商品数量
     * @param session 确认登录
     * @return
     */
    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            // 这里未登录状态就返回0，代表未登录状态下前端导航条nav的购物车数量显示为0
            return ServerResponse.createBySuccess(0);
        }
        return iCartService.getCartProductCount(user.getId());
    }
}
