package video.com.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;



public class VideoActivity extends AppCompatActivity implements  SeekBar.OnSeekBarChangeListener{
    private MediaPlayer mediaPlayer;//媒体播放器
    private LibVLC libVLC = null;//VLC库
    private TextView tv_current_time, tv_total_time;//显示当前播放时间，显示总时长
    private View play, pause, stop;//播放、暂停、停止
    private SurfaceView surfaceView;//视频界面
    private SeekBar seekBar;//拖动条
    private int currentPosition;//当前播放的位置
    private long timeLength;//视频时长
    private static Handler handler;
    private boolean isLaunched;
    public String live_url="http://vodhls1.douyucdn.cn/live/normal_live-1881699reLI7locR--20170517204912/playlist.m3u8?k=472b40f974fdb368d77512615a162f40&t=5a57898a&u=0&ct=h5&vid=664325&d=2eb94a746278bf54c30dc7483659b771";
    public String song_url="http://dl.stream.qqmusic.qq.com/C400004TXEXY2G2c7C.m4a?vkey=BC3B5D250EB9B09C5ACA75F8F57C37232D6F8143BE713DBEFFE90938050266E5BE5F3EEF47B894DB67295C48A75EC008A2A8E9AB022A0B84&guid=5444262974&uin=0&fromtag=66";
    public String url = "http://videows1.douyucdn.cn/live/normal_1846566620180429180812-upload" +
            "-7b76/playlist.m3u8?k=7844ec13765445fe66c769dfbf97c614&t=5b177baf&u=0&ct=h5&vid" +
            "=3924445&pt=2&d=65ad456ca65533e16c1789441fda5ce2";
    public String testUrl;
    public static final String VIDEO_URL = "video_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        requestPerm();//申请权限
        testUrl = "http://videows1.douyucdn.cn/live/normal_1846566620180429180812-upload-7b76" +
                "/playlist.m3u8?k=037872111ac880140eb19e0dcd013450&t=5b177f11&u=0&ct=h5&vid" +
                "=3924445&pt=2&d=a5623487974cf75187642ba82b2ac898";
        initView();
        handler=new Handler();
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){//如果进度的改变来自用户的拖动
            currentPosition=progress;//获取当前的位置
            mediaPlayer.setTime(currentPosition);//设置当前的播放时间
            seekBar.setProgress(currentPosition);//设置拖动条的进度
        }
    }

    private void launcher() {
        //在Activity中可以为按钮增加事件
        ArrayList<String> options = new ArrayList<>();
        //创建VLC库
        libVLC = new LibVLC(getApplication(), options);
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            //创建媒体播放器
            mediaPlayer = new MediaPlayer(libVLC);
            //设置视频界面
            mediaPlayer.getVLCVout().setVideoSurface(surfaceView.getHolder().getSurface(), surfaceView.getHolder());
            //将SurfaceView贴到MediaPlayer上
            mediaPlayer.getVLCVout().attachViews();
            //设置播放窗口的尺寸
            mediaPlayer.getVLCVout().setWindowSize(surfaceView.getWidth(), surfaceView.getHeight());
            //创建播放的媒体
//            Media media = new Media(libVLC, Environment.getExternalStorageDirectory().toString()+"/canon.flv");
//            Media media = new Media(libVLC, Uri.parse("http://funbox-w6.dwstatic.com/8/4/1546/186122-98-1447157302.mp4"));
            Media media = new Media(libVLC, Uri.parse(testUrl));
            //Media media = new Media(libVLC, Uri.parse(song_url));
            //设置媒体
            mediaPlayer.setMedia(media);
            timeLength=mediaPlayer.getLength();
            seekBar.setMax((int) timeLength);//设置拖动条的最大值
            seekBar.setProgress(currentPosition);//设置拖动条当前进度
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.play();//播放
        handler.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                currentPosition = (int) mediaPlayer.getTime();
                Log.d("Test", "播放时间 "+mediaPlayer.getTime());
                if(mediaPlayer.getMedia().getDuration()>0){
                    seekBar.setMax((int) mediaPlayer.getMedia().getDuration());
                    tv_total_time.setText(msToSongTime(mediaPlayer.getMedia().getDuration()));
                }
                seekBar.setProgress(currentPosition);
                tv_current_time.setText(msToSongTime(mediaPlayer.getTime()));
                //获取视频当前播放的时间位置（毫秒）
                if((currentPosition>=seekBar.getMax()-1000)&&currentPosition>0){
                    isLaunched = false;
                    pause.setBackground(getResources().getDrawable(R.mipmap.ic_launcher));
                    //Toast.makeText(Case5Activity.this, "播放完毕", Toast.LENGTH_SHORT).show();
                }
                handler.postDelayed(this, 100);
            }
        }, 100);
        surfaceView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                dp2px(this, 210)));
        mediaPlayer.getVLCVout().setWindowSize(getResources().getDisplayMetrics().widthPixels,
                dp2px(this, 210));
    }

    /*
    暂停按钮
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void pause(View view) {
        if(!isLaunched){
            launcher();
            isLaunched=true;
            pause.setBackground(getResources().getDrawable(R.mipmap.ic_launcher));
        }else {
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                pause.setBackground(getResources().getDrawable(R.mipmap.ic_launcher));
            }else {
                mediaPlayer.play();
                pause.setBackground(getResources().getDrawable(R.mipmap.ic_launcher));
            }
        }
    }

    /*
    设置全屏或取消全屏
     */
    public void full(View view) {
        if(getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    /*
    根据屏幕方向的变化做出调整
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);//不能删，否则会报异常
        if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            surfaceView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mediaPlayer.getVLCVout().setWindowSize(getResources().getDisplayMetrics().widthPixels,
                    getResources().getDisplayMetrics().heightPixels);
        } else {
            surfaceView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dp2px(this, 210)));
            mediaPlayer.getVLCVout().setWindowSize(getResources().getDisplayMetrics().widthPixels,
                    dp2px(this, 210));
        }

    }

    @Override
    public void onBackPressed() {
        if(getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            surfaceView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dp2px(this, 210)));
            mediaPlayer.getVLCVout().setWindowSize(getResources().getDisplayMetrics().widthPixels,
                    dp2px(this, 210));
        }else{
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
        launcher();
        isLaunched=true;
        pause.setBackground(getResources().getDrawable(R.mipmap.ic_launcher));
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            handler.getLooper().quit();
            handler.removeCallbacksAndMessages(null);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    //----------------------------------------------------------------------------------------------

    private void requestPerm() {
//        if(ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
//        }
    }

    /*
   将文件时长转换为歌曲时间
    */
    public String msToSongTime(long length){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        return sdf.format(new Date(length));
    }

    private void initView() {
        surfaceView=findViewById(R.id.surface);
        surfaceView.setFocusable(true);
        surfaceView.setFocusableInTouchMode(true);
        surfaceView.setKeepScreenOn(true);
        pause=findViewById(R.id.btn_pause);
        seekBar=findViewById(R.id.seekbar);
        tv_current_time=findViewById(R.id.tv_current_time);
        tv_total_time=findViewById(R.id.tv_total_time);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
