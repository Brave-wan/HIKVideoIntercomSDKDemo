package zhy.petrel.hikvision.com.hikvideointercomsdkdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.videogo.errorlayer.ErrorInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;

import zhyjsdk.isms.hikvision.com.zhyjtalklib.ParseMessageCallBack;
import zhyjsdk.isms.hikvision.com.zhyjtalklib.TALKSDK;
import zhyjsdk.isms.hikvision.com.zhyjtalklib.TalkVoiceCallBack;
import zhyjsdk.isms.hikvision.com.zhyjtalklib.bean.CallInfoMesssage;
import zhyjsdk.isms.hikvision.com.zhyjtalklib.bean.ErrorCodes;
import zhyjsdk.isms.hikvision.com.zhyjtalklib.bean.ErrorInfos;
import zhyjsdk.isms.hikvision.com.zhyjtalklib.bean.RealPlayMsgs;
import zhyjsdk.isms.hikvision.com.zhyjtalklib.utils.Toaster;

/*
* 当对讲消息过来后的接听
* Created by ligang14 on 2017/7/25.
*
* */
public class CallActivity extends AppCompatActivity implements View.OnClickListener {

    private SurfaceView surfaceView;
    private ProgressBar progressBar;

    private Handler mHandler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case RealPlayMsgs.MSG_VIDEO_SIZE_CHANGED:
                    progressBar.setVisibility(View.GONE);
                    TALKSDK.getInstance().startVoice();
                    break;
                case RealPlayMsgs.MSG_REALPLAY_PLAY_SUCCESS:
                    break;
                case RealPlayMsgs.MSG_REALPLAY_PLAY_FAIL:
                    progressBar.setVisibility(View.GONE);
                    handlePlayFail(msg.obj);
                    break;
                case RealPlayMsgs.MSG_REALPLAY_VOICETALK_SUCCESS:
                    break;
                case RealPlayMsgs.MSG_REALPLAY_VOICETALK_FAIL:
                    progressBar.setVisibility(View.GONE);
                    break;
                case RealPlayMsgs.MSG_REALPLAY_VOICETALK_STOP:
                    progressBar.setVisibility(View.GONE);
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        initView();
    }
    public void initView()
    {
        surfaceView = (SurfaceView) findViewById(R.id.callin_surfaceView);
        progressBar = (ProgressBar) findViewById(R.id.progressview);
        Button hangUp=(Button)findViewById(R.id.hangUp);
        hangUp.setOnClickListener(this);
        //首先需要先判断是否是呼入消息，然后在解析消息获取deviceSerial和channel,另外第一个参数如果是正常室内机模式是填写固定的0，如果是老小区模式需要传入callId
        //模拟推送的呼叫消息字段
        String message="";
        int type=TALKSDK.getInstance().isVoiceTalkMessage(message);
        if (type==0)
        {
            //0代表的是呼叫消息
            TALKSDK.getInstance().parseCallMessage(message, new ParseMessageCallBack() {
                @Override
                public void onSuccess(CallInfoMesssage callInfoMesssage) {
                    TALKSDK.getInstance().anserCall(0,callInfoMesssage.getExtras().getDeviceSerial(),callInfoMesssage.getChannelNo(), surfaceView,mHandler,new TalkVoiceCallBack() {
                        @Override
                        public void onSuccess() {
                            Toaster.showShort(CallActivity.this,"接听成功");
                        }

                        @Override
                        public void onFailure(int var1) {
                            switch (var1)
                            {
                                case -1:
                                    Toaster.showShort(CallActivity.this,"网络异常");
                                    break;
                                case 1:
                                    Toaster.showShort(CallActivity.this,"无呼叫");
                                    break;
                                case 3:
                                    Toaster.showShort(CallActivity.this,"已被接听");
                                    break;
                                case 4:
                                    Toaster.showShort(CallActivity.this,"获取通话状态失败");
                                    break;
                                case 5:
                                    Toaster.showShort(CallActivity.this,"发送设备状态失败");
                            }
                        }
                    });
                }

                @Override
                public void onFailure() {

                }
            });
        }
        else if (type==1)
        {
            //挂断消息这里需要自己处理，收到挂断消息一般都会将正在处于的对讲关闭，防止门口机已经挂断用户还处于不知道的情况
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.hangUp:
                TALKSDK.getInstance().hangUp(0,Constants.DEVICESERIAL, new TalkVoiceCallBack() {
                    @Override
                    public void onSuccess() {
                        boolean isSucc= TALKSDK.getInstance().stopRealPlay();
                        if (isSucc)
                        {
                            Toaster.showShort(CallActivity.this,"挂断成功");
                            TALKSDK.getInstance().stopRealPlay();
                            TALKSDK.getInstance().stopVoiceTalk();
                        }
                        else
                        {
                            Toaster.showShort(CallActivity.this,"挂断失败");

                        }
                    }

                    @Override
                    public void onFailure(int var1) {
                        Toaster.showShort(CallActivity.this,"挂断失败");
                    }
                });
                break;
        }

    }


    private void handlePlayFail(Object obj) {
        int errorCode = 0;
        if (obj != null) {
            ErrorInfo errorInfo = (ErrorInfo) obj;
            errorCode = errorInfo.errorCode;
            Toaster.showShort(this,"预览出错"+errorCode);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        TALKSDK.getInstance().hangUp(0,Constants.DEVICESERIAL, new TalkVoiceCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int var1) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TALKSDK.getInstance().stopRealPlay();
        TALKSDK.getInstance().stopVoiceTalk();
    }
}
