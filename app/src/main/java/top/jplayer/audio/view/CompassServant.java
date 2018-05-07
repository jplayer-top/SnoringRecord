package top.jplayer.audio.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import top.jplayer.audio.R;
import top.jplayer.audio.utils.ColorUtil;
import top.jplayer.audio.utils.FontMatrixUtils;

/**
 * 自定义命名空间
 * xmlns:dyzs="http://schemas.android.com/apk/res/com.dyzs.common.ui.CompassServant"
 */

public class CompassServant extends View {
    private static final String TAG = CompassServant.class.getSimpleName();
    private Context mCtx;// god of the universal
    private float mWidth, mHeight;
    private float mPadding;// 外圆相对于 view 的间距
    private float mSpacing;// 刻度与外圆, 刻度与内弧之间的间隔 the abyss between tick mark and outer circle
    private float[] mCircleCenter = new float[2];// view 的圆心点center of the universal
    private float mOuterCircleRadius;// 圆半径
    private float mPointerRadius;// 指针半径
    private float mTickRadius;// 刻度半径
    private float mInnerArcRadius;// 内弧半径
    private float mInnerArcDegree;// 内弧的度数
    private float mPerDegree;// 刻度值的平均度数
    private float mPointerDegree;// 当前指针刻度
    private RectF mPointerRectF, mTickRectF, mInnerArcRectF, mTextRectF;
    private float mCircleWidth;// 外圆的宽度 outer circle width
    private float mTickLength;// 刻度线的长度 tick mark pointer length
    private float mTickWidth;// 刻度线的宽度 tick mark pointer width
    private float mInnerArcWidth;// 内弧的宽度 color gradient width
    private float mPointerWidth;// 当前指针宽度 pointer width
    private Paint mOuterCirclePaint;// outer circle paint
    private Paint mTickPaint;// tick mark paint
    private Paint mInnerArcPaint;// color gradient paint
    private Paint mPointerPaint;// pointer paint
    private Paint mTextPaint, mTrianglePaint;
    private float mStartAngle;// 画布绘圆的开始角度
    private int mDecibel;// 总刻度数 tick mark total count
    private int[] mInnerArcGradientColors;// 内圆弧的渐变颜色值
    private float[] mGalaxyPositions;// could't be authorized
    private int mC1, mC2, mC3, mC4;// 内圆弧的颜色1,2,3,4
    private int mCCommander;// xml设置属性时,控制显示的颜色个数 command display colors, value limits[2~4]
    private int mBackgroundColor;// 默认获取背景颜色
    private float mLeapTextSize;// 当前显示刻度的文本的大小
    private static final int[] SYS_ATTRS = new int[]{
            android.R.attr.background,
            android.R.attr.padding
    };// got new skill
    @Deprecated
    private Path mTrianglePath;

    public CompassServant(Context context) {
        this(context, null);
    }

    public CompassServant(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CompassServant(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, -1);
        init(context, attrs, defStyleAttr, -1);
    }

