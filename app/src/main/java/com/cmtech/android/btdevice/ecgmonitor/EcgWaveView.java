/*
 * * SignalScanView: ��ɨ��ķ�ʽ������ʾ��������ʾ�ź�
 * Ŀǰû�м����κ��˲�����
 * Wrote by chenm, BME, GDMC
 * 2013.09.25
 */
package com.cmtech.android.btdevice.ecgmonitor;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.cmtech.android.btdeviceapp.R;


public class EcgWaveView extends View {

	private static final int DEFAULT_SIZE = 100;
	private static final int DEFAULT_XRES = 1;
	private static final int DEFAULT_YRES = 1;	
	private static final double DEFAULT_ZERO_LOCATION = 0.5;

	private static final int DEFAULT_BACKGROUND_COLOR = Color.BLACK;
	private static final int DEFAULT_GRID_COLOR = Color.RED;
	private static final int DEFAULT_ECG_COLOR = Color.WHITE;
	
	private boolean mCanShow = false;	
	private boolean mIsFirstDraw = true;

	private int mViewWidth;					//视图宽度
	private int mViewHeight;				//视图高度
	private int mInit_x, mInit_y;			//画图起始位置
	private int mPre_x, mPre_y;				//画线的前一个点坐标
	private int mCur_x, mCur_y;				//画线的当前点坐标

	// mNum：用于点计数，这个变量的作用是用来提高画线效率
	// 原理是：当来一个数据后，设mNum=0，下一个数据点来后，mNum++
	// 但是由于屏幕横向一个像素可能代表多个数据（由mXRes表示），所以，这个数据可能并没有改变屏幕的横坐标
	// 此时，再根据数据幅值，即y坐标是否相同，决定是否画线。很多情况下，是不需要画线的
	private int mNum = 0;

	private Paint mPaint = new Paint();
	private Bitmap mBackBitmap, mForeBitmap;	//背景和前景bitmap
	private Canvas mForeCanvas;					//前景画布

	private final int backgroundColor;
	private final int gridColor;
	private final int ecgColor;

	private int mSigSampleRate;

	//private int gridVWidth = 10;		// 纵向栅格宽度为多少个像素
	//private int gridHWidth = 10;		// 横向栅格宽度为多少个像素


	private int mXRes;						//X方向分辨率，表示屏幕X方向每个像素代表的数据点数>=1，sample/pixel
	private int mYRes;						//Y方向分辨率，表示屏幕Y方向每个像素代表的信号值>0，mV/pixel
	private double mZeroLocation;			//表示零值位置占视图高度的百分比
	private final LinkedBlockingQueue<Integer> mData = new LinkedBlockingQueue<Integer>();	//要显示的信号数据对象的引用


	private Thread mShowThread;

	public EcgWaveView(Context context) {
		super(context);

		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		gridColor = DEFAULT_GRID_COLOR;
		ecgColor = DEFAULT_ECG_COLOR;

		initialize();
	}

