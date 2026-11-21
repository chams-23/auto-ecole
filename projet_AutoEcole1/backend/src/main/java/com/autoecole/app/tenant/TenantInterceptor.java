package com.autoecole.app.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String X_TENANT_ID_HEADER = "X-Tenant-Id";

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        if (TenantContext.getTenantId() != null) {
            return true;
        }

        String tenantIdHeader = request.getHeader(X_TENANT_ID_HEADER);
        if (tenantIdHeader != null && !tenantIdHeader.isBlank()) {
            try {
                Long tenantId = Long.parseLong(tenantIdHeader);
                TenantContext.setTenantId(tenantId);
            } catch (NumberFormatException e) {
                TenantContext.clear();
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex
    ) {
        TenantContext.clear();
    }
}
