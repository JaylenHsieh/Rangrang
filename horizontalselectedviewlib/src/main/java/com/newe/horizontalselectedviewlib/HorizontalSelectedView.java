package com.newe.horizontalselectedviewlib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.TintTypedArray;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jaylen Hsieh
 * @date 2018/05/04
 */
public class HorizontalSelectedView extends View {
    private Context mContext;
    /**
     * 数据源字符串数组
     */
    private List<String> mStrings = new ArrayList<>();
    /**
     * 可见个数
     */
    private int seeSize = 5;
    /**
     * 每个字母所占的大小
     */
    private int anInt;
    private TextPaint mTextPaint;
    private boolean firstVisible = true;
    private int width;
    private int height;
    /**
     * 被选中文字的画笔
     */
    private Paint selectedPaint;
    private int n;
    private float downX;
    private float anOffset;
    private float selectedTextSize;
    private int selectedColor;
    private float textSize;
    private int textColor;
    private Rect mRect = new Rect();

    private int textWidth = 0;
    private int textHeight = 0;
    private int centerTextHeight = 0;


    public HorizontalSelectedView(Context context) {
        this(context, null);
    }

    public HorizontalSelectedView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public HorizontalSelectedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        setWillNotDraw(false);
        setClickable(true);
        // 初始化属性
        initAttrs(attrs);
        // 初始化画笔
        initPaint();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
        selectedPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        selectedPaint.setColor(selectedColor);
        selectedPaint.setTextSize(selectedTextSize);

    }

    /**
     * 初始化属性
     *
     * @param attrs
     */
    private void initAttrs(AttributeSet attrs) {
        TintTypedArray tta = TintTypedArray.obtainStyledAttributes(getContext(), attrs, R.styleable.HorizontalSelectedView);
        // 两种字体颜色和字体大小
        seeSize = tta.getInteger(R.styleable.HorizontalSelectedView_HorizontalSelectedViewSeeSize, 5);
        selectedTextSize = tta.getFloat(R.styleable.HorizontalSelectedView_HorizontalSelectedViewSelectedTextSize, 50);
        selectedColor = tta.getColor(R.styleable.HorizontalSelectedView_HorizontalSelectedViewSelectedTextColor, getContext().getResources().getColor(android.R.color.black));
        textSize = tta.getFloat(R.styleable.HorizontalSelectedView_HorizontalSelectedViewTextSize, 40);
        textColor = tta.getColor(R.styleable.HorizontalSelectedView_HorizontalSelectedViewTextColor, getContext().getResources().getColor(android.R.color.darker_gray));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("action", "onTouchEvent:" + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 获得点下去的 x 的坐标
                downX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                // 移动时的判断
                float scrollX = event.getX();

                if (n != 0 && n != mStrings.size() - 1) {
                    // 滑动时的偏移量，用于计算每个数字源文字的坐标值
                    anOffset = scrollX - downX;
                    break;
                } else {
                    // 当滑到两端时，添加一点阻力
                    anOffset = (float) ((scrollX - downX) / 1.5);
                }
                if (scrollX > downX) {
                    // 向右滑动时，当滑动距离大于每个单元的长度时，则改变被选中的文字
                    if (scrollX - downX >= anInt) {
                        if (n > 0) {
                            anOffset = 0;
                            n = n - 1;
                            downX = scrollX;
                        }
                    }
                } else {
                    // 向左滑动，当滑动距离大于每个单元长度时，改变被选中的数字
                    if (downX - scrollX >= anInt) {
                        if (n < mStrings.size() - 1) {
                            anOffset = 0;
                            n = n + 1;
                            downX = scrollX;
                        }
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                // 抬起手指时，偏移量归零，相当于会回弹
                anOffset = 0;
                invalidate();
                break;
            default:
                break;

        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 第一次绘制时得到控件宽高
        if (firstVisible) {
            width = getWidth();
            height = getHeight();
            anInt = width / seeSize;
            firstVisible = false;
        }
        // 加个保护，防止越界
        if (n >= 0 && n <= mStrings.size() - 1) {
            // 得到被选中的文字
            String s = mStrings.get(n);

            //得到被选中文字 绘制时所需要的宽高
            selectedPaint.getTextBounds(s, 0, s.length(), mRect);
            int centerTextWidth = mRect.width();
            centerTextHeight = mRect.height();
            // 绘制被选中文字，注意点的y坐标
            canvas.drawText(mStrings.get(n), getWidth() / 2 - centerTextWidth / 2 + anOffset, getHeight() / 2 + centerTextHeight / 2, selectedPaint);

            // 遍历mStrings, 把每个地方都绘制出来
            for (int i = 0; i < mStrings.size() - 1; i++) {
                if (n > 0 && n < mStrings.size() - 1) {
                    //因为mStrings数据源的文字长度不一样，为了让被选中两边文字距离中心宽度一样，取得左右两个文字长度的平均值
                    mTextPaint.getTextBounds(mStrings.get(n - 1), 0, mStrings.get(n - 1).length(), mRect);
                    int width1 = mRect.width();
                    mTextPaint.getTextBounds(mStrings.get(n - 1), 0, mStrings.get(n + 1).length(), mRect);
                    int width2 = mRect.width();
                    textWidth = (width1 + width2) / 2;
                }
                if (i == 0) {
                    //得到高，高度是一样的
                    mTextPaint.getTextBounds(mStrings.get(0), 0, mStrings.get(0).length(), mRect);
                    textHeight = mRect.height();
                }

                if (i != n) {
                    canvas.drawText(mStrings.get(i), (i - n) * anInt + getWidth() / 2 + anOffset, getHeight() / 2 + textHeight / 2, mTextPaint);
                }
            }
        }
    }

    /**
     * 改变中间可见文字的数目
     *
     * @param seeSizes
     */
    public void setSeeSize(int seeSizes) {
        if (seeSize > 0) {
            seeSize = seeSizes;
            invalidate();
        }
    }

    /**
     * 向左移动一个单元
     */
    public void setAnLeftOffset() {
        if (n < mStrings.size() - 1) {
            n = n + 1;
            invalidate();
        }
    }

    /**
     * 向右移动一个单元
     */
    public void setAnRightOffset(){
        if (n > 0){
            n = n -1;
            invalidate();
        }
    }

    /**
     * 获得被选中的文字
     * @return 被选中的文本
     */
    public String getSelectedString(){
        if (mStrings.size() != 0){
            return mStrings.get(n);
        }
        return null;
    }

}
