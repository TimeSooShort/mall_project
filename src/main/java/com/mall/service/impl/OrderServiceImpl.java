package com.mall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mall.common.Const;
import com.mall.common.ServerResponse;
import com.mall.dao.*;
import com.mall.pojo.*;
import com.mall.service.IOrderService;
import com.mall.util.BigDecimalUtil;
import com.mall.util.DateTimeUtil;
import com.mall.util.FTPUtil;
import com.mall.util.PropertiesUtil;
import com.mall.vo.OrderItemVO;
import com.mall.vo.OrderProductVO;
import com.mall.vo.OrderVO;
import com.mall.vo.ShippingVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 订单实现类
 * 该类中有很多对象，在此先申明：
 * OrderItem：信息过包含与产品相关，如产品名，产品id，产品图片名，单价，用户购买数量及费用，等。对应order_item表
 * Order对象，订单类，其信息包含收货地址id，订单总额，支付类型，支付时间，运费，交易完成及关闭时间，订单状态等。对应order表
 * OrderItemVO：与OrderItem相比去除了id，userId, updateTime
 * OrderProductVO ： 在前端用户在“我的购物车页”点击去支付按钮，会跳到“订单确认页”，该页面的信息分两部分：收货地址和购买的商品信息
 *                  其中商品信息根据前端的需求后端的处理就是返回OrderProductVO：它包含三个字段：1：totalPrice订单总价
 *                  2： imageHost：img.miaoshop.top 3：List<OrderItemVO> orderItemVoList: 它包含用户所需的商品信息
 * OrderVO ：OrderVO是最后返回给前端的数据，它包含了三部分：1：Order的信息。2：收货地址shipping的信息。3：产品信息，即OrderItem
 * ShippingVO：与原Shipping相比去除了四个无用信息：id, userId, createTime, updateTime
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService{

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private static AlipayTradeService service;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("src/main/resources.beta/zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        service = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 创建订单，分别生成OrderItem与Order对象，orderItem它所含的信息多是商品的信息，如商品名字，图片
     * 单价，所购买数量及要支付费用等。而Order对量的信息是订单相关的，如订单的总价，支付类型，支付状态，运费
     * 支付时间，交易完成时间等。将这些信息分开在两张表中
     * @param userId 用户id
     * @param shippingId 收货地址id
     * @return 返回结果
     */
    public ServerResponse createOrder(Integer userId, Integer shippingId){
        // 获取购物车种所有选中状态是商品，得到一个cart列表
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        if (CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }

        // 得到OrderItem列表
        ServerResponse<List<OrderItem>> response = this.getCartOrderItem(cartList, userId);
        if (!response.isSuccess()){
            return response;
        }
        List<OrderItem> orderItemList = response.getData();

        // 计算订单总价
        BigDecimal orderPrice = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList){
            orderPrice = BigDecimalUtil.add(orderPrice.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }

        //生成订单Order
        Order order = new Order();
        order.setPayment(orderPrice);
        // 该订单号码由此刻时间加random数字组成
        order.setOrderNo(this.getOrderNo());
        order.setStatus(Const.OrderStateEnum.NO_PAY.getCode());
        order.setPaymentType(Const.PaymentTypeEnum.ON_LINE.getCode());
        order.setPostage(0); //运费
        order.setShippingId(shippingId);
        order.setUserId(userId);

        int rowCount = orderMapper.insert(order);
        if (rowCount <= 0){
            return ServerResponse.createByErrorMessage("生成订单错误");
        }

        //接下来插入OrderItem数据，放在Order插入后，是因为确定订单生成成功
        for (OrderItem orderItem : orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        // 批量插入
        orderItemMapper.batchInsert(orderItemList);

        // 订单生成成功后需要更新商品库存，即product的stock
        this.reduceStock(orderItemList);

        // 删除cart表中上述cart信息
        this.cleanCartList(cartList);

        // 组成OrderVO对象返回给前端
        OrderVO orderVO = this.assembleOrderVO(order, orderItemList);
        return ServerResponse.createBySuccess(orderVO);
    }

    /**
     * 根据每个购物车选中商品，即cart，组成OrderItem对象
     * @param cartList cart列表
     * @param userId 用户id
     * @return 返回OrderItem列表
     */
    private ServerResponse<List<OrderItem>> getCartOrderItem(List<Cart> cartList, Integer userId){
        List<OrderItem> orderItemList = Lists.newArrayList();
        // 遍历购物车中选中商品
        for (Cart cart : cartList){
            // 通过id来找到对应商品
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
                return ServerResponse.createByErrorMessage("商品"+product.getName()+"已下架或删除");
            }
            //刚加入购物车的商品数量会得到修正，但一段时间商品实际数量会减少，所以有必要在此检查数量
            if (cart.getQuantity() > product.getStock()){
                return ServerResponse.createByErrorMessage("没有那么多商品了，请修正数量"+product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(userId);
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cart.getQuantity()));

            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    /**
     * 组成一个唯一的订单号码
     * @return
     */
    private long getOrderNo(){
        long currentTime = System.currentTimeMillis();
        return currentTime+new Random().nextInt(100);
    }

    /**
     * 更新product库存
     * @param list
     */
    private void reduceStock(List<OrderItem> list){
        for (OrderItem orderItem : list){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock()-orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    /**
     * 订单生成成功后，删除购物车中相应信息
     * @param cartList 生成订单了的cart列表
     */
    private void cleanCartList(List<Cart> cartList){
        for (Cart cart : cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    /**
     * 去除了原OrderItem的id，userId, updateTime
     * @param orderItem orderItem
     * @return OrderItemVO
     */
    private OrderItemVO assembleOrderItemVO(OrderItem orderItem){
        OrderItemVO orderItemVO = new OrderItemVO();
        orderItemVO.setOrderNo(orderItem.getOrderNo());
        orderItemVO.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVO.setProductId(orderItem.getProductId());
        orderItemVO.setProductImage(orderItem.getProductImage());
        orderItemVO.setProductName(orderItem.getProductName());
        orderItemVO.setQuantity(orderItem.getQuantity());
        orderItemVO.setTotalPrice(orderItem.getTotalPrice());

        orderItemVO.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVO;
    }

    /**
     * 与原Shipping相比去除了四个无用信息：id, userId, createTime, updateTime
     * @param shipping 收货地址
     * @return ShippingVO
     */
    private ShippingVO assembleShippingVo(Shipping shipping){
        ShippingVO shippingVo = new ShippingVO();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        return shippingVo;
    }

    /**
     *
     * @param order 订单信息
     * @param orderItems OrderItem列表
     * @return
     */
    private OrderVO assembleOrderVO(Order order, List<OrderItem> orderItems){
        OrderVO orderVo = new OrderVO();
        orderVo.setOrderNo(order.getOrderNo());
        // 订单总价
        orderVo.setPayment(order.getPayment());
        // 支付类型，1：在线支付
        orderVo.setPaymentType(order.getPaymentType());
        // 通过支付类型数字（code）来获取其描述信息（desc）。如：code：1，其desc为“在线支付”
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getDesc());
        // 运费
        orderVo.setPostage(order.getPostage());
        // 订单状态。0取消，10未支付，20已支付，40已发货，50订单完成，60订单关闭
        orderVo.setStatus(order.getStatus());
        //订单状态描述信息
        orderVo.setStatusDesc(Const.OrderStateEnum.codeOf(order.getStatus()).getValue());
        // 收货地址id
        orderVo.setShippingId(order.getShippingId());
        // 通过shippingId来获取shipping对象，再组成ShippingVo对象
        // 与原Shipping相比去除了四个无用信息：id, userId, createTime, updateTime
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null){
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
            orderVo.setReceiverName(shipping.getReceiverName());
        }
        // 支付时间，Order对象里是Date对象，OrderVo是String对象，需要进行转化
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        // 发货时间
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        // 交易完成时间
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        // 该条记录创建时间
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        // 交易关闭时间
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        // img.miaoshop.top
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        // 将OrderItem组成OrderItemVO，前面说过，OrderItem的信息更多的是跟产品product有关
        // 前端用来显示产品图片，产品名，产品单价，购买数量等信息.
        // 相比原OrderItem去除了单个不用的信息，id，userId，updateTime
        List<OrderItemVO> orderItemVOList = Lists.newArrayList();
        for (OrderItem orderItem : orderItems){
            orderItemVOList.add(this.assembleOrderItemVO(orderItem));
        }
        orderVo.setOrderItemVoList(orderItemVOList);
        return orderVo;
    }

    /**
     * 取消订单
     * @param userId 用户id
     * @param orderNo 订单号
     * @return 返回取消结果
     */
    public ServerResponse cancelOrder(Integer userId, Long orderNo){
        // 通过用户id及订单号获得该订单
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("没有该订单");
        }
        // 只有支付状态等于10，及未支付，才能取消订单
        if (order.getStatus() != Const.OrderStateEnum.NO_PAY.getCode()){
            return ServerResponse.createByErrorMessage("已经支付完成，无法取消");
        }
        // 更新订单状态未0，代表取消状态
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStateEnum.CANCELED.getCode());

        int rowCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (rowCount <= 0){
            return ServerResponse.createByErrorMessage("撤销订单失败");
        }
        return ServerResponse.createBySuccess();
    }

    /**
     * 该方法的调用在前端的订单确认页，在由“我的购物车”页面跳转来，需加载商品信息用来显示
     * 逻辑是: 1：通过userId来获取购物车中选定商品列表——>cartList
     * 2：用cart信息来组成OrderItem，得到OrderItem列表
     * 3: 再将每个OrderItem组成OrderItemVO，获得OrderItemVO列表，连带计算出订单总额
     * 4：最后组成OrderProductVO对象返回
     *
     * 关于这些对象的解释在这篇的开头处。
     * @param userId 用户id
     * @return OrderProductVO
     */
    public ServerResponse getOrderCartProduct(Integer userId){
        // 用于返回的OrderProductVO
        OrderProductVO orderProductVO = new OrderProductVO();
        // 获取cartList列表
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        // 组成OrderItem列表
        ServerResponse<List<OrderItem>> response = this.getCartOrderItem(cartList, userId);
        if (!response.isSuccess()){
            return response;
        }
        List<OrderItem> orderItemList = response.getData();
        // 进一步封装成OrderItemVO
        List<OrderItemVO> orderItemVOList = Lists.newArrayList();
        // 计算订单总价
        BigDecimal totalPayment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList){
            totalPayment = BigDecimalUtil.add(totalPayment.doubleValue(), orderItem.getTotalPrice().doubleValue());
            OrderItemVO orderItemVO = this.assembleOrderItemVO(orderItem);
            orderItemVOList.add(orderItemVO);
        }
        //赋值
        orderProductVO.setOrderItemVoList(orderItemVOList);
        orderProductVO.setTotalPrice(totalPayment);
        orderProductVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVO);
    }

    /**
     * 获取订单详情。即OrderVO。
     * @param userId 用户id
     * @param orderNo 订单号
     * @return OrderVO
     */
    public ServerResponse getOrderDetail(Integer userId, Long orderNo){
        // 通过用户名及订单号获得订单Order
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        // 通过用户id及订单号获取OrderItem列表
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, orderNo);
        // 组成OrderVO
        OrderVO orderVO = this.assembleOrderVO(order, orderItemList);
        return ServerResponse.createBySuccess(orderVO);
    }

    /**
     *
     * @param userId 用户id
     * @param pageSize 一页多大
     * @param pageNum 第几页
     * @return orderVOList
     */
    public ServerResponse getOrderList(Integer userId, Integer pageSize, Integer pageNum){
        PageHelper.startPage(pageNum,pageSize);
        // 获取订单列表
        List<Order> orderList = orderMapper.selectByUserId(userId);
        // 获取OrderVO列表
        List<OrderVO> orderVOList = assembleOrderVOlist(orderList, userId);
        PageInfo info = new PageInfo(orderList);
        info.setList(orderVOList);
        return ServerResponse.createBySuccess(info);
    }

    /**
     * 得到OrderVO的列表。
     * 逻辑就是将Order列表的每个都封装成OrderItem。先获取OrderItem的列表再调用assembleOrderVO方法组成OrderVO对象
     * @param orderList Order列表
     * @param userId 用户id
     * @return List<OrderVO>
     */
    private List<OrderVO> assembleOrderVOlist(List<Order> orderList, Integer userId){
        List<OrderVO> orderVOList = Lists.newArrayList();
        for (Order order : orderList){
            List<OrderItem> orderItemList;
            if (userId == null){
                //管理员在查询时不需要传id
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            }else {
                orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, order.getOrderNo());
            }
            OrderVO orderVO = this.assembleOrderVO(order, orderItemList);
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }






    public ServerResponse pay(Long orderNum, Integer userId, String path){
        Map<String, String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNum);
        if (order == null){
            return ServerResponse.createByErrorMessage("找不到该订单");
        }
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));



        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店消费”
        String subject = new StringBuilder().append("mall扫码支付，订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();


        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品3件共20.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共花费")
                .append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息
        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        // 创建好一个商品后添加至商品明细列表
        List<GoodsDetail> goodsDetailList = Lists.newArrayList();
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, orderNum);
        for (OrderItem item : orderItemList){
            GoodsDetail goodsDetail = GoodsDetail.newInstance(item.getProductId().toString(), item.getProductName(),
                    BigDecimalUtil.mul(item.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    item.getQuantity());
            goodsDetailList.add(goodsDetail);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = service.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if (!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径
                String qrPath = String.format(path + "/qr-%s.png",
                        response.getOutTradeNo());
                log.info("qrPath:" + qrPath);
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File targetFile = new File(qrPath);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    log.error("上传二维码异常",e);
                }

                String qrFileName = String.format("qr-%s.png", response.getOutTradeNo());
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + qrFileName;
                resultMap.put("qrUrl", qrUrl);

                return ServerResponse.createBySuccess(resultMap);
            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }

    public ServerResponse aliCallback(Map<String, String> params){
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("非本商店订单,忽略该回调");
        }
        if (order.getStatus() >= Const.OrderStateEnum.PAID.getCode()){
            return ServerResponse.createByErrorMessage("该行为为重复调用");
        }
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            order.setStatus(Const.OrderStateEnum.PAID.getCode());
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            orderMapper.updateByPrimaryKeySelective(order);
        }
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatform.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);  //支付宝交易号
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("该用户并没有该订单,查询无效");
        }
        if (order.getStatus() >= Const.OrderStateEnum.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }


    //=====================后台管理系统接口=========================

    /**
     * 订单列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> manageList(Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVO> orderVOList = this.assembleOrderVOlist(orderList, null);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVOList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 按订单号查询
     * @param pageNum
     * @param pageSize
     * @param orderNo 订单号
     * @return OrderVO
     */
    public ServerResponse<PageInfo> manageSearch(Integer pageNum, Integer pageSize, Long orderNo){
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("没有该订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
        OrderVO orderVO = this.assembleOrderVO(order, orderItemList);
        PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
        pageInfo.setList(Lists.newArrayList(orderVO));
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 订单详情
     * @param orderNo 订单号
     * @return OrderVO
     */
    public ServerResponse<OrderVO> manageDetail(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("没有该订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
        OrderVO orderVO = this.assembleOrderVO(order, orderItemList);
        return ServerResponse.createBySuccess(orderVO);
    }

    /**
     * 订单发货
     * @param orderNo 订单号
     * @return
     */
    public ServerResponse manageSendGoods(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("没有该订单");
        }
        // 判断订单是否已支付
        if (order.getStatus() == Const.OrderStateEnum.PAID.getCode()){
            order.setStatus(Const.OrderStateEnum.SHIPPED.getCode());
            order.setSendTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            return ServerResponse.createByErrorMessage("发货成功");
        }
        return ServerResponse.createByErrorMessage("付款失败，付款之后发货");
    }
}
