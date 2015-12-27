package com.li.coolweather.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ToggleView extends View {
	private Paint paint = new Paint();
	private Bitmap mBackground;
	private Bitmap mSlid;

	private final static int STATE_NONE = 0;
	private final static int STATE_DOWN = 1;
	private final static int STATE_MOVE = 2;
	private final static int STATE_UP = 3;

	private boolean isClosed = false;// 用来记录滑动块的状态.默认是关闭的
	private float currentX;
	private int mCurrentState = STATE_NONE;
	private OnToggleListener mListener;

	public ToggleView(Context context) {
		this(context, null);
	}

	public ToggleView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setToggleBackground(int resId) {
		mBackground = BitmapFactory.decodeResource(getResources(), resId);
	}

	public void setToggleSlid(int resId) {
		mSlid = BitmapFactory.decodeResource(getResources(), resId);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		// 确定自己控件的宽度和高度
		if (mBackground != null) {
			setMeasuredDimension(mBackground.getWidth(),
					mBackground.getHeight());
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 绘制样子
		//
		// // 画布中的左上右下 ：
		// // View的父子间的左上右下：
		// float left = 0;
		// float top = 0;
		// float right = getWidth();
		// float bottom = getHeight();
		//
		// paint.setColor(Color.RED);
		//
		// // 画矩形
		// canvas.drawRect(left, top, right, bottom, paint);

		// 画滑动的背景
		canvas.drawBitmap(mBackground, 0, 0, paint);

		int backWidth = mBackground.getWidth();
		int slidWidth = mSlid.getWidth();

		// 当按下时
		if (mCurrentState == STATE_DOWN || mCurrentState == STATE_MOVE) {
			// 如果滑块是关闭状态
			// 1. 点击左侧:没有动
			// 2. 点击右侧:动了
			if (isClosed) {
				if (currentX < slidWidth / 2f) {
					// 点击的是左侧,不动
					canvas.drawBitmap(mSlid, 0, 0, paint);
				} else {
					// 移动，滑块的中线和按下的位置对齐
					float left = currentX - slidWidth / 2f;
					if (left > (backWidth - slidWidth)) {
						left = backWidth - slidWidth;
					}
					canvas.drawBitmap(mSlid, left, 0, paint);
				}
			} else {
				// 按下打开时
				// 如果按下的位置的x方向坐标，大于 滑块中线x位置，不动
				if (currentX > (backWidth - slidWidth / 2f)) {
					canvas.drawBitmap(mSlid, backWidth - slidWidth, 0, paint);
				} else {
					float left = currentX - slidWidth / 2f;
					if (left < 0) {
						left = 0;
					}
					canvas.drawBitmap(mSlid, left, 0, paint);
				}
			}
		} else if (mCurrentState == STATE_UP) {
			float middle = mBackground.getWidth() / 2f;// 背景的中间线

			if (currentX > middle) {
				// 走到右边，打开开关
				canvas.drawBitmap(mSlid, backWidth - slidWidth, 0, paint);

				if (isClosed) {
					// 只有关闭的情况下，才可以打开
					// 开关打开了,通知接口回调
					if (mListener != null) {
						mListener.onToggleStateChanged(true);
					}
					isClosed = false;// 打开状态
				}

			} else {
				// 走到左边，关闭开关
				canvas.drawBitmap(mSlid, 0, 0, paint);

				if (!isClosed) {
					// 只有打开的情况下，才可以关闭
					// 开关关闭了,通知接口回调
					if (mListener != null) {
						mListener.onToggleStateChanged(false);
					}
					isClosed = true;// 关闭状态
				}
			}

		} else if (mCurrentState == STATE_NONE) {

			if (isClosed) {
				// 画滑动块：关闭
				canvas.drawBitmap(mSlid, 0, 0, paint);
			} else {
				// 画滑动块:打开
				canvas.drawBitmap(mSlid, backWidth - slidWidth, 0, paint);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mCurrentState = STATE_DOWN;
			currentX = event.getX();

			postInvalidate();// 子线程中触发ui绘制
			// invalidate();// 在主线程中调用
			break;
		case MotionEvent.ACTION_MOVE:
			mCurrentState = STATE_MOVE;
			currentX = event.getX();

			postInvalidate();// 子线程中触发ui绘制
			break;
		case MotionEvent.ACTION_UP:
			mCurrentState = STATE_UP;
			currentX = event.getX();// 滑动块的中线位置
			postInvalidate();// 子线程中触发ui绘制
			// invalidate();
			break;
		default:
			break;
		}

		// 消费touch
		return true;
	}

	public void setOnToggleListener(OnToggleListener listener) {
		this.mListener = listener;
	}

	public boolean isOpened() {
		return !isClosed;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public interface OnToggleListener {
		/**
		 * 通知开关是否打开还是关闭
		 * 
		 * @param isOpened
		 */
		void onToggleStateChanged(boolean isOpened);
	}

}
