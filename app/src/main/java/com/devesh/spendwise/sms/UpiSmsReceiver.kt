package com.devesh.spendwise.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.devesh.spendwise.util.UpiSmsParser

class UpiSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {

            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            for (sms in messages) {
                val messageBody = sms.messageBody
                UpiSmsParser.parseAndSave(context, messageBody)
            }
        }
    }
}

