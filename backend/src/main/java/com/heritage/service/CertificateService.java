package com.heritage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.CertificateRequest;
import com.heritage.entity.Certificate;

public interface CertificateService extends IService<Certificate> {

    void verifyCertificate(Long userId, CertificateRequest request);

    Certificate getCertificateInfo(String certificateNumber);

    Certificate getCertificateInfoByUserId(Long userId);

    void updateCertification(Long userId, CertificateRequest request);
}
