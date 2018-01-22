package com.nkming.powermenu

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import com.shamanland.fab.FloatingActionButton

class MainFragment : Fragment()
{
	companion object
	{
		private val LOG_TAG = MainFragment::class.java.canonicalName

		private const val ACTION_ON_DESTROY = "${Res.PACKAGE}.ACTION_ON_DESTROY"
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
			savedInstanceState: Bundle?): View
	{
		_root = inflater.inflate(R.layout.frag_main, container, false)
		_initRoot()
		_initButton()
		return _root
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
		if (!PermissionUtils.hasWriteExternalStorage(context!!))
		{
			_isReqPermission = true
			PermissionUtils.requestWriteExternalStorage(this)
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int,
			permissions: Array<out String>, grantResults: IntArray)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		_isReqPermission = false
	}

	override fun onStop()
	{
		super.onStop()
		_activeDangerousAction?.dismissConfirm()
		if (!_isReqPermission)
		{
			// Like noHistory, not using that because we don't want the
			// permission activity to kill our activity
			activity?.finish()
		}
	}

	override fun onDestroy()
	{
		super.onDestroy()
		LocalBroadcastManager.getInstance(_appContext)
				.sendBroadcast(Intent(ACTION_ON_DESTROY))
	}

	private data class ActionButtonMeta
	(
		val btn: FloatingActionButton,
		val bound: View,
		val onClick: () -> Unit
	)

	private data class RestartButtonMeta
	(
		val btn: FloatingActionButton,
		val bound: View,
		val label: TextView,
		val onClick: () -> Unit
	)

	private fun _initRoot()
	{
		_root.setOnClickListener(
		{
			activity?.finish()
		})
	}

	private fun _initButton()
	{
		for ((i, btn) in _actionBtns.withIndex())
		{
			btn.bound.setOnClickListener(
			{
				btn.onClick()
			})
			btn.bound.scaleX = 0f
			btn.bound.scaleY = 0f
			btn.bound.animate().scaleX(1f).scaleY(1f)
					.setInterpolator(DecelerateInterpolator())
					.setDuration(Res.ANIMATION_FAST.toLong())
					.setStartDelay(Res.ANIMATION_FAST.toLong() / 2 * i)
		}

		val pref = Preference.from(context!!)
		_restartNormalBtn.label.text = getString(
				if (pref.isSoftRebootEnabled && SystemHelper.isBusyboxPresent())
						R.string.restart_mode_normal_or_soft
				else R.string.restart_mode_normal)
	}

	private fun _initRestartBtns()
	{
		for (b in _restartMenuBtns)
		{
			b.bound.setOnClickListener(
			{
				b.onClick()
			})
		}

		val pref = Preference.from(context!!)
		if (pref.isSoftRebootEnabled && SystemHelper.isBusyboxPresent())
		{
			_restartNormalBtn.bound.isHapticFeedbackEnabled =
					pref.isHapticEnabled
			_restartNormalBtn.bound.setOnLongClickListener(
			{
				_onRestartNormalLongClick()
				true
			})
		}
	}

	private fun _onShutdownClick()
	{
		val action = object: ShutdownAction(_appContext, activity!!,
				isExplicitTheming = true)
		{
			override fun onPostConfirm()
			{
				_startReveal(_shutdownBtn.btn, R.color.shutdown_bg, true,
				{
					activity ?: return@_startReveal
					super.onPostConfirm()
				})

				_shutdownBtn.btn.isShadow = false
				_dismissOtherBounds(_shutdownBtn)
				// Disable all click bounds
				_disableOtherBounds(null as ActionButtonMeta?)
			}
		}
		// If succeeded, we'll get killed anyway
		action.onFailed = {activity?.finish()}
		action()
		_activeDangerousAction = action
	}

