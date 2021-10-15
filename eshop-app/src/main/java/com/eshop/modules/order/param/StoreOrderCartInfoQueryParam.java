package com.eshop.modules.order.param;

import com.eshop.common.web.param.QueryParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 订单购物详情表 查询参数对象
 * </p>
 *
 * @author zhonghui
 * @date 2019-10-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="StoreOrderCartInfoQueryParam对象", description="订单购物详情表查询参数")
public class StoreOrderCartInfoQueryParam extends QueryParam {
    private static final long serialVersionUID = 1L;
}