    @TargetApi(21)
    public CompassServant(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
        // startPointerAnim();
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mCtx = context;
        /* get system attr */
        TypedArray ta = context.obtainStyledAttributes(attrs, SYS_ATTRS);
        mBackgroundColor = ta.getColor(0, ContextCompat.getColor(context, R.color.black));
        mPadding = ta.getDimension(1, dp2Px(10));
        ta.recycle();
        setBackgroundColor(mBackgroundColor);

        ta = context.obtainStyledAttributes(attrs, R.styleable.CompassServant, defStyleAttr, defStyleRes);
        mCCommander = ta.getInt(R.styleable.CompassServant_cs_color_commander, 3);
        mC1 = ta.getColor(R.styleable.CompassServant_cs_color1, ContextCompat.getColor(context, R.color.white));
        mC2 = ta.getColor(R.styleable.CompassServant_cs_color2, ContextCompat.getColor(context, R.color.oxygen_green));
        mC3 = ta.getColor(R.styleable.CompassServant_cs_color3, ContextCompat.getColor(context, R.color.cinnabar_red));
        mC4 = ta.getColor(R.styleable.CompassServant_cs_color4, ContextCompat.getColor(context, R.color.pale_blue));
        mDecibel = ta.getInteger(R.styleable.CompassServant_cs_decibel, 119);
        mTickLength = ta.getDimension(R.styleable.CompassServant_cs_tick_mark_length, 80f);
        mCircleWidth = ta.getDimension(R.styleable.CompassServant_cs_outer_circle, 20f);
        mInnerArcDegree = ta.getFloat(R.styleable.CompassServant_cs_galaxy_degree, 280f);
        mLeapTextSize = ta.getDimension(R.styleable.CompassServant_cs_text_size, 50f);
        ta.recycle();

        mSpacing = 15f;
        mInnerArcDegree = mInnerArcDegree % 361f;
        mPerDegree = mInnerArcDegree / mDecibel;
        mStartAngle = (360f - mInnerArcDegree) / 2 + 90f;
        mPointerDegree = 280f; // def degree value
        mInnerArcWidth = mCircleWidth * 1.5f;
        mTickWidth = (float) (mPerDegree / 4.5f * 2 * Math.PI);
        mPointerWidth = mTickWidth * 2;

        setInnerArcColors(calcInitColors());

        mOuterCirclePaint = new Paint();
        mOuterCirclePaint.setAntiAlias(true);
        mOuterCirclePaint.setStyle(Paint.Style.STROKE);
        mOuterCirclePaint.setStrokeWidth(mCircleWidth);
        mOuterCirclePaint.setColor(ContextCompat.getColor(context, R.color.tension_grey));

        mTickPaint = new Paint();
        mTickPaint.setAntiAlias(true);
        mTickPaint.setStyle(Paint.Style.STROKE);
        mTickPaint.setStrokeWidth(mTickWidth);
        mTickPaint.setColor(ContextCompat.getColor(context, R.color.tension_grey));

        mInnerArcPaint = new Paint();
        mInnerArcPaint.setAntiAlias(true);
        mInnerArcPaint.setStyle(Paint.Style.STROKE);
        mInnerArcPaint.setStrokeWidth(mInnerArcWidth);
        mInnerArcPaint.setColor(ContextCompat.getColor(context, R.color.girl_pink));

        mPointerPaint = new Paint();
        mPointerPaint.setAntiAlias(true);
        mPointerPaint.setStyle(Paint.Style.STROKE);
        mPointerPaint.setStrokeWidth(mPointerWidth);
        mPointerPaint.setColor(ContextCompat.getColor(context, R.color.alice_blue));

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mBackgroundColor);
        mTextPaint.setStrokeWidth(4f);
        mTextPaint.setTextSize(mLeapTextSize);

