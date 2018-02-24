package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.pojo.Category;

import java.util.List;

/**
 * Created by Administrator on 2018/2/24.
 */
public interface ICategoryService {
    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse updateCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> getChildrenParallelCategory(Integer category);

    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);
}
