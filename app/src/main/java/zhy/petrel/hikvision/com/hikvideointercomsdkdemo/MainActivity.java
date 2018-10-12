package zhy.petrel.hikvision.com.hikvideointercomsdkdemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.videogo.errorlayer.ErrorInfo;
import com.videogo.util.LogUtil;

import zhyjsdk.isms.hikvision.com.zhyjtalklib.BaseApplication;
import zhyjsdk.isms.hikvision.com.zhyjtalklib.TALKSDK;
import zhyjsdk.isms.hikvision.com.zhyjtalklib.bean.RealPlayMsgs;
import zhyjsdk.isms.hikvision.com.zhyjtalklib.utils.Toaster;

public class MainActivity extends AppCompatActivity implements  View.OnClickListener {

    /**
     * 播放回调handler
     */
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case RealPlayMsgs.MSG_REALPLAY_PLAY_SUCCESS:
                    break;
                case RealPlayMsgs.MSG_REALPLAY_PLAY_FAIL:
                    progressBars.setVisibility(View.GONE);
                    handlePlayFail(msg.obj);
                    break;
                case RealPlayMsgs.MSG_VIDEO_SIZE_CHANGED:
                    progressBars.setVisibility(View.GONE);
                    TALKSDK.getInstance().startVoice();
            }
        }
    };
    private SurfaceView call_surfaceView;
    private ProgressBar progressBars;
    private boolean mIsRecording=false;
    private Button recordBtn;
    private Button stopPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startPlay=(Button)findViewById(R.id.startPlay);
        Button remoteUnlock=(Button)findViewById(R.id.remoteUnlock);
        Button captrue=(Button)findViewById(R.id.captrue);
        Button call_lift=(Button)findViewById(R.id.call_lift);
        recordBtn = (Button)findViewById(R.id.recordBtn);
        progressBars = (ProgressBar)findViewById(R.id.progressviews);
        call_surfaceView = (SurfaceView)findViewById(R.id.call_surfaceView);
        stopPlay = (Button)findViewById(R.id.stopPlay);
        captrue.setOnClickListener(this);
        startPlay.setOnClickListener(this);
        remoteUnlock.setOnClickListener(this);
        call_lift.setOnClickListener(this);
        stopPlay.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.recordBtn:
                if (!mIsRecording)
                {
                    if (TALKSDK.getInstance().startRecord()!=null)
                    {
                        mIsRecording=true;
                        recordBtn.setText("停止录像");
                    }
                }
                else
                {
                    if (TALKSDK.getInstance().stopRecord())
                    {
                        mIsRecording=false;
                        recordBtn.setText("开始录像");
                    }
                }
                break;
            case R.id.call_lift:
                callLift();
                break;
            case R.id.captrue:
                if (TALKSDK.getInstance().captrue()!=null)
                {
                    Toaster.showShort(MainActivity.this,"截图成功");
                }
                else
                {
                    Toaster.showShort(MainActivity.this,"截图失败");
                }
                break;
            case R.id.startPlay:
                progressBars.setVisibility(View.VISIBLE);
                new AsyncTask<Void,Void,Boolean>()
                {
                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        Boolean isSuccess=TALKSDK.getInstance().startRealPlay(0,Constants.DEVICESERIAL,0,call_surfaceView,mHandler);
                        return isSuccess;
                    }
                }.executeOnExecutor(MyApplication.LIMITED_TASK_EXECUTOR);
                break;
            case R.id.remoteUnlock:
                remoteOpenDoor();
                break;
            case R.id.stopPlay:
                TALKSDK.getInstance().stopRealPlay();
                break;
        }
    }



    private void handlePlayFail(Object obj) {
        int errorCode = 0;
        if (obj != null) {
            ErrorInfo errorInfo = (ErrorInfo) obj;
            errorCode = errorInfo.errorCode;
            LogUtil.debugLog("MainActivity", "handlePlayFail:" + errorCode);
        }
    }



    ////////////////////////////////////////////////调用方法//////////////////////////////////////////////////////////////







    //远程开门
    public void remoteOpenDoor()
    {
        new AsyncTask<Void,Void,Boolean>()
        {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return TALKSDK.getInstance().remoteUnlock(Constants.DEVICESERIAL, (byte) 1,0);
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean)
                {
                    Toast.makeText(getApplicationContext(), "开锁成功", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "开锁失败", Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(BaseApplication.LIMITED_TASK_EXECUTOR);

    }

    //呼叫电梯
    public void callLift()
    {
        new AsyncTask<Void,Void,Boolean>()
        {
            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean result=TALKSDK.getInstance().liftControl(Constants.DEVICESERIAL);
                return result;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean)
                {
                    Toaster.showShort(MainActivity.this,getString(R.string.kLiftControlSuccess));
                }
                else
                {
                    Toaster.showShort(MainActivity.this,getString(R.string.kLiftControlFailed));
                }
            }
        }.executeOnExecutor(MyApplication.LIMITED_TASK_EXECUTOR);
    }


    @Override
    protected void onPause() {
        super.onPause();
        TALKSDK.getInstance().stopRealPlay();
    }
}
