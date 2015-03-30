/*
 * UninstallFragment.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

public class UninstallFragment extends DialogFragment
{
	public static UninstallFragment create()
	{
		return new UninstallFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		mHandler = new Handler();
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		return new MaterialDialog.Builder(getActivity())
				.title(R.string.uninstall_title)
				.title(R.string.uninstall_content)
				.progress(true, 0)
				.cancelable(false)
				.build();
	}

	@Override
	public void onDestroyView()
	{
		// https://code.google.com/p/android/issues/detail?id=17423
		if (getDialog() != null && getRetainInstance())
		{
			getDialog().setOnDismissListener(null);
		}
		super.onDestroyView();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState == null)
		{
			uninstall();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		processResult();
	}

	@Override
	public void onDismiss(DialogInterface dialog)
	{
		super.onDismiss(dialog);
		if (getActivity() != null)
		{
			getActivity().finish();
		}
	}

	private void uninstall()
	{
		new InstallHelper.UninstallTask()
		{
			@Override
			protected Boolean doInBackground(Context... params)
			{
				SystemHelper.disableDeviceAdmin(getActivity());
				return super.doInBackground(params);
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				mResult = result;
				if (isResumed())
				{
					processResult();
				}
			}
		}.execute(getActivity());
	}

	private void processResult()
	{
		if (mResult == null)
		{
			// No result
			return;
		}

		if (!mResult)
		{
			Toast.makeText(getActivity(), R.string.uninstall_failed,
					Toast.LENGTH_LONG).show();
		}
		// If we dismiss right now, we can't finish the parent activity
		mHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				dismiss();
			}
		});
		mResult = null;
	}

	private Handler mHandler;
	private Boolean mResult;
}
