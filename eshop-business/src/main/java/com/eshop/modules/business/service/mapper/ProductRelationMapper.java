package com.eshop.modules.business.service.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eshop.common.mapper.CoreMapper;
import com.eshop.modules.business.domain.StoreProductRelation;
import com.eshop.modules.business.vo.StoreProductRelationQueryVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ProductRelationMapper extends CoreMapper<StoreProductRelation> {

    @Select("select B.id pid,A.type as category,B.store_name as storeName,B.price,B.is_integral as isIntegral," +
            "B.ot_price as otPrice,B.sales,B.image,B.is_show as isShow,B.integral as integral" +
            " from store_product_relation A left join store_product B " +
            "on A.product_id = B.id where A.type=#{type} and A.uid=#{uid} and A.is_del = 0 and B.is_del = 0 order by A.create_time desc")
    List<StoreProductRelationQueryVo> selectRelationList(Page page, @Param("uid") Long uid, @Param("type") String type);




}
