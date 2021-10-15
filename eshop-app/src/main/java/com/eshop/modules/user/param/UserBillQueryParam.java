package com.eshop.modules.user.param;

import com.eshop.common.web.param.QueryParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户账单表 查询参数对象
 * </p>
 *
 * @author wzz
 * @date 2019-10-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="UserBillQueryParam对象", description="用户账单表查询参数")
public class UserBillQueryParam extends QueryParam {
    private static final long serialVersionUID = 1L;

}
