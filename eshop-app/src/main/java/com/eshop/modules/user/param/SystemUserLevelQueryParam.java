package com.eshop.modules.user.param;

import com.eshop.common.web.param.QueryParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 设置用户等级表 查询参数对象
 * </p>
 *
 * @author wzz
 * @date 2019-12-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="SystemUserLevelQueryParam对象", description="设置用户等级表查询参数")
public class SystemUserLevelQueryParam extends QueryParam {
    private static final long serialVersionUID = 1L;
}
