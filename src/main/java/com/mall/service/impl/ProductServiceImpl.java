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
 * 产品服务实现类
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 新增OR更新产品
     * @param product 产品
     * @return 返回结果
     */
    public ServerResponse<String> saveOrUpdateProduct(Product product){
        if (product == null){
            return ServerResponse.createByErrorMessage("新增或更新产品参数错误");
        }
        // 将subImages中的第一个图作为主图展示
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

    /**
     * 产品上下架
     * @param productId 产品id
     * @param status 产品状态：1-在售
     * @return 返回结果
     */
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

    /**
     * 产品详情
     * @param productId 产品id
     * @return ProductDetailVO
     */
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

    /**
     * 封装原Product，增加了两个字段：imageHost，parentCategoryId
     * 添加imageHost是因为前端图片的地址src= imageHost + mainImage， imageHost是图片服务器域名
     * mainImage是图片名。
     * @param product Product
     * @return ProductDetailVO
     */
    private ProductDetailVO assembleProductDetailVO(Product product){
        ProductDetailVO productDetailVO = new ProductDetailVO();
        productDetailVO.setId(product.getId());
        productDetailVO.setCategoryId(product.getCategoryId());
        productDetailVO.setDetail(product.getDetail());
        productDetailVO.setMainImage(product.getMainImage());
        productDetailVO.setName(product.getName());
        productDetailVO.setPrice(product.getPrice());
        productDetailVO.setStatus(product.getStatus());
        productDetailVO.setStock(product.getStock());
        productDetailVO.setSubImages(product.getSubImages());
        productDetailVO.setSubtitle(product.getSubtitle());

        productDetailVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.miaoshop.top/"));
        // 先通过该产品的categoryId获得其Category对象，再获取其parentId的值
        Category category = categoryMapper.selectByPrimaryKey(productDetailVO.getCategoryId());
        if (category == null){
            productDetailVO.setParentCategoryId(0); //说明该商品不属于现存的任一品类范围
        }else {
            productDetailVO.setParentCategoryId(category.getParentId());
        }
        // 对时间进行处理转化
        productDetailVO.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVO.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVO;
    }

    /**
     * 产品列表
     * @param pageNum 第几页
     * @param pageSize 一页包含几个
     * @return PageInfo
     */
    public ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize){
        //startPage--start
        //填充自己的sql查询逻辑
        //pageHelper-收尾
        PageHelper.startPage(pageNum, pageSize);
        // 查询结果按id升序排列
        List<Product> productsList = productMapper.selectProductList();
        List<ProductListVO> list = Lists.newArrayList();
        // 重新封装成ProductListVO
        for (Product productItem : productsList){
            list.add(assembleProductListVO(productItem));
        }
        PageInfo pageResult = new PageInfo(productsList);
        pageResult.setList(list);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 给原product加一个字段imageHost，图片的地址src= imageHost + mainImage
     * @param product 原商品信息
     * @return ProductListVO
     */
    private ProductListVO assembleProductListVO(Product product){
        ProductListVO productListVO = new ProductListVO();
        productListVO.setId(product.getId());
        productListVO.setCategoryId(product.getCategoryId());
        productListVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.miaoshop.top/"));
        productListVO.setMainImage(product.getMainImage());
        productListVO.setName(product.getName());
        productListVO.setprice(product.getPrice());
        productListVO.setStatus(product.getStatus());
        productListVO.setSubtitle(product.getSubtitle());
        return productListVO;
    }

    /**
     * 产品搜索
     * @param productName 产品名
     * @param productId 产品id
     * @param pageNum 第几页
     * @param pageSize 一页包含几个
     * @return
     */
    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)){
            productName = "%" + productName + "%";
        }
        // 这里在写sql时要考虑到productName或productId为null的情况
        List<Product> productList = productMapper.selectProductByNameAndId(productName, productId);
        List<ProductListVO> productListVOList = Lists.newArrayList();
        for (Product productItem : productList){
            productListVOList.add(assembleProductListVO(productItem));
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVOList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 获取产品详情，对应前端的商品详情页
     * @param productId 产品id
     * @return 返回封装后的Product对象ProductDetailVO
     */
    public ServerResponse<ProductDetailVO> getDetail(Integer productId){
        if (productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return ServerResponse.createByErrorMessage("产品已删除");
        }
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("产品已下架");
        }
        ProductDetailVO productDetailVO = assembleProductDetailVO(product);
        return ServerResponse.createBySuccess(productDetailVO);
    }


    /**
     * 通过关键字或品类id来获取相关商品列表
     * @param keyword 关键字
     * @param categoryId  商品信息
     * @param pageNum 第几页
     * @param pageSize 一页显示几个商品
     * @param orderBy 商品的排列顺寻
     * @return 返回PageInfo对象
     */
    public ServerResponse<PageInfo> getProductByKeywordAndCategoryId(String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy){
        // keyword，categoryId不能同时为null
        if (StringUtils.isBlank(keyword) && categoryId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // 用来存储该categoryId以及其子孙产品的categoryId的集合
        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null){
            // 由品类id来查询该品类
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            // 若该品类不存在，且keyword为null，则返回一个包含空list的PageInfo
            if (category == null && StringUtils.isBlank(keyword)){
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVO> productListVO = Lists.newArrayList();
                PageInfo<ProductListVO> pageInfo = new PageInfo<>(productListVO);
                return ServerResponse.createBySuccess(pageInfo);
            }
            // 调用CategoryService里的selectCategoryAndChildrenById方法，
            // 通过递归将给categoryId及其子孙categoryId全部加入list
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(categoryId).getData();
        }
        // 这里没有采用else if是因为后端可以同时接受categoryId和keyword，并根据二者来查询
        // 不过前端的处理是只传来二者中的一个，前端页面搜索查找传过来的是keyword，
        // 首页的商品类别点击传来的是categoryId
        if (StringUtils.isNotBlank(keyword)){
            keyword = "%" + keyword + '%';
        }

        PageHelper.startPage(pageNum,pageSize);
        // 前端会传过来三个可能值：default，price_desc，price_asc，对default选择忽略它，即默认顺序
        if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
            // 这里告诉PageHelper按照字段price的desc/asc降升序来排列
            String[] array = orderBy.split("_");
            PageHelper.orderBy(array[0]+" "+array[1]);
        }
        // 获取产品列表
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)? null: keyword,
                categoryIdList.size() == 0? null:categoryIdList);
        List<ProductListVO> productListVO = Lists.newArrayList();
        // 将每个product封装成ProductListVo，其实只是增加了一个imageHost字段
        for (Product productItem : productList){
            productListVO.add(assembleProductListVO(productItem));
        }
        PageInfo pageInfo = new PageInfo<>(productList);
        pageInfo.setList(productListVO);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
