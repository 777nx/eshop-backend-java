package com.eshop.modules.user.rest;


import cn.hutool.crypto.SecureUtil;
import com.eshop.api.ApiResult;
import com.eshop.constant.ShopConstants;
import com.eshop.enums.BillInfoEnum;
import com.eshop.logging.aop.log.AppLog;
import com.eshop.modules.order.service.StoreOrderService;
import com.eshop.modules.order.vo.UserOrderCountVo;
import com.eshop.modules.product.service.StoreProductRelationService;
import com.eshop.modules.shop.service.SystemConfigService;
import com.eshop.modules.shop.service.SystemGroupDataService;
import com.eshop.modules.user.domain.ShopUser;
import com.eshop.modules.user.service.UserBillService;
import com.eshop.modules.user.service.UserService;
import com.eshop.modules.user.service.UserSignService;
import com.eshop.modules.user.vo.SignVo;
import com.eshop.modules.user.vo.UserQueryVo;
import com.google.common.collect.Maps;
import com.eshop.common.aop.NoRepeatSubmit;
import com.eshop.common.bean.LocalUser;
import com.eshop.common.interceptor.AuthCheck;
import com.eshop.common.util.UploadUtils;
import com.eshop.modules.user.param.UserEditParam;
import com.eshop.modules.user.param.UserPasswordParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.eshop.constant.SystemConfigConstants.YSHOP_SHOW_RECHARGE;

/**
 * <p>
 * 用户控制器
 * </p>
 *
 * @author wzz
 * @since 2019-10-16
 */
@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(value = "用户中心", tags = "用户:用户中心", description = "用户中心")
public class UserController {

    private final UserService userService;
    private final SystemGroupDataService systemGroupDataService;
    private final StoreOrderService orderService;
    private final StoreProductRelationService relationService;
    private final UserSignService userSignService;
    private final UserBillService userBillService;
    private final SystemConfigService systemConfigService;


    /**
     * 用户资料
     */
    @AuthCheck
    @GetMapping("/userinfo")
    @ApiOperation(value = "获取用户信息",notes = "获取用户信息",response = UserQueryVo.class)
    public ApiResult<Object> userInfo(){
        ShopUser shopUser = LocalUser.getUser();
        return ApiResult.ok(userService.getNewYxUserById(shopUser));
    }

    /**
     * 获取个人中心菜单
     */
    @GetMapping("/menu/user")
    @ApiOperation(value = "获取个人中心菜单",notes = "获取个人中心菜单")
    public ApiResult<Map<String,Object>> userMenu(){
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("routine_my_menus",systemGroupDataService.getDatas(ShopConstants.YSHOP_MY_MENUES));
        return ApiResult.ok(map);
    }



    /**
     * 订单统计数据
     */
    @AppLog(value = "查看订单统计数据", type = 1)
    @AuthCheck
    @GetMapping("/order/data")
    @ApiOperation(value = "订单统计数据",notes = "订单统计数据")
    public ApiResult<UserOrderCountVo> orderData(){
        Long uid = LocalUser.getUser().getUid();
        return ApiResult.ok(orderService.orderData(uid));
    }

    /**
     * 用户资金统计
     */
    @AppLog(value = "查看用户资金统计", type = 1)
    @AuthCheck
    @GetMapping("/user/balance")
    @ApiOperation(value = "用户资金统计",notes = "用户资金统计")
    public ApiResult<Object> userBalance(){
        ShopUser shopUser = LocalUser.getUser();
        Map<String,Object> map = Maps.newHashMap();
        Double[] userMoneys = userService.getUserMoney(shopUser.getUid());
        //map.put("now_money",yxUser.getNowMoney());
        map.put("nowMoney", shopUser.getNowMoney());
        map.put("orderStatusSum",userMoneys[0]);
        map.put("recharge",userMoneys[1]);
        map.put("is_hide",systemConfigService.getData(YSHOP_SHOW_RECHARGE));
        return ApiResult.ok(map);
    }


