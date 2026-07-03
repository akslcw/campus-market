package com.campus.market.config;

import cn.dev33.satoken.stp.StpInterface;
import com.campus.market.entity.User;
import com.campus.market.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限/角色数据源。
 * {@code @SaCheckRole("ADMIN")} 通过本类从数据库读取登录用户的角色，
 * 而不是依赖会话里手动写入的 role。
 *
 * @author 成员一（接管）
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserService userService;

    /** 角色列表：返回用户的 role（USER / ADMIN）。 */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = new ArrayList<>();
        User user = userService.getById(Long.valueOf(loginId.toString()));
        if (user != null && user.getRole() != null) {
            roles.add(user.getRole());
        }
        return roles;
    }

    /** 权限码列表：本项目按角色鉴权，暂不使用细粒度权限码。 */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }
}