	public EcgWaveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//第二个参数就是我们在attrs.xml文件中的<declare-styleable>标签
		//即属性集合的标签，在R文件中名称为R.styleable+name
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EcgWaveView);

		//第一个参数为属性集合里面的属性，R文件名称：R.styleable+属性集合名称+下划线+属性名称
		//第二个参数为，如果没有设置这个属性，则设置的默认的值
		backgroundColor = a.getColor(R.styleable.EcgWaveView_background_color, DEFAULT_BACKGROUND_COLOR);
		gridColor = a.getColor(R.styleable.EcgWaveView_grid_color, DEFAULT_GRID_COLOR);
		ecgColor = a.getColor(R.styleable.EcgWaveView_ecg_color, DEFAULT_ECG_COLOR);

		//最后记得将TypedArray对象回收
		a.recycle();

		initialize();
	}


	private void initialize()
	{
		//初始化分辨率
		mXRes = DEFAULT_XRES;
		mYRes = DEFAULT_YRES;

        mSigSampleRate = 250;   // 采样率为250Hz

		//初始化零值位置占视图高度的百分比
		mZeroLocation = DEFAULT_ZERO_LOCATION;

		//创建显示线程
		mShowThread = new Thread(new Runnable(){
			public void run()
			{
				while(mCanShow)
				{
					// Log.v("BME", "showing data ");
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
		mData.offer(data);
	}

	public void addData(Integer[] data) {
		mData.addAll(Arrays.asList(data));
	}

	public void setRes(int xRes, int yRes)
	{
		if((xRes < 1) || (yRes < 0)) return;

		mXRes = xRes;
		mYRes = yRes;
		mNum = 0;
	}

	public int getXRes()
	{
		return mXRes;
	}

	public int getYRes()
	{
		return mYRes;
	}

	public void setSigSampleRate(int sr) { mSigSampleRate = sr; }

	public void setZeroLocation(double zeroLocation)
	{
		mZeroLocation = zeroLocation;
		mInit_y = (int)(mViewHeight*mZeroLocation);
		if(!mIsFirstDraw) updateBackBitmap();
	}

	public void startShow()
	{
		if((mShowThread != null) && !mShowThread.isAlive())
		{
			mCanShow = true;
			mShowThread.start();
		}
	}

	public void clearView()
	{
		mData.clear();
		mIsFirstDraw = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);

		canvas.drawColor(backgroundColor);


		if(mIsFirstDraw)
		{
			mIsFirstDraw = false;

			initWhenFirstDraw();

            mPaint.setAlpha(0x40);
		}



        canvas.drawBitmap(mBackBitmap, 0, 0, null);


        canvas.drawBitmap(mForeBitmap, 0, 0, mPaint);
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
			value = mData.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Log.v("BME", "start show..." + value.intValue());

		mNum++;
		mCur_y = mInit_y-value/mYRes;

		if(mNum != mXRes)	//mCur_x不变
		{
			if(mCur_y == mPre_y)	//mCur_y也不变，为了提高速度，不用画
				return false;
			else
				mForeCanvas.drawLine(mPre_x, mPre_y, mCur_x, mCur_y, mPaint);
		}
		else	//mCur_x变了
		{
			mNum = 0;
			if(mPre_x == mViewWidth)	//最后一个像素，抹去第一列
			{
				mCur_x = 0;
				mPaint.setColor(backgroundColor);
				mForeCanvas.drawRect(mInit_x, 0, mInit_x+2, mViewHeight, mPaint);
			}
			else	//画线
			{
				mCur_x++;
				mForeCanvas.drawLine(mPre_x, mPre_y, mCur_x, mCur_y, mPaint);

				//抹去前面一个宽度为2的矩形区域
				mPaint.setColor(backgroundColor);
				mForeCanvas.drawRect(mCur_x+1, 0, mCur_x+3, mViewHeight, mPaint);
			}
			mPaint.setColor(ecgColor);
		}

		mPre_x = mCur_x;
		mPre_y = mCur_y;

		//Log.v("BME", "cur_x,cur_y" + mCur_x + "," + mCur_y);
		return true;
	}

	private void initWhenFirstDraw()
	{
		mViewWidth = getWidth();
		mViewHeight = getHeight();

		mInit_x = 0;
		mInit_y = (int)(mViewHeight*mZeroLocation);

		mPre_x = mCur_x = mInit_x;
		mPre_y = mCur_y = mInit_y;

		//创建背景Bitmap
		/*mBackBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Config.ARGB_8888);
		Canvas c = new Canvas(mBackBitmap);
		mPaint.setColor(gridColor);
		c.drawLine(mInit_x, mInit_y, mInit_x+mViewWidth, mInit_y, mPaint);*/
		createBackBitmap();

		//创建前景画布，底色透明
		mForeBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Config.ARGB_8888);
		mForeCanvas = new Canvas(mForeBitmap);
		mPaint.setColor(ecgColor);
		mPaint.setAlpha(0x40);
		mPaint.setStrokeWidth(2);
	}

	private void updateBackBitmap()
	{
		/*mBackBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Config.ARGB_8888);
		Canvas c = new Canvas(mBackBitmap);
		mPaint.setColor(gridColor);
		c.drawLine(mInit_x, mInit_y, mInit_x+mViewWidth, mInit_y, mPaint);*/
		createBackBitmap();
		mPaint.setColor(ecgColor);
		mPaint.setStrokeWidth(2);
        mPaint.setAlpha(0x40);
	}

	private void createBackBitmap()
	{
		//创建背景Bitmap
		mBackBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Config.ARGB_8888);
		Canvas c = new Canvas(mBackBitmap);
		mPaint.setColor(gridColor);

        // 25mm/s的走纸速度代表每mm的小格为0.04秒，则每小格包含的数据点为mSigSampleRate*0.04个
		int gridWidth = (int)(mSigSampleRate*0.04);     

		// 画零位线
		mPaint.setStrokeWidth(2);
		c.drawLine(mInit_x, mInit_y, mInit_x+mViewWidth, mInit_y, mPaint);

		mPaint.setStrokeWidth(1);
		//mPaint.setPathEffect(new DashPathEffect(new float[]{2, 2}, 0));	// 设为虚线

		// 画水平线
		int vCoordinate = mInit_y - gridWidth;
		int i = 0;
		while(vCoordinate > 0) {
			c.drawLine(mInit_x, vCoordinate, mInit_x+mViewWidth, vCoordinate, mPaint);
			vCoordinate -= gridWidth;
			if(++i == 5) {mPaint.setStrokeWidth(2); i = 0;}
			else mPaint.setStrokeWidth(1);
		}
		vCoordinate = mInit_y + gridWidth;
		i = 0;
		while(vCoordinate < mViewHeight) {
			c.drawLine(mInit_x, vCoordinate, mInit_x+mViewWidth, vCoordinate, mPaint);
			vCoordinate += gridWidth;
			if(++i == 5) {mPaint.setStrokeWidth(2); i = 0;}
			else mPaint.setStrokeWidth(1);
		}

		// 画垂直线
		int hCoordinate = mInit_x + gridWidth;
		i = 0;
		while(hCoordinate < mViewWidth) {
			c.drawLine(hCoordinate, 0, hCoordinate, mViewHeight, mPaint);
			hCoordinate += gridWidth;
            if(++i == 5) {mPaint.setStrokeWidth(2); i = 0;}
            else mPaint.setStrokeWidth(1);
		}

		mPaint.setPathEffect(null);

	}
}
