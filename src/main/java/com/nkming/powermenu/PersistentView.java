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
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
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

		Size screenSz = DeviceInfo.GetScreenPx(mContext);
		// Init with a good enough rect
		mScreenRect = new Rect(0, 0, screenSz.w(), screenSz.h());
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
		mIsAutohide = false;

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
		setTouchable(true);

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
		setTouchable(false);

		mChild.animate().scaleX(0.0f).scaleY(0.0f)
				.setInterpolator(new AccelerateInterpolator())
				.setDuration(Res.ANIMATION_FAST)
				.setListener(listener);
	}

	public void destroy()
	{
		Log.d(LOG_TAG, "destroy()");
		mWindowManager.removeView(mContainer);
		mWindowManager.removeView(mDummyView[0]);
		mWindowManager.removeView(mDummyView[1]);
	}

	/**
	 * Listen to click event on this view
	 *
	 * @param l
	 */
	public void setOnClickListener(View.OnClickListener l)
	{
		mContainer.setOnClickListener(l);
	}

	/**
	 * Listen to long click/press event on this view
	 *
	 * @param l
	 */
	public void setOnLongClickListener(View.OnLongClickListener l)
	{
		mContainer.setOnLongClickListener(l);
	}

	/**
	 * To hide the persistent view when running a fullscreen app
	 *
	 * @param flag
	 */
	public void setAutohide(boolean flag)
	{
		mIsAutohide = flag;
	}

	/**
	 * For the ObjectAnimator and animate the view
	 *
	 * @param x
	 */
	public void setX(int x)
	{
		mLayoutParams.x = x;
		mWindowManager.updateViewLayout(mContainer, mLayoutParams);
	}

	/**
	 * For the ObjectAnimator and animate the view
	 *
	 * @param y
	 */
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

		@Override
		protected void onLayout(boolean changed, int left, int top, int right,
				int bottom)
		{
			super.onLayout(changed, left, top, right, bottom);

			if (!mHasLayout)
			{
				mHasLayout = true;
				int x = (int)(mScreenRect.width() - getWidth()
						* (1.0f - mHiddenW));
				int y = (int)(mScreenRect.height() * 0.15f);
				mViewPos.set(x, y);
				updatePosition(x, y);
			}
		}
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

	/**
	 * Init the dummy views to detect changes in the screen size
	 */
	private void initDummyView()
	{
		// We use two views, one for horizontal and one for vertical in order to
		// prevent having one view to cover the whole screen, which will disable
		// views with filterTouchesWhenObscured
		for (int i = 0; i < 2; ++i)
		{
			mDummyView[i] = new View(mContext);

			WindowManager.LayoutParams params = new WindowManager.LayoutParams(
					(i == 0) ? WindowManager.LayoutParams.MATCH_PARENT : 0,
					(i == 0) ? 0 : WindowManager.LayoutParams.MATCH_PARENT,
					WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
							| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
					PixelFormat.TRANSLUCENT);
			params.gravity = Gravity.TOP | Gravity.LEFT;
			mWindowManager.addView(mDummyView[i], params);
		}

		final Rect oldRect = new Rect();

		mDummyView[0].addOnLayoutChangeListener(new View.OnLayoutChangeListener()
		{
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight,
					int oldBottom)
			{
				Log.d(LOG_TAG + ".OnLayoutChangeListener", "onLayoutChange(0)");
				if (left != oldRect.left || top != oldRect.top
						|| right != oldRect.right)
				{
					// Bottom is invalid in this view
					Rect newRect = new Rect(left, top, right, oldRect.bottom);
					onScreenLayoutChange(newRect, oldRect);
					oldRect.set(newRect);
				}
			}
		});

		mDummyView[1].addOnLayoutChangeListener(new View.OnLayoutChangeListener()
		{
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight,
					int oldBottom)
			{
				Log.d(LOG_TAG + ".OnLayoutChangeListener", "onLayoutChange(1)");
				if (left != oldRect.left || top != oldRect.top
						|| bottom != oldRect.bottom)
				{
					// Right is invalid in this view
					Rect newRect = new Rect(left, top, oldRect.right, bottom);
					onScreenLayoutChange(newRect, oldRect);
					oldRect.set(newRect);
				}
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

	private void onScreenLayoutChange(Rect newRect, Rect oldRect)
	{
		Log.d(LOG_TAG, "onScreenLayoutChange()");
		int location[] = new int[2];
		mDummyView[0].getLocationOnScreen(location);
		Log.d(LOG_TAG + ".OnLayoutChangeListener",
				newRect.left + "," + oldRect.left + "\n"
						+ newRect.top + "," + oldRect.top + "\n"
						+ newRect.right + "," + oldRect.right + "\n"
						+ newRect.bottom + "," + oldRect.bottom + "\n"
						+ location[0] + "," + location[1]);

		Rect rect = new Rect(newRect.left + location[0],
				newRect.top + location[1], newRect.right + location[0],
				newRect.bottom + location[1]);
		if (isFullscreen(rect) && !isFullscreen(mScreenRect))
		{
			onIntoFullscreen();
		}
		else if (!isFullscreen(rect) && isFullscreen(mScreenRect))
		{
			onOutOfFullscreen();
		}
		mScreenRect.set(rect);
		snap(true);
	}

	private void onIntoFullscreen()
	{
		Log.d(LOG_TAG, "onIntoFullscreen()");
		if (mIsAutohide)
		{
			hide(null);
		}
	}

	private void onOutOfFullscreen()
	{
		Log.d(LOG_TAG, "onOutOfFullscreen()");
		if (mIsAutohide)
		{
			show(null);
		}
	}

	private void snap(boolean isAnimate)
	{
		// Bound the top and bottom
		int y = Math.max(Math.min(mLayoutParams.y,
				mScreenRect.bottom - mChild.getHeight()), mScreenRect.top);
		int x;
		// Take center
		if ((mLayoutParams.x + mChild.getWidth() / 2)
				< mScreenRect.width() / 2 + mScreenRect.left)
		{
			x = (int)(mScreenRect.left - mChild.getWidth() * mHiddenW);
		}
		else
		{
			x = (int)(mScreenRect.right - mChild.getWidth() * (1.0f - mHiddenW));
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

	private boolean isFullscreen(Rect rect)
	{
		if (mFullscreenSize == null)
		{
			mFullscreenSize = DeviceInfo.GetFullScreenPx(mContext);
		}
		// Check whether the orientation is the same
		if ((rect.height() > rect.width())
				== (mFullscreenSize.h() > mFullscreenSize.w()))
		{
			return (rect.width() == mFullscreenSize.w()
					&& rect.height() == mFullscreenSize.h());
		}
		else
		{
			return (rect.height() == mFullscreenSize.w()
					&& rect.width() == mFullscreenSize.h());
		}
	}

	private void setTouchable(boolean flag)
	{
		if (flag)
		{
			mLayoutParams.flags &=
					~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		}
		else
		{
			mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		}
		mWindowManager.updateViewLayout(mContainer, mLayoutParams);
	}

	private int mPrimaryId;
	private PointF mInitialTouchPos;
	// The position where the view should be, it could be different with the
	// actual position during animation
	private Point mViewPos;
	private Rect mScreenRect;
	private Size mFullscreenSize;
	private boolean mIsMoving;
	private boolean mHasLayout = false;
	private ObjectAnimator mSnapAnimators[] = new ObjectAnimator[2];

	private float mAlpha;
	private float mHiddenW;
	private boolean mIsAutohide;

	private Context mContext;
	private Handler mHandler;
	private Runnable mLongPressRunnable;
	private ContainerView mContainer;
	private View mChild;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLayoutParams;
	// Used to detect app hiding navigation bar
	private View mDummyView[] = new View[2];
}
