package com.eshop.modules.user.param;

import com.eshop.common.web.param.QueryParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户提现表 查询参数对象
 * </p>
 *
 * @author wzz
 * @date 2019-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="UserExtractQueryParam对象", description="用户提现表查询参数")
public class UserExtractQueryParam extends QueryParam {
    private static final long serialVersionUID = 1L;
}
