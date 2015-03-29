/*
 * InstallFragment.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

public class InstallConfirmFragment extends DialogFragment
{
	public static interface Listener
	{
		public void onInstallConfirmed();
	}

	public static InstallConfirmFragment create()
	{
		return new InstallConfirmFragment();
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		return new MaterialDialog.Builder(getActivity())
				.title(R.string.install_confirm_title)
				.content(R.string.install_confirm_content)
				.positiveText(R.string.install_confirm_positive)
				.negativeText(android.R.string.cancel)
				.callback(new ButtonCallback())
				.build();
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		if (!(activity instanceof Listener))
		{
			Log.wtf(LOG_TAG + ".onActivityCreated",
					"Activity must implement Listener");
			// In case wtf doesn't throw
			throw new IllegalStateException();
		}
		mListener = (Listener)activity;
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onDismiss(DialogInterface dialog)
	{
		super.onDismiss(dialog);
		if (getActivity() != null && !mIsNoFinish)
		{
			getActivity().finish();
		}
	}

	private static final String LOG_TAG = Res.LOG_TAG + "."
			+ InstallConfirmFragment.class.getSimpleName();

	private class ButtonCallback extends MaterialDialog.ButtonCallback
	{
		@Override
		public void onPositive(MaterialDialog dialog)
		{
			if (mListener != null)
			{
				mListener.onInstallConfirmed();
				mIsNoFinish = true;
			}
		}
	}

	private Listener mListener;
	private boolean mIsNoFinish = false;
}
