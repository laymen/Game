package mouse.sunflower.com.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;
import java.util.Vector;

public class FingerDrawView extends View{

    private Paint paint;
    private Bitmap bitmap;
    private Canvas canvas;
    private Path path;
    private Paint bitmapPaint;
    private Paint borderPaint;
    private List<Point> points;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

//之前用的是500
    private static final String LOGTAG = "FingerDrawView";
    protected static final Point STROKE_END = new Point(1196, 1196);
    //protected static final Point CHAR_END = new Point(255, 255);

    public FingerDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);//图像边缘相对清晰一点，锯齿痕迹不那么明显
        paint.setDither(true);//设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
        paint.setColor(0xFF0000FF);//0x0079FF
        paint.setStyle(Style.STROKE);//设置画笔为空心
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(2);

        //绘制边框
        borderPaint = new Paint();
        borderPaint.setColor(0xFFFF0000);//
        borderPaint.setStrokeWidth(2);
        borderPaint.setStyle(Style.STROKE);

        bitmap = Bitmap.createBitmap(1196, 1196, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        path = new Path();
        bitmapPaint = new Paint(Paint.DITHER_FLAG);

        points = new Vector<Point>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0x0079FF);//0x0079FF   0xFFAAAAAA
        canvas.drawRect(0, 0, 1196, 1196, borderPaint);
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.drawPath(path, paint);
    }

    private void touch_start(float x, float y) {
        path.reset();
        path.moveTo(x, y);
        mX = x;
        mY = y;
        points.add(new Point((int)x, (int)y));
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        points.add(new Point((int)x, (int)y));
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        path.lineTo(mX, mY);
        // commit the path to our offscreen
        canvas.drawPath(path, paint);//绘制一个路径，参数一为Path路径对象
        // kill this so we don't double draw
        path.reset();
        points.add(STROKE_END);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public void resetView() {
        points.clear();
        bitmap.eraseColor(Color.TRANSPARENT);
        invalidate();
    }

    protected void dump() {
        for(Point pt : points) {
            Log.i("mouse----laymen", String.format("(%d, %d)", pt.x, pt.y));
        }
    }

    protected List<Point> getPoints() {
        return points;
    }
}
