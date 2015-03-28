/*
 * RevealView.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

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
