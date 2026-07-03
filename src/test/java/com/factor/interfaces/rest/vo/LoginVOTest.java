package com.factor.interfaces.rest.vo;

import com.factor.domain.auth.Account;
import com.factor.domain.auth.AuthSession;
import com.factor.domain.auth.PermissionCode;
import com.factor.domain.auth.Role;
import com.factor.domain.auth.RoleScope;
import com.factor.domain.auth.UserType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoginVOTest {

    @Test
    void shouldCreateLoginVO() {
        Account account = new Account("1", "testuser", "pass", "测试用户", UserType.TRADER, "role1", "tenant1", true);
        Role role = new Role("role1", "TEST", "测试角色", UserType.TRADER, RoleScope.BUSINESS,
                List.of(PermissionCode.TRADE_ORDER_VIEW), false);
        AuthSession session = new AuthSession("token-123", account, role, List.of());

        LoginVO vo = LoginVO.from(session);

        assertNotNull(vo);
        assertEquals("token-123", vo.token());
        assertNotNull(vo.account());
        assertEquals("testuser", vo.account().username());
        assertEquals("测试用户", vo.account().displayName());
        assertEquals(UserType.TRADER, vo.account().userType());
        assertNotNull(vo.role());
        assertNotNull(vo.menus());
    }
}