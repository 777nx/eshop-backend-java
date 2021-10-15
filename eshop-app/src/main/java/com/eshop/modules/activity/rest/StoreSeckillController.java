
package com.eshop.modules.activity.rest;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.eshop.api.ApiResult;
import com.eshop.api.EshopException;
import com.eshop.logging.aop.log.AppLog;
import com.eshop.common.bean.LocalUser;
import com.eshop.common.interceptor.AuthCheck;
import com.eshop.constant.ShopConstants;
import com.eshop.modules.activity.service.StoreSeckillService;
import com.eshop.modules.activity.service.dto.SeckillTimeDto;
import com.eshop.modules.activity.vo.SeckillConfigVo;
import com.eshop.modules.activity.vo.StoreSeckillVo;
import com.eshop.modules.activity.vo.StoreSeckillQueryVo;
import com.eshop.modules.product.service.StoreProductRelationService;
import com.eshop.modules.shop.domain.SystemGroupData;
import com.eshop.modules.shop.service.SystemGroupDataService;
import com.eshop.modules.shop.service.dto.SystemGroupDataQueryCriteria;
import com.eshop.utils.OrderUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 商品秒杀产品前端控制器
 * </p>
 *
 * @author zhonghui
 * @since 2019-12-14
 */
@Slf4j
@RestController
@RequestMapping
@Api(value = "商品秒杀", tags = "营销:商品秒杀")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StoreSeckillController {

    private final StoreSeckillService storeSeckillService;
    private final SystemGroupDataService systemGroupDataService;
    private final StoreProductRelationService relationService;

    /**
     * 秒杀产品列表
     */
    @GetMapping("/seckill/list/{time}")
    @ApiOperation(value = "秒杀产品列表", notes = "秒杀产品列表")
    public ApiResult<List<StoreSeckillQueryVo>> getStoreSeckillPageList(@PathVariable String time,
                                                                          @RequestParam(value = "page",defaultValue = "1") int page,
                                                                          @RequestParam(value = "limit",defaultValue = "10") int limit){
        if (StrUtil.isBlank(time) || !NumberUtil.isNumber(time)){
            throw new EshopException("参数错误");
        }
        return ApiResult.ok(storeSeckillService.getList(page, limit, Integer.valueOf(time)));
    }


    /**
     * 根据id获取商品秒杀产品详情
     */
    @AppLog(value = "根据id获取商品秒杀产品详情", type = 1)
    @AuthCheck
    @GetMapping("/seckill/detail/{id}")
    @ApiOperation(value = "秒杀产品详情", notes = "秒杀产品详情")
    public ApiResult<StoreSeckillVo> getStoreSeckill(@PathVariable Long id){
        Long uid = LocalUser.getUser().getUid();
        StoreSeckillVo storeSeckillVo = storeSeckillService.getDetail(id);
        storeSeckillVo.setUserCollect(relationService
                .isProductRelation(storeSeckillVo.getStoreInfo().getProductId(),uid));
        return ApiResult.ok(storeSeckillVo);
    }


    /**
     * 秒杀产品时间区间
     */
    @GetMapping("/seckill/index")
    @ApiOperation(value = "秒杀产品时间区间", notes = "秒杀产品时间区间")
    public ApiResult<SeckillConfigVo> getStoreSeckillIndex() {
        //获取秒杀配置
        AtomicInteger seckillTimeIndex = new AtomicInteger();
        SeckillConfigVo seckillConfigVo = new SeckillConfigVo();

        SystemGroupDataQueryCriteria queryCriteria = new SystemGroupDataQueryCriteria();
        queryCriteria.setGroupName(ShopConstants.YSHOP_SECKILL_TIME);
        List<SystemGroupData> systemGroupDataList = systemGroupDataService.queryAll(queryCriteria);

        List<SeckillTimeDto> list = new ArrayList<>();
        int today = OrderUtil.dateToTimestampT(DateUtil.beginOfDay(new Date()));
        systemGroupDataList.forEach(i -> {
            String jsonStr = i.getValue();
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            int time = Integer.valueOf(jsonObject.get("time").toString());//时间 5
            int continued = Integer.valueOf(jsonObject.get("continued").toString());//活动持续事件  3
            SimpleDateFormat sdf = new SimpleDateFormat("HH");
            String nowTime = sdf.format(new Date());
            String index = nowTime.substring(0, 1);
            int currentHour = "0".equals(index) ? Integer.valueOf(nowTime.substring(1, 2)) : Integer.valueOf(nowTime);
            SeckillTimeDto seckillTimeDto = new SeckillTimeDto();
            seckillTimeDto.setId(i.getId());
            //活动结束时间
            int activityEndHour = time + continued;
            if (activityEndHour > 24) {
                seckillTimeDto.setState("即将开始");
                seckillTimeDto.setTime(jsonObject.get("time").toString().length() > 1 ? jsonObject.get("time").toString() + ":00" : "0" + jsonObject.get("time").toString() + ":00");
                seckillTimeDto.setStatus(2);
                seckillTimeDto.setStop(today + activityEndHour * 3600);
            } else {
                if (currentHour >= time && currentHour < activityEndHour) {
                    seckillTimeDto.setState("抢购中");
                    seckillTimeDto.setTime(jsonObject.get("time").toString().length() > 1 ? jsonObject.get("time").toString() + ":00" : "0" + jsonObject.get("time").toString() + ":00");
                    seckillTimeDto.setStatus(1);
                    seckillTimeDto.setStop(today + activityEndHour * 3600);
                    seckillTimeIndex.set(systemGroupDataList.indexOf(i));
                } else if (currentHour < time) {
                    seckillTimeDto.setState("即将开始");
                    seckillTimeDto.setTime(jsonObject.get("time").toString().length() > 1 ? jsonObject.get("time").toString() + ":00" : "0" + jsonObject.get("time").toString() + ":00");
                    seckillTimeDto.setStatus(2);
                    seckillTimeDto.setStop(OrderUtil.dateToTimestamp(new Date()) + activityEndHour * 3600);
                } else if (currentHour >= activityEndHour) {
                    seckillTimeDto.setState("已结束");
                    seckillTimeDto.setTime(jsonObject.get("time").toString().length() > 1 ? jsonObject.get("time").toString() + ":00" : "0" + jsonObject.get("time").toString() + ":00");
                    seckillTimeDto.setStatus(0);
                    seckillTimeDto.setStop(today + activityEndHour * 3600);
                }
            }
            list.add(seckillTimeDto);
        });
        seckillConfigVo.setSeckillTimeIndex(seckillTimeIndex.get());
        seckillConfigVo.setSeckillTime(list);
        return ApiResult.ok(seckillConfigVo);
    }
}

