package com.eshop.modules.user.param;

import com.eshop.common.web.param.QueryParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 微信用户表 查询参数对象
 * </p>
 *
 * @author wzz
 * @date 2019-10-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="WechatUserQueryParam对象", description="微信用户表查询参数")
public class WechatUserQueryParam extends QueryParam {
    private static final long serialVersionUID = 1L;
}