	private fun _onSleepClick()
	{
		_startReveal(_sleepBtn.btn, R.color.sleep_bg, true, null)

		// Finish activity on screen off
		val receiver = object: BroadcastReceiver()
		{
			override fun onReceive(context: Context?, intent: Intent?)
			{
				activity?.finish()
				activity?.unregisterReceiver(this)
			}
		}
		activity!!.registerReceiver(receiver,
				IntentFilter(Intent.ACTION_SCREEN_ON))

		// Sleep will run on a new thread and involve su, that takes quite some
		// time so do it at once
		val action = SleepAction(_appContext)
		action.onFailed =
		{
			activity?.finish()
			activity?.unregisterReceiver(receiver)
		}
		action()

		_sleepBtn.btn.isShadow = false
		_dismissOtherBounds(_sleepBtn)
		_disableOtherBounds(null as ActionButtonMeta?)
	}

	private fun _onRestartClick()
	{
		for ((i, b) in _actionBtns.withIndex())
		{
			if (b === _restartBtn)
			{
				// Keep restart btn
				continue
			}

			b.bound.animate().xBy(100f).alpha(0f)
					.setInterpolator(AccelerateInterpolator())
					.setDuration(Res.ANIMATION_FAST.toLong())
					.setStartDelay((50 * i).toLong())
		}
		_disableOtherBounds(_restartBtn)

		_restartBtn.btn.animate().rotationBy(180f)
				.setInterpolator(AccelerateDecelerateInterpolator())
				.setDuration(Res.ANIMATION_FAST.toLong())
				.setStartDelay(0)
		_restartBtn.bound.setOnClickListener(null)
		_showRestartMenu(Res.ANIMATION_FAST / 2,
		{
			activity ?: return@_showRestartMenu
			_initRestartBtns()
		})
	}

	private fun _onScreenshotClick()
	{
		val close_duration = resources.getInteger(R.integer.close_duration)
				.toLong()
		_startReveal(_screenshotBtn.btn, R.color.screenshot_bg, true,
		{
			activity ?: return@_startReveal
			activity!!.finish()

			val receiver = object: BroadcastReceiver()
			{
				override fun onReceive(context: Context?, intent: Intent?)
				{
					LocalBroadcastManager.getInstance(_appContext)
							.unregisterReceiver(this)

					_handler.postDelayed(
					{
						ScreenshotAction(_appContext)()
					}, close_duration)
				}
			}
			LocalBroadcastManager.getInstance(_appContext)
					.registerReceiver(receiver, IntentFilter(ACTION_ON_DESTROY))
		})

		_screenshotBtn.btn.isShadow = false
		_dismissOtherBounds(_screenshotBtn)
		_disableOtherBounds(null as ActionButtonMeta?)
	}

	private fun _onRestartMenuClick(meta: RestartButtonMeta,
			rebootMode: SystemHelper.RebootMode)
	{
		val action = object: RebootAction(_appContext, activity!!, rebootMode,
				isExplicitTheming = true)
		{
			override fun onPostConfirm()
			{
				_startReveal(meta.btn, R.color.restart_bg, true,
				{
					activity ?: return@_startReveal
					super.onPostConfirm()
				})

				meta.btn.isShadow = false
				_dismissOtherBounds(meta)
				_dismissOtherLabels(null)
				_disableOtherBounds(null as RestartButtonMeta?)
			}
		}
		// If succeeded, we'll get killed anyway
		action.onFailed = {activity?.finish()}
		action()
		_activeDangerousAction = action
	}

	private fun _onRestartNormalClick()
	{
		_onRestartMenuClick(_restartNormalBtn, SystemHelper.RebootMode.NORMAL)
	}