    /**
     * 签到用户信息
     */
    @AppLog(value = "签到用户信息", type = 1)
    @AuthCheck
    @PostMapping("/sign/user")
    @ApiOperation(value = "签到用户信息",notes = "签到用户信息")
    public ApiResult<UserQueryVo> sign(){
        ShopUser shopUser = LocalUser.getUser();
        return ApiResult.ok(userSignService.userSignInfo(shopUser));
    }

    /**
     * 签到配置
     */
    @GetMapping("/sign/config")
    @ApiOperation(value = "签到配置",notes = "签到配置")
    public ApiResult<Object> signConfig(){
        return ApiResult.ok(systemGroupDataService.getDatas(ShopConstants.YSHOP_SIGN_DAY_NUM));
    }

    /**
     * 签到列表
     */
    @AppLog(value = "查看签到列表", type = 1)
    @AuthCheck
    @GetMapping("/sign/list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码,默认为1", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "limit", value = "页大小,默认为10", paramType = "query", dataType = "int")
    })
    @ApiOperation(value = "签到列表",notes = "签到列表")
    public ApiResult<List<SignVo>> signList(@RequestParam(value = "page",defaultValue = "1") int page,
                                            @RequestParam(value = "limit",defaultValue = "10") int limit){
        Long uid = LocalUser.getUser().getUid();
        return ApiResult.ok(userSignService.getSignList(uid,page,limit));
    }

    /**
     * 签到列表（年月）
     */

    @AuthCheck
    @GetMapping("/sign/month")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码,默认为1", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "limit", value = "页大小,默认为10", paramType = "query", dataType = "int")
    })
    @ApiOperation(value = "签到列表（年月）",notes = "签到列表（年月）")
    public ApiResult<Object> signMonthList(@RequestParam(value = "page",defaultValue = "1") int page,
                                           @RequestParam(value = "limit",defaultValue = "10") int limit){
        Long uid = LocalUser.getUser().getUid();
        return ApiResult.ok(userBillService.getUserBillList(page, limit,uid, BillInfoEnum.SIGN_INTEGRAL.getValue()));
    }

    /**
     * 开始签到
     */
    @AppLog(value = "开始签到", type = 1)
    @NoRepeatSubmit
    @AuthCheck
    @PostMapping("/sign/integral")
    @ApiOperation(value = "开始签到",notes = "开始签到")
    public ApiResult<Object> signIntegral(){
        ShopUser shopUser = LocalUser.getUser();
        int integral = userSignService.sign(shopUser);;

        Map<String,Object> map = new LinkedHashMap<>();
        map.put("integral",integral);
        return ApiResult.ok(map,"签到获得" + integral + "积分");
    }

    @AppLog(value = "用户修改信息", type = 1)
    @AuthCheck
    @PostMapping("/user/edit")
    @ApiOperation(value = "用户修改信息",notes = "用修改信息")
    public ApiResult<Object> edit(@RequestBody @Validated UserEditParam param) throws ParseException {
        ShopUser shopUser = LocalUser.getUser();

        if(param.getFile() != null) {
            shopUser.setAvatar(UploadUtils.getImgUrl(param.getFile()));
        }
        shopUser.setNickname(param.getNickname());
        shopUser.setPhone(param.getPhone());
        shopUser.setAddres(param.getAddres());
        shopUser.setBirthday(param.getBirthday());

        userService.updateById(shopUser);
        return ApiResult.ok("修改成功");
    }

    @AppLog(value = "修改密码", type = 1)
    @AuthCheck
    @PostMapping("/user/password")
    @ApiOperation(value = "用户修改密码",notes = "用户修改密码")
    public ApiResult<String> password(@RequestBody UserPasswordParam param){
        ShopUser shopUser = LocalUser.getUser();
        String password = shopUser.getPassword();
        if(!password.equals(SecureUtil.md5(param.getPassword()))){
            return ApiResult.fail("原密码输入错误，请重试！");
        }
        shopUser.setPassword(SecureUtil.md5(param.getNewPassword()));
        userService.updateById(shopUser);
        return ApiResult.ok("修改成功");
    }

}

