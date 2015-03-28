/*
 * MainFragment.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.nkming.utils.Res;
import com.shamanland.fab.FloatingActionButton;

public class MainFragment extends Fragment
{
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View root = inflater.inflate(R.layout.frag_main, container, false);
		initRoot(root);
		initButton(root);
		initReveal(root);
		return root;
	}

	private static final int SHUTDOWN_ID = 0;
	private static final int SLEEP_ID = 1;
	private static final int RESTART_ID = 2;

	private static final String LOG_TAG = Res.LOG_TAG + "."
			+ MainFragment.class.getSimpleName();

	private static interface PostRevealCallback
	{
		public void run();
	}

	private void initRoot(View root)
	{
		root.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				getActivity().finish();
			}
		});
	}

	private void initButton(View root)
	{
		for (int i = 0; i < mActionBtns.length; ++i)
		{
			mActionBtns[i] = (FloatingActionButton)root.findViewById(getViewId(i));
			final int id = i;
			mActionBtns[i].setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					onActionBtnClick(id);
				}
			});
			mActionBtns[i].setScaleX(0.0f);
			mActionBtns[i].setScaleY(0.0f);
			mActionBtns[i].animate().scaleX(1.0f).scaleY(1.0f)
					.setInterpolator(new DecelerateInterpolator())
					.setDuration(250).setStartDelay(100 * i);
		}
	}

	private void initReveal(View root)
	{
		mReveal = (RevealView)root.findViewById(R.id.reveal);
	}

	private void onActionBtnClick(int id)
	{
		switch (id)
		{
		default:
			Log.e(LOG_TAG + ".onActionBtnClick", "Unknown id");
			return;

		case SHUTDOWN_ID:
			onShutdownClick();
			return;

		case SLEEP_ID:
			onSleepClick();
			return;

		case RESTART_ID:
			onRestartClick();
			return;
		}
	}

	private void onShutdownClick()
	{
		startReveal(SHUTDOWN_ID, new PostRevealCallback()
		{
			@Override
			public void run()
			{
				if (!SystemHelper.shutdown(getActivity()))
				{
					Toast.makeText(getActivity(), R.string.shutdown_fail,
							Toast.LENGTH_LONG).show();
					getActivity().finish();
				}
			}
		});
	}

	private void onSleepClick()
	{
		startReveal(SLEEP_ID, new PostRevealCallback()
		{
			@Override
			public void run()
			{
				if (!SystemHelper.sleep(getActivity()))
				{
					Toast.makeText(getActivity(), R.string.sleep_fail,
							Toast.LENGTH_LONG).show();
				}
				getActivity().finish();
			}
		});
	}

	private void onRestartClick()
	{
		startReveal(RESTART_ID, new PostRevealCallback()
		{
			@Override
			public void run()
			{
				getActivity().finish();
			}
		});
	}

	private void startReveal(int btnId, final PostRevealCallback callback)
	{
		mReveal.setColor(getResources().getColor(getColorId(btnId)));

		int location[] = new int[2];
		mActionBtns[btnId].getLocationInWindow(location);
		int revealLocation[] = new int[2];
		mReveal.getLocationInWindow(revealLocation);
		int btnRadius = mActionBtns[btnId].getWidth() / 2;
		int x = location[0] - revealLocation[0] + btnRadius;
		int y = location[1] - revealLocation[1] + btnRadius;
		mReveal.setCenter(x, y);

		float radius = (float)Math.sqrt(Math.pow(mReveal.getHeight(), 2)
				+ Math.pow(mReveal.getWidth(), 2));
		ObjectAnimator anim = ObjectAnimator.ofFloat(mReveal, "radius",
				btnRadius, radius);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setDuration(250);
		if (callback != null)
		{
			anim.addListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					callback.run();
				}
			});
		}
		anim.start();

		for (int i = 0; i < 3; ++i)
		{
			if (i == btnId)
			{
				mActionBtns[i].setShadow(false);
			}
			else
			{
				mActionBtns[i].animate().alpha(0.0f)
						.setInterpolator(new AccelerateInterpolator())
						.setDuration(150).setStartDelay(0);
			}
		}
	}

	private int getViewId(int btnId)
	{
		switch (btnId)
		{
		default:
			Log.e(LOG_TAG + ".getViewId", "Unknown id");
		case SHUTDOWN_ID:
			return R.id.shutdown_id;

		case SLEEP_ID:
			return R.id.sleep_btn;

		case RESTART_ID:
			return R.id.restart_btn;
		}
	}

	private int getColorId(int btnId)
	{
		switch (btnId)
		{
		default:
			Log.e(LOG_TAG + ".getColorId", "Unknown id");
		case SHUTDOWN_ID:
			return R.color.shutdown_bg;

		case SLEEP_ID:
			return R.color.sleep_bg;

		case RESTART_ID:
			return R.color.restart_bg;
		}
	}

	private FloatingActionButton mActionBtns[] = new FloatingActionButton[3];
	private RevealView mReveal;
}