	private fun _onRestartNormalLongClick()
	{
		val action = object: SoftRebootAction(_appContext, activity!!,
				isExplicitTheming = true)
		{
			override fun onPostConfirm()
			{
				_startReveal(_restartNormalBtn.btn, R.color.restart_bg, true,
				{
					activity ?: return@_startReveal
					super.onPostConfirm()
				})

				_restartNormalBtn.btn.isShadow = false
				_dismissOtherBounds(_restartNormalBtn)
				_dismissOtherLabels(null)
				_disableOtherBounds(null as RestartButtonMeta?)
			}
		}
		// If succeeded, we'll get killed anyway
		action.onFailed = {activity?.finish()}
		action()
		_activeDangerousAction = action
	}

	private fun _onRestartRecoveryClick()
	{
		_onRestartMenuClick(_restartRecoveryBtn,
				SystemHelper.RebootMode.RECOVERY)
	}

	private fun _onRestartBootloaderClick()
	{
		_onRestartMenuClick(_restartBootloaderBtn,
				SystemHelper.RebootMode.BOOTLOADER)
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
	private fun _startReveal(atView: View, colorId: Int,
			isDelayCallback: Boolean, callback: (() -> Unit)?)
	{
		_reveal.setColor(resources.getColor(colorId))

		val location = IntArray(2)
		atView.getLocationInWindow(location)
		val revealLocation = IntArray(2)
		_reveal.getLocationInWindow(revealLocation)
		var x = (location[0] - revealLocation[0]).toFloat()
		var y = (location[1] - revealLocation[1]).toFloat()
		val viewRadius = Math.sqrt(Math.pow(atView.width / 2.0, 2.0) * 2)
		val viewRotationRadian = Math.toRadians(atView.rotation + 135.0)
		x -= (viewRadius * Math.cos(viewRotationRadian)).toFloat()
		y -= -(viewRadius * Math.sin(viewRotationRadian)).toFloat()
		_reveal.setCenter(x, y)

		var l: Animator.AnimatorListener? = null
		if (callback != null)
		{
			l = object : AnimatorListenerAdapter()
			{
				override fun onAnimationEnd(animation: Animator?)
				{
					if (isDelayCallback)
					{
						_handler.postDelayed(callback, 20)
					}
					else
					{
						callback()
					}
				}
			}
		}
		_reveal.reveal(Res.ANIMATION_MID, l)
	}

	/**
	 * Show the restart menu
	 *
	 * @param delay Delay the show animation
	 * @param callback Call after the animation finished
	 */
	private fun _showRestartMenu(delay: Int, callback: (() -> Unit)?)
	{
		for (b in _restartMenuBtns)
		{
			if (b === _restartNormalBtn)
			{
				// Skip the normal button
				continue
			}
			for (v in arrayOf(b.bound, b.label))
			{
				v.visibility = View.VISIBLE
				v.alpha = 0f
				v.y = v.y + 50
				v.animate().alpha(1f).yBy(-50f)
						.setInterpolator(DecelerateInterpolator())
						.setDuration(Res.ANIMATION_FAST.toLong())
						.setStartDelay(delay.toLong())
			}
		}

		_restartNormalBtn.label.visibility = View.VISIBLE
		_restartNormalBtn.label.alpha = 0f
		_restartNormalBtn.label.animate().alpha(1f)
				.setInterpolator(DecelerateInterpolator())
				.setDuration(Res.ANIMATION_FAST.toLong())
				.setStartDelay(delay.toLong())
		// All animations start and end at the same time, so we only need to set
		// once here
		if (callback != null)
		{
			_restartNormalBtn.label.animate()
					.setListener(object: AnimatorListenerAdapter()
					{
						override fun onAnimationEnd(animation: Animator?)
						{
							callback()
						}
					})
		}
	}

	private fun _dismissOtherBounds(keep: ActionButtonMeta?)
	{
		for (b in _actionBtns)
		{
			if (b.bound != keep?.bound)
			{
				_dismissView(b.bound)
			}
		}
	}

	private fun _dismissOtherBounds(keep: RestartButtonMeta?)
	{
		for (b in _restartMenuBtns)
		{
			if (b.bound != keep?.bound)
			{
				_dismissView(b.bound)
			}
		}
	}

	private fun _dismissOtherLabels(keep: RestartButtonMeta?)
	{
		for (b in _restartMenuBtns)
		{
			if (b.label != keep?.label)
			{
				_dismissView(b.label)
			}
		}
	}

	private fun _dismissView(v: View)
	{
		v.animate().alpha(0f)
				.setInterpolator(AccelerateInterpolator())
				.setDuration(Res.ANIMATION_FAST.toLong())
				.setStartDelay(0)
	}

	private fun _disableOtherBounds(keep: ActionButtonMeta?)
	{
		for (b in _actionBtns)
		{
			if (b.bound != keep?.bound)
			{
				_disableBound(b.bound)
			}
		}
	}

	private fun _disableOtherBounds(keep: RestartButtonMeta?)
	{
		for (b in _actionBtns)
		{
			if (b.bound != keep?.bound)
			{
				_disableBound(b.bound)
			}
		}
	}

	private fun _disableBound(bound: View)
	{
		bound.setOnClickListener(null)
		bound.isFocusable = false
		bound.isClickable = false
		bound.isEnabled = false
	}

	private val _appContext by lazy({activity!!.applicationContext})
	private val _handler by lazy({Handler()})
	private var _activeDangerousAction: DangerousAction? = null
	private var _isReqPermission = false

	private lateinit var _root: View
	private val _actionBtns by lazy(
	{
		arrayOf(ActionButtonMeta(
						_root.findViewById(R.id.shutdown_id)
								as FloatingActionButton,
						_root.findViewById(R.id.shutdown_btn_bound),
						{_onShutdownClick()}),
				ActionButtonMeta(
						_root.findViewById(R.id.sleep_btn)
								as FloatingActionButton,
						_root.findViewById(R.id.sleep_btn_bound),
						{_onSleepClick()}),
				ActionButtonMeta(
						_root.findViewById(R.id.restart_btn)
								as FloatingActionButton,
						_root.findViewById(R.id.restart_btn_bound),
						{_onRestartClick()}),
				ActionButtonMeta(
						_root.findViewById(R.id.screenshot_btn)
								as FloatingActionButton,
						_root.findViewById(R.id.screenshot_btn_bound),
						{_onScreenshotClick()}))
	})

	private val _shutdownBtn: ActionButtonMeta
		get() = _actionBtns[0]

	private val _sleepBtn: ActionButtonMeta
		get() = _actionBtns[1]

	private val _restartBtn: ActionButtonMeta
		get() = _actionBtns[2]

	private val _screenshotBtn: ActionButtonMeta
		get() = _actionBtns[3]

	private val _restartMenuBtns by lazy(
	{
		arrayOf(RestartButtonMeta(
						_restartBtn.btn,
						_restartBtn.bound,
						_root.findViewById(R.id.restart_normal_label)
								as TextView,
						{_onRestartNormalClick()}),
				RestartButtonMeta(
						_root.findViewById(R.id.restart_recovery_btn)
								as FloatingActionButton,
						_root.findViewById(R.id.restart_recovery_btn_bound),
						_root.findViewById(R.id.restart_recovery_label)
								as TextView,
						{_onRestartRecoveryClick()}),
				RestartButtonMeta(
						_root.findViewById(R.id.restart_bootloader_btn)
								as FloatingActionButton,
						_root.findViewById(R.id.restart_bootloader_btn_bound),
						_root.findViewById(R.id.restart_bootloader_label)
								as TextView,
						{_onRestartBootloaderClick()}))
	})

	private val _restartNormalBtn: RestartButtonMeta
		get() = _restartMenuBtns[0]

	private val _restartRecoveryBtn: RestartButtonMeta
		get() = _restartMenuBtns[1]

	private val _restartBootloaderBtn: RestartButtonMeta
		get() = _restartMenuBtns[2]

	private val _reveal by lazy({_root.findViewById(R.id.reveal) as RevealView})
}
