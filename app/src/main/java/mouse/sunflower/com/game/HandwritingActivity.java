package mouse.sunflower.com.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.game.classify.Predict;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mouse on 2016/11/2.
 */
public class HandwritingActivity extends Activity {
    Button reset_button1;
    Button reset_button2;
    Button reco_button2;
    //手势写数字
    private FingerDrawView drawViewL;
    private FingerDrawView drawViewR;
    private TextView resultView;
    //倒计时
    private Timer mTimer = null;
    private TextView send_tv;
    //随机生成左右数字，是为了呈现在android界面上
    private TextView mNumber1;
    private TextView mNumber2;
    private int lNumber;//记录左边输入框里面的数据
    private int rNumber;//记录右边输入框里面的数据
    //添加音乐
    MediaPlayer mMediaPlayer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置去标题
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置横屏
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.main);
        //添加音乐
        mMediaPlayer = new MediaPlayer();

        drawViewL = (FingerDrawView) findViewById(R.id.draw_view1);//左手绘
        drawViewR = (FingerDrawView) findViewById(R.id.draw_view2);//右手绘

        resultView = (TextView) findViewById(R.id.result);
        resultView.setText("正确数：" + 0 + "\t&\t" + "错误数：" + 0);
        //重置
        reset_button1 = (Button) findViewById(R.id.reset_button1);
        reset_button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawViewL.resetView();
            }
        });
        reset_button2 = (Button) findViewById(R.id.reset_button2);
        reset_button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawViewR.resetView();
            }
        });

        reco_button2 = (Button) findViewById(R.id.reco_button2);
        reco_button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //左手
                List<Point> points1 = drawViewL.getPoints();
                handleReco1(points1, drawViewL, true);
                //右手
                List<Point> points2 = drawViewR.getPoints();
                handleReco2(points2, drawViewR, false);

                //进行下一轮随机数的生成
                randomNumber();
            }
        });
        reco_button2.setClickable(false);
        //倒计时
        send_tv = (TextView) findViewById(R.id.timer);
        send_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                randomNumber();//初始化随机数
                startTimer();
                //添加音乐播放
                if (mMediaPlayer.isPlaying()){
                    mMediaPlayer.reset();
                }
                mMediaPlayer=MediaPlayer.create(HandwritingActivity.this,R.raw.bg);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.start();

            }
        });
        //随机生成左右数字
        mNumber1 = (TextView) findViewById(R.id.number1);
        mNumber2 = (TextView) findViewById(R.id.number2);

    }

    //开始倒计时
    private void startTimer() {
        send_tv.setClickable(false);//时间按钮
        reco_button2.setClickable(true);
        reset_button1.setClickable(true);
        reset_button2.setClickable(true);
        if (mTimer == null) {
            mTimer = new Timer(true);
        }
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 0, 1000);
    }

    private int countdown = 60;
    private int counRightL = 0;//记录正确的个数
    private int countWrongL = 0;//记录错误的个数
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            if (msg.what == 0) {
                send_tv.setText(countdown + "秒");
                send_tv.setAlpha(0.5f);
                if (0 == countdown) {
                    //停止倒计时显示页面
                    resume();
                    stopTimer();
                    counRightL = 0;
                    countWrongL = 0;
                    drawViewL.resetView();//清屏
                    drawViewR.resetView();
                    //关闭音乐
                    if (mMediaPlayer.isPlaying()){
                        mMediaPlayer.stop();
                    }
                    return;
                }
                countdown--;
            }
        }
    };

    private void resume() {
        stopTimer();
        ivisible();
        countdown = 60;
        //倒计时结束后,让按钮重新可点击
        send_tv.setClickable(true);
        send_tv.setText("开始");
        send_tv.setAlpha(0.9f);
    }

    private void ivisible() {//不可见
        reco_button2.setClickable(false);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 对数据进行识别处理
     */
    private void handleReco1(List<Point> points, FingerDrawView drawView, boolean mark) {
        if (points.size() == 0) {//说明用户没有输入任何的信息
            Toast.makeText(this, "请绘数字0-9！", Toast.LENGTH_SHORT).show();
        } else {
            //后识别
            ArrayList<com.game.dataStruct.Point> points1 = new ArrayList<>();
            for (int i = 0; i < points.size() - 2; i++) {
                points1.add(new com.game.dataStruct.Point(points.get(i).x, points.get(i).y));
            }
            Predict classifier = new Predict(getApplicationContext());
            int[] numberlist = classifier.predictList(points1);//识别后的数字
            for (int i = 0; i < numberlist.length; i++) {
                Log.i("左手识别为：", numberlist[i] + "");
            }
            boolean find = false;
            if (mark) {//true时为左手
                for (int i = 0; i < numberlist.length; i++) {
                    if (numberlist[i] == lNumber) {
                        counRightL++;
                        find = true;
                        countdown+=2;
                        Log.i("mouse-左手输入对了:", "mouse-左手输入对了");
                        break;
                    }
                }
                if (find == false) {
                    //没有的话返回false
                    countWrongL++;
                    find = true;
                    Log.i("mouse-左手输入错了:", "mouse-左手输入错了");
                }

            }
            Log.i("day-----day:", "左提示出的：" + lNumber);
            resultView.setText("正确数：" + counRightL + "&" + "错误数：" + countWrongL);
            //开始自动清屏了
            drawView.resetView();
        }

    }

    private void handleReco2(List<Point> points, FingerDrawView drawView, boolean mark) {
        if (points.size() == 0) {//说明用户没有输入任何的信息
            Toast.makeText(this, "请绘数字0-9！", Toast.LENGTH_SHORT).show();
        } else {
            //后识别
            ArrayList<com.game.dataStruct.Point> points1 = new ArrayList<>();
            for (int i = 0; i < points.size() - 2; i++) {
                points1.add(new com.game.dataStruct.Point(points.get(i).x, points.get(i).y));
            }
            Predict classifier = new Predict(getApplicationContext());
            int[] numberlist = classifier.predictList(points1);//识别后的数字
            Log.i("day-----day:", "右提示出的：" + rNumber);
            for (int i = 0; i < numberlist.length; i++) {
                Log.i("右手识别为：", numberlist[i] + "");
            }
            boolean find = false;
            if (!mark) {//false时为右手
                for (int i = 0; i < numberlist.length; i++) {
                    if (numberlist[i] == rNumber) {
                        counRightL++;
                        find = true;
                        countdown+=2;
                        Log.i("mouse-右手输入对了:", "右手输入对了");
                        break;
                    }
                }
                if (find == false) {
                    //没有的话返回false
                    countWrongL++;
                    find = true;
                    Log.i("mouse-右手输入错了:", "右手输入错了");
                }

            }
        }
        resultView.setText("正确数：" + counRightL + "&" + "错误数：" + countWrongL);
        //开始自动清屏了
        drawView.resetView();
    }

    private void randomNumber() {
        Random random = new Random();
        lNumber = random.nextInt(10);
        rNumber = random.nextInt(10);
        mNumber1.setText(lNumber + "");
        mNumber2.setText(rNumber + "");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 创建退出对话框
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            // 设置对话框标题
            //isExit.setTitle("系统提示");
            // 设置对话框消息
            isExit.setMessage("确定要退出吗？");
            // 添加选择按钮并注册监听
            isExit.setButton("确定", listener);
            isExit.setButton2("取消", listener);
            // 显示对话框
            isExit.show();

        }
        return false;

    }

    /**
     * 监听对话框里面的button点击事件
     */
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    mMediaPlayer.stop();
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };
}
