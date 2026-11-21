package com.autoecole.app.tenant;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class TenantContext {

    private static final String KEY = "tenant_id";
    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
        RequestAttributes ra = getRequestAttributesQuietly();
        if (ra != null) {
            ra.setAttribute(KEY, tenantId, RequestAttributes.SCOPE_REQUEST);
        }
    }

    public static Long getTenantId() {
        RequestAttributes ra = getRequestAttributesQuietly();
        if (ra != null) {
            Object v = ra.getAttribute(KEY, RequestAttributes.SCOPE_REQUEST);
            if (v instanceof Long tid) return tid;
            if (v instanceof Integer i) return i.longValue();
        }
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
        RequestAttributes ra = getRequestAttributesQuietly();
        if (ra != null) {
            try {
                ra.removeAttribute(KEY, RequestAttributes.SCOPE_REQUEST);
            } catch (Exception ignore) {}
        }
    }

    private static RequestAttributes getRequestAttributesQuietly() {
        try {
            return RequestContextHolder.currentRequestAttributes();
        } catch (Exception e) {
            return null;
        }
    }
}

