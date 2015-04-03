/*
 * PersistentView.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.nkming.utils.sys.DeviceInfo;
import com.nkming.utils.type.Size;
import com.nkming.utils.unit.DimensionUtils;

public class PersistentView
{
	public PersistentView(Handler handler, Context context, int resId)
	{
		mHandler = handler;
		mContext = context;
		mContainer = new ContainerView(context);
		mChild = LayoutInflater.from(context).inflate(resId, mContainer, true);

		mScreenSize = DeviceInfo.GetScreenPx(context);
		mWindowManager = (WindowManager)context.getSystemService(
				Context.WINDOW_SERVICE);

		mLayoutParams = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
						| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				PixelFormat.TRANSLUCENT);
		mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowManager.addView(mContainer, mLayoutParams);

		mPrimaryId = -1;
		mInitialPos = new PointF();
		mIsMoving = false;

		mLongPressRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				onLongPress();
			}
		};
	}

	public void destroy()
	{
		mWindowManager.removeView(mContainer);
	}

	public void setOnClickListener(View.OnClickListener l)
	{
		mContainer.setOnClickListener(l);
	}

	public void setOnLongClickListener(View.OnLongClickListener l)
	{
		mContainer.setOnLongClickListener(l);
	}

	public void setX(int x)
	{
		mLayoutParams.x = x;
		mWindowManager.updateViewLayout(mContainer, mLayoutParams);
	}

	public void setY(int y)
	{
		mLayoutParams.y = y;
		mWindowManager.updateViewLayout(mContainer, mLayoutParams);
	}

	private static final String LOG_TAG = Res.LOG_TAG + "."
			+ PersistentView.class.getSimpleName();

	private class ContainerView extends FrameLayout
	{
		public ContainerView(Context context)
		{
			super(context);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			switch (event.getActionMasked())
			{
			case MotionEvent.ACTION_DOWN:
				mPrimaryId = event.getActionIndex();
				mInitialPos = new PointF(event.getRawX(), event.getRawY());
				mHandler.postDelayed(mLongPressRunnable,
						ViewConfiguration.get(mContext).getLongPressTimeout());
				break;

			case MotionEvent.ACTION_POINTER_UP:
				if (event.getActionIndex() == mPrimaryId)
				{
					if (mPrimaryId != -1 && !mIsMoving)
					{
						performClick();
					}
					reset();
				}
				break;

			case MotionEvent.ACTION_UP:
				if (mPrimaryId != -1 && !mIsMoving)
				{
					performClick();
				}
				reset();
				break;

			case MotionEvent.ACTION_CANCEL:
				reset();
				break;

			case MotionEvent.ACTION_MOVE:
				if (event.getActionIndex() == mPrimaryId)
				{
					onActionMove(event);
				}
				break;
			}
			return true;
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right,
				int bottom)
		{
			super.onLayout(changed, left, top, right, bottom);

			if (!mHasLayout)
			{
				mHasLayout = true;
				int x = (int)(mScreenSize.w() - getWidth() * (1.0f - mHiddenW));
				int y = (int)(mScreenSize.h() * 0.15f);
				updatePosition(x, y);
			}
		}
	}

	private void onActionMove(MotionEvent event)
	{
		boolean wasMoving = mIsMoving;
		evaluateMoving(event);
		if (mIsMoving != wasMoving)
		{
			onTransitMoveMode();
		}

		// Take center
		int y = (int)event.getRawY() - mChild.getHeight() / 2;
		int x = (int)event.getRawX() - mChild.getWidth() / 2;
		updatePosition(x, y);
	}

	/**
	 * User has moved enough distance that we can safely recognize as a move
	 */
	private void onTransitMoveMode()
	{
		Log.d(LOG_TAG, "onTransitMoveMode()");
		mHandler.removeCallbacks(mLongPressRunnable);
	}

	private void onLongPress()
	{
		Log.d(LOG_TAG, "onLongPress()");
		mContainer.performLongClick();
		reset();
	}

	private void snap()
	{
		int y = mLayoutParams.y;
		int x;
		if (mLayoutParams.x < mScreenSize.w() / 2)
		{
			x = (int)(0 - mChild.getWidth() * mHiddenW);
		}
		else
		{
			x = (int)(mScreenSize.w() - mChild.getWidth() * (1.0f - mHiddenW));
		}
		updatePositionAnimated(x, y);
	}

	private void reset()
	{
		if (mPrimaryId != -1)
		{
			snap();

			mPrimaryId = -1;
			mInitialPos = new PointF();
			mIsMoving = false;
			mHandler.removeCallbacks(mLongPressRunnable);
		}
	}

	private void evaluateMoving(MotionEvent event)
	{
		float threshold = DimensionUtils.dpToPx(mContext, 48);
		if (Math.abs(event.getRawX() - mInitialPos.x) >= threshold
				|| Math.abs(event.getRawY() - mInitialPos.y) >= threshold)
		{
			mIsMoving = true;
		}
	}

	private void updatePosition(int x, int y)
	{
		mLayoutParams.x = x;
		mLayoutParams.y = y;
		mWindowManager.updateViewLayout(mContainer, mLayoutParams);
	}

	private void updatePositionAnimated(int x, int y)
	{
		ObjectAnimator animX = ObjectAnimator.ofInt(this, "x", mLayoutParams.x,
				x);
		animX.setInterpolator(new AccelerateDecelerateInterpolator());
		animX.setDuration(Res.ANIMATION_FAST);
		animX.start();

		ObjectAnimator animY = ObjectAnimator.ofInt(this, "y", mLayoutParams.y,
				y);
		animY.setInterpolator(new AccelerateDecelerateInterpolator());
		animY.setDuration(Res.ANIMATION_FAST);
		animY.start();
	}

	private int mPrimaryId;
	private PointF mInitialPos;
	private boolean mIsMoving;
	private boolean mHasLayout = false;

	/// Percentage of width that is beyond the edge of the screen
	private float mHiddenW = 0.15f;

	private Context mContext;
	private Handler mHandler;
	private Runnable mLongPressRunnable;
	private ContainerView mContainer;
	private View mChild;
	private Size mScreenSize;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLayoutParams;
}
