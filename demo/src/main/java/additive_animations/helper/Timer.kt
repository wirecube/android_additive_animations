package additive_animations.helper

import android.os.Handler
import android.os.Looper

class UtilTimer(private val runnable: Runnable) {

    private val mHandler = Handler(Looper.getMainLooper())
    private var mTimeoutTick: Long? = null


    private val completionHandler = Runnable {
        runnable.run()
    }

    /**
     * Starts the timer with the specified [delay] in milliseconds.
     */
    fun startTimer(delay: Long) {
        mHandler.removeCallbacks(completionHandler)
        mTimeoutTick = System.currentTimeMillis() + delay
        mHandler.postDelayed(completionHandler, delay)
    }

    /**
     * Immediately cancels the timer and removes the callbacks.
     */
    fun cancelTimer() {
        mTimeoutTick = null
        mHandler.removeCallbacks(completionHandler)
    }

}