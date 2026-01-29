package com.heritage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.UserAddressCreateRequest;
import com.heritage.dto.UserAddressUpdateRequest;
import com.heritage.dto.UserAddressVO;
import com.heritage.entity.UserAddress;

import java.util.List;

public interface UserAddressService extends IService<UserAddress> {

    Long createAddress(Long userId, UserAddressCreateRequest request);

    void updateAddress(Long userId, Long addressId, UserAddressUpdateRequest request);

    void deleteAddress(Long userId, Long addressId);

    List<UserAddressVO> listAddresses(Long userId);

    void setDefault(Long userId, Long addressId);
}
