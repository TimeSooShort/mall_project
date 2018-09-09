package com.mall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.dao.CartMapper;
import com.mall.dao.ProductMapper;
import com.mall.pojo.Cart;
import com.mall.pojo.Product;
import com.mall.service.ICartService;
import com.mall.util.BigDecimalUtil;
import com.mall.util.PropertiesUtil;
import com.mall.vo.CartProductListVO;
import com.mall.vo.CartProductVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * 前端在增/减商品数量，产品勾选/弃选，产品全选/全不选，单个产品删除，一键删除已选中商品
 * 的功能实现逻辑是：从后台重新拿到全部所需信息，然后重新渲染页面，这样保证用户不会超量购买
 * 因此，抽象出getCartProductListVO方法，在该方法里组合cart与product信息，并对购买数量进行校验修正
 * 在上述这些功能对应方法里最后都是调用getCartProductListVO方法
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     *  购物车List列表。
     *  前端用户进入购物车查看页，后台要做的是：根据userId获取Cart列表，
     *  然后根据每个cart里的productId获取其Product，然后将Product部分信息连同Cart构成CartProductVO，
     *  这里会对原cart里quantity字段，即购买数量进行校验修正。这样就得到了CartProductVO列表，
     *  再将CartProductVO列表连同allChecked（是否全部选中），cartTotalPrice（购物车选中商品总价）
     *  构成CartProductListVO对象，返回给前端。
     *  之所以进行这样的处理，因为购物车页面前端需要cart表与product表的信息，所以将cart与product合成
     *  CartProductVO；得到的CartProductVO列表集合封装成CartProductListVO
     * @param userId 用户id
     * @return CartProductListVO
     */
    public ServerResponse<CartProductListVO> getList(Integer userId){
        CartProductListVO cartProductListVO = this.getCartProductListVO(userId);
        return ServerResponse.createBySuccess(cartProductListVO);
    }

    /**
     * 在前端的商品详情页，用户点击加入购物车按键触发add.do请求，
     * 而商品可能已存在在Cart表中，所以在代码中要进行区别，是insert或是update
     * 另一问题是商品的数量的校正，前端传来的商品购买数量数据可能超过该商品目前的库存
     * 尤其是商品库存紧张或是双11时。代码里统一在getCartProductListVO方法中对Cart的quantity进行校正
     * @param productId 产品id
     * @param count 前端传来的用户想要购买的产品数量
     * @return
     */
    public ServerResponse<CartProductListVO> addProduct(Integer userId, Integer productId, Integer count){
        if (userId == null || productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //添加 下架商品不能加到购物车
        int status = productMapper.selectByPrimaryKey(productId).getStatus();
        if (status != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("商品已下架或删除，不能添加到购物车");
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);

        if (cart == null){
            Cart cartNew = new Cart();
            cartNew.setQuantity(count);
            cartNew.setChecked(Const.Cart.CHECKED);
            cartNew.setUserId(userId);
            cartNew.setProductId(productId);
            cartMapper.insert(cartNew);
        } else {
            cart.setQuantity(cart.getQuantity()+count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.getList(userId);
    }

    /**
     * 更新cart数量，先根据用户id，产品id得到cart，在更新数据库里该cart的quantity数据
     * 最后调用getList，进行信息的组合及数量修正
     * @param userId 用户id
     * @param productId 产品id
     * @param count 前端传来的用户想要购买的产品数量
     * @return CartProductListVO
     */
    public ServerResponse<CartProductListVO> updateCount(Integer userId,Integer productId, Integer count){
        if (productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart != null){
            cart.setQuantity(count);
        }
        // 这一步是必须的，因为在getList里只有在数量超过库存时才会更新cart
        cartMapper.updateByPrimaryKeySelective(cart);
        return this.getList(userId);
    }

    /**
     * 前端删除单个产品cart，与删除选中商品都会调用该方法
     * @param userId 用户id
     * @param ids 前端将要删除的产品id以逗号分隔转换成字符转传过来
     * @return CartProductListVO
     */
    public ServerResponse<CartProductListVO> deleteProductInCart(Integer userId, String ids){
        // 将字符串ids解析成list
        List<String> productIdList = Splitter.on(",").splitToList(ids);
        if (CollectionUtils.isEmpty(productIdList)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteOneProductInCart(userId, productIdList);
        return this.getList(userId);
    }

    /**
     * 购物车中单个产品的选中与非选中，全部商品的选中与非选中，皆由该方法实现。
     * select.do : 跟新该产品cart记录checked为1，update_time为now（）
     * un_select.do : 跟新该产品cart记录checked为0，update_time为now（）
     * select_all.do : 跟新该用户所有产品cart记录checked为1，update_time为now（）
     * select.do : 跟新该用户所有产品cart记录checked为0，update_time为now（）
     * 由此sql语句为：
     * UPDATE mmall_cart SET checked=#{checked}, update_time=now() where user_id=#{userId}
     *      <if test="productId != null">
     *           and product_id=#{productId}
     *      </if>
     * @param userId 用户id
     * @param productId 产品id
     * @param checked 1：选中，0：未选中
     * @return CartProductListVO
     */
    public ServerResponse<CartProductListVO> selectOrUnSelect(Integer userId, Integer productId, Integer checked){
        cartMapper.checkedOrUncheckedProduct(userId, productId, checked);
        return this.getList(userId);
    }

    /**
     * 获取购物车中商品数量
     * SELECT IFNULL(sum(quantity), 0) as count from mmall_cart where user_id=#{userId}
     * @param userId 用户id
     * @return 数量
     */
    public ServerResponse<Integer> getCartProductCount(Integer userId){
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    /**
     *  前端在显示购物车时需要许多产品的信息，这些信息在product表中
     *  所以先根据userId获取Cart列表，在将每个cart封装成CartProductVO，
     *  CartProductVO比Cart新增了productName，productSubtitle，productMainImage
     *  productPrice（BigDecimal类型），productStatus，productTotalPrice（BigDecimal类型），productStock，limitQuantity
     *  在得到CartProductVO列表后，将其联合imageHost, allChecked, cartTotalPrice，组成CartProductListVO
     * @param userId 用户id
     * @return CartProductListVO
     */
    private CartProductListVO getCartProductListVO(Integer userId){
        List<CartProductVO> cartProductVOList = Lists.newArrayList();
        BigDecimal allTotalPrice = new BigDecimal("0");
        CartProductListVO cartProductListVO = new CartProductListVO();

        // 根据userId获得Cart列表
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        // 将每个Cart加上Product信息组成CartProductVO
        if (CollectionUtils.isNotEmpty(cartList)){
            for (Cart cartItem : cartList){
                CartProductVO cartProductVO = new CartProductVO();
                cartProductVO.setId(cartItem.getId());
                cartProductVO.setUserId(cartItem.getUserId());
                cartProductVO.setProductId(cartItem.getProductId());
                cartProductVO.setChecked(cartItem.getChecked());

                // 根据cart中的productId字段获取其对应的产品Product
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null){
                    // 封装product信息
                    cartProductVO.setProductName(product.getName());
                    cartProductVO.setProductMainImage(product.getMainImage());
                    cartProductVO.setProductSubtitle(product.getSubtitle());
                    cartProductVO.setProductStatus(product.getStatus());
                    cartProductVO.setProductPrice(product.getPrice());
                    cartProductVO.setProductStock(product.getStock());

                    // Cart中quantity代表用户之前购买商品时选择的数量，由于商品库存的变化
                    // 该值可能已经过期，所以这里需要校验，确保返回正确的数据
                    int realBuyCount;
                    if (product.getStock() >= cartItem.getQuantity()){
                        realBuyCount = cartItem.getQuantity();
                        // cartProductVO的limitQuantity字段表明库存情况：充足/不足
                        cartProductVO.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS); //库存充足
                    } else {
                        realBuyCount = product.getStock();
                        // 库存不足，小于该用户原定要购买的数量
                        cartProductVO.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);

                        // 修订cart表中数据
                        Cart cart = new Cart();
                        cart.setId(cartItem.getId());
                        cart.setQuantity(product.getStock());
                        cartMapper.updateByPrimaryKeySelective(cart);
                    }
                    cartProductVO.setQuantity(realBuyCount);

                    // 该商品用户实际购买数量所需花费的总钱数
                    cartProductVO.setProductTotalPrice(BigDecimalUtil.mul(realBuyCount, product.getPrice().doubleValue()));
                }
                if (cartItem.getChecked() == Const.Cart.CHECKED){
                    // 计算总价
                    allTotalPrice = BigDecimalUtil.add(allTotalPrice.doubleValue(), cartProductVO.getProductTotalPrice().doubleValue());
                }
                cartProductVOList.add(cartProductVO);
            }
        }
        // 组成cartProductListVO返回给前端
        cartProductListVO.setCartProductVOList(cartProductVOList);
        cartProductListVO.setCartTotalPrice(allTotalPrice);
        cartProductListVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.miaoshop.top/"));
        cartProductListVO.setAllChecked(this.allCheckedStatus(userId));

        return cartProductListVO;
    }

    /**
     * SELECT count(1) FROM mmall_cart WHERE checked = 0 AND user_id=#{userId}
     * 统计记录里未选中的数量。
     * 前端在勾选全部产品后全选框也会跟着处于被选中状态，功能实现逻辑就在这
     * @param userId 用户id
     * @return
     */
    private boolean allCheckedStatus(Integer userId){
        if (userId == null) return false;
        return cartMapper.selectUnCheckedCartNumByUserId(userId) == 0;
    }
}
