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
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class SignalWaveView extends View {

	private static final int DEFAULT_SIZE = 100;
	private static final int DEFAULT_XRES = 1;
	private static final int DEFAULT_YRES = 1;	
	private static final double DEFAULT_ZERO_LOCATION = 0.5;
	
	private boolean mCanShow = false;	
	private boolean mIsFirstDraw = true;

	private int mViewWidth;					//视图宽度
	private int mViewHeight;				//视图高度
	private int mInit_x, mInit_y;			//画图起始位置
	private int mPre_x, mPre_y;				//画线的前一个点坐标
	private int mCur_x, mCur_y;				//画线的当前点坐标
	private int mNum = 0;					//用于点计数

	private Paint mPaint = new Paint();
	private Bitmap mBackBitmap, mForeBitmap;	//背景和前景bitmap
	private Canvas mForeCanvas;					//前景画布

	private int mXRes;						//X方向分辨率，表示屏幕X方向每个像素代表的采样点数>=1，sample/pixel
	private int mYRes;						//Y方向分辨率，表示屏幕Y方向每个像素代表的信号值>0，mV/pixel
	private double mZeroLocation;			//表示零值位置占视图高度的百分比
	private final LinkedBlockingQueue<Integer> mData = new LinkedBlockingQueue<Integer>();	//要显示的信号数据对象的引用


	private Thread mShowThread;

	public SignalWaveView(Context context) {
		super(context);

		initialize();
	}

	/*public LinkedBlockingQueue<Integer> getData()
	{
		return mData;
	}*/

	public void addData(Integer data) {
		mData.offer(data);
	}

	public void addData(Integer[] data) {
		mData.addAll(Arrays.asList(data));
	}

	public void SetRes(int xRes, int yRes)
	{
		if((xRes < 1) || (yRes < 0)) return;

		mXRes = xRes;
		mYRes = yRes;
		mNum = 0;
	}

	public void SetZeroLocation(double zeroLocation)
	{
		mZeroLocation = zeroLocation;
		mInit_y = (int)(mViewHeight*mZeroLocation);
		if(!mIsFirstDraw) updateBackBitmap();
	}

	public int GetXRes()
	{
		return mXRes;
	}

	public int GetYRes()
	{
		return mYRes;
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

		canvas.drawColor(Color.BLACK);


		if(mIsFirstDraw)
		{
			mIsFirstDraw = false;

			initWhenFirstDraw();
		}

		canvas.drawBitmap(mForeBitmap, 0, 0, mPaint);
		canvas.drawBitmap(mBackBitmap, 0, 0, null);

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

		Log.v("BME", "start show..." + value.intValue());


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
				mPaint.setColor(Color.BLACK);
				mForeCanvas.drawLine(mInit_x, 0, mInit_x+2, mViewHeight, mPaint);
				mPaint.setColor(Color.WHITE);
			}
			else	//画线
			{
				mCur_x++;
				mForeCanvas.drawLine(mPre_x, mPre_y, mCur_x, mCur_y, mPaint);

				//抹去前面一个宽度为2的矩形区域
				mPaint.setColor(Color.BLACK);
				mForeCanvas.drawRect(mCur_x+1, 0, mCur_x+3, mViewHeight, mPaint);
				mPaint.setColor(Color.WHITE);
			}
		}

		mPre_x = mCur_x;
		mPre_y = mCur_y;

		//Log.v("BME", "cur_x,cur_y" + mCur_x + "," + mCur_y);
		return true;
	}



	private void initialize()
	{
		//初始化分辨率
		mXRes = DEFAULT_XRES;
		mYRes = DEFAULT_YRES;

		//初始化零值位置占视图高度的百分比
		mZeroLocation = DEFAULT_ZERO_LOCATION;

		//创建显示线程
		mShowThread = new Thread(new Runnable(){
			public void run()
			{
				while(mCanShow)
				{
					Log.v("BME", "showing data ");
					if(drawPoint())
					{
						postInvalidate();
					}
				}
			}
		});

		/*
		//初始化显示数据缓存区
		Context context = getContext();
		if (context instanceof MainActivity)
		{
		    MainActivity activity = (MainActivity)context;
		    mData = activity.mData;
		}
		*/
	}

	private void initWhenFirstDraw()
	{
		mViewWidth = getWidth();
		mViewHeight = getHeight();

		mInit_x = 0;
		mInit_y = (int)(mViewHeight*mZeroLocation);

		mPre_x = mCur_x = mInit_x;
		mPre_y = mCur_y = mInit_y;

		//创建背景画布
		mBackBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Config.ARGB_8888);
		Canvas c = new Canvas(mBackBitmap);
		mPaint.setColor(Color.RED);
		c.drawLine(mInit_x, mInit_y, mInit_x+mViewWidth, mInit_y, mPaint);

		//创建前景画布，底色透明
		mForeBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Config.ARGB_8888);
		mForeCanvas = new Canvas(mForeBitmap);
		mPaint.setColor(Color.WHITE);
	}

	private void updateBackBitmap()
	{
		mBackBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Config.ARGB_8888);
		Canvas c = new Canvas(mBackBitmap);
		mPaint.setColor(Color.RED);
		c.drawLine(mInit_x, mInit_y, mInit_x+mViewWidth, mInit_y, mPaint);
		mPaint.setColor(Color.WHITE);
	}
}
