package com.xiaoban.app.location;

import android.content.Context;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;

public class LocationHelper {

    public interface LocationCallback {
        void onSuccess(String adCode);

        void onError(String message);
    }

    public static void getCurrentAdCode(Context context, LocationCallback callback) {
        Context appContext = context.getApplicationContext();
        AMapLocationClient.updatePrivacyShow(appContext, true, true);
        AMapLocationClient.updatePrivacyAgree(appContext, true);

        try {
            AMapLocationClient locationClient = new AMapLocationClient(appContext);
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setOnceLocation(true);
            option.setNeedAddress(true);
            option.setHttpTimeOut(10000);

            locationClient.setLocationOption(option);
            locationClient.setLocationListener(location -> {
                locationClient.stopLocation();
                locationClient.onDestroy();
                handleLocationResult(location, callback);
            });
            locationClient.startLocation();
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    private static void handleLocationResult(AMapLocation location, LocationCallback callback) {
        if (location == null) {
            callback.onError("定位失败");
            return;
        }

        if (location.getErrorCode() != 0) {
            callback.onError(location.getErrorInfo());
            return;
        }

        String adCode = location.getAdCode();
        if (TextUtils.isEmpty(adCode)) {
            callback.onError("未获取到城市编码");
            return;
        }

        callback.onSuccess(adCode);
    }
}
