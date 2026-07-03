package com.campus.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.market.entity.User;

public interface UserService extends IService<User> {
    User findByUsername(String username);
}