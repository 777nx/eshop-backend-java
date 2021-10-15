package com.eshop.modules.shop.vo;

import com.eshop.modules.activity.vo.StoreCombinationQueryVo;
import com.eshop.modules.activity.vo.StoreSeckillQueryVo;
import com.eshop.modules.product.vo.StoreProductQueryVo;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("首页数据")
public class IndexVo {

    @ApiModelProperty("banner")
    private List<JSONObject> banner;
    //首页按钮
    @ApiModelProperty("首页按钮")
    private List<JSONObject> menus;
    //精品推荐->拼团
    @ApiModelProperty("精品推荐")
    private List<StoreProductQueryVo> bastList;
    //首发新品->秒杀
    @ApiModelProperty("首发新品")
    private List<StoreProductQueryVo> firstList;
    //猜你喜欢
    @ApiModelProperty("猜你喜欢")
    private List<StoreProductQueryVo> benefit;
    //热门榜单
    @ApiModelProperty("热门榜单")
    private List<StoreProductQueryVo> likeInfo;
    //滚动
    @ApiModelProperty("滚动")
    private List<JSONObject> roll;
    //精品推荐->拼团
    @ApiModelProperty("精品推荐->拼团")
    private List<StoreCombinationQueryVo> combinationList;
    //首发新品->秒杀
    @ApiModelProperty("首发新品->秒杀")
    private List<StoreSeckillQueryVo> seckillList;


}
