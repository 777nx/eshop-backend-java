package com.eshop.modules.auth.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @ClassName ForgetParam
 * @author zhonghui
 * @Date 2021/4/01
 **/
@Data
public class ForgetParam {

    @NotBlank(message = "手机号必填")
    @ApiModelProperty(value = "手机号码")
    private String account;

    @NotBlank(message = "验证码必填")
    @ApiModelProperty(value = "验证码")
    private String captcha;

    @NotBlank(message = "密码必填")
    @ApiModelProperty(value = "密码")
    private String password;

}
