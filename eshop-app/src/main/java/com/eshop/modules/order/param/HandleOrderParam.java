package com.eshop.modules.order.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @ClassName HandleOrderParam
 * @Author zhonghui
 * @Date 2020/6/23
 **/
@Getter
@Setter
public class HandleOrderParam {

    @NotBlank(message = "参数有误")
    @ApiModelProperty(value = "订单ID")
    private String id;
}
