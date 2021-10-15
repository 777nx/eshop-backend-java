package com.eshop.modules.user.rest;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eshop.api.ApiResult;
import com.eshop.api.EshopException;
import com.eshop.constant.ShopConstants;
import com.eshop.enums.AppFromEnum;
import com.eshop.enums.BillDetailEnum;
import com.eshop.logging.aop.log.AppLog;
import com.eshop.modules.mp.service.WeixinPayService;
import com.eshop.modules.shop.domain.SystemGroupData;
import com.eshop.modules.shop.service.SystemGroupDataService;
import com.eshop.modules.shop.service.dto.SystemGroupDataQueryCriteria;
import com.eshop.modules.shop.vo.SystemGroupDataVo;
import com.eshop.modules.user.domain.ShopUser;
import com.eshop.modules.user.domain.UserBill;
import com.eshop.modules.user.service.UserBillService;
import com.eshop.modules.user.service.UserRechargeService;
import com.eshop.modules.user.service.UserService;
import com.github.binarywang.wxpay.bean.order.WxPayAppOrderResult;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.order.WxPayMwebOrderResult;
import com.eshop.common.bean.LocalUser;
import com.eshop.common.interceptor.AuthCheck;
import com.eshop.modules.user.param.RechargeParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户充值 前端控制器
 * </p>
 *
 * @author wzz
 * @since 2020-03-01
 */
@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(value = "用户充值", tags = "用户:用户充值", description = "用户充值")
public class UserRechargeController {

    private final UserRechargeService userRechargeService;
    private final WeixinPayService weixinPayService;
    private final SystemGroupDataService systemGroupDataService;
    private final UserService userService;
    private final UserBillService userBillService;

    /**
     * 充值方案
     */
    @GetMapping("/recharge/index")
    @ApiOperation(value = "充值方案",notes = "充值方案",response = ApiResult.class)
    public ApiResult<Object> getWays(){
        SystemGroupDataQueryCriteria queryCriteria = new SystemGroupDataQueryCriteria();
        queryCriteria.setGroupName(ShopConstants.YSHOP_RECHARGE_PRICE_WAYS);
        List<SystemGroupData> systemGroupDataList = systemGroupDataService.queryAll(queryCriteria);

        List<SystemGroupDataVo> systemGroupDataVoList = systemGroupDataList.stream().map(s->{
            SystemGroupDataVo systemGroupDataVo = new SystemGroupDataVo();
            BeanUtil.copyProperties(s,systemGroupDataVo,"value");
            systemGroupDataVo.setValue(JSON.parseObject(s.getValue()));
            return systemGroupDataVo;
        }).collect(Collectors.toList());

        Map<String,Object> map = new LinkedHashMap<>();
        map.put("rechargePriceWays",systemGroupDataVoList);
        return ApiResult.ok(map);
    }

