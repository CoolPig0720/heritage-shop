package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.CertificateRequest;
import com.heritage.entity.Certificate;
import com.heritage.entity.User;
import com.heritage.mapper.CertificateMapper;
import com.heritage.mapper.UserMapper;
import com.heritage.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl extends ServiceImpl<CertificateMapper, Certificate> implements CertificateService {

    private final UserMapper userMapper;

    @Override
    @Transactional
    public void verifyCertificate(Long userId, CertificateRequest request) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getCertificateNumber, request.getCertificateNumber());
        Certificate certificate = this.getOne(wrapper);

        if (certificate == null) {
            throw new BusinessException("证件号不存在");
        }

        if (!certificate.getName().equals(request.getName())) {
            throw new BusinessException("证件号与姓名不匹配");
        }

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getCertificateNumber, request.getCertificateNumber());
        if (userMapper.selectCount(userWrapper) > 0) {
            throw new BusinessException("该证件号已被使用");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (certificate.getRole().equals("MERCHANT") && !user.getRole().equals("MERCHANT")) {
            throw new BusinessException("该证件号只能用于商家认证");
        }

        if (certificate.getRole().equals("USER") && !user.getRole().equals("USER")) {
            throw new BusinessException("该证件号只能用于普通用户认证");
        }

        user.setCertificateNumber(request.getCertificateNumber());
        userMapper.updateById(user);
    }

    @Override
    public Certificate getCertificateInfo(String certificateNumber) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getCertificateNumber, certificateNumber);
        return this.getOne(wrapper);
    }

    @Override
    public Certificate getCertificateInfoByUserId(Long userId) {
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getId, userId);
        User user = userMapper.selectOne(userWrapper);

        if (user == null || user.getCertificateNumber() == null) {
            return null;
        }

        return getCertificateInfo(user.getCertificateNumber());
    }

    @Override
    @Transactional
    public void updateCertification(Long userId, CertificateRequest request) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getCertificateNumber, request.getCertificateNumber());
        Certificate certificate = this.getOne(wrapper);

        if (certificate == null) {
            throw new BusinessException("证件号不存在");
        }

        if (!certificate.getName().equals(request.getName())) {
            throw new BusinessException("证件号与姓名不匹配");
        }

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getCertificateNumber, request.getCertificateNumber());
        if (userMapper.selectCount(userWrapper) > 0) {
            throw new BusinessException("该证件号已被使用");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (certificate.getRole().equals("MERCHANT") && !user.getRole().equals("MERCHANT")) {
            throw new BusinessException("该证件号只能用于商家认证");
        }

        if (certificate.getRole().equals("USER") && !user.getRole().equals("USER")) {
            throw new BusinessException("该证件号只能用于普通用户认证");
        }

        user.setCertificateNumber(request.getCertificateNumber());
        userMapper.updateById(user);
    }
}
