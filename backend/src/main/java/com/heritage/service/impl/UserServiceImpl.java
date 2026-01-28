package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.*;
import com.heritage.entity.User;
import com.heritage.mapper.UserMapper;
import com.heritage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, request.getAccount());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("账号已存在");
        }

        User user = new User();
        user.setAccount(request.getAccount());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(request.getRole());

        this.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, request.getAccount());
        User user = this.getOne(wrapper);

        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setAccount(user.getAccount());
        userVO.setName(user.getName());
        userVO.setRole(user.getRole());
        userVO.setAvatar(user.getAvatar());
        userVO.setCertificateNumber(user.getCertificateNumber());
        userVO.setRegisterTime(user.getRegisterTime());

        LoginResponse response = new LoginResponse();
        response.setUserInfo(userVO);

        return response;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setPassword(user.getPassword());
        return userVO;
    }

    @Override
    @Transactional
    public void updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            if (request.getName().length() < 2 || request.getName().length() > 20) {
                throw new BusinessException("名称长度必须在2-20位之间");
            }
            user.setName(request.getName());
        }
        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (request.getPassword().length() < 6 || request.getPassword().length() > 20) {
                throw new BusinessException("密码长度必须在6-20位之间");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        this.updateById(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }

        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        this.updateById(user);
    }

    @Override
    public Page<UserVO> getUserList(PageQuery query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (query.getName() != null && !query.getName().trim().isEmpty()) {
            wrapper.like(User::getName, query.getName().trim());
        }
        
        if (query.getRole() != null && !query.getRole().trim().isEmpty()) {
            wrapper.eq(User::getRole, query.getRole().trim());
        }
        
        List<User> allUsers = this.list(wrapper);
        
        List<User> adminUsers = allUsers.stream()
                .filter(user -> "ADMIN".equals(user.getRole()))
                .sorted((a, b) -> b.getRegisterTime().compareTo(a.getRegisterTime()))
                .toList();
        
        List<User> otherUsers = allUsers.stream()
                .filter(user -> !"ADMIN".equals(user.getRole()))
                .sorted((a, b) -> b.getRegisterTime().compareTo(a.getRegisterTime()))
                .toList();
        
        List<User> mergedUsers = new ArrayList<>();
        mergedUsers.addAll(adminUsers);
        mergedUsers.addAll(otherUsers);
        
        int total = mergedUsers.size();
        int start = (query.getPage() - 1) * query.getSize();
        int end = Math.min(start + query.getSize(), total);
        
        List<User> pageUsers = start < total ? mergedUsers.subList(start, end) : new ArrayList<>();
        
        Page<UserVO> voPage = new Page<>(query.getPage(), query.getSize(), total);
        voPage.setRecords(pageUsers.stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).toList());

        return voPage;
    }
}
