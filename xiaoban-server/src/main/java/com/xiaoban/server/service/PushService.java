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
import com.xiaoban.server.entity.BindingRelation;
import com.xiaoban.server.mapper.BindingRelationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

    @Value("${jpush.app-key}")
    private String appKey;

    @Value("${jpush.master-secret}")
    private String masterSecret;

    private final BindingRelationMapper bindingRelationMapper;

    public void pushToUser(Long userId, String title, String content, Map<String, String> extras) {
        try {
            JPushClient client = new JPushClient(masterSecret, appKey);
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
            PushResult result = client.sendPush(builder.build());
            log.info("推送到用户{}成功: {}", userId, result);
        } catch (APIConnectionException | APIRequestException e) {
            log.error("推送到用户{}失败", userId, e);
        }
    }

    public void pushToElderFamily(Long elderId, String title, String content) {
        List<BindingRelation> relations = bindingRelationMapper.selectList(
                new LambdaQueryWrapper<BindingRelation>()
                        .eq(BindingRelation::getElderId, elderId)
                        .eq(BindingRelation::getStatus, "active")
        );
        for (BindingRelation relation : relations) {
            pushToUser(relation.getChildId(), title, content, Map.of("elderId", String.valueOf(elderId)));
        }
    }
}
