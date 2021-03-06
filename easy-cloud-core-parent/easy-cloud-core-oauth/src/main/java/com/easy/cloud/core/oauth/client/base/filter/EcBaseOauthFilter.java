package com.easy.cloud.core.oauth.client.base.filter;

import com.easy.cloud.core.authority.filter.base.EcBaseAuthenticatingFilter;
import com.easy.cloud.core.common.string.utils.EcStringUtils;
import com.easy.cloud.core.oauth.client.base.token.EcBaseOauthToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * 基础授权过滤器
 *
 * @author daiqi
 * @create 2018-06-29 17:38
 */
public abstract class EcBaseOauthFilter extends EcBaseAuthenticatingFilter {
    //服务器端登录成功/失败后重定向到的客户端地址
    private String redirectUrl = "http://www.baidu.com";

    private String failureUrl = "http://www.baidu.com";

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        return getOAuth2Token(request);
    }

    protected abstract EcBaseOauthToken getOAuth2Token(ServletRequest httpRequest);

    protected abstract String getOauthCodeKey();

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        String error = request.getParameter("error");
        String errorDescription = request.getParameter("error_description");
        //如果服务端返回了错误
        if (EcStringUtils.isNotEmpty(error)) {
            WebUtils.issueRedirect(request, response, failureUrl + "?error=" + error + "error_description=" + errorDescription);
            return false;
        }
        if (EcStringUtils.isEmpty(request.getParameter(getOauthCodeKey()))) {
            //如果用户没有身份验证，且没有auth code，则重定向到服务端授权
            saveRequestAndRedirectToLogin(request, response);
            return false;
        }

        return executeLogin(request, response);
    }

    @Override
    public String getSuccessUrl() {
        return redirectUrl;
    }

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request,
                                     ServletResponse response) throws Exception {
        issueSuccessRedirect(request, response);
        return false;
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException ae, ServletRequest request,
                                     ServletResponse response) {
        Subject subject = getSubject(request, response);
        if (subject.isAuthenticated() || subject.isRemembered()) {
            try {
                issueSuccessRedirect(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                WebUtils.issueRedirect(request, response, failureUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


}
