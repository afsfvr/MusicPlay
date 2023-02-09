package com.example.musicplayer.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.example.musicplayer.entity.LrcRow;
import com.example.musicplayer.receiver.PlayerManagerReceiver;
import com.example.musicplayer.util.Constant;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LyricView extends View {
    private static final String TAG = LyricView.class.getName();
    /**
     * 正常歌词模式
     */
    private static final int DISPLAY_MODE_NORMAL = 0;
    /**
     * 拖动歌词模式
     */
    private static final int DISPLAY_MODE_SEEK = 1;
    /**
     * 缩放歌词模式
     */
    private static final int DISPLAY_MODE_SCALE = 2;
    /**
     * 最小移动的距离，当拖动歌词时如果小于该距离不做处理
     */
    private static final int mMinSeekFiredOffset = 10;
    /**
     * 歌词的当前展示模式
     */
    private int mDisplayMode = DISPLAY_MODE_NORMAL;

    /**
     * 歌词集合，包含所有行的歌词
     */
    private List<LrcRow> mLrcRows;

    /**
     * 当前高亮歌词的行数
     */
    private int mHighLightRow = 0;
    /**
     * 当前高亮歌词的字体颜色
     */
    private int mHighLightRowColor = Color.YELLOW;
    /**
     * 不高亮歌词的字体颜色
     */
    private int mNormalRowColor = Color.WHITE;

    /**
     * 拖动歌词时，在当前高亮歌词下面的一条直线的字体颜色
     **/
    private int mSeekLineColor = Color.CYAN;
    /**
     * 拖动歌词时，展示当前高亮歌词的时间的字体颜色
     **/
    private int mSeekLineTextColor = Color.CYAN;
    /**
     * 拖动歌词时，展示当前高亮歌词的时间的字体大小默认值
     **/
    private int mSeekLineTextSize = 20;

    /**
     * 歌词字体大小默认值
     **/
    private int mLrcFontSize = 40;    // font size of lrc
    /**
     * 歌词字体大小最小值
     **/
    private int mMinLrcFontSize = 15;
    /**
     * 歌词字体大小最大值
     **/
    private int mMaxLrcFontSize = 100;

    /**
     * 两行歌词之间的间距
     **/
    private int mPaddingY = mLrcFontSize;
    /**
     * 拖动歌词时，在当前高亮歌词下面的一条直线的起始位置
     **/
    private int mSeekLinePaddingX = 0;

    /**
     * 点击歌词的监听类
     **/
    private OnClickListener onClickListener;

    /**
     * 当没有歌词的时候展示的内容
     **/
    private String mLoadingLrcTip = "无歌词，点击选择歌词文件";

    private Paint mPaint;

    private Paint framePaint;

    /**
     * 当前播放的时间
     */
    long currentMillis;
    private long slideTime;
    private final Timer timer = new Timer();
    /**
     * 拖动停止后的持续时间
     */
    private static final int DURATION = 2000;

    private boolean desktopLrc = false;
    private boolean noLyric = true;

    public LyricView(Context context) {
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(mLrcFontSize);
        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setTextSize(mLrcFontSize);
        framePaint.setColor(Color.BLACK);
        framePaint.setTextAlign(Align.CENTER);
        //        framePaint.setTypeface(Typeface.DEFAULT_BOLD);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(3);
    }

    public LyricView(Context context, AttributeSet attr) {
        super(context, attr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(mLrcFontSize);
        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setTextSize(mLrcFontSize);
        framePaint.setColor(Color.BLACK);
        framePaint.setTextAlign(Align.CENTER);
        //        framePaint.setTypeface(Typeface.DEFAULT_BOLD);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(3);
    }

    public void setDesktopLrc(boolean desktopLrc) {
        this.desktopLrc = desktopLrc;
    }

    public void setmHighLightRowColor(int mHighLightRowColor) {
        if (mHighLightRowColor == Color.WHITE) mNormalRowColor = Color.GRAY;
        else mNormalRowColor = Color.WHITE;
        this.mHighLightRowColor = mHighLightRowColor;
    }

    public void setLoadingTipText(String text) {
        mLoadingLrcTip = text;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int height = getHeight(); // height of this view
        final int width = getWidth(); // width of this view
        //当没有歌词的时候
        if (noLyric) {
            if (mLoadingLrcTip != null) {
                // draw tip when no lrc.
                mPaint.setColor(mHighLightRowColor);
                mPaint.setTextSize(mLrcFontSize);
                framePaint.setTextSize(mLrcFontSize);
                mPaint.setTextAlign(Align.CENTER);
                canvas.drawText(mLoadingLrcTip, width / 2f, height / 2f - mLrcFontSize, framePaint);
                canvas.drawText(mLoadingLrcTip, width / 2f, height / 2f - mLrcFontSize, mPaint);
            }
            return;
        }

        int rowY, rowNum; // vertical point of each row.
        final int rowX = width / 2;
        /**
         * 分以下三步来绘制歌词：
         *
         * 	第1步：高亮地画出正在播放的那句歌词
         *	第2步：画出正在播放的那句歌词的上面可以展示出来的歌词
         *	第3步：画出正在播放的那句歌词的下面的可以展示出来的歌词
         */

        // 1、 高亮地画出正在要高亮的的那句歌词
        int highlightRowY = height / 2 - mLrcFontSize;

        LrcRow lrcRow = null;
        try {
            lrcRow = mLrcRows.get(mHighLightRow);
            if (mDisplayMode == DISPLAY_MODE_SEEK || lrcRow.getEndTime() <= lrcRow.getStartTime()) {
                drawHighLrcRow(canvas, height, rowX, highlightRowY);
            } else {
                drawKaraokeHighLightLrcRow(canvas, width, rowX, highlightRowY);
            }

            // 上下拖动歌词的时候 画出拖动要高亮的那句歌词的时间 和 高亮的那句歌词下面的一条直线
            if (mDisplayMode == DISPLAY_MODE_SEEK) {
                // 画出高亮的那句歌词下面的一条直线
                mPaint.setColor(mSeekLineColor);
                //该直线的x坐标从0到屏幕宽度  y坐标为高亮歌词和下一行歌词中间
                canvas.drawLine(mSeekLinePaddingX, highlightRowY + mPaddingY, width - mSeekLinePaddingX, highlightRowY + mPaddingY, mPaint);

                // 画出高亮的那句歌词的时间
                mPaint.setColor(mSeekLineTextColor);
                mPaint.setTextSize(mSeekLineTextSize);
                mPaint.setTextAlign(Align.LEFT);
                canvas.drawText(lrcRow.getStartTimeString(), 0, highlightRowY, mPaint);
            }

            // 2、画出正在播放的那句歌词的上面可以展示出来的歌词
            mPaint.setColor(mNormalRowColor);
            mPaint.setTextSize(mLrcFontSize);
            framePaint.setTextSize(mLrcFontSize);
            mPaint.setTextAlign(Align.CENTER);
            rowNum = mHighLightRow - 1;
            rowY = highlightRowY - mPaddingY - mLrcFontSize;

            //画出正在播放的那句歌词的上面所有的歌词
            while (rowY > - mLrcFontSize && rowNum >= 0) {
                String text = mLrcRows.get(rowNum).getContent();
                canvas.drawText(text, rowX, rowY, framePaint);
                canvas.drawText(text, rowX, rowY, mPaint);
                rowY -= (mPaddingY + mLrcFontSize);
                rowNum--;
            }

            // 3、画出正在播放的那句歌词的下面的可以展示出来的歌词
            rowNum = mHighLightRow + 1;
            rowY = highlightRowY + mPaddingY + mLrcFontSize;

            //画出正在播放的那句歌词的所有下面的可以展示出来的歌词
            while (rowY < height && rowNum < mLrcRows.size()) {
                String text = mLrcRows.get(rowNum).getContent();
                canvas.drawText(text, rowX, rowY, framePaint);
                canvas.drawText(text, rowX, rowY, mPaint);
                rowY += (mPaddingY + mLrcFontSize);
                rowNum++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void drawKaraokeHighLightLrcRow(Canvas canvas, int width, int rowX, int highlightRowY) {
        LrcRow highLrcRow = mLrcRows.get(mHighLightRow);
        String highlightText = highLrcRow.getContent();

        // 先画一层普通颜色的
        mPaint.setColor(mNormalRowColor);
        mPaint.setTextSize(mLrcFontSize);
        framePaint.setTextSize(mLrcFontSize);
        mPaint.setTextAlign(Align.CENTER);
        canvas.drawText(highlightText, rowX, highlightRowY, framePaint);
        canvas.drawText(highlightText, rowX, highlightRowY, mPaint);

        // 再画一层高亮颜色的
        int highLineWidth = (int) mPaint.measureText(highlightText);
        int leftOffset = (width - highLineWidth) / 2;
        long start = highLrcRow.getStartTime();
        long end = highLrcRow.getEndTime();
        // 高亮的宽度
        int highWidth = (int) ((currentMillis - start) * 1.0f / (end - start) * highLineWidth);
        if (highWidth > 0) {
            //画一个 高亮的bitmap
            mPaint.setColor(mHighLightRowColor);
            Bitmap textBitmap = Bitmap.createBitmap(highWidth, highlightRowY + mPaddingY, Bitmap.Config.ARGB_8888);
            Canvas textCanvas = new Canvas(textBitmap);
            textCanvas.drawText(highlightText, highLineWidth / 2f, highlightRowY, mPaint);
            canvas.drawBitmap(textBitmap, leftOffset, 0, mPaint);
            textBitmap.recycle();
        }
    }

    private void drawHighLrcRow(Canvas canvas, int height, int rowX, int highlightRowY) {
        String highlightText = mLrcRows.get(mHighLightRow).getContent();
        mPaint.setColor(mHighLightRowColor);
        mPaint.setTextSize(mLrcFontSize);
        framePaint.setTextSize(mLrcFontSize);
        mPaint.setTextAlign(Align.CENTER);
        canvas.drawText(highlightText, rowX, highlightRowY, framePaint);
        canvas.drawText(highlightText, rowX, highlightRowY, mPaint);
    }

    private float mLastMotionY;
    /**
     * 第一个手指的坐标
     **/
    private PointF mPointerOneLastMotion = new PointF();
    /**
     * 第二个手指的坐标
     **/
    private PointF mPointerTwoLastMotion = new PointF();
    /**
     * 是否是第一次移动，当一个手指按下后开始移动的时候，设置为true,
     * 当第二个手指按下的时候，即两个手指同时移动的时候，设置为false
     */
    private boolean mIsFirstMove = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (desktopLrc) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            //手指按下
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "down,mLastMotionY:" + mLastMotionY);
                mLastMotionY = event.getY();
                mIsFirstMove = true;
                invalidate();
                break;
            //手指移动
            case MotionEvent.ACTION_MOVE:
                if (noLyric) {
                    if (Math.abs(event.getY() - mLastMotionY) > mMinSeekFiredOffset) {
                        mDisplayMode = DISPLAY_MODE_SEEK;
                    }
                } else {
                    if (event.getPointerCount() == 2) {
                        Log.d(TAG, "two move");
                        doScale(event);
                        return true;
                    }
                    Log.d(TAG, "one move");
                    // single pointer mode ,seek
                    //如果是双指同时按下，进行歌词大小缩放，抬起其中一个手指，另外一个手指不离开屏幕地移动的话，不做任何处理
                    if (mDisplayMode == DISPLAY_MODE_SCALE) {
                        //if scaling but pointer become not two ,do nothing.
                        return true;
                    }
                    //如果一个手指按下，在屏幕上移动的话，拖动歌词上下
                    doSeek(event);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                //手指抬起
            case MotionEvent.ACTION_UP:
                if (onClickListener != null) {
                    if (noLyric) {
                        if (mDisplayMode != DISPLAY_MODE_SEEK) {
                            onClickListener.onClick();
                        }
                    } else {
                        if (mDisplayMode == DISPLAY_MODE_SEEK) {
                            slideTime = System.currentTimeMillis();
                            timer.schedule(new LrcTask(), DURATION);
                        } else if (mDisplayMode == DISPLAY_MODE_NORMAL) {
                            if (System.currentTimeMillis() - slideTime < DURATION) {
                                onClickListener.onSlideClick((int) mLrcRows.get(mHighLightRow).getStartTime());
                                slideTime = 0;
                            } else {
                                onClickListener.onClick();
                            }
                            invalidate();
                        } else {
                            invalidate();
                        }
                    }
                }
                mDisplayMode = DISPLAY_MODE_NORMAL;
                break;
        }
        return true;
    }

    /**
     * 处理双指在屏幕移动时的，歌词大小缩放
     */
    private void doScale(MotionEvent event) {
        //如果歌词的模式为：拖动歌词模式
        if (mDisplayMode == DISPLAY_MODE_SEEK) {
            //如果是单指按下，在进行歌词上下滚动，然后按下另外一个手指，则把歌词模式从 拖动歌词模式 变为 缩放歌词模式
            mDisplayMode = DISPLAY_MODE_SCALE;
            Log.d(TAG, "change mode from DISPLAY_MODE_SEEK to DISPLAY_MODE_SCALE");
            return;
        }
        // two pointer mode , scale font
        if (mIsFirstMove) {
            mDisplayMode = DISPLAY_MODE_SCALE;
            invalidate();
            mIsFirstMove = false;
            //两个手指的x坐标和y坐标
            setTwoPointerLocation(event);
        }
        //获取歌词大小要缩放的比例
        int scaleSize = getScale(event);
        Log.d(TAG, "scaleSize:" + scaleSize);
        //如果缩放大小不等于0，进行缩放，重绘LrcView
        if (scaleSize != 0) {
            setNewFontSize(scaleSize);
            invalidate();
        }
        setTwoPointerLocation(event);
    }

    /**
     * 处理单指在屏幕移动时，歌词上下滚动
     */
    private void doSeek(MotionEvent event) {
        float y = event.getY();//手指当前位置的y坐标
        float offsetY = y - mLastMotionY; //第一次按下的y坐标和目前移动手指位置的y坐标之差
        //如果移动距离小于10，不做任何处理
        if (Math.abs(offsetY) < mMinSeekFiredOffset) {
            return;
        }
        //将模式设置为拖动歌词模式
        mDisplayMode = DISPLAY_MODE_SEEK;
        int rowOffset = Math.abs((int) offsetY / mLrcFontSize); //歌词要滚动的行数

        Log.d(TAG, "move to new hightlightrow : " + mHighLightRow + " offsetY: " + offsetY + " rowOffset:" + rowOffset);

        if (offsetY < 0) {
            //手指向上移动，歌词向下滚动
            mHighLightRow += rowOffset;//设置要高亮的歌词为 当前高亮歌词 向下滚动rowOffset行后的歌词
        } else if (offsetY > 0) {
            //手指向下移动，歌词向上滚动
            mHighLightRow -= rowOffset;//设置要高亮的歌词为 当前高亮歌词 向上滚动rowOffset行后的歌词
        }
        //设置要高亮的歌词为0和mHignlightRow中的较大值，即如果mHignlightRow < 0，mHighLightRow=0
        mHighLightRow = Math.max(0, mHighLightRow);
        //设置要高亮的歌词为0和mHignlightRow中的较小值，即如果mHignlight > RowmLrcRows.size()-1，mHighLightRow=mLrcRows.size()-1
        mHighLightRow = Math.min(mHighLightRow, mLrcRows.size() - 1);
        //如果歌词要滚动的行数大于0，则重画LrcView
        if (rowOffset > 0) {
            mLastMotionY = y;
            invalidate();
        }
    }

    /**
     * 设置当前两个手指的x坐标和y坐标
     */
    private void setTwoPointerLocation(MotionEvent event) {
        mPointerOneLastMotion.x = event.getX(0);
        mPointerOneLastMotion.y = event.getY(0);
        mPointerTwoLastMotion.x = event.getX(1);
        mPointerTwoLastMotion.y = event.getY(1);
    }

    /**
     * 设置缩放后的字体大小
     */
    private void setNewFontSize(int scaleSize) {
        //设置歌词缩放后的的最新字体大小
        mLrcFontSize += scaleSize;
        mLrcFontSize = Math.max(mLrcFontSize, mMinLrcFontSize);
        mLrcFontSize = Math.min(mLrcFontSize, mMaxLrcFontSize);

        //设置显示高亮的那句歌词的时间最新字体大小
        mSeekLineTextSize = mLrcFontSize / 2;

        //设置两行歌词之间的间距
        mPaddingY = mLrcFontSize;
    }

    /**
     * 获取歌词大小要缩放的比例
     */
    private int getScale(MotionEvent event) {
        Log.d(TAG, "scaleSize getScale");
        float x0 = event.getX(0);
        float y0 = event.getY(0);
        float x1 = event.getX(1);
        float y1 = event.getY(1);

        float maxOffset = 0; // max offset between x or y axis,used to decide scale size

        boolean zooMin = false;
        //第一次双指之间的x坐标的差距
        float oldXOffset = Math.abs(mPointerOneLastMotion.x - mPointerTwoLastMotion.x);
        //第二次双指之间的x坐标的差距
        float newXOffset = Math.abs(x1 - x0);

        //第一次双指之间的y坐标的差距
        float oldYOffset = Math.abs(mPointerOneLastMotion.y - mPointerTwoLastMotion.y);
        //第二次双指之间的y坐标的差距
        float newYOffset = Math.abs(y1 - y0);

        //双指移动之后，判断双指之间移动的最大差距
        maxOffset = Math.max(Math.abs(newXOffset - oldXOffset), Math.abs(newYOffset - oldYOffset));
        //如果x坐标移动的多一些
        if (maxOffset == Math.abs(newXOffset - oldXOffset)) {
            //如果第二次双指之间的x坐标的差距大于第一次双指之间的x坐标的差距则是放大，反之则缩小
            zooMin = newXOffset > oldXOffset ? true : false;
        }
        //如果y坐标移动的多一些
        else {
            //如果第二次双指之间的y坐标的差距大于第一次双指之间的y坐标的差距则是放大，反之则缩小
            zooMin = newYOffset > oldYOffset;
        }
        Log.d(TAG, "scaleSize maxOffset:" + maxOffset);
        if (zooMin) {
            return (int) (maxOffset / 10);//放大双指之间移动的最大差距的1/10
        } else {
            return - (int) (maxOffset / 10);//缩小双指之间移动的最大差距的1/10
        }
    }

    /**
     * 设置歌词行集合
     *
     * @param lrcRows
     */
    public void setLrc(List<LrcRow> lrcRows) {
        noLyric = lrcRows == null || lrcRows.size() == 0;
        mHighLightRow = 0;
        mLrcRows = lrcRows;
        invalidate();
    }

    /**
     * 播放的时候调用该方法滚动歌词，高亮正在播放的那句歌词
     *
     * @param time
     */
    public void seekLrcToTime(long time) {
        if (noLyric || mDisplayMode != DISPLAY_MODE_NORMAL || System.currentTimeMillis() - slideTime < DURATION) {
            return;
        }

        currentMillis = time;
        Log.d(TAG, "seekLrcToTime:" + time);

        for (int i = 0; i < mLrcRows.size(); i++) {
            LrcRow current = mLrcRows.get(i);
            LrcRow next = i + 1 == mLrcRows.size() ? null : mLrcRows.get(i + 1);
            /**
             *  正在播放的时间大于current行的歌词的时间而小于next行歌词的时间， 设置要高亮的行为current行
             *  正在播放的时间大于current行的歌词，而current行为最后一句歌词时，设置要高亮的行为current行
             */
            if ((time >= current.getStartTime() && next != null && time < next.getStartTime())
                    || (time > current.getStartTime() && next == null)) {
                mHighLightRow = i;
                invalidate();
                return;
            }
        }
    }

    /**
     * 点击歌词时的监听
     */
    public interface OnClickListener {
        /**
         * 点击方法
         */
        void onClick();

        /**
         * 拖动歌词后的点击方法
         */
        void onSlideClick(int time);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    class LrcTask extends TimerTask {
        @Override
        public void run() {
            if (mDisplayMode == DISPLAY_MODE_NORMAL && PlayerManagerReceiver.getMediaPlayerStatus() != Constant.STATUS_PLAY) {
                invalidate();
                seekLrcToTime(currentMillis);
            }
        }
    }
}