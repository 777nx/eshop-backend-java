package com.eshop.modules.product.param;

import com.eshop.common.web.param.QueryParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 商品属性详情表 查询参数对象
 * </p>
 *
 * @author zhonghui
 * @date 2019-10-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="StoreProductAttrResultQueryParam对象", description="商品属性详情表查询参数")
public class StoreProductAttrResultQueryParam extends QueryParam {
    private static final long serialVersionUID = 1L;
}
