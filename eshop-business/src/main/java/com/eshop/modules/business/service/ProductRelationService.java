package com.eshop.modules.business.service;

import com.eshop.common.service.BaseService;
import com.eshop.modules.business.domain.StoreProductRelation;
import com.eshop.modules.business.vo.StoreProductRelationQueryVo;

import java.util.List;

public interface ProductRelationService extends BaseService<StoreProductRelation> {

    /**
     * 是否收藏
     * @param productId 商品ID
     * @param uid 用户ID
     * @return Boolean
     */
    Boolean isProductRelation(long productId, long uid);

    /**
     *添加收藏
     * @param productId 商品id
     * @param uid 用户id
     */
    void addRroductRelation(long productId,long uid,String category);

    /**
     * 取消收藏
     * @param productId 商品id
     * @param uid 用户id
     */
    void delRroductRelation(long productId,long uid,String category);

    /**
     * 获取用户收藏列表
     * @param page page
     * @param limit limit
     * @param uid 用户id
     * @return list
     */
    List<StoreProductRelationQueryVo> userCollectProduct(int page, int limit, Long uid, String type);



}