package com.tamersarioglu.cronometer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var isStart= false

    private val limitTime: Long = 15 * 1000 // 15 sec start point

    companion object {
        private const val IS_START_KEY = "IS_START"
        private const val LAST_TIME_SAVED_KEY = "LAST_TIME_SAVED"
        private const val TIME_REMAIN = "TIME_REMAIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        Paper.init(this)

        isStart = Paper.book().read(IS_START_KEY, false)
        if (isStart) {
            button_Start.isEnabled = false

            checkTime()
        } else {
            button_Start.isEnabled = true
        }

        button_Start.setOnClickListener {
            if (!isStart) {
                countdownTimer.start(limitTime)
                Paper.book().write(IS_START_KEY, true)
            }
        }

        countdownTimer.setOnCountdownEndListener {
            Toast.makeText(this, "Finish", Toast.LENGTH_SHORT).show()
            reset()
        }

        countdownTimer.setOnCountdownIntervalListener(1000
        ) { _, remainTime -> Log.d("TIMER", "" + remainTime) }
    }

    override fun onStop() {
        Paper.book().write(TIME_REMAIN, countdownTimer.remainTime)
        Paper.book().write(LAST_TIME_SAVED_KEY, System.currentTimeMillis())
        super.onStop()
    }

    private fun checkTime() {
        val currentTime = System.currentTimeMillis()
        val lastTimeSaved = Paper.book().read<Long>(LAST_TIME_SAVED_KEY, 0)
        val timeRemain = Paper.book().read(TIME_REMAIN, 0).toLong()

        val result = timeRemain + (lastTimeSaved - currentTime)

        if (result > 0) {
            countdownTimer.start(result)
        } else {
            countdownTimer.stop()
            reset()
        }
    }

    private fun reset() {
        button_Start.isEnabled = true
        Paper.book().delete(IS_START_KEY)
        Paper.book().delete(LAST_TIME_SAVED_KEY)
        Paper.book().delete(TIME_REMAIN)
        isStart = false
    }
}