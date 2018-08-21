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
 * Created by Administrator on 2018/3/1.
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    public ServerResponse<CartProductListVO> getList(Integer userId){
        CartProductListVO cartProductListVO = this.getCartProductListVO(userId);
        return ServerResponse.createBySuccess(cartProductListVO);
    }

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

    public ServerResponse<CartProductListVO> updateCount(Integer userId,Integer productId, Integer count){
        if (productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart != null){
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        return this.getList(userId);
    }

    public ServerResponse<CartProductListVO> deleteProductInCart(Integer userId, String ids){
        List<String> productIdList = Splitter.on(",").splitToList(ids);
        if (CollectionUtils.isEmpty(productIdList)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteOneProductInCart(userId, productIdList);
        return this.getList(userId);
    }

    public ServerResponse<CartProductListVO> selectOrUnSelect(Integer userId, Integer productId, Integer checked){
        cartMapper.checkedOrUncheckedProduct(userId, productId, checked);
        return this.getList(userId);
    }

    public ServerResponse<Integer> getCartProductCount(Integer userId){
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }


    private CartProductListVO getCartProductListVO(Integer userId){
        List<CartProductVO> cartProductVOList = Lists.newArrayList();
        BigDecimal allTotalPrice = new BigDecimal("0");
        CartProductListVO cartProductListVO = new CartProductListVO();

        List<Cart> cartList = cartMapper.selectByUserId(userId);
        if (CollectionUtils.isNotEmpty(cartList)){
            for (Cart cartItem : cartList){
                CartProductVO cartProductVO = new CartProductVO();
                cartProductVO.setId(cartItem.getId());
                cartProductVO.setUserId(cartItem.getUserId());
                cartProductVO.setProductId(cartItem.getProductId());
                cartProductVO.setChecked(cartItem.getChecked());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null){
                    cartProductVO.setProductName(product.getName());
                    cartProductVO.setProductMainImage(product.getMainImage());
                    cartProductVO.setProductSubtitle(product.getSubtitle());
                    cartProductVO.setProductStatus(product.getStatus());
                    cartProductVO.setProductPrice(product.getPrice());
                    cartProductVO.setProductStock(product.getStock());

                    int realBuyCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()){
                        realBuyCount = cartItem.getQuantity();
                        cartProductVO.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS); //库存充足
                    } else {
                        realBuyCount = product.getStock();
                        cartProductVO.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);

                        Cart cart = new Cart();
                        cart.setId(cartItem.getId());
                        cart.setQuantity(product.getStock());
                        cartMapper.updateByPrimaryKeySelective(cart);
                    }
                    cartProductVO.setQuantity(realBuyCount);

                    cartProductVO.setProductTotalPrice(BigDecimalUtil.mul(realBuyCount, product.getPrice().doubleValue()));
                }
                if (cartItem.getChecked() == Const.Cart.CHECKED){
                    allTotalPrice = BigDecimalUtil.add(allTotalPrice.doubleValue(), cartProductVO.getProductTotalPrice().doubleValue());
                }
                cartProductVOList.add(cartProductVO);
            }
        }
        cartProductListVO.setCartProductVOList(cartProductVOList);
        cartProductListVO.setCartTotalPrice(allTotalPrice);
        cartProductListVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        cartProductListVO.setAllChecked(this.allCheckedStatus(userId));

        return cartProductListVO;
    }

    private boolean allCheckedStatus(Integer userId){
        if (userId == null) return false;
        return cartMapper.selectUnCheckedCartNumByUserId(userId) == 0;
    }
}
