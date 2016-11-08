package mouse.sunflower.com.game;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
    Button reset_button1, reco_button1;
    Button reset_button2, reco_button2;
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
    private int number1 = -1;//左边-识别后的数字
    private int number2 = -1;//右边-识别后的数字


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
        //识别
        reco_button1 = (Button) findViewById(R.id.reco_button);
        reco_button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                List<Point> points = drawViewL.getPoints();
                handleReco(points, number1,drawViewL,true);
            }
        });

        reco_button2 = (Button) findViewById(R.id.reco_button2);
        reco_button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                List<Point> points = drawViewR.getPoints();
                handleReco(points, number2,drawViewR,false);
            }
        });

        //倒计时
        send_tv = (TextView) findViewById(R.id.timer);
        send_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                randomNumber();//初始化随机数
                startTimer();
            }
        });
        //随机生成左右数字
        mNumber1 = (TextView) findViewById(R.id.number1);
        mNumber2 = (TextView) findViewById(R.id.number2);

    }

    //开始倒计时
    private void startTimer() {
        send_tv.setClickable(false);//时间按钮
        reco_button1.setClickable(true);
        reset_button1.setClickable(true);
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
                    return;
                }
                countdown--;
                number1 = -1;//还原初试的状态
                number2 = -1;
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
        reco_button1.setClickable(false);
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
    private void handleReco(List<Point> points, int number,FingerDrawView drawView,boolean mark) {
        if (points.size() == 0) {//说明用户没有输入任何的信息
            Toast.makeText(this, "请绘数字0-9！", Toast.LENGTH_SHORT).show();
        } else {
            //后识别
            ArrayList<com.game.dataStruct.Point> points1 = new ArrayList<>();
            for (int i = 0; i < points.size() - 2; i++) {
                points1.add(new com.game.dataStruct.Point(points.get(i).x, points.get(i).y));
            }
            Predict classifier = new Predict(getApplicationContext());
            number = classifier.predictList(points1);//识别后的数字
            Log.i("识别为：", number + "");

            if (number != -1) {//做左处理
                if (mark) {
                    if (number == lNumber) {
                        counRightL++;
                    } else {
                        countWrongL++;
                    }
                }else{
                    if (number == rNumber) {
                        counRightL++;
                    } else {
                        countWrongL++;
                    }

                }



            }
            Log.i("day-----day:", "识别出的数字是:" + number + "----" + "左边提示出的：" + lNumber);
            resultView.setText("正确数：" + counRightL + "&" + "错误数：" + countWrongL);
            //开始自动清屏了
            drawView.resetView();
            //进行下一轮随机数的生成
            randomNumber();
        }
    }

    private void randomNumber() {
        Random random = new Random();
        lNumber = random.nextInt(10);
        rNumber = random.nextInt(10);
        mNumber1.setText(lNumber + "");
        mNumber2.setText(rNumber + "");
    }
}
