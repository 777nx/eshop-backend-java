
package com.eshop.controller;


import cn.hutool.core.util.NumberUtil;
import com.eshop.api.ApiResult;
import com.eshop.api.EshopException;
import com.eshop.common.aop.NoRepeatSubmit;
import com.eshop.common.bean.LocalUser;
import com.eshop.common.interceptor.AuthCheck;
import com.eshop.logging.aop.log.AppLog;
import com.eshop.modules.business.service.ProductRelationService;
import com.eshop.modules.business.vo.StoreProductRelationQueryVo;
import com.eshop.modules.product.param.StoreProductRelationQueryParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 商品控制器
 * </p>
 *
 * @author zhonghui
 * @since 2019-10-19
 */
@Slf4j
@RestController
@Api(value = "产品模块", tags = "商城:产品模块")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductCollectController {

    private final ProductRelationService productRelationService;

    /**
     * 添加收藏
     */
    @AppLog(value = "添加收藏", type = 1)
    @NoRepeatSubmit
    @AuthCheck
    @PostMapping("/collect/add")
    @ApiOperation(value = "添加收藏",notes = "添加收藏")
    public ApiResult<Boolean> collectAdd(@Validated @RequestBody StoreProductRelationQueryParam param){
        long uid = LocalUser.getUser().getUid();
        if(!NumberUtil.isNumber(param.getId())) {
            throw new EshopException("参数非法");
        }
        productRelationService.addRroductRelation(Long.valueOf(param.getId()),uid,param.getCategory());
        return ApiResult.ok();
    }

    /**
     * 取消收藏
     */
    @AppLog(value = "取消收藏", type = 1)
    @NoRepeatSubmit
    @AuthCheck
    @PostMapping("/collect/del")
    @ApiOperation(value = "取消收藏",notes = "取消收藏")
    public ApiResult<Boolean> collectDel(@Validated @RequestBody StoreProductRelationQueryParam param){
        long uid = LocalUser.getUser().getUid();
        if(!NumberUtil.isNumber(param.getId())) {
            throw new EshopException("参数非法");
        }
        productRelationService.delRroductRelation(Long.valueOf(param.getId()),
                uid,param.getCategory());
        return ApiResult.ok();
    }
    /**
     * 批量取消收藏 collect收藏 foot 足迹
     */
    @AppLog(value = "批量取消收藏", type = 1)
    @NoRepeatSubmit
    @AuthCheck
    @PostMapping("/collect/dels/{productIds}")
    @ApiOperation(value = "批量取消收藏",notes = "批量取消收藏")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> collectDels(@PathVariable String productIds,@RequestBody StoreProductRelationQueryParam param){
        long uid = LocalUser.getUser().getUid();
        String[] ids = productIds.split(",");
        if(ids.length > 0){
            for (String id : ids){
                productRelationService.delRroductRelation(Long.parseLong(id), uid, param.getCategory());
            }
        }else{
            throw new EshopException("参数非法");
        }
        return ApiResult.ok();
    }

    @AuthCheck
    @GetMapping("/collect/user")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码,默认为1", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "limit", value = "页大小,默认为10", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "type", value = "foot为足迹,collect为收藏", paramType = "query", dataType = "String")
    })
    @ApiOperation(value = "获取收藏产品,或足迹",notes = "获取收藏产品,或足迹")
    public ApiResult<List<StoreProductRelationQueryVo>> collectUser(@RequestParam(value = "page",defaultValue = "1") int page,
                                                                    @RequestParam(value = "limit",defaultValue = "500") int limit,
                                                                    @RequestParam(value = "type") String type){
        Long uid = LocalUser.getUser().getUid();
        List<StoreProductRelationQueryVo> storeProductRelationQueryVos = productRelationService.userCollectProduct(page, limit, uid, type);
        return ApiResult.ok(storeProductRelationQueryVos);
    }



}

