/*
 * RestartDetailFragment.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;

public class RestartDetailFragment extends Fragment
{
	public static RestartDetailFragment create()
	{
		return new RestartDetailFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		mRootView = inflater.inflate(R.layout.restart_details, container, false);
		initButtons();
		expand(container);
		return mRootView;
	}

	private static final int NORMAL_ID = 0;
	private static final int RECOVERY_ID = 1;
	private static final int BOOTLOADER_ID = 2;

	private static final String LOG_TAG = Res.LOG_TAG + "."
			+ RestartDetailFragment.class.getSimpleName();

	private void initButtons()
	{
		for (int i = 0; i < mBtns.length; ++i)
		{
			mBtns[i] = (Button)mRootView.findViewById(getViewId(i));
		}
	}

	private void expand(View container)
	{
		mRootView.measure(
				View.MeasureSpec.makeMeasureSpec(container.getMeasuredWidth(),
						View.MeasureSpec.EXACTLY),
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		ValueAnimator anim = ValueAnimator.ofInt(0, mRootView.getMeasuredHeight());
		anim.setDuration(Res.ANIMATION_FAST);
		anim.setInterpolator(new AccelerateDecelerateInterpolator());
		anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				Integer val = (Integer)animation.getAnimatedValue();
				mRootView.getLayoutParams().height = val;
				mRootView.requestLayout();
			}
		});
		anim.start();
	}

	private int getViewId(int btnId)
	{
		switch (btnId)
		{
		default:
			Log.e(LOG_TAG + ".getViewId", "Unknown id");
		case NORMAL_ID:
			return R.id.restart_normal;

		case RECOVERY_ID:
			return R.id.restart_recovery;

		case BOOTLOADER_ID:
			return R.id.restart_bootloader;
		}
	}

	private View mRootView;
	private Button mBtns[] = new Button[3];
}
