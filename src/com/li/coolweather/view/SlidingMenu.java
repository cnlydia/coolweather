package com.li.coolweather.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.Scroller;

public class SlidingMenu extends ViewGroup {
	private View mMeunView;
	private View mContentView;
	private int mMenuWidth;
	private float mDownX;

	private Scroller mScroller;
	private float mDownY;

	private boolean isOpened = false;

	public SlidingMenu(Context context) {
		this(context, null);
	}

	public SlidingMenu(Context context, AttributeSet attrs) {
		super(context, attrs);

		mScroller = new Scroller(context);
	}

	@Override
	protected void onFinishInflate() {
		mMeunView = getChildAt(0);
		mContentView = getChildAt(1);

		LayoutParams params = mMeunView.getLayoutParams();
		mMenuWidth = params.width;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		// 调用孩子的测量方法
		// 1. 测量菜单
		// int widthMeasureSpec:宽度的期望
		// int heightMeasureSpec：高度的期望
		// widthMeasureSpec: 32位的二进制数据
		// 头两位: 代表期望模式
		// UNSPECIFIED:未指定
		// EXACTLY:精确的
		// AT_MOST:最大的
		// 后30位:代码 数据 EXACTLY， AT_MOST-->精确的数据

		// slidingMenu的高度
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		// int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		int menuWidthSpec = MeasureSpec.makeMeasureSpec(mMenuWidth,
				MeasureSpec.EXACTLY);
		// int menuHeightSpec = MeasureSpec
		// .makeMeasureSpec(heightSize, heightMode);
		mMeunView.measure(menuWidthSpec, heightMeasureSpec);// 期望你的大小
		// mMeunView.measure(0, 0);

		// 2. 测量内容
		mContentView.measure(widthMeasureSpec, heightMeasureSpec);

		// 设置自己的宽度和高度
		setMeasuredDimension(widthSize, heightSize);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		int menuLeft = -mMeunView.getMeasuredWidth();
		int menuTop = 0;
		int menuRight = 0;
		int menuBottom = mMeunView.getMeasuredHeight();

		// 1. 菜单部分
		mMeunView.layout(menuLeft, menuTop, menuRight, menuBottom);

		int contentLeft = 0;
		int contentTop = 0;
		int contentRight = mContentView.getMeasuredWidth();
		int contentBottom = mContentView.getMeasuredHeight();

		// 2. 内容部分
		mContentView.layout(contentLeft, contentTop, contentRight,
				contentBottom);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mDownX = event.getX();
			mDownY = event.getY();

			break;
		case MotionEvent.ACTION_MOVE:
			float moveX = event.getX();
			float moveY = event.getY();
			// 水平移动时要拦截touch事件

			if (Math.abs(mDownX - moveX) > Math.abs(mDownY - moveY)) {
				// 水平方向移动,拦截
				return true;
			}

			break;
		case MotionEvent.ACTION_UP:
			break;
		default:
			break;
		}

		return super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mDownX = event.getX();

			break;
		case MotionEvent.ACTION_MOVE:
			float moveX = event.getX();

			int diffX = (int) (mDownX - moveX + 0.5f);

			// 临界点的判断

			// 获得滚动时屏幕左上角点
			int scrollX = getScrollX();
			// 判断预计是否要超过

			if (scrollX + diffX < -mMeunView.getMeasuredWidth()) {
				// 左侧超出了范围,最多显示菜单部分
				scrollTo(-mMeunView.getMeasuredWidth(), 0);
			} else if (scrollX + diffX > 0) {
				// 右侧超出
				scrollTo(0, 0);
			} else {
				scrollBy(diffX, 0);
			}

			// scrollTo(diffX, 0);

			mDownX = moveX;
			break;
		case MotionEvent.ACTION_UP:
			int currentX = getScrollX();
			int middleX = (int) (-mMeunView.getMeasuredWidth() / 2f + 0.5f);

			// if (middleX < currentX) {
			// // 显示内容区域
			//
			// scrollTo(0, 0);
			// } else {
			// // 显示菜单区域
			// scrollTo(-mMeunView.getMeasuredWidth(), 0);
			// }

			switchMenu(!(middleX < currentX));
			break;
		default:
			break;
		}
		// 消费touch
		return true;
	}

	private void switchMenu(boolean showMenu) {
		if (showMenu) {
			isOpened = true;
			// 显示菜单区域
			// scrollTo(endX, 0);

			// 从一个位置滑动到endX

			int startX = getScrollX();// 开始时坐标x
			int startY = getScrollY();// 开始时坐标y

			int endX = -mMeunView.getMeasuredWidth();
			int endY = 0;

			int dx = endX - startX;// 增量位置 结果位置-开始位置
			int dy = endY - startY;
			int duration = Math.abs(dx) * 10;// 滑动的时长
			if (duration > 600) {
				duration = 600;
			}

			// 模拟数据变化(-200,-199,-198...0)
			mScroller.startScroll(startX, startY, dx, dy, duration);

			// 触发view的绘制
			postInvalidate();// 子线程中触发ui绘制
		} else {
			isOpened = false;
			// 显示内容区域
			// scrollTo(0, 0);

			// 从一个位置滑动到0
			int startX = getScrollX();// 开始时坐标x
			int startY = getScrollY();// 开始时坐标y

			int endX = 0;
			int endY = 0;

			int dx = endX - startX;// 增量位置 结果位置-开始位置
			int dy = endY - startY;
			int duration = Math.abs(dx) * 10;// 滑动的时长
			if (duration > 600) {
				duration = 600;
			}

			// 模拟数据变化(-200,-199,-198...0)
			mScroller.startScroll(startX, startY, dx, dy, duration);

			// 触发view的绘制
			postInvalidate();// 子线程中触发ui绘制

		}
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), 0);
			postInvalidate();// 子线程中触发ui绘制
		}
	}

	public void closeMenu() {
		switchMenu(false);
	}

	public void openMenu() {
		switchMenu(true);
	}

	public boolean isOpened() {
		return isOpened;
	}

}
