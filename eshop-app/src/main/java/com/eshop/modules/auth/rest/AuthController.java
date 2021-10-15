
package com.eshop.modules.auth.rest;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eshop.api.ApiResult;
import com.eshop.api.EshopException;
import com.eshop.common.bean.LocalUser;
import com.eshop.common.enums.SmsTypeEnum;
import com.eshop.common.interceptor.AuthCheck;
import com.eshop.common.util.JwtToken;
import com.eshop.common.util.SmsUtils;
import com.eshop.constant.ShopConstants;
import com.eshop.enums.ShopCommonEnum;
import com.eshop.modules.auth.param.*;
import com.eshop.modules.services.AuthService;
import com.eshop.modules.user.domain.ShopUser;
import com.eshop.modules.user.service.UserService;
import com.eshop.utils.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName 认证服务
 * @Author zhonghui
 * @Date 2020/4/30
 **/
@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(value = "认证模块", tags = "商城:认证")
public class AuthController {

    private final UserService userService;
    private final RedisUtils redisUtil;
    private final AuthService authService;

    @Value("${single.login}")
    private Boolean singleLogin;

    @ApiOperation("H5登录授权")
    @PostMapping(value = "/login")
    public ApiResult<Map<String, Object>> login(@Validated @RequestBody HLoginParam loginDTO, HttpServletRequest request) {
        ShopUser shopUser = userService.getOne(Wrappers.<ShopUser>lambdaQuery()
                .eq(ShopUser::getUsername,loginDTO.getUsername())
                .eq(ShopUser::getPassword,SecureUtil.md5(loginDTO.getPassword())),false);

        if(shopUser == null) {
            throw new EshopException("账号或者密码不正确");
        }

        String token =  JwtToken.makeToken(shopUser.getUid(), shopUser.getUsername());
        String expiresTimeStr = JwtToken.getExpireTime(token);

        // 保存在线信息
        authService.save(shopUser, token, request);
        // 返回 token
        Map<String, Object> map = new HashMap<String, Object>(2) {{
            put("token", token);
            put("expires_time", expiresTimeStr);
        }};

        userService.setSpread(loginDTO.getSpread(), shopUser.getUid());

        if(singleLogin){
            //踢掉之前已经登录的token
            authService.checkLoginOnUser(shopUser.getUsername(),token);
        }

        return ApiResult.ok(map).setMsg("登陆成功");
    }


    @PostMapping("/register")
    @ApiOperation(value = "H5/APP注册新用户", notes = "H5/APP注册新用户")
    public ApiResult<String> register(@Validated @RequestBody RegParam param) {
        Object codeObj = redisUtil.get("code_" + param.getAccount());
        if(codeObj == null){
            return ApiResult.fail("请先获取验证码");
        }
        String code = codeObj.toString();
        if (!StrUtil.equals(code, param.getCaptcha())) {
            return ApiResult.fail("验证码错误");
        }
        ShopUser shopUser = userService.getOne(Wrappers.<ShopUser>lambdaQuery()
                .eq(ShopUser::getPhone,param.getAccount()),false);
        if (ObjectUtil.isNotNull(shopUser)) {
            return ApiResult.fail("用户已存在");
        }

        authService.register(param);
        return ApiResult.ok("","注册成功");
    }


    @PostMapping("/register/verify")
    @ApiOperation(value = "短信验证码发送", notes = "短信验证码发送")
    public ApiResult<String> verify(@Validated @RequestBody VerityParam param) {
        ShopUser shopUser = userService.getOne(Wrappers.<ShopUser>lambdaQuery()
                .eq(ShopUser::getPhone,param.getPhone()),false);
        if (SmsTypeEnum.REGISTER.getValue().equals(param.getType()) && ObjectUtil.isNotNull(shopUser)) {
            return ApiResult.fail("手机号已注册");
        }
        if (SmsTypeEnum.LOGIN.getValue().equals(param.getType()) && ObjectUtil.isNull(shopUser)) {
            return ApiResult.fail("账号不存在");
        }
        String codeKey = "code_" + param.getPhone();
        if (ObjectUtil.isNotNull(redisUtil.get(codeKey))) {
            return ApiResult.fail("10分钟内有效:" + redisUtil.get(codeKey).toString());
        }
        String code = RandomUtil.randomNumbers(ShopConstants.YSHOP_SMS_SIZE);

        //redis存储
        redisUtil.set(codeKey, code, ShopConstants.YSHOP_SMS_REDIS_TIME);

        String enable = redisUtil.getY("sms_enable");
        if (ShopCommonEnum.ENABLE_2.getValue().toString().equals(enable)) {
            return ApiResult.ok(code);
        }

        //发送阿里云短信
        JSONObject json = new JSONObject();
        json.put("code",code);
        try {
            SmsUtils.sendSms(param.getPhone(),json.toJSONString());
        } catch (ClientException e) {
            redisUtil.del(codeKey);
            e.printStackTrace();
            return ApiResult.ok("发送失败："+e.getErrMsg());
       }
        return ApiResult.ok("发送成功，请注意查收");


    }

    @AuthCheck
    @ApiOperation(value = "退出登录", notes = "退出登录")
    @PostMapping(value = "/auth/logout")
    public ApiResult<String> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        String[] tokens = bearerToken.split(" ");
        String token = tokens[1];
        authService.logout(LocalUser.getUser().getUsername(), token);
        return ApiResult.ok("退出成功");
    }

    @ApiOperation(value = "忘记密码", notes = "忘记密码")
    @PostMapping(value = "/forget")
    public ApiResult<String> forget(@Validated @RequestBody ForgetParam param) {
        Object codeObj = redisUtil.get("code_" + param.getAccount());
        if(codeObj == null){
            return ApiResult.fail("请先获取验证码");
        }
        String code = codeObj.toString();
        if (!StrUtil.equals(code, param.getCaptcha())) {
            return ApiResult.fail("验证码错误");
        }
        ShopUser shopUser = userService.getOne(Wrappers.<ShopUser>lambdaQuery()
                .eq(ShopUser::getPhone,param.getAccount()),false);
        if (ObjectUtil.isNull(shopUser)) {
            return ApiResult.fail("该用户不存在");
        }

        shopUser.setPassword(SecureUtil.md5(param.getPassword()));
        userService.updateById(shopUser);
        return ApiResult.ok("密码重置成功");
    }
}
