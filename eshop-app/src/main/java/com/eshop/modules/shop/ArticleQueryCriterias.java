package com.eshop.modules.shop;

import com.eshop.annotation.Query;
import com.eshop.modules.mp.service.dto.ArticleQueryCriteria;
import lombok.Data;

@Data
public class ArticleQueryCriterias extends ArticleQueryCriteria {

    @Query(type = Query.Type.INNER_LIKE)
    private String title;
}
