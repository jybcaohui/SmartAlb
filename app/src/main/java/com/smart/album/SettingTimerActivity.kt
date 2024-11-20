package com.smart.album

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.smart.album.adapters.TimerAdapter
import com.smart.album.utils.PreferencesHelper
import java.math.RoundingMode
import java.text.DecimalFormat

class SettingTimerActivity : BaseActivity() {

    private var countDownTimer: CountDownTimer? = null
    private lateinit var imgBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvTimerShow: TextView
    private lateinit var lvTimer: LinearLayout
    private lateinit var rvTimerProgress: RelativeLayout
    private lateinit var circularProgressIndicator: CircularProgressIndicator
    private var timerMinutes:Int = 0
    private val timerOptions = listOf(
        "Off",
        "5 Minutes",
        "15 Minutes",
        "30 Minutes",
        "1 hour",
        "2 hours",
        "Custom:")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_timer)
        imgBack = findViewById(R.id.img_back)
        tvTitle = findViewById(R.id.tv_title)
        tvTimer = findViewById(R.id.tv_timer)
        lvTimer = findViewById(R.id.lv_timer)
        tvTimerShow = findViewById(R.id.tv_timer_show)
        rvTimerProgress = findViewById(R.id.rv_timer_progress)
        circularProgressIndicator = findViewById(R.id.circular_progress_indicator)

        tvTitle.text = getString(R.string.schedule_setting)

        imgBack.setOnClickListener{
            finish()
        }
        timerMinutes =  PreferencesHelper.getInstance(this@SettingTimerActivity).getInt(PreferencesHelper.TIMER_MINUTES,0)
        lvTimer.requestFocus()
        lvTimer.setOnClickListener{
            startActivity(Intent(this, SettingCountDownTimeActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        timerMinutes =  PreferencesHelper.getInstance(this@SettingTimerActivity).getInt(PreferencesHelper.TIMER_MINUTES,0)
        showTimerProgress()
    }

    private fun showTimerProgress(){
        if(timerMinutes > 0){
            if(countDownTimer != null){
                countDownTimer?.cancel()
            }
            rvTimerProgress.visibility = View.VISIBLE
            tvTimer.text = "$timerMinutes Minutes"
            val totalMillis = (timerMinutes * 60 * 1000).toLong()
            countDownTimer = object : CountDownTimer(App.timerLast, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    // 更新进度
                    circularProgressIndicator.progress = divideAndMultiply(totalMillis-millisUntilFinished,totalMillis)
                    tvTimerShow.text = formatTime((millisUntilFinished/1000).toInt())
                }

                override fun onFinish() {
                }
            }
            (countDownTimer as CountDownTimer).start()
        } else {
            tvTimer.text = "Off"
            rvTimerProgress.visibility = View.GONE
        }
    }

    fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }

    fun divideAndMultiply(a: Long, b: Long): Int {
        // 确保除数不为0，避免除零错误
        require(b != 0L) { "Divisor cannot be zero" }
        // 将Long转换为Double
        val doubleA = a.toDouble()
        val doubleB = b.toDouble()
        // 执行除法运算
        val divisionResult = doubleA / doubleB
        // 保留两位小数
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.HALF_UP
        val formattedResult = df.format(divisionResult).toDouble()
        // 乘以100
        val multipliedResult = formattedResult * 100
        // 转换为整数
        return multipliedResult.toInt()
    }


}
