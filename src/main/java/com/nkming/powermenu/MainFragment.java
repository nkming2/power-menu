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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

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

	private static interface PostAnimationCallback
	{
		public void run();
	}

	private static final int SHUTDOWN_ID = 0;
	private static final int SLEEP_ID = 1;
	private static final int RESTART_ID = 2;

	private static final int RESTART_NORMAL_ID = 0;
	private static final int RESTART_RECOVERY_ID = 1;
	private static final int RESTART_BOOTLOADER_ID = 2;

	private static final String LOG_TAG = Res.LOG_TAG + "."
			+ MainFragment.class.getSimpleName();

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
					.setDuration(Res.ANIMATION_FAST)
					.setStartDelay(Res.ANIMATION_FAST / 2 * i);
		}

		mRestartBtns[RESTART_NORMAL_ID] = mActionBtns[RESTART_ID];
		mRestartBtns[RESTART_RECOVERY_ID] = (FloatingActionButton)root
				.findViewById(R.id.restart_recovery_btn);
		mRestartBtns[RESTART_BOOTLOADER_ID] = (FloatingActionButton)root
				.findViewById(R.id.restart_bootloader_btn);
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
		startReveal(SHUTDOWN_ID, new PostAnimationCallback()
		{
			@Override
			public void run()
			{
				// App probably closed
				if (getActivity() == null)
				{
					return;
				}
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
		startReveal(SLEEP_ID, new PostAnimationCallback()
		{
			@Override
			public void run()
			{
				// App probably closed
				if (getActivity() == null)
				{
					return;
				}
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
		int ids[] = {SHUTDOWN_ID, SLEEP_ID};
		for (int i = 0; i < ids.length; ++i)
		{
			mActionBtns[ids[i]].animate().xBy(100).alpha(0.0f)
					.setInterpolator(new AccelerateInterpolator())
					.setDuration(Res.ANIMATION_FAST)
					.setStartDelay(50 * i);
			disableButton(mActionBtns[ids[i]]);
		}

		mActionBtns[RESTART_ID].animate().rotationBy(360)
				.setInterpolator(new AccelerateDecelerateInterpolator())
				.setDuration(Res.ANIMATION_MID)
				.setStartDelay(0);
		mActionBtns[RESTART_ID].setOnClickListener(null);
		showRestartMenu(Res.ANIMATION_MID - Res.ANIMATION_FAST);
	}

	private void startReveal(int btnId, final PostAnimationCallback callback)
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

		int longerW = Math.max(mReveal.getWidth() - x, x);
		int longerH = Math.max(mReveal.getHeight() - y, y);
		float radius = (float)Math.sqrt(Math.pow(longerW, 2) + Math.pow(
				longerH, 2));
		ObjectAnimator anim = ObjectAnimator.ofFloat(mReveal, "radius",
				btnRadius, radius);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setDuration(Res.ANIMATION_MID);
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
						.setDuration(Res.ANIMATION_FAST).setStartDelay(0);
			}
			disableButton(mActionBtns[i]);
		}
	}

	private void disableButton(View btn)
	{
		btn.setOnClickListener(null);
		btn.setFocusable(false);
		btn.setClickable(false);
		btn.setEnabled(false);
	}

	/**
	 * Show the restart menu
	 *
	 * @param delay Delay the show animation
	 */
	private void showRestartMenu(int delay)
	{
		int ids[] = {RESTART_RECOVERY_ID, RESTART_BOOTLOADER_ID};
		for (int i = 0; i < ids.length; ++i)
		{
			mRestartBtns[ids[i]].setVisibility(View.VISIBLE);
			mRestartBtns[ids[i]].setAlpha(0.0f);
			mRestartBtns[ids[i]].setY(mRestartBtns[ids[i]].getY() + 50);
			mRestartBtns[ids[i]].animate().alpha(1.0f).yBy(-50)
					.setInterpolator(new DecelerateInterpolator())
					.setDuration(Res.ANIMATION_FAST)
					.setStartDelay(delay);
		}

		View normalLabel = getView().findViewById(R.id.restart_normal_label);
		normalLabel.setVisibility(View.VISIBLE);
		normalLabel.setAlpha(0.0f);
		normalLabel.animate().alpha(1.0f)
				.setInterpolator(new DecelerateInterpolator())
				.setDuration(Res.ANIMATION_FAST)
				.setStartDelay(delay);

		View labels[] = new View[2];
		labels[0] = getView().findViewById(R.id.restart_recovery_label);
		labels[1] = getView().findViewById(R.id.restart_bootloader_label);
		for (int i = 0; i < 2; ++i)
		{
			labels[i].setVisibility(View.VISIBLE);
			labels[i].setAlpha(0.0f);
			labels[i].setY(labels[i].getY() + 50);
			labels[i].animate().alpha(1.0f).yBy(-50)
					.setInterpolator(new DecelerateInterpolator())
					.setDuration(Res.ANIMATION_FAST)
					.setStartDelay(delay);
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
	private FloatingActionButton mRestartBtns[] = new FloatingActionButton[3];
	private RevealView mReveal;
}
