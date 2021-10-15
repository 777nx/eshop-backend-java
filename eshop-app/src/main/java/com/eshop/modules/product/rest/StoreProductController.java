
package com.eshop.modules.product.rest;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.eshop.api.ApiResult;
import com.eshop.constant.ShopConstants;
import com.eshop.logging.aop.log.AppLog;
import com.eshop.common.bean.LocalUser;
import com.eshop.common.interceptor.AuthCheck;
import com.eshop.constant.SystemConfigConstants;
import com.eshop.enums.AppFromEnum;
import com.eshop.enums.ProductEnum;
import com.eshop.enums.ShopCommonEnum;
import com.eshop.modules.product.domain.StoreProduct;
import com.eshop.modules.product.param.StoreProductQueryParam;
import com.eshop.modules.product.service.StoreProductRelationService;
import com.eshop.modules.product.service.StoreProductReplyService;
import com.eshop.modules.product.service.StoreProductService;
import com.eshop.modules.product.vo.ProductVo;
import com.eshop.modules.product.vo.ReplyCountVo;
import com.eshop.modules.product.vo.StoreProductQueryVo;
import com.eshop.modules.product.vo.StoreProductReplyQueryVo;
import com.eshop.modules.services.CreatShareProductService;
import com.eshop.modules.shop.domain.SystemAttachment;
import com.eshop.modules.shop.service.SystemAttachmentService;
import com.eshop.modules.shop.service.SystemConfigService;
import com.eshop.modules.user.domain.ShopUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
public class StoreProductController {

    private final StoreProductService storeProductService;
    private final StoreProductRelationService productRelationService;
    private final StoreProductReplyService replyService;
    private final SystemConfigService systemConfigService;
    private final SystemAttachmentService systemAttachmentService;
    private final CreatShareProductService creatShareProductService;
    @Value("${file.path}")
    private String path;


