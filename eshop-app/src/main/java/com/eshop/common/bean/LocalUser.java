
package com.eshop.common.bean;


import com.eshop.api.ApiCode;
import com.eshop.api.UnAuthenticatedException;
import com.eshop.common.util.JwtToken;
import com.eshop.common.util.RequestUtils;
import com.eshop.modules.user.domain.ShopUser;
import com.auth0.jwt.interfaces.Claim;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 全局user
 * @author zhonghui
 * @date 2020-04-30
 */
public class LocalUser {
    private static ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    public static void set(ShopUser user, Integer scope) {
        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("scope", scope);
        LocalUser.threadLocal.set(map);
    }

    public static void clear() {
        LocalUser.threadLocal.remove();
    }

    public static ShopUser getUser() {
        Map<String, Object> map = LocalUser.threadLocal.get();
        ShopUser user = (ShopUser)map.get("user");
        return user;
    }

    public static Integer getScope() {
        Map<String, Object> map = LocalUser.threadLocal.get();
        Integer scope = (Integer)map.get("scope");
        return scope;
    }

    public static Long getUidByToken(){
        String bearerToken =  RequestUtils.getRequest().getHeader("Authorization");
        if (StringUtils.isEmpty(bearerToken)) {
            return 0L;
        }

        if (!bearerToken.startsWith("Bearer")) {
            return 0L;
        }
        String[] tokens = bearerToken.split(" ");
        if (!(tokens.length == 2)) {
            return 0L;
        }
        String token = tokens[1];

        Optional<Map<String, Claim>> optionalMap = JwtToken.getClaims(token);
        Map<String, Claim> map = optionalMap
                .orElseThrow(() -> new UnAuthenticatedException(ApiCode.UNAUTHORIZED));

        return  map.get("uid").asLong();
    }
}
