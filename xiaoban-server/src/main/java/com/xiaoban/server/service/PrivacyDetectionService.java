package com.xiaoban.server.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrivacyDetectionService {

    private static final List<String> PRIVACY_PATTERNS = List.of(
            "别告诉孩子", "别让孩子知道", "别告诉我儿子", "别告诉我女儿",
            "这个不要说", "别跟他们说", "别跟她说", "保密", "不要告诉"
    );

    public boolean isPrivate(String text) {
        for (String pattern : PRIVACY_PATTERNS) {
            if (text.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}
