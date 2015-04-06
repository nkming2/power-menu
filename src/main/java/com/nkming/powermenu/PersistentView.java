/*
 * PersistentView.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.nkming.utils.sys.DeviceInfo;
import com.nkming.utils.type.Size;
import com.nkming.utils.unit.DimensionUtils;

public class PersistentView
{
	public static class Config
	{
		public Handler handler;
		public Context context;
		/// Resource id for the content view
		public int resId;
		/// Alpha value for the view when idle
		public float alpha = 1.0f;
		/// Percentage of width that is beyond the edge of the screen
		public float hiddenW = 0.15f;
	}

	public PersistentView(Config config)
	{
		mHandler = config.handler;
		mContext = config.context;
		mWindowManager = (WindowManager)mContext.getSystemService(
				Context.WINDOW_SERVICE);

		mIsPortrait = (mContext.getResources().getConfiguration().orientation
				== Configuration.ORIENTATION_PORTRAIT);
		updateScreenSize();
		initView(config);
		initDummyView();

		mChild.setScaleX(0);
		mChild.setScaleY(0);
		show(null);

		mPrimaryId = -1;
		mInitialTouchPos = new PointF();
		mViewPos = new Point();
		mIsMoving = false;
		mAlpha = config.alpha;
		mHiddenW = config.hiddenW;

		mLongPressRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				onLongPress();
			}
		};
	}

	public void show(Animator.AnimatorListener listener)
	{
		Log.d(LOG_TAG, "show(...)");
		mContainer.setTouchable(true);

		mChild.animate().scaleX(1.0f).scaleY(1.0f)
				.setInterpolator(new DecelerateInterpolator())
				.setDuration(Res.ANIMATION_FAST)
				.setListener(listener);
	}

	public void hide(Animator.AnimatorListener listener)
	{
		Log.d(LOG_TAG, "hide(...)");
		reset(true);
		// A hidden view should not be responding to touch
		mContainer.setTouchable(false);

		mChild.animate().scaleX(0.0f).scaleY(0.0f)
				.setInterpolator(new AccelerateInterpolator())
				.setDuration(Res.ANIMATION_FAST)
				.setListener(listener);
	}

	public void destroy()
	{
		Log.d(LOG_TAG, "destroy()");
		mWindowManager.removeView(mContainer);
		mWindowManager.removeView(mDummyView);
	}

	public void onOrientationChange(boolean isPortrait)
	{
		if (isPortrait != mIsPortrait)
		{
			mIsPortrait = isPortrait;
			updateScreenSize();
			snap(false);
		}
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
			if (!mIsTouchable)
			{
				return false;
			}

			switch (event.getActionMasked())
			{
			case MotionEvent.ACTION_DOWN:
				onActionDown(event);
				break;

			case MotionEvent.ACTION_POINTER_UP:
				if (event.getActionIndex() == mPrimaryId)
				{
					if (mPrimaryId != -1 && !mIsMoving)
					{
						onClick();
					}
					else
					{
						reset(false);
					}
				}
				break;

			case MotionEvent.ACTION_UP:
				if (mPrimaryId != -1 && !mIsMoving)
				{
					onClick();
				}
				else
				{
					reset(false);
				}
				break;

			case MotionEvent.ACTION_CANCEL:
				reset(false);
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

		public void setTouchable(boolean flag)
		{
			mIsTouchable = flag;
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
				mViewPos.set(x, y);
				updatePosition(x, y);
			}
		}

		private boolean mIsTouchable = true;
	}

	private void initView(Config config)
	{
		mContainer = new ContainerView(mContext);
		/// Setting attachToRoot to true works differently which we don't want
		mChild = LayoutInflater.from(mContext).inflate(config.resId, mContainer,
				false);
		mChild.setAlpha(config.alpha);
		mContainer.addView(mChild);

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
	}

	private void initDummyView()
	{
		mDummyView = new View(mContext);

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
						| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
				PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowManager.addView(mDummyView, params);

		mDummyView.addOnLayoutChangeListener(new View.OnLayoutChangeListener()
		{
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight,
					int oldBottom)
			{
				Size fullScreen = DeviceInfo.GetFullScreenPx(mContext);
				if (right - left == fullScreen.w()
						&& bottom - top == fullScreen.h())
				{
					Log.d(LOG_TAG + ".OnLayoutChangeListener", "Hiding nav bar");
					mHasNavigationBar = false;
				}
				else
				{
					Log.d(LOG_TAG + ".OnLayoutChangeListener", "Showing nav bar");
					mHasNavigationBar = true;
				}
				updateScreenSize();
				snap(false);
			}
		});
	}

	private void onActionDown(MotionEvent event)
	{
		mPrimaryId = event.getActionIndex();
		mInitialTouchPos = new PointF(event.getRawX(), event.getRawY());
		mHandler.postDelayed(mLongPressRunnable,
				ViewConfiguration.get(mContext).getLongPressTimeout());

		for (int i = 0; i < mSnapAnimators.length; ++i)
		{
			if (mSnapAnimators[i] != null)
			{
				mSnapAnimators[i].cancel();
				mSnapAnimators[i] = null;
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

		mChild.animate().alpha(1.0f)
				.setInterpolator(new AccelerateDecelerateInterpolator())
				.setDuration(Res.ANIMATION_FAST);
	}

	private void onClick()
	{
		Log.d(LOG_TAG, "onClick()");
		mContainer.performClick();
		reset(true);
	}

	private void onLongPress()
	{
		Log.d(LOG_TAG, "onLongPress()");
		mContainer.performLongClick();
		reset(true);
	}

	private void updateScreenSize()
	{
		if (mHasNavigationBar)
		{
			mScreenSize = DeviceInfo.GetScreenPx(mContext);
		}
		else
		{
			mScreenSize = DeviceInfo.GetFullScreenPx(mContext);
		}
	}

	private void snap(boolean isAnimate)
	{
		// Bound the top and bottom
		int y = Math.max(Math.min(mLayoutParams.y,
				mScreenSize.h() - mChild.getHeight()), 0);
		int x;
		// Take center
		if ((mLayoutParams.x + mChild.getWidth() / 2) < mScreenSize.w() / 2)
		{
			x = (int)(0 - mChild.getWidth() * mHiddenW);
		}
		else
		{
			x = (int)(mScreenSize.w() - mChild.getWidth() * (1.0f - mHiddenW));
		}
		mViewPos.set(x, y);

		if (isAnimate)
		{
			updatePositionAnimated(x, y);
		}
		else
		{
			updatePosition(x, y);
		}
	}

	private void backInitial(boolean isAnimate)
	{
		if (isAnimate)
		{
			updatePositionAnimated(mViewPos.x, mViewPos.y);
		}
		else
		{
			updatePosition(mViewPos.x, mViewPos.y);
		}
	}

	/**
	 * Reset the view's state. If the view is moving, it would be either snapped
	 * to the closest border or back to its initial position, depending on
	 * @a isBackInitial
	 *
	 * @param isBackInitial
	 */
	private void reset(boolean isBackInitial)
	{
		if (mPrimaryId != -1)
		{
			if (isBackInitial)
			{
				backInitial(true);
			}
			else
			{
				snap(true);
			}
			mChild.animate().alpha(mAlpha)
					.setInterpolator(new AccelerateDecelerateInterpolator())
					.setDuration(Res.ANIMATION_FAST);

			mPrimaryId = -1;
			mInitialTouchPos = new PointF();
			mIsMoving = false;
			mHandler.removeCallbacks(mLongPressRunnable);
		}
	}

	private void evaluateMoving(MotionEvent event)
	{
		float threshold = DimensionUtils.dpToPx(mContext, 24);
		if (Math.abs(event.getRawX() - mInitialTouchPos.x) >= threshold
				|| Math.abs(event.getRawY() - mInitialTouchPos.y) >= threshold)
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
		animX.addListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				mSnapAnimators[0] = null;
			}
		});
		animX.start();
		mSnapAnimators[0] = animX;

		ObjectAnimator animY = ObjectAnimator.ofInt(this, "y", mLayoutParams.y,
				y);
		animY.setInterpolator(new AccelerateDecelerateInterpolator());
		animY.setDuration(Res.ANIMATION_FAST);
		animY.addListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				mSnapAnimators[1] = null;
			}
		});
		animY.start();
		mSnapAnimators[1] = animY;
	}

	private int mPrimaryId;
	private PointF mInitialTouchPos;
	// The position where the view should be, it could be different with the
	// actual position during animation
	private Point mViewPos;
	private boolean mIsMoving;
	private boolean mHasLayout = false;
	private ObjectAnimator mSnapAnimators[] = new ObjectAnimator[2];
	private boolean mIsPortrait;
	private boolean mHasNavigationBar = true;

	private float mAlpha;
	private float mHiddenW;

	private Context mContext;
	private Handler mHandler;
	private Runnable mLongPressRunnable;
	private ContainerView mContainer;
	private View mChild;
	private Size mScreenSize;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLayoutParams;
	// Used to detect app hiding navigation bar
	private View mDummyView;
}
