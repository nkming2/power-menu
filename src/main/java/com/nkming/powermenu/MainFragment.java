/*
 * MainFragment.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
		return root;
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
		mShutdownBtn = (FloatingActionButton)root.findViewById(R.id.shutdown_id);
		mShutdownBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onShutdownClick();
			}
		});
		mShutdownBtn.setScaleX(0.0f);
		mShutdownBtn.setScaleY(0.0f);
		mShutdownBtn.animate().scaleX(1.0f).scaleY(1.0f)
				.setInterpolator(new DecelerateInterpolator())
				.setDuration(250);

		mSleepBtn = (FloatingActionButton)root.findViewById(R.id.sleep_btn);
		mSleepBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onSleepClick();
			}
		});
		mSleepBtn.setScaleX(0.0f);
		mSleepBtn.setScaleY(0.0f);
		mSleepBtn.animate().scaleX(1.0f).scaleY(1.0f)
				.setInterpolator(new DecelerateInterpolator())
				.setDuration(250).setStartDelay(100);


		mRestartBtn = (FloatingActionButton)root.findViewById(R.id.restart_btn);
		mRestartBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onRestartClick();
			}
		});
		mRestartBtn.setScaleX(0.0f);
		mRestartBtn.setScaleY(0.0f);
		mRestartBtn.animate().scaleX(1.0f).scaleY(1.0f)
				.setInterpolator(new DecelerateInterpolator())
				.setDuration(250).setStartDelay(200);
	}

	private void onShutdownClick()
	{
		if (!SystemHelper.shutdown(getActivity()))
		{
			Toast.makeText(getActivity(), R.string.shutdown_fail,
					Toast.LENGTH_LONG).show();
			getActivity().finish();
		}
	}

	private void onSleepClick()
	{

	}

	private void onRestartClick()
	{

	}

	private FloatingActionButton mShutdownBtn;
	private FloatingActionButton mSleepBtn;
	private FloatingActionButton mRestartBtn;
}
