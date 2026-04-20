package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heritage.common.BusinessException;
import com.heritage.dto.CustomizeMessageCreateRequest;
import com.heritage.dto.CustomizeMessageVO;
import com.heritage.entity.CustomizeMessage;
import com.heritage.entity.CustomizeRequest;
import com.heritage.entity.User;
import com.heritage.mapper.CustomizeMessageMapper;
import com.heritage.mapper.CustomizeRequestMapper;
import com.heritage.mapper.UserMapper;
import com.heritage.service.CustomizeMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomizeMessageServiceImpl implements CustomizeMessageService {

    private final CustomizeMessageMapper customizeMessageMapper;
    private final CustomizeRequestMapper customizeRequestMapper;
    private final UserMapper userMapper;

    @Override
    public Long sendMessage(Long userId, String role, Long requestId, CustomizeMessageCreateRequest request) {
        // 校验工单存在且用户为参与方
        CustomizeRequest cr = customizeRequestMapper.selectById(requestId);
        if (cr == null) {
            throw new BusinessException("工单不存在");
        }
        if (!cr.getUserId().equals(userId) && !cr.getMerchantId().equals(userId)) {
            throw new BusinessException("无权限在该工单发送消息");
        }

        // 确定 senderType
        String senderType = "USER";
        if ("MERCHANT".equals(role) && cr.getMerchantId().equals(userId)) {
            senderType = "MERCHANT";
        }

        CustomizeMessage message = new CustomizeMessage();
        message.setRequestId(requestId);
        message.setSenderId(userId);
        message.setSenderType(senderType);
        message.setContent(sanitizeContent(request.getContent().trim()));
        customizeMessageMapper.insert(message);
        return message.getId();
    }

    @Override
    public List<CustomizeMessageVO> listMessages(Long userId, Long requestId) {
        // 校验工单存在且用户为参与方
        CustomizeRequest cr = customizeRequestMapper.selectById(requestId);
        if (cr == null) {
            throw new BusinessException("工单不存在");
        }
        if (!cr.getUserId().equals(userId) && !cr.getMerchantId().equals(userId)) {
            throw new BusinessException("无权限查看该工单消息");
        }

        LambdaQueryWrapper<CustomizeMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomizeMessage::getRequestId, requestId)
                .orderByAsc(CustomizeMessage::getCreateTime);
        List<CustomizeMessage> messages = customizeMessageMapper.selectList(wrapper);
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }

        return messages.stream().map(this::toVO).collect(Collectors.toList());
    }

    private CustomizeMessageVO toVO(CustomizeMessage message) {
        CustomizeMessageVO vo = new CustomizeMessageVO();
        BeanUtils.copyProperties(message, vo);

        User sender = userMapper.selectById(message.getSenderId());
        if (sender != null) {
            vo.setSenderName(sender.getName());
            vo.setSenderAvatar(sender.getAvatar());
        }
        return vo;
    }

    @Override
    public void markAsRead(Long userId, Long requestId) {
        // 校验工单存在且用户为参与方
        CustomizeRequest cr = customizeRequestMapper.selectById(requestId);
        if (cr == null) {
            return;
        }

        // 将该工单下对方发的未读消息标记为已读
        LambdaUpdateWrapper<CustomizeMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CustomizeMessage::getRequestId, requestId)
                .ne(CustomizeMessage::getSenderId, userId)
                .eq(CustomizeMessage::getIsRead, 0)
                .set(CustomizeMessage::getIsRead, 1)
                .set(CustomizeMessage::getReadTime, LocalDateTime.now());
        customizeMessageMapper.update(null, updateWrapper);
    }

    @Override
    public Long countUnreadMessages(Long userId) {
        // 查询所有当前用户参与的工单中，对方发的未读消息数
        LambdaQueryWrapper<CustomizeMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(CustomizeMessage::getSenderId, userId)
                .eq(CustomizeMessage::getIsRead, 0)
                .inSql(CustomizeMessage::getRequestId,
                        "SELECT id FROM customize_request WHERE user_id = " + userId
                                + " OR merchant_id = " + userId);
        return customizeMessageMapper.selectCount(wrapper);
    }

    private String sanitizeContent(String content) {
        if (content == null) {
            return "";
        }
        return content.replace("<", "&lt;").replace(">", "&gt;");
    }
}
