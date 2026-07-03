package com.campus.market.controller;

import com.campus.market.common.ResultCode;
import com.campus.market.common.exception.BusinessException;
import com.campus.market.dto.RegisterDTO;
import com.campus.market.entity.User;
import com.campus.market.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Test
    void registerRejectsDuplicateUsernameAfterTrimmingInput() {
        UserService userService = mock(UserService.class);
        AuthController controller = new AuthController(userService);
        RegisterDTO dto = registerDto(" alice ");

        when(userService.findByUsername("alice")).thenReturn(existingUser("alice"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.register(dto));

        assertEquals(ResultCode.USERNAME_EXISTS.getCode(), exception.getCode());
        verify(userService).findByUsername("alice");
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void registerConvertsDuplicateKeyExceptionToUsernameExists() {
        UserService userService = mock(UserService.class);
        AuthController controller = new AuthController(userService);
        RegisterDTO dto = registerDto("alice");

        when(userService.findByUsername("alice")).thenReturn(null);
        when(userService.save(any(User.class))).thenThrow(new DuplicateKeyException("Duplicate entry"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.register(dto));

        assertEquals(ResultCode.USERNAME_EXISTS.getCode(), exception.getCode());
    }

    private static RegisterDTO registerDto(String username) {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(username);
        dto.setPassword("Password123");
        dto.setNickname("Alice");
        return dto;
    }

    private static User existingUser(String username) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        return user;
    }
}
