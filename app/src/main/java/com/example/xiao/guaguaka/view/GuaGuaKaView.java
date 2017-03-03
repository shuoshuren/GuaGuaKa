package com.example.xiao.guaguaka.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.example.xiao.guaguaka.R;

public class GuaGuaKaView extends View {

	private Paint mOutPaint;
	private Path mPath;
	private Canvas mCanvas;
	private Bitmap mBitmap;
	private int mLastX;
	private int mLastY;
	private Bitmap mOutBitmap;

	// ------------------------------------------
	private Bitmap bitmap;

	private String mText;
	private Paint mBackPaint;
	private Rect mTextBound;
	private int mTextSize;
	private int mTextColor;
	private volatile boolean mComplete = false;

	/**
	 * 刮刮卡完成的回调
	 * 
	 * @author xiao
	 * 
	 */
	public interface OnGuaGuaKaCompleteListenner {
		void onComplete();
	}

	private OnGuaGuaKaCompleteListenner mListenner;

	public void setOnGuaGuaKaCompleteListenner(
			OnGuaGuaKaCompleteListenner listenner) {
		this.mListenner = listenner;
	}

	public GuaGuaKaView(Context context) {
		this(context, null);
	}

	public GuaGuaKaView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GuaGuaKaView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		init();

		TypedArray a = null;
		try {
			a = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.GuaGuaKa, defStyleAttr, 0);
			int count = a.getIndexCount();
			for (int i = 0; i < count; i++) {
				int attr = a.getIndex(i);
				switch (attr) {
				case R.styleable.GuaGuaKa_text:
					mText = a.getString(attr);
					break;
				case R.styleable.GuaGuaKa_textSize:
					mTextSize = (int) a.getDimension(attr, TypedValue
							.applyDimension(TypedValue.COMPLEX_UNIT_SP, 32,
									getResources().getDisplayMetrics()));
					break;
				case R.styleable.GuaGuaKa_textColor:
					mTextColor = a.getColor(attr, 0X000000);
					break;

				default:
					break;
				}
			}

		} finally {
			if (a != null) {

				a.recycle();
			}
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);

		setOutPaint();
		setBackPaint();

		//
		mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30, mOutPaint);
		mCanvas.drawBitmap(mOutBitmap, null, new Rect(0, 0, width, height),
				null);

	}

	/**
	 * 设置绘制中奖信息的画笔属性
	 */
	private void setBackPaint() {
		mBackPaint.setColor(mTextColor);
		mBackPaint.setStyle(Style.FILL);
		mBackPaint.setTextSize(mTextSize);
		mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastX = x;
			mLastY = y;
			mPath.moveTo(mLastX, mLastY);
			break;
		case MotionEvent.ACTION_MOVE:
			int dx = Math.abs(x - mLastX);
			int dy = Math.abs(y - mLastY);
			if (dx > 3 || dy > 3) {
				mPath.lineTo(x, y);
			}
			mLastX = x;
			mLastY = y;
			break;
		case MotionEvent.ACTION_UP:
			new Thread(mRunnable).start();
			break;

		}

		invalidate();
		return true;
	}

	/**
	 * 计算擦除面积的线程
	 */
	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			int w = getWidth();
			int h = getHeight();

			float wipeArea = 0;
			float totalArea = w * h;
			Bitmap bitmap = mBitmap;
			int[] mPixels = new int[w * h];
			// 获取bitmap上所有的像素信息
			bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					int index = i + j * w;
					if (mPixels[index] == 0) {
						wipeArea++;
					}
				}
			}

			if (wipeArea > 0 && totalArea > 0) {
				int percent = (int) (wipeArea * 100 / totalArea);
				Log.i("xc", "percent=" + percent);
				if (percent > 60) {
					// 清除掉图层区域
					mComplete = true;
					postInvalidate();

				}
			}

		}
	};

	@Override
	protected void onDraw(Canvas canvas) {

		// canvas.drawBitmap(bitmap, 0, 0, null);

		canvas.drawText(mText, getWidth() / 2 - mTextBound.width() / 2,
				getHeight() / 2 + mTextBound.height() / 2, mBackPaint);

		if (!mComplete) {
			drawPath();

			canvas.drawBitmap(mBitmap, 0, 0, null);
		} else {
			if (mListenner != null) {
				mListenner.onComplete();
			}
		}

	}

	private void drawPath() {

		mOutPaint.setStyle(Style.STROKE);
		mOutPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));

		mCanvas.drawPath(mPath, mOutPaint);
	}

	/**
	 * 设置画笔的属性
	 */
	private void setOutPaint() {
		mOutPaint.setColor(Color.parseColor("#C0C0C0"));
		mOutPaint.setAntiAlias(true);
		mOutPaint.setDither(true);
		mOutPaint.setStrokeJoin(Join.ROUND);
		mOutPaint.setStrokeCap(Cap.ROUND);
		mOutPaint.setStyle(Style.FILL);
		mOutPaint.setStrokeWidth(20);
	}

	/**
	 * 初始化
	 */
	private void init() {
		mOutPaint = new Paint();
		mPath = new Path();
		mOutBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.fg_guaguaka);

		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.t2);

		mText = "谢谢惠顾";
		mTextBound = new Rect();
		mBackPaint = new Paint();
		mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
				32, getResources().getDisplayMetrics());
	}

	public void setText(String text) {
		this.mText = text;
		mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
	}

}
