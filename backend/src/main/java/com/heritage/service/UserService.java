package com.heritage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.*;
import com.heritage.entity.User;

public interface UserService extends IService<User> {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserVO getUserInfo(Long userId);

    void updateUserInfo(Long userId, UserUpdateRequest request);

    void updatePassword(Long userId, PasswordUpdateRequest request);

    Page<UserVO> getUserList(PageQuery query);
}
