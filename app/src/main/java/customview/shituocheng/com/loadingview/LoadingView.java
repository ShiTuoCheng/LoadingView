package customview.shituocheng.com.loadingview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.icu.util.Measure;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by shituocheng on 2016/11/29.
 */

public class LoadingView extends View {

    //画笔
    private Paint paint;

    //默认模块颜色
    private int defaultColor = Color.rgb(200, 200, 200);

    //block颜色
    private int blockColor;

    //绘制区域的宽高
    private int width;
    private int height;

    private static final int DEFAULT_MIN_WIDTH = 200;   //默认宽度
    private static final int DEFAULT_MIN_HEIGHT = 100;  //默认高度

    private static final int MIN_ALPHA = 100;      //透明度最小值
    private static final int MAX_ALPHA = 200;      //透明度最大值

    private static final int BLOCK_STATE_IN = 1;       //增加
    private static final int BLOCK_STATE_DE = 2;       //减小

    private static final int STEP_NUM = 15;    //总帧数，从0开始为第一帧
    private float blockStepHeight;  //每次变化的高度
    private int blockStepAlpha;     //每次透明度变化的量
    private int blockStep;  //当前帧

    //当前帧
    private int block1Step;
    private int block2Step;
    private int block3Step;
    private int[] blocksStep;

    //方块的间距
    private float blockSpace;

    //方块的宽和高
    private float blockWidth;
    private float blockHeight;

    private float blockMinHeight;   //最小的高度
    private float blockMaxHeight;   //最大的高度

    //方块的顶坐标
    private float blockTop;

    //方块的左坐标
    private float block1Left;
    private float block2Left;
    private float block3Left;
    private float[] blocksLeft;

    //状态 增加或减小
    private int blockState;
    private int block1State;
    private int block2State;
    private int block3State;
    private int[] blocksState;

    //圆角半径
    private float r;

    public LoadingView(Context context) {
        super(context);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);

        blockColor = typedArray.getColor(R.styleable.LoadingView_block_color, defaultColor);
        //回收，以便重用
        typedArray.recycle();
    }

    //初始化
    private void init(){

        //初始化画笔为抗锯齿
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //设置画笔颜色
        paint.setColor(blockColor);
        //设置画笔透明度
        paint.setAlpha(100);
        //设置view的宽高
        width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        height = getMeasuredHeight() - getPaddingBottom() - getPaddingTop();

        //        blockSpace = 25;
//        blockWidth = 100;
//        blockHeight = 110;
        blockSpace = width * 0.05f;
        blockWidth = width * 0.2f;
        blockMinHeight = blockWidth * 1.1f;
        blockMaxHeight = blockWidth * 1.5f;

//        blockHeight = blockMinHeight;

//        blockTop = (height - blockHeight) / 2;

        block2Left = (width - blockWidth) / 2;
        block1Left = block2Left - blockSpace - blockWidth;
        block3Left = block2Left + blockSpace + blockWidth;

        //初始化方块起始状态为变大
//        blockState = BLOCK_STATE_IN;
        block1State = BLOCK_STATE_IN;
        block2State = BLOCK_STATE_IN;
        block3State = BLOCK_STATE_IN;

        //初始化每一帧height变化量
        blockStepHeight = (blockMaxHeight - blockMinHeight) / STEP_NUM;
        blockStepAlpha = Math.round((MAX_ALPHA - MIN_ALPHA) / STEP_NUM);

//        blockStep = 0;
        //为每个block初始化不同的起点
        block1Step = 14;
        block2Step = 7;
        block3Step = 0;

        r = blockWidth / 8;

        blocksStep = new int[]{block1Step, block2Step, block3Step};
        blocksState = new int[]{block1State, block2State, block3State};
        blocksLeft = new float[]{block1Left, block2Left, block3Left};

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //判断自定义view的measure

        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST){
            setMeasuredDimension(DEFAULT_MIN_WIDTH, DEFAULT_MIN_HEIGHT);
        } else if (widthMode == MeasureSpec.AT_MOST){
            setMeasuredDimension(DEFAULT_MIN_WIDTH, heightSize);
        } else if (heightMeasureSpec == MeasureSpec.AT_MOST){
            setMeasuredDimension(widthSize, DEFAULT_MIN_HEIGHT);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        for (int i = 0; i < 3; i++){
            drawBlock(canvas, i);
        }

        postInvalidate();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void drawBlock(Canvas canvas, int index){
        float blockTop;
        float blockLeft = blocksLeft[index];
        float blockHeight;
        int blockAlpha;

        if (blocksState[index] == BLOCK_STATE_IN) {
            //如果是变大状态，帧数+1
            blocksStep[index]++;
        } else {
            //如果是变小状态，帧数-1
            blocksStep[index]--;
        }

        //根据当前帧数获得高度
        blockHeight = blockMinHeight + blocksStep[index] * blockStepHeight;

        //根据当前帧数获得透明度
        blockAlpha = MIN_ALPHA + blocksStep[index] * blockStepAlpha;

        //保持height值在最大值和最小值范围之内
        blockHeight = Math.min(blockMaxHeight, Math.max(blockMinHeight, blockHeight));

        //保持alpha值在最大值和最小值范围之内
        blockAlpha = Math.min(MAX_ALPHA, Math.max(MIN_ALPHA, blockAlpha));

        //获取顶点坐标
        blockTop = (height - blockHeight) / 2;

        //设置透明度
        paint.setAlpha(blockAlpha);

        //绘制方块
        canvas.drawRoundRect(blockLeft, blockTop, blockLeft + blockWidth, blockTop + blockHeight, r, r, paint);


        // -5 是为了增加一个停顿感
        if (blocksStep[index] >= STEP_NUM) {
            //如果帧数已经是最后一帧，状态改为变小状态
            blocksState[index] = BLOCK_STATE_DE;
        } else if(blocksStep[index] <= 0 - 5){
            //如果帧数已经是第一帧，状态改为变大状态
            blocksState[index] = BLOCK_STATE_IN;
        }
    }
}
