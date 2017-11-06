package com.ccl.view.radarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ccl on 2017/11/6.
 * 雷达图
 */

public class RadarView extends View {
    private float radius;//蛛网最大半径
    private int centerX;//中心点X
    private int centerY;//中心点Y
    private Paint radarPaint;
    private Paint textPaint;
    private Paint scorePaint;
    private int fieldNum;//元素数量
    private int radarCount;//网层数
    private float mAngle = 0;//水平偏移角度
    private float angle = 0;//角度
    private List<RadarModel> mData = new ArrayList<>();
    private int maxScore;//最大分数
    private int radarColor; //蛛网颜色
    private int textColor; //字体颜色
    private int scoreColor; //内容颜色
    private float textSize; //字体大小
    private float pointSize; //原点大小


    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RadarView);
        radarColor = a.getColor(R.styleable.RadarView_rvRadarColor, Color.GRAY);
        textColor = a.getColor(R.styleable.RadarView_rvTextColor, Color.BLACK);
        scoreColor = a.getColor(R.styleable.RadarView_rvScoreColor, Color.BLUE);
        textSize = a.getDimension(R.styleable.RadarView_rvTextSize, 24);
        pointSize = a.getDimension(R.styleable.RadarView_rvPointSize, 8);
        radarCount = a.getInteger(R.styleable.RadarView_rvRadarNum, 5);
        a.recycle();


        radarPaint = new Paint();
        radarPaint.setColor(radarColor);
        radarPaint.setStyle(Paint.Style.STROKE);
        radarPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);

        scorePaint = new Paint();
        scorePaint.setColor(scoreColor);
        scorePaint.setAntiAlias(true);
        scorePaint.setStrokeWidth(2);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        radius = Math.min(h, w) / 2 * 0.8f;//考虑到还要绘制文字 所以*0.8
        //中心坐标
        centerX = w / 2;
        centerY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mData == null || mData.size() == 0) {//判断数据是否存在
            return;
        }
        drawRadar(canvas);
        drawLines(canvas);
        drawText(canvas);
        drawScore(canvas);
        //旋转角度保证底部水平
        setRotation(0 - mAngle);
    }

    /**
     * 绘制蛛网 多边形根据参数来定
     *
     * @param canvas
     */
    private void drawRadar(Canvas canvas) {
        Path path = new Path();
        float distanceR = radius / (radarCount - 1);//蜘蛛丝之间的间距
        float lastTwoY = 0;
        float lastY = 0;
        float lastTwoX = 0;
        float lastX = 0;
        for (int i = 1; i < radarCount; i++) {//中点不用绘制
            float currentR = distanceR * i;
            path.reset();//每次画完一圈后需重置path
            for (int j = 0; j < fieldNum; j++) {
                if (j == 0) {//确定第一个点的位置
                    path.moveTo(centerX + currentR, centerY);
                } else {
                    //计算下一个点的位置
                    float x = (float) (centerX + currentR * Math.cos(angle * j));
                    float y = (float) (centerY + currentR * Math.sin(angle * j));
                    //绘制直线
                    path.lineTo(x, y);
                    //记录最后两点的坐标 假设垂直Y不在一个水平线上 则旋转画布保证图形正中
                    if (i == 1)//犹豫每条蛛丝角度完全相同 取一次值即可
                        if (j == fieldNum - 2) {
                            lastTwoY = y;
                            lastTwoX = x;
                        } else if (j == fieldNum - 1) {
                            lastY = y;
                            lastX = x;
                        }
                }
            }
            path.close();//最后一个点画完后闭合
            canvas.drawPath(path, radarPaint);
        }
        //计算旋转居中角度
        if (lastTwoY != lastY) {
            float lenX = lastX - lastTwoX;
            float lenY = Math.abs(lastTwoY - lastY);
            mAngle = (float) (Math.atan(lenY / lenX) / Math.PI * 180);
        }
    }

    /**
     * 绘制连线
     *
     * @param canvas
     */
    private void drawLines(Canvas canvas) {
        Path path = new Path();
        for (int i = 0; i < fieldNum; i++) {
            path.reset();
            path.moveTo(centerX, centerY);
            float x = (float) (centerX + radius * Math.cos(angle * i));
            float y = (float) (centerY + radius * Math.sin(angle * i));
            path.lineTo(x, y);
            canvas.drawPath(path, radarPaint);
        }
    }

    /**
     * 绘制文字
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();//获取字体配置
        float fontHeight = fontMetrics.descent - fontMetrics.ascent;//获取字体高度
        for (int i = 0; i < fieldNum; i++) {
            canvas.save();
            float dis = textPaint.measureText(mData.get(i).getName());
            //计算文字位置
            float x = (float) (centerX + (radius + fontHeight / 2) * Math.cos(angle * i));
            float y = (float) (centerY + (radius + fontHeight / 2) * Math.sin(angle * i));
            //根据文字长度计算偏移量保证不覆盖到蛛网
            canvas.translate(x, y);
            if (angle * i >= 0 && angle * i <= Math.PI / 2) {//第1象限
                //canvas.rotate(mAngle, 0 - dis / 2, fontHeight / 2);
                canvas.rotate(mAngle, 0, 0);
                canvas.drawText(mData.get(i).getName(), 0, 0 + fontHeight / 2, textPaint);
            } else if (angle * i >= 3 * Math.PI / 2 && angle * i <= Math.PI * 2) {//第4象限
                //canvas.rotate(mAngle, 0 - dis / 2, fontHeight / 2);
                canvas.rotate(mAngle, 0, 0);
                canvas.drawText(mData.get(i).getName(), 0, 0, textPaint);
            } else if (angle * i > Math.PI / 2 && angle * i <= Math.PI) {//第2象限
                //canvas.rotate(mAngle, 0 - dis / 2, fontHeight / 2);
                canvas.rotate(mAngle, 0, 0);
                canvas.drawText(mData.get(i).getName(), 0 - dis, 0 + fontHeight / 2, textPaint);
            } else if (angle * i >= Math.PI && angle * i < 3 * Math.PI / 2) {//第3象限
                canvas.rotate(mAngle, 0, 0);
                //canvas.rotate(mAngle, 0 - dis / 2, fontHeight / 2);
                canvas.drawText(mData.get(i).getName(), 0 - dis, 0, textPaint);
            }
            canvas.restore();
        }
    }

    /**
     * 绘制分数
     *
     * @param canvas
     */
    private void drawScore(Canvas canvas) {
        Path path = new Path();
        scorePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        scorePaint.setAlpha(255);//不透明先画边框
        for (int i = 0; i < fieldNum; i++) {
            double percent = (double) mData.get(i).getScore() / (double) maxScore;
            float x = (float) (centerX + radius * Math.cos(angle * i) * percent);
            float y = (float) (centerY + radius * Math.sin(angle * i) * percent);
            if (i == 0) {
                path.moveTo(x, y);//移动到第一个点
            } else {
                path.lineTo(x, y);
            }
            //绘制小圆点
            canvas.drawCircle(x, y, pointSize, scorePaint);
        }
        path.close();
        //画边框
        scorePaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, scorePaint);
        //绘制填充区域
        scorePaint.setAlpha(127);
        scorePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(path, scorePaint);
    }

    /**
     * 填充数据
     *
     * @param data
     */
    public void setData(List<RadarModel> data) {
        this.mData.addAll(data);
        fieldNum = mData.size();
        maxScore = 0;
        //获取最大分数
        for (RadarModel radarModel : mData) {
            if (radarModel.getScore() > maxScore)
                maxScore = radarModel.getScore();
        }
        //参数是弧度 则为角度/180 * 参数个数
        float jiaodu = 360 / fieldNum;
        angle = (float) (Math.PI * jiaodu / 180);
        invalidate();
    }

    /**
     * 设置圈数
     *
     * @param radarCount
     */
    public void setRadarCount(int radarCount) {
        this.radarCount = radarCount;
        invalidate();
    }

    public void setRadarColor(int radarColor) {
        this.radarColor = radarColor;
        invalidate();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        invalidate();
    }

    public void setScoreColor(int scoreColor) {
        this.scoreColor = scoreColor;
        invalidate();
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    static class RadarModel {
        private int score;
        private String name;

        public RadarModel(int score, String name) {
            this.score = score;
            this.name = name;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
