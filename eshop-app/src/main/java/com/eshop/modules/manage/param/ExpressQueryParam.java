package com.eshop.modules.manage.param;

import com.eshop.common.web.param.QueryParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 快递公司表 查询参数对象
 * </p>
 *
 * @author zhonghui
 * @date 2019-12-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="ExpressQueryParam对象", description="快递公司表查询参数")
public class ExpressQueryParam extends QueryParam {
    private static final long serialVersionUID = 1L;
}