        mTrianglePaint = new Paint();
        mTrianglePaint.setAntiAlias(true);
        mTrianglePaint.setColor(ContextCompat.getColor(context, R.color.tension_grey));
    }

    private int[] calcInitColors() {
        mCCommander = mCCommander % 5;
        if (mCCommander < 2) {
            mCCommander = 2;
        }
        int[] retColors = new int[mCCommander], colors = new int[]{mC1, mC2, mC3, mC4};
        // System.arraycopy(colors, 0, retColors, 0, mCCommander);
        for (int i = 0; i < mCCommander; i++) {
            retColors[i] = colors[i];
        }
        return retColors;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        System.out.println("on measure");
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        if (mWidth >= mHeight) {
            mOuterCircleRadius = mHeight / 2 - mPadding;
        } else {
            mOuterCircleRadius = mWidth / 2 - mPadding;
        }
        mCircleCenter[0] = mWidth / 2;
        mCircleCenter[1] = mHeight / 2;

        mPointerRadius = mOuterCircleRadius - mCircleWidth / 2;
        float l, t, r, b;
        l = mCircleCenter[0] - mPointerRadius;
        t = mCircleCenter[1] - mPointerRadius;
        r = mCircleCenter[0] + mPointerRadius;
        b = mCircleCenter[1] + mPointerRadius;
        mPointerRectF = new RectF(l, t, r, b);

        mTickRadius = mOuterCircleRadius - mCircleWidth - mTickLength / 2 - mSpacing;
        l = mCircleCenter[0] - mTickRadius;
        t = mCircleCenter[1] - mTickRadius;
        r = mCircleCenter[0] + mTickRadius;
        b = mCircleCenter[1] + mTickRadius;
        mTickRectF = new RectF(l, t, r, b);

        mInnerArcRadius = mOuterCircleRadius - mCircleWidth - mTickLength - mInnerArcWidth / 2 - mSpacing * 2;
        l = mCircleCenter[0] - mInnerArcRadius;
        t = mCircleCenter[1] - mInnerArcRadius;
        r = mCircleCenter[0] + mInnerArcRadius;
        b = mCircleCenter[1] + mInnerArcRadius;
        mInnerArcRectF = new RectF(l, t, r, b);

        l += mInnerArcWidth;
        t += mInnerArcWidth;
        r -= mInnerArcWidth;
        b -= mInnerArcWidth;
        mTextRectF = new RectF(l, t, r, b);

        mTrianglePath = new Path();
        mTrianglePath.moveTo(mCircleCenter[0] - mPadding / 2, mPadding / 4);
        mTrianglePath.lineTo(mCircleCenter[0] + mPadding / 2, mPadding / 4);
        mTrianglePath.lineTo(mCircleCenter[0], mPadding - mPadding / 4);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        System.out.println("on size changed");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDarkFlameMaster(canvas);
        drawInnerArc(canvas);
    }

    /**
     * 画外圆, 计算当前指针角度对应的刻度, 遍历旋转绘制刻度
     *
     * @param canvas
     */
    private void drawDarkFlameMaster(Canvas canvas) {
        /* draw circle */
        canvas.drawCircle(mCircleCenter[0], mCircleCenter[1], mPointerRadius, mOuterCirclePaint);

        /* draw tick mark, triangle mark and color pointer */
        int dBPointer = (int) (mPointerDegree * mDecibel / mInnerArcDegree);
        for (int i = 0; i <= mDecibel; i++) {
            canvas.save();
            float rotateDegree;
            rotateDegree = mStartAngle + 90 + mPerDegree * i;
            canvas.rotate(rotateDegree, mCircleCenter[0], mCircleCenter[1]);
            if (i <= dBPointer) {
                mTickPaint.setColor(getPointerColor(i));//ContextCompat.getColor(mCtx, R.color.blair_grey));
                canvas.drawLine(
                        mCircleCenter[0],
                        mTickRectF.top - mTickLength / 2,
                        mCircleCenter[0],
                        mTickRectF.top + mTickLength / 2,
                        mTickPaint);
                if (i == dBPointer) {
                    mPointerPaint.setColor(getPointerColor(i));
                    canvas.drawLine(
                            mCircleCenter[0],
                            mPadding,
                            mCircleCenter[0],
                            mTickRectF.top + mTickLength / 2,
                            mPointerPaint);
                    // canvas.drawPath(mTrianglePath, mTrianglePaint);
                }
            } else {
                mTickPaint.setColor(ContextCompat.getColor(mCtx, R.color.tension_grey));
                canvas.drawLine(
                        mCircleCenter[0],
                        mTickRectF.top - mTickLength / 2,
                        mCircleCenter[0],
                        mTickRectF.top + mTickLength / 2,
                        mTickPaint);
            }
            canvas.restore();
        }
        drawLeapText(canvas, dBPointer + 1);
    }

    private void drawLeapText(Canvas canvas, int decibel) {
        String text = decibel + "";
        mTextPaint.setTextSize(mLeapTextSize);
        mTextPaint.setColor(mBackgroundColor);
        canvas.drawCircle(mCircleCenter[0], mCircleCenter[1], Math.abs(mTextRectF.bottom - mTextRectF.top) / 2, mTextPaint);
        mTextPaint.setColor(ColorUtil.getColorReverse(mBackgroundColor));
        float textWidth = mTextPaint.measureText(text) * 1.0f;
        float textHalfHeight = FontMatrixUtils.calcTextHalfHeightPoint(mTextPaint);
        canvas.drawText(text, mCircleCenter[0] - textWidth / 2, mCircleCenter[1] + textHalfHeight / 2, mTextPaint);
        mTextPaint.setTextSize(mLeapTextSize / 2);
        canvas.drawText("dB", mCircleCenter[0] + textWidth / 2, mCircleCenter[1] + textHalfHeight / 2, mTextPaint);
    }

    private void drawInnerArc(Canvas canvas) {
        /* draw colorful gradient */
        SweepGradient sweepGradient = new SweepGradient(
                mCircleCenter[0],
                mCircleCenter[1],
                mInnerArcGradientColors,
                mGalaxyPositions);
        mInnerArcPaint.setShader(sweepGradient);
        Path path = new Path();
        canvas.save();
        canvas.rotate(mStartAngle, mCircleCenter[0], mCircleCenter[1]);
        path.addArc(mInnerArcRectF, 0, mInnerArcDegree);
        canvas.drawPath(path, mInnerArcPaint);
        canvas.restore();
    }

    /**
     * 重置当前指针并开始动画
     * reset current pointer and start anim
     *
     * @param value
     */
    public void setPointerDecibel(int value) {
        value = value % mDecibel;
        float degree = mInnerArcDegree * value / mDecibel;
        this.mLastValue = mPointerDegree;
        this.mPointerDegree = degree;
        startPointerAnim();
        // invalidate();
    }

    /**
     * SweepGradient 颜色值对应的1f：360f(圆的角度)
     * pointerDegreeRate 计算解释：pointerTick/totalPointer对应了颜色值的渐变,
     * 因为当前圆可以设置为存在缺口的圆弧, 所以按照比例同成于圆弧 galaxyDegree/360f（圆的角度）
     * 颜色区间计算：通过上述得到的float值, 去校验当前指针在 positions 的哪个区间, 然后换算当前颜色值RGB
     */
    public int getPointerColor(int pointerTick) {
        if (mInnerArcGradientColors.length == 1) {
            return mInnerArcGradientColors[0];
        }
        float degreeRate = mInnerArcDegree / 360f;
        float pointerDegreeRate = pointerTick * 1f / (mDecibel + 1) * degreeRate;
        int resSColor = ContextCompat.getColor(mCtx, R.color.white);
        int resEColor = ContextCompat.getColor(mCtx, R.color.oxygen_green);
        float rangeColorRate = 0f;
        for (int i = 0; i < mGalaxyPositions.length; i++) {
            if (i == 0) {
                resSColor = mInnerArcGradientColors[0];
                resEColor = mInnerArcGradientColors[1];
                continue;
            }
            if (pointerDegreeRate < mGalaxyPositions[i]) {
                float s = mGalaxyPositions[i - 1];
                float e = mGalaxyPositions[i];
                rangeColorRate = (pointerDegreeRate - s) / (e - s);
                resSColor = mInnerArcGradientColors[i - 1];
                resEColor = mInnerArcGradientColors[i];
                break;
            }
        }
        return ColorUtil.getCompositeColor(resSColor, resEColor, rangeColorRate);
    }

    private ValueAnimator mAnimator;
    private float mLastValue = 0f;

    private void startPointerAnim() {
        long duration = (long) (10 * Math.abs(mPointerDegree - mLastValue));
        mAnimator = ValueAnimator.ofFloat(mLastValue, mPointerDegree);
        mAnimator.setDuration(duration);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPointerDegree = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) {
                    listener.startTension();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    private ServantListener listener;

    public void setServantListener(ServantListener listener) {
        this.listener = listener;
    }

    public interface ServantListener {
        void startTension();
    }

    public int[] getGalaxyColors() {
        return mInnerArcGradientColors;
    }

    /**
     * 设置颜色的时候同时计算 positions
     * ！不处理任何颜色值设置异常
     * set color gradient and automatic calculate positions
     * not care about the color with positions exceptions, 'cause the os will handle with it
     */
    public void setInnerArcColors(@Nullable int[] colors) {
        if (colors == null) {
            colors = new int[]{
                    ContextCompat.getColor(mCtx, R.color.white),
                    ContextCompat.getColor(mCtx, R.color.oxygen_green),
                    ContextCompat.getColor(mCtx, R.color.cinnabar_red)
            };
        }
        this.mInnerArcGradientColors = colors;
        setPositions(null);
        invalidate();
    }

    /* not allow use */
    private float[] getGalaxyPositions() {
        return mGalaxyPositions;
    }

    /* not allow use */

    /**
     * calc per color to per position
     *
     * @param positions
     */
    private void setPositions(@Nullable float[] positions) {
        float degreeRate = mInnerArcDegree / 360f;
        if (positions == null) {// set positions average allocation
            positions = new float[mInnerArcGradientColors.length];
            for (int i = 0; i < positions.length; i++) {
                positions[i] = i * degreeRate / (positions.length - 1);
            }
        } else {// use degree rate while reset positions
            for (int i = 0; i < positions.length; i++) {
                positions[i] = positions[i] * degreeRate;
            }
        }
        this.mGalaxyPositions = positions;
    }

    private float dp2Px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mCtx.getResources().getDisplayMetrics());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
