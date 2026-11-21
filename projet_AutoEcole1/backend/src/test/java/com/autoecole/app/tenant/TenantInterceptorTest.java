package com.autoecole.app.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantInterceptorTest {

    private final TenantInterceptor interceptor = new TenantInterceptor();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldNotOverwriteTenantSetByJwtWhenHeaderPresent() {
        Long jwtTenantId = 42L;
        TenantContext.setTenantId(jwtTenantId);

        when(request.getHeader("X-Tenant-Id")).thenReturn("999");

        interceptor.preHandle(request, response, handler);

        assertEquals(jwtTenantId, TenantContext.getTenantId(),
                "TenantInterceptor ne doit pas écraser un TenantContext déjà défini par le JWT");
    }

    @Test
    void shouldNotOverwriteTenantSetByJwtWhenHeaderMissing() {
        Long jwtTenantId = 42L;
        TenantContext.setTenantId(jwtTenantId);

        when(request.getHeader("X-Tenant-Id")).thenReturn(null);

        interceptor.preHandle(request, response, handler);

        assertEquals(jwtTenantId, TenantContext.getTenantId(),
                "TenantInterceptor ne doit pas nettoyer un TenantContext déjà défini par le JWT");
    }

    @Test
    void shouldUseHeaderWhenTenantContextIsEmpty() {
        Long headerTenantId = 10L;
        assertNull(TenantContext.getTenantId());

        when(request.getHeader("X-Tenant-Id")).thenReturn(String.valueOf(headerTenantId));

        interceptor.preHandle(request, response, handler);

        assertEquals(headerTenantId, TenantContext.getTenantId(),
                "TenantInterceptor doit utiliser le header X-Tenant-Id quand aucun JWT n'a défini le contexte");
    }

    @Test
    void shouldKeepNullWhenContextEmptyAndNoHeader() {
        assertNull(TenantContext.getTenantId());
        when(request.getHeader("X-Tenant-Id")).thenReturn(null);

        interceptor.preHandle(request, response, handler);

        assertNull(TenantContext.getTenantId());
    }

    @Test
    void shouldIgnoreInvalidHeaderWhenContextEmpty() {
        assertNull(TenantContext.getTenantId());
        when(request.getHeader("X-Tenant-Id")).thenReturn("not-a-number");

        interceptor.preHandle(request, response, handler);

        assertNull(TenantContext.getTenantId());
    }

    @Test
    void shouldClearAfterCompletion() {
        TenantContext.setTenantId(1L);

        interceptor.afterCompletion(request, response, handler, null);

        assertNull(TenantContext.getTenantId());
    }
}
