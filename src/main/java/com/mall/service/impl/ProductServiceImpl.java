package com.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.dao.CategoryMapper;
import com.mall.dao.ProductMapper;
import com.mall.pojo.Category;
import com.mall.pojo.Product;
import com.mall.service.ICategoryService;
import com.mall.service.IProductService;
import com.mall.util.DateTimeUtil;
import com.mall.util.PropertiesUtil;
import com.mall.vo.ProductDetailVO;
import com.mall.vo.ProductListVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by Administrator on 2018/2/25.
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    public ServerResponse<String> saveOrUpdateProduct(Product product){
        if (product == null){
            return ServerResponse.createByErrorMessage("新增或更新产品参数错误");
        }
        if (StringUtils.isNotBlank(product.getSubImages())){
            String[] subImageArray = product.getSubImages().split(",");
            if (subImageArray.length > 0){
                product.setMainImage(subImageArray[0]);
            }
        }
        if (product.getId() != null){
            int rowCount = productMapper.updateByPrimaryKey(product);
            if (rowCount > 0){
                return ServerResponse.createBySuccess("更新产品成功");
            }
            return ServerResponse.createByErrorMessage("更新产品失败");
        }else {
            int rowCount = productMapper.insert(product);
            if (rowCount > 0) return ServerResponse.createBySuccessMessage("添加产品成功");
            return ServerResponse.createByErrorMessage("添加产品失败");
        }
    }

    public ServerResponse<String> setSaleStatus(Integer productId, Integer status){
        if (productId == null || status == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0) return ServerResponse.createBySuccessMessage("更新产品状态成功");
        return ServerResponse.createByErrorMessage("更新产品状态失败");
    }

    public ServerResponse<ProductDetailVO> manageProductDetail(Integer productId){
        if (productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或删除");
        }
//        后台管理要得到的是产品信息，无关是否下架
//        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
//            return ServerResponse.createByErrorMessage("产品已下架或删除");
//        }
        ProductDetailVO productDetailVO = assembleProductDetailVO(product);
        return ServerResponse.createBySuccess(productDetailVO);
    }

    public ProductDetailVO assembleProductDetailVO(Product product){
        ProductDetailVO productDetailVO = new ProductDetailVO();
        productDetailVO.setId(product.getId());
        productDetailVO.setCategoryId(product.getCategoryId());
        productDetailVO.setDetail(productDetailVO.getDetail());
        productDetailVO.setMainImage(product.getMainImage());
        productDetailVO.setName(product.getName());
        productDetailVO.setPrice(product.getPrice());
        productDetailVO.setStatus(product.getStatus());
        productDetailVO.setStock(product.getStock());
        productDetailVO.setSubImages(product.getSubImages());
        productDetailVO.setSubtitle(product.getSubtitle());

        productDetailVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        Category category = categoryMapper.selectByPrimaryKey(productDetailVO.getCategoryId());
        if (category == null){
            productDetailVO.setParentCategoryId(0); //默认根节点
        }else {
            productDetailVO.setParentCategoryId(category.getParentId());
        }
        productDetailVO.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVO.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVO;
    }

    public ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize){
        //startPage--start
        //填充自己的sql查询逻辑
        //pageHelper-收尾
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productsList = productMapper.selectProductList();
        List<ProductListVO> list = Lists.newArrayList();
        for (Product productItem : productsList){
            list.add(assembleProductListVO(productItem));
        }
        PageInfo pageResult = new PageInfo(productsList);
        pageResult.setList(list);
        return ServerResponse.createBySuccess(pageResult);
    }

    private ProductListVO assembleProductListVO(Product product){
        ProductListVO productListVO = new ProductListVO();
        productListVO.setId(product.getId());
        productListVO.setCategoryId(product.getCategoryId());
        productListVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        productListVO.setMainImage(product.getMainImage());
        productListVO.setName(product.getName());
        productListVO.setprice(product.getPrice());
        productListVO.setStatus(product.getStatus());
        productListVO.setSubtitle(product.getSubtitle());
        return productListVO;
    }

    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectProductByNameAndId(productName, productId);
        List<ProductListVO> productListVOList = Lists.newArrayList();
        for (Product productItem : productList){
            productListVOList.add(assembleProductListVO(productItem));
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVOList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<ProductDetailVO> getDetail(Integer productId){
        if (productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return ServerResponse.createByErrorMessage("产品已下架或删除");
        }
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("产品已下架或删除");
        }
        ProductDetailVO productDetailVO = assembleProductDetailVO(product);
        return ServerResponse.createBySuccess(productDetailVO);
    }

    public ServerResponse<PageInfo> getProductByKeywordAndCategoryId(String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy){
        if (StringUtils.isBlank(keyword) && categoryId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)){
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVO> productListVO = Lists.newArrayList();
                PageInfo<ProductListVO> pageInfo = new PageInfo<>(productListVO);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(categoryId).getData();
        }
        if (StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append('%').toString();
        }

        PageHelper.startPage(pageNum,pageSize);
        if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
            String[] array = orderBy.split("_");
            PageHelper.orderBy(array[0]+" "+array[1]);
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)? null: keyword,
                categoryIdList.size() == 0? null:categoryIdList);
        List<ProductListVO> productListVO = Lists.newArrayList();
        for (Product productItem : productList){
            productListVO.add(assembleProductListVO(productItem));
        }
        PageInfo<ProductListVO> pageInfo = new PageInfo<>(productListVO);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
