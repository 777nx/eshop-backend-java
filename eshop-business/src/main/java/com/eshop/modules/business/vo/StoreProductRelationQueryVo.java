package com.eshop.modules.business.vo;


import com.eshop.serializer.DoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel(value = "StoreProductRelationQueryVo对象", description = "商品点赞和收藏表查询参数")
public class StoreProductRelationQueryVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @ApiModelProperty(value = "用户ID")
    private Long uid;

    @ApiModelProperty(value = "商品ID")
    private Long productId;

    @ApiModelProperty(value = "类型(收藏(collect）、点赞(like))")
    private String type;

    @ApiModelProperty(value = "某种类型的商品(普通商品、秒杀商品)")
    private String category;

    @ApiModelProperty(value = "添加时间")
    private Date createTime;

    @ApiModelProperty(value = "产品图片")
    private String image;

    @ApiModelProperty(value = "是否显示")
    private Integer isShow;

    @ApiModelProperty(value = "原价")
    @JsonSerialize(using = DoubleSerializer.class)
    private Double otPrice;

    @ApiModelProperty(value = "父ID")
    private Integer pid;

    @ApiModelProperty(value = "产品价格")
    @JsonSerialize(using = DoubleSerializer.class)
    private Double price;

    @ApiModelProperty(value = "产品销量")
    private Integer sales;

    @ApiModelProperty(value = "商品名称")
    private String storeName;

    @ApiModelProperty(value = "是否开启积分兑换")
    private Integer isIntegral;

    @ApiModelProperty(value = "积分")
    private Integer integral;

}
