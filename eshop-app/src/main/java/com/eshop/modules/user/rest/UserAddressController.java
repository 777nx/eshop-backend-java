package com.eshop.modules.user.rest;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eshop.api.ApiResult;
import com.eshop.api.EshopException;
import com.eshop.constant.ShopConstants;
import com.eshop.logging.aop.log.AppLog;
import com.eshop.modules.template.domain.SystemCity;
import com.eshop.modules.template.service.SystemCityService;
import com.eshop.modules.template.service.mapper.SystemCityMapper;
import com.eshop.modules.user.param.AddressParam;
import com.eshop.modules.user.service.UserAddressService;
import com.eshop.modules.user.vo.CityVo;
import com.eshop.modules.user.vo.UserAddressQueryVo;
import com.eshop.utils.StringUtils;
import com.google.common.collect.Lists;
import com.eshop.common.bean.LocalUser;
import com.eshop.common.interceptor.AuthCheck;
import com.eshop.common.util.CityTreeUtil;
import com.eshop.common.web.param.IdParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户地前端控制器
 * </p>
 *
 * @author wzz
 * @since 2019-10-28
 */
@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(value = "用户地址", tags = "用户:用户地址", description = "用户地址")
public class UserAddressController {

    private final UserAddressService userAddressService;
    private final SystemCityService systemCityService;
    private final SystemCityMapper systemCityMapper;


    @Cacheable(cacheNames = ShopConstants.YSHOP_REDIS_CITY_KEY)
    @GetMapping("/city_list")
    @ApiOperation(value = "城市列表",notes = "城市列表")
    public ApiResult<List<CityVo>> getTest() {
        List<SystemCity> systemCities = systemCityService.list();

        List<CityVo> cityVOS = Lists.newArrayList();

        for (SystemCity systemCity : systemCities){
            CityVo cityVO = new CityVo();

            cityVO.setValue(systemCity.getCityId());
            cityVO.setLabel(systemCity.getName());
            cityVO.setPid(systemCity.getParentId());

            cityVOS.add(cityVO);
        }


        return ApiResult.ok(CityTreeUtil.list2TreeConverter(cityVOS, 0));

    }

    @GetMapping("/city_list/{cityId}")
    public ApiResult<Object> getCityByPid(@PathVariable String cityId){
        LambdaQueryWrapper<SystemCity> queryWrapper  = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if (StringUtils.isNotBlank(cityId)) {
            queryWrapper.eq(SystemCity::getParentId, cityId);
        }
        List<SystemCity> res = systemCityMapper.selectList(queryWrapper);
        return ApiResult.ok(res);
    }

    /**
    * 添加或修改地址
    */
    @AppLog(value = "添加或修改地址", type = 1)
    @AuthCheck
    @PostMapping("/address/edit")
    @ApiOperation(value = "添加或修改地址",notes = "添加或修改地址")
    public ApiResult<Map<String,Object>> addUserAddress(@Valid @RequestBody AddressParam param){
        Long uid = LocalUser.getUser().getUid();
        Long id = userAddressService.addAndEdit(uid,param);
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("id",id);
        return ApiResult.ok(map);
    }

    /**
     * 设置默认地址
     */
    @AppLog(value = "设置默认地址", type = 1)
    @AuthCheck
    @PostMapping("/address/default/set")
    @ApiOperation(value = "设置默认地址",notes = "设置默认地址")
    public ApiResult<Boolean> setDefault(@Valid @RequestBody IdParam idParam){
        Long uid = LocalUser.getUser().getUid();
        userAddressService.setDefault(uid,Long.valueOf(idParam.getId()));
        return ApiResult.ok();
    }

    /**
    * 删除用户地址
    */
    @AuthCheck
    @PostMapping("/address/del")
    @ApiOperation(value = "删除用户地址",notes = "删除用户地址")
    public ApiResult<Boolean> deleteUserAddress(@Valid @RequestBody IdParam idParam){
        userAddressService.removeById(idParam.getId());
        return ApiResult.ok();
    }


    /**
     * 用户地址列表
     */
    @AuthCheck
    @GetMapping("/address/list")
    @ApiOperation(value = "用户地址列表",notes = "用户地址列表")
    public ApiResult<List<UserAddressQueryVo>> getUserAddressPageList(@RequestParam(value = "page",defaultValue = "1") int page,
                                                                        @RequestParam(value = "limit",defaultValue = "10") int limit){
        Long uid = LocalUser.getUser().getUid();
        List<UserAddressQueryVo> addressQueryVos = userAddressService.getList(uid,page,limit);
        return ApiResult.ok(addressQueryVos);
    }

    /**
     * 地址详情
     */
    @AuthCheck
    @GetMapping("/address/detail/{id}")
    @ApiOperation(value = "地址详情",notes = "地址详情")
    public ApiResult<UserAddressQueryVo> addressDetail(@PathVariable String id){
        if(StrUtil.isBlank(id) || !NumberUtil.isNumber(id)){
            throw new EshopException("参数非法");
        }
        return ApiResult.ok(userAddressService.getDetail(Long.valueOf(id)));
    }

}

