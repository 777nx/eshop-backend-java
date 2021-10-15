package com.eshop.modules.user.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @ClassName UserEditParam
 * @author wzz
 * @Date 2020/02/07
 **/
@Data
public class UserEditParam implements Serializable {

    @ApiModelProperty(value = "用户头像")
    private String avatar;

    @Size(min = 1, max = 60,message = "长度超过了限制")
    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "手机号码")
    private String phone;

    @ApiModelProperty(value = "详细地址")
    private String addres;

    @ApiModelProperty(value = "生日")
    private String birthday;

    private MultipartFile file;

}
