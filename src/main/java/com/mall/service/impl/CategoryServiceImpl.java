package com.mall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mall.common.ServerResponse;
import com.mall.dao.CategoryMapper;
import com.mall.pojo.Category;
import com.mall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2018/2/24.
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(categoryName);
        category.setStatus(true);  //代表此分类可用

        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0) return ServerResponse.createBySuccessMessage("添加品类成功");
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    @Override
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setId(categoryId);
        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0) return ServerResponse.createBySuccessMessage("更新品类名字成功");
        return ServerResponse.createByErrorMessage("更新品类名字失败");
    }

    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer category) {
        List<Category> list = categoryMapper.selectCategoryChildrenByParentId(category);
        if (CollectionUtils.isEmpty(list)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(list);
    }

    @Override
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId) {
        Set<Category> set = Sets.newHashSet();
        findChildrenCategory(categoryId, set);
        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null){
            for (Category categoryItem : set){
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    private Set<Category> findChildrenCategory(Integer categoryId, Set<Category> set){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null){
            set.add(category);
        }
        List<Category> list = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem : list){
            findChildrenCategory(categoryItem.getId(), set);
        }
        return set;
    }
}
