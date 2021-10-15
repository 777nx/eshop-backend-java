package com.eshop.modules.user.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName UserPasswordParam
 * @author chx
 * @Date 2020/03/16
 **/
@Data
public class UserPasswordParam implements Serializable {

    @ApiModelProperty(value = "用户旧密码")
    private String password;

    @ApiModelProperty(value = "用户新密码")
    private String newPassword;

}
