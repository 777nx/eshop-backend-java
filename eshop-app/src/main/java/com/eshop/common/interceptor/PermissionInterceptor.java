
package com.eshop.common.interceptor;


import com.eshop.api.ApiCode;
import com.eshop.api.UnAuthenticatedException;
import com.eshop.common.bean.LocalUser;
import com.eshop.common.util.JwtToken;
import com.eshop.constant.ShopConstants;
import com.eshop.modules.user.domain.ShopUser;
import com.eshop.modules.user.service.UserService;
import com.eshop.utils.RedisUtils;
import com.auth0.jwt.interfaces.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

/**
 * 权限拦截器
 * @author zhonghui
 * @date 2020-04-30
 */
public class PermissionInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtils redisUtils;

    public PermissionInterceptor() {
        super();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Optional<AuthCheck> authCheck = this.getAuthCheck(handler);
        if (!authCheck.isPresent()) {
            return true;
        }

        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.isEmpty(bearerToken)) {
            throw new UnAuthenticatedException(ApiCode.UNAUTHORIZED);
        }

        if (!bearerToken.startsWith("Bearer")) {
            throw new UnAuthenticatedException(ApiCode.UNAUTHORIZED);
        }
        String[] tokens = bearerToken.split(" ");
        if (!(tokens.length == 2)) {
            throw new UnAuthenticatedException(ApiCode.UNAUTHORIZED);
        }
        String token = tokens[1];

        Optional<Map<String, Claim>> optionalMap = JwtToken.getClaims(token);
        Map<String, Claim> map = optionalMap
                .orElseThrow(() -> new UnAuthenticatedException(ApiCode.UNAUTHORIZED));

        String uName = map.get("uName").asString();

        //检测用户是否被踢出
        if (redisUtils.get(ShopConstants.YSHOP_APP_LOGIN_USER + uName + ":" + token) == null) {
            throw new UnAuthenticatedException(ApiCode.UNAUTHORIZED);
        }
        boolean valid = this.hasPermission(authCheck.get(), map);
        if(valid){
            this.setToThreadLocal(map);
        }
        return valid;
    }

    private void setToThreadLocal(Map<String,Claim> map) {
        Integer uid = map.get("uid").asInt();
        Integer scope = map.get("scope").asInt();
        ShopUser user = userService.getById(uid);
        if(user == null) {
            throw new UnAuthenticatedException(ApiCode.NOT_PERMISSION);
        }
        LocalUser.set(user, scope);
    }

    private boolean hasPermission(AuthCheck authCheck, Map<String, Claim> map) {
        Integer level = authCheck.value();
        Integer scope = map.get("scope").asInt();
        if (level > scope) {
            throw new UnAuthenticatedException(ApiCode.NOT_PERMISSION);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LocalUser.clear();
        super.afterCompletion(request, response, handler, ex);
    }

    private Optional<AuthCheck> getAuthCheck(Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AuthCheck authCheck = handlerMethod.getMethod().getAnnotation(AuthCheck.class);
            if (authCheck == null) {
                return Optional.empty();
            }
            return Optional.of(authCheck);
        }
        return Optional.empty();
    }

}
