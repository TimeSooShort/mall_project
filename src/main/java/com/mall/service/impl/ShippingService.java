package com.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mall.common.ServerResponse;
import com.mall.dao.ShippingMapper;
import com.mall.pojo.Shipping;
import com.mall.service.IShippingService;
import com.mall.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/4.
 */
@Service("iShippingService")
public class ShippingService implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse<Map> add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if (rowCount > 0){
            Map<String, Integer> result = Maps.newHashMap();
            result.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功", result);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    public ServerResponse<String> del(Integer userId, Integer shippingId){
        //采用双验证为了防止横向越权，如某一登录用户删除其他用户的地址信息
        int rowCount = shippingMapper.deleteByUserIdShippingId(userId, shippingId);
        if (rowCount > 0){
            return ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    public ServerResponse<String> update(Shipping shipping){
        Shipping shipping1 = this.select(shipping.getId(), shipping.getUserId()).getData();
        shipping.setCreateTime(shipping1.getCreateTime());
        int rowCount = shippingMapper.updateByUserIdShippingId(shipping);
        if (rowCount > 0){
            return ServerResponse.createBySuccessMessage("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    public ServerResponse<Shipping> select(Integer shippingId, Integer userId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(shippingId, userId);
        if (shipping == null){
            return ServerResponse.createByErrorMessage("未找到地址信息");
        }
        return ServerResponse.createBySuccess("地址已找到", shipping);
    }

    public ServerResponse<PageInfo> list(Integer pageNum, Integer pageSize, Integer userId){
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo<Shipping> pageInfo = new PageInfo<>(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
