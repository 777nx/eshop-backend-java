
package com.eshop.modules.product.rest;

import com.eshop.annotation.AnonymousAccess;
import com.eshop.api.ApiResult;
import com.eshop.modules.category.service.StoreCategoryService;
import com.eshop.utils.CateDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 商品分类前端控制器
 * </p>
 *
 * @author zhonghui
 * @since 2019-10-22
 */
@Slf4j
@RestController
@Api(value = "商品分类", tags = "商城:商品分类")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StoreCategoryController {

    private final StoreCategoryService storeCategoryService;


    /**
     * 商品分类列表
     */
    @AnonymousAccess
    @GetMapping("/category")
    @ApiOperation(value = "商品分类列表",notes = "商品分类列表")
    public ApiResult<List<CateDTO>> getStoreCategoryPageList(){
        return ApiResult.ok(storeCategoryService.getList());
    }

}

