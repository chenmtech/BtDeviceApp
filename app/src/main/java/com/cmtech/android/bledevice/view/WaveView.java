/*
 * * SignalScanView: ��ɨ��ķ�ʽ������ʾ��������ʾ�ź�
 * Ŀǰû�м����κ��˲�����
 * Wrote by chenm, BME, GDMC
 * 2013.09.25
 */
package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.cmtech.android.bledeviceapp.R;

import java.util.concurrent.LinkedBlockingQueue;


public class WaveView extends View {

	private static final int DEFAULT_SIZE = 100;                // 缺省View的大小
	private static final int DEFAULT_XRES = 2;                  // 缺省的X方向的分辨率
	private static final float DEFAULT_YRES = 1.0f;	            // 缺省的Y方向的分辨率
	private static final double DEFAULT_ZERO_LOCATION = 0.5;   // 缺省的零线位置在Y方向的高度的比例
	private static final int DEFAULT_GRID_WIDTH = 10;           // 背景栅格像素宽度

	private static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
	private static final int DEFAULT_GRID_COLOR = Color.RED;
	private static final int DEFAULT_WAVE_COLOR = Color.BLACK;
	
	private boolean canShow = false;
	private boolean isFirstDraw = true;

	private int viewWidth;					//视图宽度
	private int viewHeight;				    //视图高度
	private int initX, initY;			        //画图起始位置
	private int preX, preY;				    //画线的前一个点坐标
	private int curX, curY;				    //画线的当前点坐标

	private Paint mainPaint = new Paint();
	private Bitmap backBitmap, foreBitmap;	//背景和前景bitmap
	private Canvas foreCanvas;					//前景画布

	private final int backgroundColor;
	private final int gridColor;
	private final int waveColor;


	private final LinkedBlockingQueue<Integer> viewData = new LinkedBlockingQueue<Integer>();	//要显示的信号数据对象的引用
	private Thread showThread;

	// View初始化主要需要设置下面4个参数
	private int gridWidth;                // 栅格像素宽度
	private int xRes;						//X方向分辨率，表示屏幕X方向每个数据点占多少个像素，pixel/data
	private float yRes;					//Y方向分辨率，表示屏幕Y方向每个像素代表的信号值的变化，DeltaSignal/pixel
	private double zeroLocation;			//表示零值位置占视图高度的百分比



	public WaveView(Context context) {
		super(context);

		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		gridColor = DEFAULT_GRID_COLOR;
        waveColor = DEFAULT_WAVE_COLOR;

		initialize();
	}

