
package com.eshop.modules.shop.rest;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.resource.ClassPathResource;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eshop.api.ApiResult;
import com.eshop.api.EshopException;
import com.eshop.constant.ShopConstants;
import com.eshop.enums.ProductEnum;
import com.eshop.modules.activity.service.StoreCombinationService;
import com.eshop.modules.activity.service.StoreSeckillService;
import com.eshop.modules.activity.vo.StoreCombinationQueryVo;
import com.eshop.modules.canvas.domain.StoreCanvas;
import com.eshop.modules.canvas.service.StoreCanvasService;
import com.eshop.modules.mp.service.WechatLiveService;
import com.eshop.modules.product.service.StoreProductService;
import com.eshop.modules.product.vo.StoreProductQueryVo;
import com.eshop.modules.shop.service.SystemGroupDataService;
import com.eshop.modules.shop.vo.IndexVo;
import com.eshop.utils.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName IndexController
 * @Author zhonghui
 * @Date 2019/10/19
 **/
@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(value = "首页模块", tags = "商城:首页模块")
public class IndexController {
    private final SystemGroupDataService systemGroupDataService;
    private final StoreProductService storeProductService;
    private final StoreCombinationService storeCombinationService;
    private final StoreSeckillService storeSeckillService;
    private final WechatLiveService wechatLiveService;

    private final StoreCanvasService storeCanvasService;


    @GetMapping("/getCanvas")
    @ApiOperation(value = "读取画布数据")
    public ResponseEntity<StoreCanvas> getCanvas(StoreCanvas storeCanvas){
        StoreCanvas canvas = storeCanvasService.getOne(new LambdaQueryWrapper<StoreCanvas>()
                .eq(StoreCanvas::getTerminal, storeCanvas.getTerminal())
                .orderByDesc(StoreCanvas::getCanvasId).last("limit 1"));
        return new ResponseEntity<>(canvas, HttpStatus.OK);
    }

    @Cacheable(cacheNames = ShopConstants.YSHOP_REDIS_INDEX_KEY)
    @GetMapping("/index")
    @ApiOperation(value = "首页数据",notes = "首页数据")
    public ApiResult<IndexVo> index(){
        IndexVo indexVo = IndexVo.builder()
                .banner(systemGroupDataService.getDatas(ShopConstants.YSHOP_HOME_BANNER))//
                .bastList(storeProductService.getList(1,6, ProductEnum.TYPE_1.getValue()))//
                .benefit(storeProductService.getList(1,10,ProductEnum.TYPE_4.getValue()))//
                .combinationList(storeCombinationService.getList(1,8).getStoreCombinationQueryVos())//
                .firstList(storeProductService.getList(1,6,ProductEnum.TYPE_3.getValue()))
                .likeInfo(storeProductService.getList(1,8,ProductEnum.TYPE_2.getValue()))
                .menus(systemGroupDataService.getDatas(ShopConstants.YSHOP_HOME_MENUS))//
                .roll(systemGroupDataService.getDatas(ShopConstants.YSHOP_HOME_ROLL_NEWS))
                .seckillList(storeSeckillService.getList(1, 4))
                .build();
        return ApiResult.ok(indexVo);
    }

    @Cacheable(cacheNames = ShopConstants.YSHOP_REDIS_INDEX_KEY)
    @GetMapping("/index/banner")
    @ApiOperation(value = "轮播图",notes = "轮播图")
    public ApiResult<List<JSONObject>> indexBanner(){
        return ApiResult.ok(systemGroupDataService.getDatas(ShopConstants.YSHOP_HOME_BANNER));
    }

    @Cacheable(cacheNames = ShopConstants.YSHOP_REDIS_INDEX_KEY)
    @GetMapping("/index/bastList")
    @ApiOperation(value = "精品推荐",notes = "精品推荐")
    public ApiResult<List<StoreProductQueryVo>> indexBastList(){
        return ApiResult.ok(storeProductService.getList(1,6, ProductEnum.TYPE_1.getValue()));
    }
    @Cacheable(cacheNames = ShopConstants.YSHOP_REDIS_INDEX_KEY)
    @GetMapping("/index/like")
    @ApiOperation(value = "猜你喜欢",notes = "猜你喜欢")
    public ApiResult<List<StoreProductQueryVo>> indexBenefit(){
        return ApiResult.ok(storeProductService.getList(1,10,ProductEnum.TYPE_4.getValue()));
    }
    @Cacheable(cacheNames = ShopConstants.YSHOP_REDIS_INDEX_KEY)
    @GetMapping("/index/combinationList")
    @ApiOperation(value = "拼团",notes = "拼团")
    public ApiResult<List<StoreCombinationQueryVo>> indexCombinationList(){
        return ApiResult.ok(storeCombinationService.getList(1,8).getStoreCombinationQueryVos());
    }

    @Cacheable(cacheNames = ShopConstants.YSHOP_REDIS_INDEX_KEY)
    @GetMapping("/index/menus")
    @ApiOperation(value = "首页菜单",notes = "首页菜单")
    public ApiResult<List<JSONObject>> indexMenus(){
        return ApiResult.ok(systemGroupDataService.getDatas(ShopConstants.YSHOP_HOME_MENUS));
    }

    @GetMapping("/search/keyword")
    @ApiOperation(value = "热门搜索关键字获取",notes = "热门搜索关键字获取")
    public ApiResult<List<String>> search(){
        List<JSONObject> list = systemGroupDataService.getDatas(ShopConstants.YSHOP_HOT_SEARCH);
        List<String>  stringList = new ArrayList<>();
        for (JSONObject object : list) {
            stringList.add(object.getString("title"));
        }
        return ApiResult.ok(stringList);
    }


    @GetMapping("/citys")
    @ApiOperation(value = "获取城市json",notes = "获取城市json")
    public ApiResult<JSONObject> cityJson(){
        String path = "city.json";
        String name = "city.json";
        try {
            File file = FileUtil.inputStreamToFile(new ClassPathResource(path).getStream(), name);
            FileReader fileReader = new FileReader(file,"UTF-8");
            String string = fileReader.readString();
            JSONObject jsonObject = JSON.parseObject(string);
            return ApiResult.ok(jsonObject);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new EshopException("无数据");
        }

    }


}
