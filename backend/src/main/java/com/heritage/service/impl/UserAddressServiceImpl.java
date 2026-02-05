package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.UserAddressCreateRequest;
import com.heritage.dto.UserAddressUpdateRequest;
import com.heritage.dto.UserAddressVO;
import com.heritage.entity.UserAddress;
import com.heritage.mapper.UserAddressMapper;
import com.heritage.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    @Override
    @Transactional
    public Long createAddress(Long userId, UserAddressCreateRequest request) {
        boolean shouldSetDefault = Boolean.TRUE.equals(request.getIsDefault());
        if (!shouldSetDefault) {
            LambdaQueryWrapper<UserAddress> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(UserAddress::getUserId, userId)
                    .eq(UserAddress::getStatus, 1);
            shouldSetDefault = this.count(countWrapper) == 0;
        }

        if (shouldSetDefault) {
            unsetDefaultForUser(userId);
        }

        String regionNamePath = normalizePath(request.getRegionNamePath());
        if (regionNamePath == null) {
            throw new BusinessException("地区不能为空");
        }
        String regionCodePath = normalizePathAllowEmptyToNull(request.getRegionCodePath());
        Integer regionDepth = request.getRegionDepth() != null ? request.getRegionDepth() : computeDepth(regionNamePath);
        if (regionDepth == null || regionDepth <= 0) {
            throw new BusinessException("地区层级不合法");
        }

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setRegionNamePath(regionNamePath);
        address.setRegionCodePath(regionCodePath);
        address.setRegionDepth(regionDepth);
        applyRegionRedundancy(address, regionNamePath);
        address.setDetailAddress(request.getDetailAddress());
        address.setIsDefault(shouldSetDefault ? 1 : 0);
        address.setStatus(1);
        this.save(address);
        return address.getId();
    }

    @Override
    @Transactional
    public void updateAddress(Long userId, Long addressId, UserAddressUpdateRequest request) {
        UserAddress address = getActiveOwnedAddress(userId, addressId);

        if (request.getRegionNamePath() != null) {
            String v = normalizePath(request.getRegionNamePath());
            if (v == null) {
                throw new BusinessException("地区不能为空");
            }
            address.setRegionNamePath(v);
            Integer depth = request.getRegionDepth() != null ? request.getRegionDepth() : computeDepth(v);
            if (depth == null || depth <= 0) {
                throw new BusinessException("地区层级不合法");
            }
            address.setRegionDepth(depth);
            applyRegionRedundancy(address, v);
        } else if (request.getRegionDepth() != null) {
            Integer depth = request.getRegionDepth();
            if (depth <= 0) {
                throw new BusinessException("地区层级不合法");
            }
            address.setRegionDepth(depth);
        }

        if (request.getRegionCodePath() != null) {
            address.setRegionCodePath(normalizePathAllowEmptyToNull(request.getRegionCodePath()));
        }

        if (request.getReceiverName() != null) {
            String v = request.getReceiverName().trim();
            if (v.isEmpty()) {
                throw new BusinessException("收货人姓名不能为空");
            }
            address.setReceiverName(v);
        }
        if (request.getReceiverPhone() != null) {
            String v = request.getReceiverPhone().trim();
            if (v.isEmpty()) {
                throw new BusinessException("收货人手机号不能为空");
            }
            address.setReceiverPhone(v);
        }
        if (request.getProvince() != null) {
            String v = request.getProvince().trim();
            address.setProvince(v.isEmpty() ? null : v);
        }
        if (request.getCity() != null) {
            String v = request.getCity().trim();
            address.setCity(v.isEmpty() ? null : v);
        }
        if (request.getDistrict() != null) {
            String v = request.getDistrict().trim();
            address.setDistrict(v.isEmpty() ? null : v);
        }
        if (request.getDetailAddress() != null) {
            String v = request.getDetailAddress().trim();
            if (v.isEmpty()) {
                throw new BusinessException("详细地址不能为空");
            }
            address.setDetailAddress(v);
        }

        this.updateById(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress address = getActiveOwnedAddress(userId, addressId);
        this.removeById(address.getId());
    }

    @Override
    public List<UserAddressVO> listAddresses(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getStatus, 1)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getUpdateTime)
                .orderByDesc(UserAddress::getCreateTime);
        List<UserAddress> list = this.list(wrapper);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long addressId) {
        UserAddress address = getActiveOwnedAddress(userId, addressId);
        unsetDefaultForUser(userId);
        address.setIsDefault(1);
        this.updateById(address);
    }

    private void unsetDefaultForUser(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getStatus, 1)
                .eq(UserAddress::getIsDefault, 1);
        List<UserAddress> defaults = this.list(wrapper);
        if (defaults.isEmpty()) {
            return;
        }
        for (UserAddress a : defaults) {
            a.setIsDefault(0);
        }
        this.updateBatchById(defaults);
    }

    private UserAddress getActiveOwnedAddress(Long userId, Long addressId) {
        UserAddress address = this.getById(addressId);
        if (address == null || !Objects.equals(address.getStatus(), 1)) {
            throw new BusinessException("地址不存在");
        }
        if (!Objects.equals(address.getUserId(), userId)) {
            throw new BusinessException("无权限操作该地址");
        }
        return address;
    }

    private UserAddressVO toVO(UserAddress address) {
        UserAddressVO vo = new UserAddressVO();
        BeanUtils.copyProperties(address, vo);
        return vo;
    }

    private String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        String trimmed = path.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String[] parts = trimmed.split("/");
        List<String> cleaned = new ArrayList<>();
        for (String p : parts) {
            if (p == null) {
                continue;
            }
            String t = p.trim();
            if (!t.isEmpty()) {
                cleaned.add(t);
            }
        }
        if (cleaned.isEmpty()) {
            return null;
        }
        StringJoiner joiner = new StringJoiner("/");
        for (String c : cleaned) {
            joiner.add(c);
        }
        return joiner.toString();
    }

    private String normalizePathAllowEmptyToNull(String path) {
        return normalizePath(path);
    }

    private int computeDepth(String regionNamePath) {
        String normalized = normalizePath(regionNamePath);
        if (normalized == null) {
            return 0;
        }
        return normalized.split("/").length;
    }

    private void applyRegionRedundancy(UserAddress address, String regionNamePath) {
        String normalized = normalizePath(regionNamePath);
        if (normalized == null) {
            address.setProvince(null);
            address.setCity(null);
            address.setDistrict(null);
            return;
        }
        String[] parts = normalized.split("/");
        address.setProvince(parts.length >= 1 ? parts[0] : null);
        address.setCity(parts.length >= 2 ? parts[1] : null);
        address.setDistrict(parts.length >= 3 ? parts[2] : null);
    }
}