	public WaveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//第二个参数就是我们在attrs.xml文件中的<declare-styleable>标签
		//即属性集合的标签，在R文件中名称为R.styleable+name
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaveView);

		//第一个参数为属性集合里面的属性，R文件名称：R.styleable+属性集合名称+下划线+属性名称
		//第二个参数为，如果没有设置这个属性，则设置的默认的值
		backgroundColor = a.getColor(R.styleable.WaveView_background_color, DEFAULT_BACKGROUND_COLOR);
		gridColor = a.getColor(R.styleable.WaveView_grid_color, DEFAULT_GRID_COLOR);
        waveColor = a.getColor(R.styleable.WaveView_wave_color, DEFAULT_WAVE_COLOR);

		//最后记得将TypedArray对象回收
		a.recycle();

		initialize();
	}


	private void initialize()
	{
		//初始化分辨率
		xRes = DEFAULT_XRES;
		yRes = DEFAULT_YRES;

        gridWidth = DEFAULT_GRID_WIDTH;

		//初始化零值位置占视图高度的百分比
		zeroLocation = DEFAULT_ZERO_LOCATION;

		//创建显示线程
		showThread = new Thread(new Runnable(){
			public void run()
			{
			while(canShow)
			{
				// 先画点，再刷新
				if(drawPoint())
				{
					postInvalidate();
				}
			}
			}
		});
	}

	public void addData(Integer data) {
		viewData.offer(data);
	}

	public void setRes(int xRes, float yRes)
	{
		if((xRes < 1) || (yRes < 0)) return;
		this.xRes = xRes;
		this.yRes = yRes;
	}

	public int getXRes()
	{
		return xRes;
	}

	public float getYRes()
	{
		return yRes;
	}

	public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;}

	public void setZeroLocation(double zeroLocation)
	{
		this.zeroLocation = zeroLocation;
		initY = (int)(viewHeight * this.zeroLocation);
		if(!isFirstDraw) updateBackBitmap();
	}

	public void startShow()
	{
		if((showThread != null) && !showThread.isAlive())
		{
			canShow = true;
			showThread.start();
		}
	}

	public void clearView()
	{
		viewData.clear();
		isFirstDraw = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);

		canvas.drawColor(backgroundColor);

		if(isFirstDraw)
		{
			isFirstDraw = false;

			initWhenFirstDraw();
		}

		canvas.drawBitmap(foreBitmap, 0, 0, mainPaint);

        canvas.drawBitmap(backBitmap, 0, 0, null);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		setMeasuredDimension(calculateMeasure(widthMeasureSpec), calculateMeasure(heightMeasureSpec));
	}

	private int calculateMeasure(int measureSpec)
	{
		int size = (int)(DEFAULT_SIZE * getResources().getDisplayMetrics().density);
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if(specMode == MeasureSpec.EXACTLY)
		{
			size = specSize;
		}
		else if(specMode == MeasureSpec.AT_MOST)
		{
			size = Math.min(size, specSize);
		}
		return size;
	}

	private boolean drawPoint()
	{
		Integer value = 0;

		try {
			value = viewData.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		curY = initY - Math.round(value/yRes);

		if(preX == viewWidth)	//最后一个像素，抹去第一列
		{
			curX = initX;
			mainPaint.setColor(backgroundColor);
			foreCanvas.drawRect(initX, 0, initX +2, viewHeight, mainPaint);
		}
		else	//画线
		{
			curX += xRes;
			mainPaint.setColor(waveColor);
			foreCanvas.drawLine(preX, preY, curX, curY, mainPaint);

			//抹去前面一个宽度为2的矩形区域
			mainPaint.setColor(backgroundColor);
			foreCanvas.drawRect(curX +1, 0, curX +3, viewHeight, mainPaint);
		}
		//mainPaint.setColor(ecgColor);

		preX = curX;
		preY = curY;

		return true;
	}

	private void initWhenFirstDraw()
	{
		viewWidth = getWidth();
		viewHeight = getHeight();

		//创建背景Bitmap
		createBackBitmap();

		//创建前景画布，底色透明
		foreBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);
		foreCanvas = new Canvas(foreBitmap);

        preX = curX = initX;
        preY = curY = initY;
	}

	private void updateBackBitmap()
	{
		createBackBitmap();
	}

	private void createBackBitmap()
	{
        initX = 0;
        initY = (int)(viewHeight * zeroLocation);

		//创建背景Bitmap
		backBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);
		Canvas c = new Canvas(backBitmap);
		mainPaint.setColor(gridColor);

		// 画零位线
		mainPaint.setStrokeWidth(2);
		c.drawLine(initX, initY, initX + viewWidth, initY, mainPaint);

		mainPaint.setStrokeWidth(1);

		// 画水平线
		int vCoordinate = initY - gridWidth;
		int i = 1;
		while(vCoordinate > 0) {
			c.drawLine(initX, vCoordinate, initX + viewWidth, vCoordinate, mainPaint);
			vCoordinate -= gridWidth;
			if(++i == 5) {
                mainPaint.setStrokeWidth(2); i = 0;}
			else mainPaint.setStrokeWidth(1);
		}
        mainPaint.setStrokeWidth(1);
		vCoordinate = initY + gridWidth;
		i = 1;
		while(vCoordinate < viewHeight) {
			c.drawLine(initX, vCoordinate, initX + viewWidth, vCoordinate, mainPaint);
			vCoordinate += gridWidth;
			if(++i == 5) {
                mainPaint.setStrokeWidth(2); i = 0;}
			else mainPaint.setStrokeWidth(1);
		}

		// 画垂直线
        mainPaint.setStrokeWidth(1);
		int hCoordinate = initX + gridWidth;
		i = 1;
		while(hCoordinate < viewWidth) {
			c.drawLine(hCoordinate, 0, hCoordinate, viewHeight, mainPaint);
			hCoordinate += gridWidth;
            if(++i == 5) {
                mainPaint.setStrokeWidth(2); i = 0;}
            else mainPaint.setStrokeWidth(1);
		}

        // 画定标脉冲
/*        mainPaint.setStrokeWidth(2);
		mainPaint.setColor(Color.BLACK);
		c.drawLine(0, initY, 2*gridWidth, initY, mainPaint);
		c.drawLine(2*gridWidth, initY, 2*gridWidth, initY-10*gridWidth, mainPaint);
		c.drawLine(2*gridWidth, initY-10*gridWidth, 7*gridWidth, initY-10*gridWidth, mainPaint);
        c.drawLine(7*gridWidth, initY-10*gridWidth, 7*gridWidth, initY, mainPaint);
        c.drawLine(7*gridWidth, initY, 10*gridWidth, initY, mainPaint);*/

        mainPaint.setColor(waveColor);
        mainPaint.setStrokeWidth(2);
    }
}
