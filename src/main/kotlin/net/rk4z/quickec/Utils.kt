package net.rk4z.quickec

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val T by lazy { QuickEC.thread }

@Suppress("FunctionName")
inline fun QEC(
	delay: Long = 0,
	period: Long = -1,
	unit: TimeUnit = TimeUnit.MILLISECONDS,
	newThread: Boolean = false,
	crossinline task: () -> Unit
) {
	val executor = if (newThread) Executors.newSingleThreadScheduledExecutor() else T

	if (period > 0) {
		executor.scheduleAtFixedRate({ task() }, delay, period, unit)
	} else {
		executor.schedule({ task() }, delay, unit)
	}

	if (newThread && period <= 0) {
		executor.shutdown()
	}
}