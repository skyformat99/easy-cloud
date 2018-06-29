package com.easy.cloud.core.authority.filter.login;

import com.easy.cloud.core.authority.constant.EcAuthorityConstant;
import com.easy.cloud.core.authority.filter.base.EcAccessControlFilter;
import com.easy.cloud.core.basic.pojo.dto.EcBaseServiceResult;
import com.easy.cloud.core.basic.utils.EcBaseUtils;
import com.easy.cloud.core.common.http.utils.EcRequestUtils;
import com.easy.cloud.core.common.log.utils.EcLogUtils;
import com.easy.cloud.core.operator.sysuser.pojo.dto.SysUserDTO;
import com.easy.cloud.core.operator.sysuser.service.SysUserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.crazycake.shiro.AuthCachePrincipal;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author daiqi
 * @create 2018-06-27 19:10
 */
public abstract class EcBaseLoginFilter extends EcAccessControlFilter {
    @Autowired
    protected SysUserService sysUserService;
    @Autowired
    protected org.apache.shiro.mgt.SecurityManager securityManager;
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        Object loginInfo =request.getAttribute(EcAuthorityConstant.LOGIN_INFO);
        if (EcBaseUtils.isNotNull(loginInfo)) {
            return true;
        }
        String loginName = request.getParameter(EcAuthorityConstant.LOGIN_NAME);
        SysUserDTO authCachePrincipal = (SysUserDTO) getAuthCachePrincipal(loginName);
        if (authCachePrincipal != null) {
            SecurityUtils.setSecurityManager(securityManager);
            Subject subject = SecurityUtils.getSubject();
            authCachePrincipal.setPassword(request.getParameter("password"));
            sysUserService.login(subject, authCachePrincipal);
            EcLogUtils.info("获取数据成功" ,authCachePrincipal, logger);
            request.setAttribute(EcAuthorityConstant.USERNAME, authCachePrincipal.getAuthCacheKey());
            request.setAttribute(EcAuthorityConstant.LOGIN_INFO, authCachePrincipal);
            printToJson(request, response, EcBaseServiceResult.newInstanceOfSuccess());
            return false;
        }
        return true;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {

        return false;
    }

    protected abstract AuthCachePrincipal getAuthCachePrincipal(String loginName);

}
