
package com.eshop.modules.coupon.rest;

import cn.hutool.core.util.NumberUtil;
import com.eshop.api.ApiResult;
import com.eshop.api.EshopException;
import com.eshop.logging.aop.log.AppLog;
import com.eshop.common.aop.NoRepeatSubmit;
import com.eshop.common.bean.LocalUser;
import com.eshop.common.interceptor.AuthCheck;
import com.eshop.modules.activity.service.StoreCouponIssueService;
import com.eshop.modules.activity.service.StoreCouponUserService;
import com.eshop.modules.activity.vo.StoreCouponUserVo;
import com.eshop.modules.activity.vo.StoreCouponIssueQueryVo;
import com.eshop.modules.activity.vo.StoreCouponUserQueryVo;
import com.eshop.modules.coupon.param.StoreCouponQueryParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 优惠券 todo
 * </p>
 *
 * @author zhonghui
 * @since 2019-10-02
 */
@Slf4j
@RestController
@Api(value = "优惠券", tags = "营销:优惠券")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CouponController {

    private final StoreCouponIssueService couponIssueService;
    private final StoreCouponUserService storeCouponUserService;

    /**
     * 可领取优惠券列表
     */
    @AuthCheck
    @GetMapping("/coupons")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码,默认为1", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "limit", value = "页大小,默认为10", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "productId", value = "产品ID", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "type", value = "优惠券类型 0通用券 1商品券 2内部券", paramType = "query", dataType = "int")
    })
    @ApiOperation(value = "可领取优惠券列表",notes = "可领取优惠券列表")
    public ApiResult<List<StoreCouponIssueQueryVo>> getList(@RequestParam(value = "page",defaultValue = "1") int page,
                                                            @RequestParam(value = "limit",defaultValue = "10") int limit,
                                                            @RequestParam(value = "productId",required = false) Long productId,
                                                            @RequestParam(value = "type",required = false) Integer type){
        Long uid = LocalUser.getUser().getUid();
        return ApiResult.ok(couponIssueService.getCouponList(page, limit,uid,productId,type));
    }

    /**
     * 领取优惠券
     */
    @AppLog(value = "领取优惠券", type = 1)
    @NoRepeatSubmit
    @AuthCheck
    @PostMapping("/coupon/receive")
    @ApiOperation(value = "领取优惠券",notes = "领取优惠券")
    public ApiResult<Boolean> receive(@Validated @RequestBody StoreCouponQueryParam param){
        Long uid = LocalUser.getUser().getUid();
        if(!NumberUtil.isNumber(param.getCouponId())){
           throw new EshopException("参数非法");
        }
        Integer couponId = Integer.valueOf(param.getCouponId());
        couponIssueService.issueUserCoupon(couponId,uid);
        return ApiResult.ok();
    }

    /**
     * 用户已领取优惠券
     */
    @AppLog(value = "查看已领取优惠券", type = 1)
    @AuthCheck
    @GetMapping("/coupons/user/{type}")
    @ApiOperation(value = "用户已领取优惠券",notes = "用户已领取优惠券")
    public ApiResult<List<StoreCouponUserQueryVo>> getUserList(){
        Long uid = LocalUser.getUser().getUid();
        List<StoreCouponUserQueryVo> list = storeCouponUserService.getUserCoupon(uid);
        return ApiResult.ok(list);
    }

    /**
     * 优惠券 订单获取
     */
    @AuthCheck
    @GetMapping("/coupons/order/{cartIds}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cartIds", value = "购物车ID,多个用,分割", paramType = "query", dataType = "int")
    })
    @ApiOperation(value = "优惠券订单获取",notes = "优惠券订单获取")
    public ApiResult<List<StoreCouponUserVo>> orderCoupon(@PathVariable String cartIds){
        Long uid = LocalUser.getUser().getUid();
        return ApiResult.ok(storeCouponUserService.beUsableCouponList(uid,cartIds));
    }


}

