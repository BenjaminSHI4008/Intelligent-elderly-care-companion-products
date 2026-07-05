package com.xiaoban.server.service;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.Notification;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoban.server.config.properties.JpushProperties;
import com.xiaoban.server.entity.BindingRelation;
import com.xiaoban.server.mapper.BindingRelationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

    private final JpushProperties jpushConfig;
    private final BindingRelationMapper bindingRelationMapper;

    public void pushToUser(Long userId, String title, String content, Map<String, String> extras) {
        if (!jpushConfig.isEnabled()
                || !StringUtils.hasText(jpushConfig.getAppKey())
                || !StringUtils.hasText(jpushConfig.getMasterSecret())) {
            log.warn("JPush 未启用或未配置 app-key / master-secret，跳过推送 userId={}", userId);
            return;
        }

        try {
            JPushClient client = new JPushClient(jpushConfig.getMasterSecret(), jpushConfig.getAppKey());
            PushPayload.Builder builder = PushPayload.newBuilder()
                    .setPlatform(Platform.android())
                    .setAudience(Audience.alias(String.valueOf(userId)))
                    .setNotification(Notification.newBuilder()
                            .addPlatformNotification(AndroidNotification.newBuilder()
                                    .setTitle(title)
                                    .setAlert(content)
                                    .addExtras(extras)
                                    .build())
                            .build())
                    .setOptions(Options.newBuilder().setApnsProduction(false).build());

            PushPayload payload = builder.build();
            PushResult result = client.sendPush(payload);
            log.info("JPush 推送成功 userId={} msgId={}", userId, result.msg_id);
        } catch (APIConnectionException | APIRequestException e) {
            log.error("JPush 推送失败 userId={}", userId, e);
        }
    }

    public void pushToElderFamily(Long elderId, String title, String content) {
        pushToBoundChildren(elderId, title, content, Map.of("elderId", String.valueOf(elderId)));
    }

    public void pushToBoundChildren(Long elderId, String title, String content, Map<String, String> extras) {
        List<BindingRelation> relations = bindingRelationMapper.selectList(
                new LambdaQueryWrapper<BindingRelation>()
                        .eq(BindingRelation::getElderId, elderId)
                        .eq(BindingRelation::getStatus, "active")
        );
        for (BindingRelation relation : relations) {
            pushToUser(relation.getChildId(), title, content, extras);
        }
    }
}
