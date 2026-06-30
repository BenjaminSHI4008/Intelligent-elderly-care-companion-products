package com.xiaoban.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoban.server.common.BusinessException;
import com.xiaoban.server.common.ResultCode;
import com.xiaoban.server.config.properties.AmapWeatherProperties;
import com.xiaoban.server.vo.WeatherVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final String PLACEHOLDER_PREFIX = "your-";

    private final AmapWeatherProperties weatherConfig;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public WeatherVO getCurrentWeather(String city) {
        validateAmapKey();

        try {
            String cityCode = !StringUtils.hasText(city)
                    ? weatherConfig.getDefaultCityCode()
                    : city.trim();

            String url = "https://restapi.amap.com/v3/weather/weatherInfo"
                    + "?key=" + weatherConfig.getApiKey()
                    + "&city=" + cityCode
                    + "&extensions=base";

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (!"1".equals(root.path("status").asText())) {
                String info = root.path("info").asText("未知错误");
                String infocode = root.path("infocode").asText("");
                log.warn("高德天气接口返回失败: city={}, info={}, infocode={}, response={}",
                        cityCode, info, infocode, response);
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "天气获取失败：" + info);
            }

            JsonNode lives = root.path("lives");
            if (!lives.isArray() || lives.isEmpty()) {
                log.warn("高德天气接口未返回实时天气数据: city={}, response={}", cityCode, response);
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "天气获取失败：未返回天气数据");
            }

            JsonNode live = lives.get(0);

            return WeatherVO.builder()
                    .city(live.path("city").asText())
                    .weather(live.path("weather").asText())
                    .temperature(live.path("temperature").asText())
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取天气失败", e);
            throw new BusinessException(ResultCode.SERVER_ERROR);
        }
    }

    private void validateAmapKey() {
        if (!weatherConfig.isEnabled()) {
            throw new BusinessException(ResultCode.SERVER_ERROR.getCode(), "天气服务已禁用");
        }
        String key = weatherConfig.getApiKey();
        if (!StringUtils.hasText(key) || key.startsWith(PLACEHOLDER_PREFIX)) {
            throw new BusinessException(ResultCode.SERVER_ERROR.getCode(),
                    "天气服务未配置，请修改 config.yaml -> external-services.amap-weather.api-key");
        }
    }
}
