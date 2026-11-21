package com.autoecole.app.tenant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TenantContextTest {

    @Test
    void shouldStoreAndClearTenantId() {
        TenantContext.clear();

        TenantContext.setTenantId(42L);

        assertEquals(42L, TenantContext.getTenantId());

        TenantContext.clear();

        assertNull(TenantContext.getTenantId());
    }
}
