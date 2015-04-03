/*
 * PersistentView.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.nkming.utils.sys.DeviceInfo;
import com.nkming.utils.type.Size;

public class PersistentView extends FrameLayout
{
	public PersistentView(Context context)
	{
		super(context);
		init();
	}

	public PersistentView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public PersistentView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	/**
	 * Inflate a persistent view and add it to the WindowManager
	 *
	 * @param context
	 * @param resId
	 * @return
	 */
	public static PersistentView create(Context context, int resId)
	{
		PersistentView v = (PersistentView)LayoutInflater.from(context).inflate(
				resId, null);
		WindowManager wm = (WindowManager)context.getSystemService(
				Context.WINDOW_SERVICE);
		wm.addView(v, v.mLayoutParams);
		return v;
	}

	public static void destroy(Context context, PersistentView v)
	{
		WindowManager wm = (WindowManager)context.getSystemService(
				Context.WINDOW_SERVICE);
		wm.removeView(v);
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

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getActionMasked())
		{
		case MotionEvent.ACTION_DOWN:
			mPrimaryId = event.getActionIndex();
			mInitialPos = new PointF(event.getRawX(), event.getRawY());
			break;

		case MotionEvent.ACTION_POINTER_UP:
			if (event.getActionIndex() == mPrimaryId)
			{
				reset();
			}
			break;

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			reset();
			break;

		case MotionEvent.ACTION_MOVE:
			if (event.getActionIndex() == mPrimaryId)
			{
				onActionMove(event);
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	private static final String LOG_TAG = Res.LOG_TAG + "."
			+ PersistentView.class.getSimpleName();

	private void init()
	{
		mScreenSize = DeviceInfo.GetScreenPx(getContext());
		mWindowManager = (WindowManager)getContext().getSystemService(
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

		reset();
	}

	private void onActionMove(MotionEvent event)
	{
		Log.i("", event.getRawX() + "," + event.getRawY());
		evaluateMoving(event);
		if (mIsMoving)
		{
			// Take center
			int y = (int)event.getRawY() - getHeight() / 2;
			int x;
			if (event.getRawX() < mScreenSize.w() / 2)
			{
				x = (int)(0 - getWidth() * mHiddenW);
			}
			else
			{
				x = (int)(mScreenSize.w() - getWidth() * (1.0f - mHiddenW));
			}
			updatePosition(x, y);
		}
	}

	private void reset()
	{
		mPrimaryId = -1;
		mInitialPos = new PointF();
		mIsMoving = false;
	}

	private void evaluateMoving(MotionEvent event)
	{
		if (Math.abs(event.getRawX() - mInitialPos.x) >= 100
				|| Math.abs(event.getRawY() - mInitialPos.y) >= 100)
		{
			mIsMoving = true;
		}
	}

	private void updatePosition(int x, int y)
	{
		mLayoutParams.x = x;
		mLayoutParams.y = y;
		mWindowManager.updateViewLayout(this, mLayoutParams);
	}

	private int mPrimaryId;
	private PointF mInitialPos;
	private boolean mIsMoving;
	private boolean mHasLayout = false;

	/// Percentage of width that is beyond the edge of the screen
	private float mHiddenW = 0.15f;

	private Size mScreenSize;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLayoutParams;
}
