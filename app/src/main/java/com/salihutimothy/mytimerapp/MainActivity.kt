package com.salihutimothy.mytimerapp

import android.os.Bundle
import android.os.CountDownTimer
import android.os.health.TimerStat
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.salihutimothy.mytimerapp.databinding.ActivityMainBinding
import com.salihutimothy.mytimerapp.util.PrefUtil
import me.zhanghai.android.materialprogressbar.MaterialProgressBar

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var timer: CountDownTimer
    private lateinit var fabStart: FloatingActionButton
    private lateinit var fabPause: FloatingActionButton
    private lateinit var fabStop: FloatingActionButton
    private lateinit var progressCountdown: MaterialProgressBar
    private lateinit var tvCountdown: TextView
    private var timerLengthSeconds = 0L
    private var timerState = TimerState.Stopped
    private var secondsRemaining = 0L

    enum class TimerState {
        Stopped, Paused, Running
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setIcon(R.drawable.ic_placeholder)
        supportActionBar?.title = "     Timer"

        fabStart = findViewById(R.id.fab_start)
        fabPause = findViewById(R.id.fab_pause)
        fabStop = findViewById(R.id.fab_stop)

        fabStart.setOnClickListener {
            startTimer()
            timerState = TimerState.Running
            updateButtons()
        }

        fabPause.setOnClickListener {
            timer.cancel()
            timerState = TimerState.Paused
            updateButtons()
        }

        fabStop.setOnClickListener {
            timer.cancel()
            onTimerFinished()
        }
    }

    override fun onResume() {
        super.onResume()

        initTimer()
    }

    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.Running) {
            timer.cancel()
        }
        else if (timerState == TimerState.Paused) {

        }

        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        PrefUtil.setSecondsRemaining(secondsRemaining, this)
        PrefUtil.setTimerState(timerState, this)

    }

    private fun initTimer(){
        timerState = PrefUtil.getTimerState(this)

        if (timerState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        secondsRemaining = if (timerState == TimerState.Running || timerState == TimerState.Paused)
            PrefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds

        if (timerState == TimerState.Running)
            startTimer()

        updateButtons()
        updateCountdownUI()
    }

    private fun onTimerFinished() {
        progressCountdown = findViewById(R.id.progress_countdown)

        timerState = TimerState.Stopped

        setNewTimerLength()

        progressCountdown.progress = 0

        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    private fun startTimer(){
        timerState = TimerState.Running

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }

            override fun onFinish() = onTimerFinished()

        }.start()
    }

    private fun setNewTimerLength(){
        progressCountdown = findViewById(R.id.progress_countdown)

        val lengthInMinutes = PrefUtil.getTimerLength(this)
        timerLengthSeconds = (lengthInMinutes * 60L)
        progressCountdown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength() {
        progressCountdown = findViewById(R.id.progress_countdown)

        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
        progressCountdown.max = timerLengthSeconds.toInt()
    }

    private fun updateCountdownUI() {
        tvCountdown = findViewById(R.id.tv_countdown)
        progressCountdown = findViewById(R.id.progress_countdown)

        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        val time = "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0$secondsStr"}"
        tvCountdown.text = time
        progressCountdown.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }

    private fun updateButtons(){
        fabStart = findViewById(R.id.fab_start)
        fabPause = findViewById(R.id.fab_pause)
        fabStop = findViewById(R.id.fab_stop)

        when (timerState) {
            TimerState.Running ->{
                fabStart.isEnabled = false
                fabPause.isEnabled = true
                fabStop.isEnabled = true
            }
            TimerState.Stopped -> {
                fabStart.isEnabled = true
                fabPause.isEnabled = false
                fabStop.isEnabled = false
            }
            TimerState.Paused -> {
                fabStart.isEnabled = true
                fabPause.isEnabled = false
                fabStop.isEnabled = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


}