/*
 * RevealView.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

public class RevealView extends View
{
	public RevealView(Context context)
	{
		super(context);
		init();
	}

	public RevealView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public RevealView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public RevealView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	public void setRadius(float val)
	{
		mRadius = val;
		invalidate();
	}

	public float getRadius()
	{
		return mRadius;
	}

	public void setColor(int color)
	{
		mPaint.setColor(color);
	}

	public void setCenter(float x, float y)
	{
		mX = x;
		mY = y;
	}

	public void reveal(int duration, Animator.AnimatorListener listener)
	{
		float longerW = Math.max(getWidth() - mX, mX);
		float longerH = Math.max(getHeight() - mY, mY);
		float revealRadius = (float)Math.sqrt(Math.pow(longerW, 2) + Math.pow(
				longerH, 2));
		ObjectAnimator anim = ObjectAnimator.ofFloat(this, "radius", 0,
				revealRadius);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setDuration(duration);
		if (listener != null)
		{
			anim.addListener(listener);
		}
		anim.start();
	}

	@Override
	public void draw(Canvas canvas)
	{
		canvas.drawCircle(mX, mY, mRadius, mPaint);
	}

	private void init()
	{
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(0xFFFFFFFF);
		mRadius = 0.0f;
		mX = 0.0f;
		mY = 0.0f;
	}

	private Paint mPaint;
	private float mRadius;
	private float mX;
	private float mY;
}