    /**
     * 公众号充值/H5充值
     */
    @AppLog(value = "公众号充值", type = 1)
    @AuthCheck
    @PostMapping("/recharge/wechat")
    @ApiOperation(value = "公众号充值/H5充值",notes = "公众号充值/H5充值",response = ApiResult.class)
    public ApiResult<Map<String,Object>> add(@Valid @RequestBody RechargeParam param){
        ShopUser user = LocalUser.getUser();

        Map<String,Object> map = new LinkedHashMap<>();
        map.put("type",param.getFrom());
        SystemGroupData systemGroupData = systemGroupDataService.getById(param.getRecharId());
        if(systemGroupData == null) {
            throw new EshopException("充值方案不存在");
        }

        JSONObject jsonObject = JSON.parseObject(systemGroupData.getValue());
        String price = jsonObject.getString("price");
        String giveMoney =jsonObject.getString("give_price");
        String orderSn = userRechargeService.addRecharge(user,price,giveMoney);

        if(AppFromEnum.WEIXIN_H5.getValue().equals(param.getFrom())){
            WxPayMwebOrderResult result = (WxPayMwebOrderResult)weixinPayService
                    .unifyPay(orderSn,param.getFrom(), BillDetailEnum.TYPE_1.getValue(),"H5充值");
            map.put("data",result.getMwebUrl());
        }else if(AppFromEnum.ROUNTINE.getValue().equals(param.getFrom())){
            WxPayMpOrderResult wxPayMpOrderResult = (WxPayMpOrderResult)weixinPayService
                    .unifyPay(orderSn,param.getFrom(), BillDetailEnum.TYPE_1.getValue(),"小程序充值");
            Map<String,String> jsConfig = new HashMap<>();
            jsConfig.put("timeStamp",wxPayMpOrderResult.getTimeStamp());
            jsConfig.put("appId",wxPayMpOrderResult.getAppId());
            jsConfig.put("paySign",wxPayMpOrderResult.getPaySign());
            jsConfig.put("nonceStr",wxPayMpOrderResult.getNonceStr());
            jsConfig.put("package",wxPayMpOrderResult.getPackageValue());
            jsConfig.put("signType",wxPayMpOrderResult.getSignType());
            map.put("data",jsConfig);
        }else if(AppFromEnum.APP.getValue().equals(param.getFrom())){
            WxPayAppOrderResult wxPayAppOrderResult = (WxPayAppOrderResult)weixinPayService
                    .unifyPay(orderSn,param.getFrom(), BillDetailEnum.TYPE_1.getValue(),"app充值");
            Map<String,String> jsConfig = new HashMap<>();
            jsConfig.put("partnerid",wxPayAppOrderResult.getPartnerId());
            jsConfig.put("appid",wxPayAppOrderResult.getAppId());
            jsConfig.put("prepayid",wxPayAppOrderResult.getPrepayId());
            jsConfig.put("package",wxPayAppOrderResult.getPackageValue());
            jsConfig.put("noncestr",wxPayAppOrderResult.getNonceStr());
            jsConfig.put("timestamp",wxPayAppOrderResult.getTimeStamp());
            jsConfig.put("sign",wxPayAppOrderResult.getSign());
            map.put("data",jsConfig);
        }else{
            WxPayMpOrderResult result = (WxPayMpOrderResult)weixinPayService
                    .unifyPay(orderSn,param.getFrom(), BillDetailEnum.TYPE_1.getValue(),"公众号充值");
            Map<String,String> config = new HashMap<>();
            config.put("timestamp",result.getTimeStamp());
            config.put("appId",result.getAppId());
            config.put("nonceStr",result.getNonceStr());
            config.put("package",result.getPackageValue());
            config.put("signType",result.getSignType());
            config.put("paySign",result.getPaySign());
            map.put("data",config);
        }

        return ApiResult.ok(map);
    }

    /**
     * 模拟充值
     */
    @AppLog(value = "模拟充值", type = 1)
    @AuthCheck
    @PostMapping("/recharge/test")
    @ApiOperation(value = "模拟充值",notes = "模拟充值",response = ApiResult.class)
    public ApiResult<String> addTest(@Valid @RequestBody RechargeParam param){
        ShopUser user = LocalUser.getUser();
        // 通过传过来的充值方案ID查找
        SystemGroupData systemGroupData = systemGroupDataService.getById(param.getRecharId());
        if(systemGroupData == null) {
            throw new EshopException("充值方案不存在");
        }
        JSONObject jsonObject = JSON.parseObject(systemGroupData.getValue());
        // 充值金额
        String price = jsonObject.getString("price");
        // 赠送金额
        String giveMoney =jsonObject.getString("give_price");
        // 根据充值方案，充值到账户
        String orderSn = userRechargeService.addRecharge(user,price,giveMoney);
        // 订单号不为空进入充值
        if(orderSn != null){
            // 充值金额
            BigDecimal new_price = new BigDecimal(price);
            // 赠送金额
            BigDecimal new_giveMoney = new BigDecimal(giveMoney);
            // 总计充值金额
            BigDecimal total = new_price.add(new_giveMoney);
            // 用当前余额加上充值总金额
            BigDecimal balance = user.getNowMoney().add(total);
            // 实体类赋值用户余额
            user.setNowMoney(balance);
            // 更新用户信息
            userService.updateById(user);
            // 往账单表插入充值记录
            UserBill userBill = new UserBill();
            // 用户ID
            userBill.setUid(user.getUid());
            // 账单标题
            userBill.setTitle("账户充值");
            // 明细种类：金额
            userBill.setCategory(BillDetailEnum.CATEGORY_1.getValue());
            // 明细类型：充值
            userBill.setType(BillDetailEnum.TYPE_1.getValue());
            // 1=获得
            userBill.setPm(1);
            // 明细数字
            userBill.setNumber(total);
            // 账户余额
            userBill.setBalance(balance);
            // 备注
            userBill.setMark("模拟充值：充值" + new_price + "元赠送" + new_giveMoney + "元");
            userBillService.save(userBill);
        }

        return ApiResult.ok("充值成功");
    }


}

