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
import android.view.animation.AccelerateInterpolator;

import com.shamanland.fab.FloatingActionButton;

public class MainFragment extends Fragment
{
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View root = inflater.inflate(R.layout.frag_main, container, false);
		initButton(root);
		return root;
	}

	private void initButton(View root)
	{
		mShutdownBtn = (FloatingActionButton)root.findViewById(R.id.shutdown_id);
		mRestartBtn = (FloatingActionButton)root.findViewById(R.id.restart_btn);
		mSleepBtn = (FloatingActionButton)root.findViewById(R.id.sleep_btn);
	}

	private FloatingActionButton mShutdownBtn;
	private FloatingActionButton mRestartBtn;
	private FloatingActionButton mSleepBtn;
}
