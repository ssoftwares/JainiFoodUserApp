package com.ssoftwares.userapp.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.ssoftwares.userapp.R
import com.ssoftwares.userapp.activity.DashboardActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonParser
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class NotificationService() : FirebaseMessagingService() {

    private val REQUEST_CODE = 1
    private var NOTIFICATION_ID = 6578
    private val NOTIFICATION_ID_REMIND_ME = 6578
    //SharedPreferenceManager sharedPreferenceManager;

    //SharedPreferenceManager sharedPreferenceManager;
    @SuppressLint("WrongThread")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {

            val messageObj = JSONObject(remoteMessage.data["message"].toString())
            Log.e("isPeram", messageObj.toString())
            Log.e("isPeram", messageObj.toString())
            val isCheck = checkIfKeyExists(messageObj.toString(), "video_details")
            Log.e("isAvilble", isCheck.toString() + "")
            var title = ""
            var message = ""
            if (isCheck) {
                title = messageObj.getString("title")
                message = messageObj.getString("message")
                val videodetailObject = messageObj.getJSONObject("video_details")
                if (videodetailObject.getString("image").equals("null", ignoreCase = true)) {
                    showNotifications(getString(R.string.app_name), title, message)
                } else {
                    val getUrl: String = videodetailObject.getString("image")
                    sendNotification(this, title, message, getUrl).execute()
                }
            } else {
                title = messageObj.getString("title")
                message = messageObj.getString("message")
                showNotifications(getString(R.string.app_name), getString(R.string.app_name), "OrderPlace")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun showNotifications(
        title: String,
        title1: String,
        message: String
    ) {
        val intent = Intent(this, DashboardActivity::class.java)
        val channelId = "channel-01"
        val channelName = "Channel Name"
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel =
                NotificationChannel(channelId, channelName, importance)
            manager.createNotificationChannel(mChannel)
            val mBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(getIcon())
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setContentTitle(title1)
                .setContentText(message)
            val stackBuilder =
                TaskStackBuilder.create(this)
            stackBuilder.addNextIntent(intent)
            val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
           // mBuilder.setContentIntent(resultPendingIntent)
            manager.notify(NOTIFICATION_ID, mBuilder.build())
        } else {
            val pendingIntent = PendingIntent.getActivity(
                this, REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification = NotificationCompat.Builder(this)
                .setContentText(message)
                .setContentTitle(title1)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setSmallIcon(getIcon())
                .build()
            manager.notify(NOTIFICATION_ID, notification)
        }
        NOTIFICATION_ID++
    }

    private fun getIcon(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) R.drawable.ic_small_notification else R.drawable.ic_small_notification
    }

    private fun getLargeIcon(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) R.drawable.ic_small_notification else R.drawable.ic_small_notification
    }

    private fun checkIfKeyExists(response: String, key: String): Boolean {
        val parser = JsonParser()
        val jsonObject = parser.parse(response).asJsonObject
        return jsonObject.has(key)
    }

     inner class sendNotification(
        var ctx: Context,
        var title: String,
        var message: String,
        var imageUrl: String
    ) : AsyncTask<String?, Void?, Bitmap?>() {
         override fun doInBackground(vararg p0: String?): Bitmap? {
             val `in`: InputStream
             try {
                 val url = URL(imageUrl)
                 val connection =
                     url.openConnection() as HttpURLConnection
                 connection.doInput = true
                 connection.connect()
                 `in` = connection.inputStream
                 return BitmapFactory.decodeStream(`in`)
             } catch (e: MalformedURLException) {
                 e.printStackTrace()
             } catch (e: IOException) {
                 e.printStackTrace()
             }
             return null
         }
        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            try {
               // val intent = Intent(ctx, HomeActivity::class.java)
                val channelId = "channel-01"
                val channelName = "Channel Name"
                val manager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val importance = NotificationManager.IMPORTANCE_HIGH
                    val mChannel =
                        NotificationChannel(channelId, channelName, importance)
                    manager.createNotificationChannel(mChannel)
                    val mBuilder =
                        NotificationCompat.Builder(ctx, channelId)
                            .setSmallIcon(icon)
                            .setAutoCancel(true)
                            .setColor(ContextCompat.getColor(ctx, R.color.colorAccent))
                            .setLargeIcon(result)
                            .setContentTitle(title)
                            .setContentText(message)
                  /*  val stackBuilder =
                        TaskStackBuilder.create(ctx)
                    stackBuilder.addNextIntent(intent)
                    val resultPendingIntent = stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )*/
                  //  mBuilder.setContentIntent(resultPendingIntent)
                    manager.notify(NOTIFICATION_ID, mBuilder.build())
                } else {
                  /*  val pendingIntent = PendingIntent.getActivity(
                        ctx, REQUEST_CODE,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT
                    )*/
                    val notification = NotificationCompat.Builder(ctx)
                        .setContentText(message)
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setColor(ContextCompat.getColor(ctx, R.color.colorAccent))
                        .setSmallIcon(icon)
                        .setLargeIcon(result)
                        .build()
                    manager.notify(NOTIFICATION_ID, notification)
                }
                NOTIFICATION_ID++
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private val icon: Int
            private get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) R.drawable.ic_small_notification else R.drawable.ic_small_notification

     }
}