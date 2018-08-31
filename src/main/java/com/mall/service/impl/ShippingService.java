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
 * 地址服务实现
 */
@Service("iShippingService")
public class ShippingService implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 地址添加
     * @param userId 用户id
     * @param shipping 收货人信息
     * @return
     */
    public ServerResponse<Map> add(Integer userId, Shipping shipping){
        // 给前端传来的数据添加userId
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if (rowCount > 0){
            Map<String, Integer> result = Maps.newHashMap();
            result.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功", result);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    /**
     * 删除收货地址
     * @param userId 用户id
     * @param shippingId 收货地址id
     * @return
     */
    public ServerResponse<String> del(Integer userId, Integer shippingId){
        //采用双验证为了防止横向越权，如某一登录用户删除其他用户的地址信息
        int rowCount = shippingMapper.deleteByUserIdShippingId(userId, shippingId);
        if (rowCount > 0){
            return ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }


    /**
     * 跟新收货地址
     * @param shipping 信息
     * @return
     */
    public ServerResponse<String> update(Shipping shipping){
        // 先通过id与userId得到数据可里的信息
        Shipping result = this.select(shipping.getId(), shipping.getUserId()).getData();
        if (result == null) return ServerResponse.createByErrorMessage("未找到地址信息");
        // 获取其创建时间
        shipping.setCreateTime(result.getCreateTime());
        // 更新信息
        int rowCount = shippingMapper.updateByUserIdShippingId(shipping);
        if (rowCount > 0){
            return ServerResponse.createBySuccessMessage("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    /**
     * 选中查看具体的地址信息，即前端编辑按钮的点击
     * @param shippingId 收货地址id
     * @param userId 用户id
     * @return Shipping
     */
    public ServerResponse<Shipping> select(Integer shippingId, Integer userId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(shippingId, userId);
        if (shipping == null){
            return ServerResponse.createByErrorMessage("未找到地址信息");
        }
        return ServerResponse.createBySuccess("地址已找到", shipping);
    }

    /**
     *
     * @param pageNum 第几页
     * @param pageSize 一页几个
     * @param userId 用户id
     * @return PageInfo
     */
    public ServerResponse<PageInfo> list(Integer pageNum, Integer pageSize, Integer userId){
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo<Shipping> pageInfo = new PageInfo<>(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
