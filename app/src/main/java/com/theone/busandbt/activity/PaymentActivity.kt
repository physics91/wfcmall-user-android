package com.theone.busandbt.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ActivityUtils
import com.theone.busandbt.R
import com.theone.busandbt.eventbus.order.ReceiveDongBaekGeonPaymentEvent
import org.greenrobot.eventbus.EventBus

/**
 * 동백전 결제 결과를 받기 위한 액티비티
 */
class PaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        val i = intent ?: return
        if (i.data?.host == "payment") {
            EventBus.getDefault().postSticky(ReceiveDongBaekGeonPaymentEvent(i))
        }

        ActivityUtils.startActivity(Intent(this, MainActivity::class.java))
    }
}