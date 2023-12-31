package com.eshop.modules.coupon.param;

import com.eshop.common.web.param.QueryParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 优惠券前台用户领取记录表 查询参数对象
 * </p>
 *
 * @author zhonghui
 * @date 2019-10-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="StoreCouponIssueUserQueryParam对象", description="优惠券前台用户领取记录表查询参数")
public class StoreCouponIssueUserQueryParam extends QueryParam {
    private static final long serialVersionUID = 1L;
}