    /**
     * 获取首页更多产品
     */
    @GetMapping("/groom/list/{type}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "类型：1精品推荐，2热门榜单，3首发新品，4促销单品", paramType = "query", dataType = "int")
    })
    @ApiOperation(value = "获取首页更多产品",notes = "获取首页更多产品")
    public ApiResult<Map<String,Object>> moreGoodsList(@PathVariable Integer type){
        Map<String,Object> map = new LinkedHashMap<>();
        // 精品推荐
        if(ProductEnum.TYPE_1.getValue().equals(type)){
            map.put("list",storeProductService.getList(1,20,ProductEnum.TYPE_1.getValue()));
        // 热门榜单
        }else if(type.equals(ProductEnum.TYPE_2.getValue())){
            map.put("list",storeProductService.getList(1,20,ProductEnum.TYPE_2.getValue()));
        // 首发新品
        }else if(type.equals(ProductEnum.TYPE_3.getValue())){
            map.put("list",storeProductService.getList(1,20,ProductEnum.TYPE_3.getValue()));
        // 促销单品
        }else if(type.equals(ProductEnum.TYPE_4.getValue())){
            map.put("list",storeProductService.getList(1,20,ProductEnum.TYPE_4.getValue()));
        }

        return ApiResult.ok(map);
    }

    /**
     * 获取产品列表
     */
    @GetMapping("/products")
    @ApiOperation(value = "商品列表",notes = "商品列表")
    public ApiResult<Map<String, Object>> goodsList(StoreProductQueryParam productQueryParam){
        return ApiResult.ok(storeProductService.getGoodsListByPage(productQueryParam));
    }

    /**
     * 为你推荐
     */
    @GetMapping("/product/hot")
    @ApiOperation(value = "为你推荐",notes = "为你推荐")
    public ApiResult<List<StoreProductQueryVo>> productRecommend(StoreProductQueryParam queryParam){
        return ApiResult.ok(storeProductService.getList(queryParam.getPage(), queryParam.getLimit(),
                ShopCommonEnum.IS_STATUS_1.getValue()));
    }

    /**
     * 商品详情海报
     */
    @AppLog(value = "商品详情海报", type = 1)
    @AuthCheck
    @GetMapping("/product/poster/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "商品ID", paramType = "query", dataType = "int")
    })
    @ApiOperation(value = "商品详情海报",notes = "商品详情海报")
    public ApiResult<String> prodoctPoster(@PathVariable Integer id,@RequestParam(value = "from",defaultValue = "h5") String from) throws IOException, FontFormatException {
        ShopUser userInfo = LocalUser.getUser();

        long uid = userInfo.getUid();

        StoreProduct storeProduct = storeProductService.getById(id);
        // 海报
        String siteUrl = systemConfigService.getData(SystemConfigConstants.SITE_URL);
        if(StrUtil.isEmpty(siteUrl)){
            return ApiResult.fail("未配置h5地址");
        }
        String apiUrl = systemConfigService.getData(SystemConfigConstants.API_URL);
        if(StrUtil.isEmpty(apiUrl)){
            return ApiResult.fail("未配置api地址");
        }
        String name = id+"_"+uid + "_"+from+"_product_detail_wap.jpg";
        SystemAttachment attachment = systemAttachmentService.getInfo(name);
        String sepa = File.separator;
        String fileDir = path+"qrcode"+ sepa;
        String qrcodeUrl = "";
        if(ObjectUtil.isNull(attachment)){
            File file = FileUtil.mkdir(new File(fileDir));
            //如果类型是小程序
            if(AppFromEnum.ROUNTINE.getValue().equals(from)){
                siteUrl = siteUrl+"/product/";
                //生成二维码
                QrCodeUtil.generate(siteUrl+"?id="+id+"&spread="+uid+"&pageType=good&codeType="+AppFromEnum.ROUNTINE.getValue(), 180, 180,
                        FileUtil.file(fileDir+name));
            }
            else if(AppFromEnum.APP.getValue().equals(from)){
                siteUrl = siteUrl+"/product/";
                //生成二维码
                QrCodeUtil.generate(siteUrl+"?id="+id+"&spread="+uid+"&pageType=good&codeType="+AppFromEnum.APP.getValue(), 180, 180,
                        FileUtil.file(fileDir+name));
            //如果类型是h5
            }else if(AppFromEnum.H5.getValue().equals(from)){
                //生成二维码
                QrCodeUtil.generate(siteUrl+"/detail/"+id+"?spread="+uid, 180, 180,
                        FileUtil.file(fileDir+name));
            }else {
                //生成二维码
                String uniUrl = systemConfigService.getData(SystemConfigConstants.UNI_SITE_URL);
                siteUrl =  StrUtil.isNotBlank(uniUrl) ? uniUrl :  ShopConstants.DEFAULT_UNI_H5_URL;
                QrCodeUtil.generate(siteUrl+"/pages/shop/GoodsCon/index?id="+id+"&spread="+uid, 180, 180,
                        FileUtil.file(fileDir+name));
            }
            systemAttachmentService.attachmentAdd(name,String.valueOf(FileUtil.size(file)),
                    fileDir+name,"qrcode/"+name);

            qrcodeUrl = apiUrl + "/api/file/qrcode/"+name;
        }else{
            qrcodeUrl = apiUrl + "/api/file/" + attachment.getSattDir();
        }
        String spreadPicName = id+"_"+uid + "_"+from+"_product_user_spread.jpg";
        String spreadPicPath = fileDir+spreadPicName;
        String rr =  creatShareProductService.creatProductPic(storeProduct,qrcodeUrl,
                spreadPicName,spreadPicPath,apiUrl);
        return ApiResult.ok(rr);
    }



    /**
     * 普通商品详情
     */
    //@AppLog(value = "普通商品详情", type = 1)
    //@AuthCheck
    @GetMapping("/product/detail/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "商品ID", paramType = "query", dataType = "long",required = true),
            @ApiImplicitParam(name = "latitude", value = "纬度", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "longitude", value = "经度", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "from", value = "来自:", paramType = "query", dataType = "string")
    })
    @ApiOperation(value = "普通商品详情",notes = "普通商品详情")
    public ApiResult<ProductVo> detail(@PathVariable long id,
                                       @RequestParam(value = "",required=false) String latitude,
                                       @RequestParam(value = "",required=false) String longitude,
                                       @RequestParam(value = "",required=false) String from)  {
        long uid = LocalUser.getUidByToken();
        storeProductService.incBrowseNum(id);
        ProductVo productDTO = storeProductService.goodsDetail(id,uid,latitude,longitude);
        return ApiResult.ok(productDTO);
    }

    /**
     * 获取产品评论
     */
    @GetMapping("/reply/list/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "商品ID", paramType = "query", dataType = "long",required = true),
            @ApiImplicitParam(name = "type", value = "评论分数类型", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "page", value = "页码,默认为1", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "limit", value = "页大小,默认为10", paramType = "query", dataType = "int")
    })
    @ApiOperation(value = "获取产品评论",notes = "获取产品评论")
    public ApiResult<List<StoreProductReplyQueryVo>> replyList(@PathVariable Long id,
                                                               @RequestParam(value = "type",defaultValue = "0") int type,
                                                               @RequestParam(value = "page",defaultValue = "1") int page,
                                                               @RequestParam(value = "limit",defaultValue = "10") int limit){
        return ApiResult.ok(replyService.getReplyList(id,type, page,limit));
    }

    /**
     * 获取产品评论数据
     */
    @GetMapping("/reply/config/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "商品ID", paramType = "query", dataType = "int")
    })
    @ApiOperation(value = "获取产品评论数据",notes = "获取产品评论数据")
    public ApiResult<ReplyCountVo> replyCount(@PathVariable Integer id){
        return ApiResult.ok(replyService.getReplyCount(id));
    }



}

