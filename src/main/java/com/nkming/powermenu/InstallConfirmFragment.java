/*
 * InstallFragment.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

public class InstallConfirmFragment extends DialogFragment
{
	public static InstallConfirmFragment create()
	{
		return new InstallConfirmFragment();
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		return new MaterialDialog.Builder(getActivity())
				.title(R.string.install_title)
				.content(R.string.install_content)
				.positiveText(R.string.install_positive)
				.negativeText(android.R.string.cancel)
				.callback(new ButtonCallback())
				.build();
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

	private class ButtonCallback extends MaterialDialog.ButtonCallback
	{
		@Override
		public void onPositive(MaterialDialog dialog)
		{

		}
	}
}
