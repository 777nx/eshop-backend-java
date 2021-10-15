
package com.eshop.modules.services;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.eshop.api.EshopException;
import com.eshop.common.util.IpUtil;
import com.eshop.constant.ShopConstants;
import com.eshop.enums.AppFromEnum;
import com.eshop.modules.auth.param.LoginParam;
import com.eshop.modules.auth.param.RegParam;
import com.eshop.modules.shop.domain.SystemAttachment;
import com.eshop.modules.shop.service.SystemAttachmentService;
import com.eshop.modules.user.domain.ShopUser;
import com.eshop.modules.user.service.UserService;
import com.eshop.modules.user.service.dto.WechatUserDto;
import com.eshop.modules.user.vo.OnlineUser;
import com.eshop.modules.mp.config.WxMpConfiguration;
import com.eshop.modules.mp.config.WxMaConfiguration;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eshop.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @ClassName 登陆认证服务类
 * @Author zhonghui
 * @Date 2020/6/14
 **/
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthService {

    private final UserService userService;
    private final RedisUtils redisUtils;
    private static Integer expiredTimeIn;
    private final SystemAttachmentService systemAttachmentService;

    @Value("${eshop.security.token-expired-in}")
    public void setExpiredTimeIn(Integer expiredTimeIn) {
        AuthService.expiredTimeIn = expiredTimeIn;
    }




    /**
     * 小程序登陆
     * @param loginParam loginParam
     * @return long
     */
    @Transactional(rollbackFor = Exception.class)
    public ShopUser wxappLogin(LoginParam loginParam) {
        String code = loginParam.getCode();
        String encryptedData = loginParam.getEncryptedData();
        String iv = loginParam.getIv();
        String spread = loginParam.getSpread();
        try {
            //读取redis配置
            String appId = redisUtils.getY(ShopKeyUtils.getWxAppAppId());
            String secret = redisUtils.getY(ShopKeyUtils.getWxAppSecret());
            if (StrUtil.isBlank(appId) || StrUtil.isBlank(secret)) {
                throw new EshopException("请先配置小程序");
            }
            WxMaService wxMaService = WxMaConfiguration.getWxMaService();
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);

            WxMaUserInfo wxMpUser = wxMaService.getUserService()
                    .getUserInfo(session.getSessionKey(), encryptedData, iv);
            String openid = session.getOpenid();
            //如果开启了UnionId
            if (StrUtil.isNotBlank(session.getUnionid())) {
                openid = session.getUnionid();
            }

            ShopUser shopUser = userService.getOne(Wrappers.<ShopUser>lambdaQuery()
                    .eq(ShopUser::getUsername, openid),false);

            if (ObjectUtil.isNull(shopUser)) {


                //过滤掉表情
                String ip = IpUtil.getRequestIp();
                shopUser = ShopUser.builder()
                        .username(openid)
                        .nickname(wxMpUser.getNickName())
                        .avatar(wxMpUser.getAvatarUrl())
                        .addIp(ip)
                        .lastIp(ip)
                        .userType(AppFromEnum.ROUNTINE.getValue())
                        .build();

                //构建微信用户
                WechatUserDto wechatUserDTO = WechatUserDto.builder()
                        .nickname(wxMpUser.getNickName())
                        .routineOpenid(session.getOpenid())
                        .unionId(session.getUnionid())
                        .sex(Integer.valueOf(wxMpUser.getGender()))
                        .language(wxMpUser.getLanguage())
                        .city(wxMpUser.getCity())
                        .province(wxMpUser.getProvince())
                        .country(wxMpUser.getCountry())
                        .headimgurl(wxMpUser.getAvatarUrl())
                        .build();

                shopUser.setWxProfile(wechatUserDTO);

                userService.save(shopUser);

            } else {
                WechatUserDto wechatUser = shopUser.getWxProfile();
                if ((StrUtil.isBlank(wechatUser.getRoutineOpenid()) && StrUtil.isNotBlank(wxMpUser.getOpenId()))
                        || (StrUtil.isBlank(wechatUser.getUnionId()) && StrUtil.isNotBlank(wxMpUser.getUnionId()))) {
                    wechatUser.setRoutineOpenid(wxMpUser.getOpenId());
                    wechatUser.setUnionId(wxMpUser.getUnionId());

                    shopUser.setWxProfile(wechatUser);

                }
                shopUser.setUserType(AppFromEnum.ROUNTINE.getValue());
                userService.updateById(shopUser);
            }
            userService.setSpread(spread, shopUser.getUid());
            redisUtils.set(ShopConstants.YSHOP_MINI_SESSION_KET + shopUser.getUid(), session.getSessionKey());
            return shopUser;
        } catch (WxErrorException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new EshopException(e.toString());
        }


    }

    /**
     * 公众号登陆
     * @param code code码
     * @param spread 上级用户
     * @return uid
     */
    @Transactional(rollbackFor = Exception.class)
    public ShopUser wechatLogin(String code, String spread){
        try {
            WxMpService wxService = WxMpConfiguration.getWxMpService();
            WxOAuth2AccessToken wxOAuth2AccessToken = wxService.getOAuth2Service().getAccessToken(code);
            WxOAuth2UserInfo wxOAuth2UserInfo = wxService.getOAuth2Service().getUserInfo(wxOAuth2AccessToken, null);
            WxMpUser wxMpUser = wxService.getUserService().userInfo(wxOAuth2UserInfo.getOpenid());
            String openid = wxMpUser.getOpenId();

            //如果开启了UnionId
            if (StrUtil.isNotBlank(wxMpUser.getUnionId())) {
                openid = wxMpUser.getUnionId();
            }

            ShopUser shopUser = userService.getOne(Wrappers.<ShopUser>lambdaQuery()
                    .eq(ShopUser::getUsername,openid),false);

            //long uid = 0;
            ShopUser returnUser = null;
            if(shopUser == null){
                //过滤掉表情
                String nickname = wxMpUser.getNickname();
                log.info("昵称：{}", nickname);
                //用户保存
                String ip = IpUtil.getRequestIp();
                ShopUser user = ShopUser.builder()
                        .username(openid)
                        .nickname(nickname)
                        .avatar(wxMpUser.getHeadImgUrl())
                        .addIp(ip)
                        .lastIp(ip)
                        .userType(AppFromEnum.WECHAT.getValue())
                        .build();

                //构建微信用户
                WechatUserDto wechatUserDTO = WechatUserDto.builder()
                        .nickname(nickname)
                        .openid(wxMpUser.getOpenId())
                        .unionId(wxMpUser.getUnionId())
                        .sex(wxMpUser.getSex())
                        .language(wxMpUser.getLanguage())
                        .city(wxMpUser.getCity())
                        .province(wxMpUser.getProvince())
                        .country(wxMpUser.getCountry())
                        .headimgurl(wxMpUser.getHeadImgUrl())
                        .subscribe(wxMpUser.getSubscribe())
                        .subscribeTime(wxMpUser.getSubscribeTime())
                        .build();

                user.setWxProfile(wechatUserDTO);
                userService.save(user);

                returnUser = user;
            }else{
                returnUser = shopUser;
                WechatUserDto wechatUser = shopUser.getWxProfile();
                if((StrUtil.isBlank(wechatUser.getOpenid()) && StrUtil.isNotBlank(wxMpUser.getOpenId()))
                        || (StrUtil.isBlank(wechatUser.getUnionId()) && StrUtil.isNotBlank(wxMpUser.getUnionId()))){
                    wechatUser.setOpenid(wxMpUser.getOpenId());
                    wechatUser.setUnionId(wxMpUser.getUnionId());

                    shopUser.setWxProfile(wechatUser);
                }

                shopUser.setUserType(AppFromEnum.WECHAT.getValue());
                userService.updateById(shopUser);

            }

            userService.setSpread(spread,returnUser.getUid());

            log.error("spread:{}",spread);

            return returnUser;

        } catch (WxErrorException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new EshopException(e.toString());
        }
    }


    /**
     * 注册
     * @param param RegDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(RegParam param){

        String account = param.getAccount();
        String ip = IpUtil.getRequestIp();
        ShopUser user = ShopUser.builder()
                .username(account)
                .nickname(param.getNickname())
                .password(SecureUtil.md5(param.getPassword()))
                .phone(account)
                .avatar(ShopConstants.YSHOP_DEFAULT_AVATAR)
                .addIp(ip)
                .lastIp(ip)
                .userType(AppFromEnum.H5.getValue())
                .build();

        userService.save(user);

        //设置推广关系
        if (StrUtil.isNotBlank(param.getInviteCode())) {
            SystemAttachment systemAttachment = systemAttachmentService.getByCode(param.getInviteCode());
            if(systemAttachment != null){
                userService.setSpread(String.valueOf(systemAttachment.getUid()),
                        user.getUid());
            }
        }

    }


    /**
     * 保存在线用户信息
     * @param shopUser /
     * @param token /
     * @param request /
     */
    public void save(ShopUser shopUser, String token, HttpServletRequest request){
        String job = "yshop开发工程师";
        String ip = StringUtils.getIp(request);
        String browser = StringUtils.getBrowser(request);
        String address = StringUtils.getCityInfo(ip);
        OnlineUser onlineUser = null;
        try {
            onlineUser = new OnlineUser(shopUser.getUsername(), shopUser.getNickname(), job, browser ,
                    ip, address, EncryptUtils.desEncrypt(token), new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
        redisUtils.set(ShopConstants.YSHOP_APP_LOGIN_USER +onlineUser.getUserName() + ":" + token, onlineUser, AuthService.expiredTimeIn);
    }

    /**
     * 检测用户是否在之前已经登录，已经登录踢下线
     *
     * @param userName 用户名
     */
    public void checkLoginOnUser(String userName, String igoreToken) {
        List<OnlineUser> onlineUsers = this.getAll(userName);
        if (onlineUsers == null || onlineUsers.isEmpty()) {
            return;
        }
        for (OnlineUser onlineUser : onlineUsers) {
            try {
                String token = EncryptUtils.desDecrypt(onlineUser.getKey());
                if (StringUtils.isNotBlank(igoreToken) && !igoreToken.equals(token)) {
                    this.kickOut(userName, onlineUser.getKey());
                } else if (StringUtils.isBlank(igoreToken)) {
                    this.kickOut(userName, onlineUser.getKey());
                }
            } catch (Exception e) {
                log.error("checkUser is error", e);
            }
        }
    }

    /**
     * 踢出用户
     *
     * @param key /
     */
    public void kickOut(String userName, String key) throws Exception {
        key = ShopConstants.YSHOP_APP_LOGIN_USER + userName + ":" + EncryptUtils.desDecrypt(key);
        redisUtils.del(key);
    }

    /**
     * 退出登录
     * @param token /
     */
    public void logout(String userName,String token) {
        String key = ShopConstants.YSHOP_APP_LOGIN_USER+ userName + ":" + token;
        redisUtils.del(key);
    }

    /**
     * 查询全部数据，不分页
     *
     * @param uName /
     * @return /
     */
    private List<OnlineUser> getAll(String uName) {
        List<String> keys = null;
        keys = redisUtils.scan(ShopConstants.YSHOP_APP_LOGIN_USER + uName + ":" + "*");

        Collections.reverse(keys);
        List<OnlineUser> onlineUsers = new ArrayList<>();
        for (String key : keys) {
            OnlineUser onlineUser = (OnlineUser) redisUtils.get(key);
            onlineUsers.add(onlineUser);
        }
        onlineUsers.sort((o1, o2) -> o2.getLoginTime().compareTo(o1.getLoginTime()));
        return onlineUsers;
    }



}
