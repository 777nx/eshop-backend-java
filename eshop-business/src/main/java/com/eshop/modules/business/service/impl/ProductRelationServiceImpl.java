package com.eshop.modules.business.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eshop.api.EshopException;
import com.eshop.common.service.impl.BaseServiceImpl;
import com.eshop.dozer.service.IGenerator;
import com.eshop.modules.business.domain.StoreProductRelation;
import com.eshop.modules.business.service.ProductRelationService;
import com.eshop.modules.business.service.mapper.ProductRelationMapper;
import com.eshop.modules.business.vo.StoreProductRelationQueryVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ProductRelationServiceImpl extends BaseServiceImpl<ProductRelationMapper, StoreProductRelation> implements ProductRelationService {

    private final ProductRelationMapper productRelationMapper;
    private final IGenerator generator;

    /**
     * 获取用户收藏列表
     * @param page page
     * @param limit limit
     * @param uid 用户id
     * @return list
     */
    @Override
    public List<StoreProductRelationQueryVo> userCollectProduct(int page, int limit, Long uid, String type) {
        Page<StoreProductRelation> pageModel = new Page<>(page, limit);
        List<StoreProductRelationQueryVo> list = productRelationMapper.selectRelationList(pageModel,uid,type);
        return list;
    }

    /**
     * 添加收藏
     * @param productId 商品id
     * @param uid 用户id
     */
    @Override
    public void addRroductRelation(long productId,long uid,String category) {
        if(isProductRelation(productId,uid)) {
            throw new EshopException("已收藏");
        }
        StoreProductRelation storeProductRelation = StoreProductRelation.builder()
                .productId(productId)
                .uid(uid)
                .type(category)
                .build();
        productRelationMapper.insert(storeProductRelation);
    }

    /**
     * 取消收藏
     * @param productId 商品id
     * @param uid 用户id
     */
    @Override
    public void delRroductRelation(long productId,long uid,String category) {
        StoreProductRelation productRelation = this.lambdaQuery()
                .eq(StoreProductRelation::getProductId,productId)
                .eq(StoreProductRelation::getUid,uid)
                .eq(StoreProductRelation::getType,category)
                .one();
        if(productRelation == null) {
            throw new EshopException("已取消");
        }
        this.removeById(productRelation.getId());
    }


    /**
     * 是否收藏
     * @param productId 商品ID
     * @param uid 用户ID
     * @return Boolean
     */
    @Override
    public Boolean isProductRelation(long productId, long uid) {
        int count = productRelationMapper
                .selectCount(Wrappers.<StoreProductRelation>lambdaQuery()
                        .eq(StoreProductRelation::getUid,uid)
                        .eq(StoreProductRelation::getType,"collect")
                        .eq(StoreProductRelation::getProductId,productId));
        if(count > 0) {
            return true;
        }

        return false;
    }


}
