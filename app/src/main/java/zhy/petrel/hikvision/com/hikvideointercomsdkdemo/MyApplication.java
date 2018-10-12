package zhy.petrel.hikvision.com.hikvideointercomsdkdemo;

import zhyjsdk.isms.hikvision.com.zhyjtalklib.BaseApplication;
import zhyjsdk.isms.hikvision.com.zhyjtalklib.TALKSDK;

/**
 * Created by ligang14 on 2017/7/15.
 */

public class MyApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        TALKSDK.getInstance().initTALKSDK(Constants.APP_KEY);
        TALKSDK.getInstance().setEzAccessToken(Constants.EZACCESSTOKEN);
    }
}
