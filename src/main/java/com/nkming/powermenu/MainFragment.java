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
import android.os.Bundle;
import android.os.Handler;
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
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mHandler = new Handler();
	}

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
			mActionBtnBounds[i] = root.findViewById(getViewBoundId(i));
			final int id = i;
			mActionBtnBounds[i].setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					onActionBtnClick(id);
				}
			});
			mActionBtnBounds[i].setScaleX(0.0f);
			mActionBtnBounds[i].setScaleY(0.0f);
			mActionBtnBounds[i].animate().scaleX(1.0f).scaleY(1.0f)
					.setInterpolator(new DecelerateInterpolator())
					.setDuration(Res.ANIMATION_FAST)
					.setStartDelay(Res.ANIMATION_FAST / 2 * i);
		}

		mRestartBtns[RESTART_NORMAL_ID] = mActionBtns[RESTART_ID];
		mRestartBtnBounds[RESTART_NORMAL_ID] = mActionBtnBounds[RESTART_ID];
		mRestartLabels[RESTART_NORMAL_ID] = root.findViewById(
				R.id.restart_normal_label);

		mRestartBtns[RESTART_RECOVERY_ID] = (FloatingActionButton)root
				.findViewById(R.id.restart_recovery_btn);
		mRestartBtnBounds[RESTART_RECOVERY_ID] = root
				.findViewById(R.id.restart_recovery_btn_bound);
		mRestartLabels[RESTART_RECOVERY_ID] = root.findViewById(
				R.id.restart_recovery_label);

		mRestartBtns[RESTART_BOOTLOADER_ID] = (FloatingActionButton)root
				.findViewById(R.id.restart_bootloader_btn);
		mRestartBtnBounds[RESTART_BOOTLOADER_ID] = root
				.findViewById(R.id.restart_bootloader_btn_bound);
		mRestartLabels[RESTART_BOOTLOADER_ID] = root.findViewById(
				R.id.restart_bootloader_label);
	}

	private void initReveal(View root)
	{
		mReveal = (RevealView)root.findViewById(R.id.reveal);
	}

	private void initRestartBtns()
	{
		int ids[] = {RESTART_NORMAL_ID, RESTART_RECOVERY_ID,
				RESTART_BOOTLOADER_ID};
		for (int i = 0; i < ids.length; ++i)
		{
			final int id = ids[i];
			mRestartBtnBounds[id].setOnClickListener(
					new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							onRestartMenuClick(id);
						}
					});
		}
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
		startReveal(mActionBtns[SHUTDOWN_ID], R.color.shutdown_bg, true,
				new Runnable()
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

		mActionBtns[SHUTDOWN_ID].setShadow(false);
		dismissOtherViews(mActionBtnBounds, mActionBtnBounds[SHUTDOWN_ID]);
		// Disable all click bounds
		disableOtherButtonBounds(mActionBtnBounds, null);
	}

	private void onSleepClick()
	{
		startReveal(mActionBtns[SLEEP_ID], R.color.sleep_bg, true,
				new Runnable()
		{
			@Override
			public void run()
			{
				// App probably closed
				if (getActivity() == null)
				{
					return;
				}
				getActivity().finish();
			}
		});

		// Sleep will run on a new thread and involve su, that takes quite some
		// time so do it at once
		SystemHelper.sleep(getActivity().getApplicationContext());

		mActionBtns[SLEEP_ID].setShadow(false);
		dismissOtherViews(mActionBtnBounds, mActionBtnBounds[SLEEP_ID]);
		disableOtherButtonBounds(mActionBtnBounds, null);
	}

	private void onRestartClick()
	{
		int ids[] = {SHUTDOWN_ID, SLEEP_ID};
		for (int i = 0; i < ids.length; ++i)
		{
			mActionBtnBounds[ids[i]].animate().xBy(100).alpha(0.0f)
					.setInterpolator(new AccelerateInterpolator())
					.setDuration(Res.ANIMATION_FAST)
					.setStartDelay(50 * i);
			disableButtonBound(mActionBtnBounds[ids[i]]);
		}

		mActionBtns[RESTART_ID].animate().rotationBy(180)
				.setInterpolator(new AccelerateDecelerateInterpolator())
				.setDuration(Res.ANIMATION_MID)
				.setStartDelay(0);
		mActionBtnBounds[RESTART_ID].setOnClickListener(null);
		showRestartMenu(Res.ANIMATION_MID - Res.ANIMATION_FAST,
				new Runnable()
		{
			@Override
			public void run()
			{
				initRestartBtns();
			}
		});
	}

	private void onRestartMenuClick(final int id)
	{
		startReveal(mRestartBtns[id], R.color.restart_bg, true,
				new Runnable()
		{
			@Override
			public void run()
			{
				// App probably closed
				if (getActivity() == null)
				{
					return;
				}
				if (!SystemHelper.reboot(getRebootMode(id), getActivity()))
				{
					Toast.makeText(getActivity(), R.string.restart_fail,
							Toast.LENGTH_LONG).show();
					getActivity().finish();
				}
			}
		});

		mRestartBtns[id].setShadow(false);
		dismissOtherViews(mRestartBtnBounds, mRestartBtnBounds[id]);
		dismissOtherViews(mRestartLabels, null);
		disableOtherButtonBounds(mRestartBtnBounds, null);
	}

	/**
	 * Start the reveal animation
	 *
	 * @param atView Center the effect at the location of this view
	 * @param colorId The color resource of the effect
	 * @param isDelayCallback Whether to delay the callback after the animation
	 * finished
	 * @param callback The callback to be run after the animation
	 */
	private void startReveal(View atView, int colorId,
			final boolean isDelayCallback, final Runnable callback)
	{
		mReveal.setColor(getResources().getColor(colorId));

		int location[] = new int[2];
		atView.getLocationInWindow(location);
		int revealLocation[] = new int[2];
		mReveal.getLocationInWindow(revealLocation);
		int x = location[0] - revealLocation[0];
		int y = location[1] - revealLocation[1];
		double viewRadius = Math.sqrt(Math.pow(atView.getWidth() / 2, 2) * 2);
		double viewRotationRadian = Math.toRadians(atView.getRotation() + 135);
		x -= viewRadius * Math.cos(viewRotationRadian);
		y -= -(viewRadius * Math.sin(viewRotationRadian));
		mReveal.setCenter(x, y);

		Animator.AnimatorListener listener = null;
		if (callback != null)
		{
			listener = new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					if (isDelayCallback)
					{
						mHandler.postDelayed(callback, 20);
					}
					else
					{
						callback.run();
					}
				}
			};
		}
		mReveal.reveal(Res.ANIMATION_MID, listener);
	}

	private void dismissOtherViews(View views[], View keep)
	{
		for (View v : views)
		{
			if (v != keep)
			{
				v.animate().alpha(0.0f)
						.setInterpolator(new AccelerateInterpolator())
						.setDuration(Res.ANIMATION_FAST).setStartDelay(0);
			}
		}
	}

	private void disableOtherButtonBounds(View btns[], View keepBtn)
	{
		for (View v : btns)
		{
			if (v != keepBtn)
			{
				disableButtonBound(v);
			}
		}
	}

	private void disableButtonBound(View btn)
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
	 * @param callback Call after the animation finished
	 */
	private void showRestartMenu(int delay, final Runnable callback)
	{
		int ids[] = {RESTART_RECOVERY_ID, RESTART_BOOTLOADER_ID};
		for (int i = 0; i < ids.length; ++i)
		{
			final int id = ids[i];
			View views[] = {mRestartBtnBounds[id], mRestartLabels[id]};
			for (View v : views)
			{
				v.setVisibility(View.VISIBLE);
				v.setAlpha(0.0f);
				v.setY(v.getY() + 50);
				v.animate().alpha(1.0f).yBy(-50)
						.setInterpolator(new DecelerateInterpolator())
						.setDuration(Res.ANIMATION_FAST)
						.setStartDelay(delay);
			}
		}

		mRestartLabels[RESTART_NORMAL_ID].setVisibility(View.VISIBLE);
		mRestartLabels[RESTART_NORMAL_ID].setAlpha(0.0f);
		mRestartLabels[RESTART_NORMAL_ID].animate().alpha(1.0f)
				.setInterpolator(new DecelerateInterpolator())
				.setDuration(Res.ANIMATION_FAST)
				.setStartDelay(delay);
		// All animations start and end at the same time, so we only need to set
		// once here
		if (callback != null)
		{
			mRestartLabels[RESTART_NORMAL_ID].animate().setListener(
					new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					callback.run();
				}
			});
		}
	}

	private int getViewBoundId(int btnId)
	{
		switch (btnId)
		{
		default:
			Log.e(LOG_TAG + ".getViewId", "Unknown id");
		case SHUTDOWN_ID:
			return R.id.shutdown_btn_bound;

		case SLEEP_ID:
			return R.id.sleep_btn_bound;

		case RESTART_ID:
			return R.id.restart_btn_bound;
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

	private SystemHelper.RebootMode getRebootMode(int restartBtnId)
	{
		switch (restartBtnId)
		{
		default:
			Log.e(LOG_TAG + ".getRebootMode", "Unknown id");
		case RESTART_NORMAL_ID:
			return SystemHelper.RebootMode.NORMAL;

		case RESTART_RECOVERY_ID:
			return SystemHelper.RebootMode.RECOVERY;

		case RESTART_BOOTLOADER_ID:
			return SystemHelper.RebootMode.BOOTLOADER;
		}
	}

	private Handler mHandler;
	private View mActionBtnBounds[] = new View[3];
	private FloatingActionButton mActionBtns[] = new FloatingActionButton[3];
	private View mRestartBtnBounds[] = new View[3];
	private FloatingActionButton mRestartBtns[] = new FloatingActionButton[3];
	private View mRestartLabels[] = new View[3];
	private RevealView mReveal;
}
