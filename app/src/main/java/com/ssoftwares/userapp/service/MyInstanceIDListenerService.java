package com.ssoftwares.userapp.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.ssoftwares.userapp.R;
import com.ssoftwares.userapp.activity.DashboardActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MyInstanceIDListenerService extends FirebaseMessagingService {

    private static final int REQUEST_CODE = 1;
    private static int NOTIFICATION_ID = 6578;
    private static int NOTIFICATION_ID_REMIND_ME = 6578;
    //SharedPreferenceManager sharedPreferenceManager;

    public MyInstanceIDListenerService() {
        super();
    }

    @SuppressLint("WrongThread")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            Log.e("getData", new Gson().toJson(remoteMessage.toString()));
            JSONObject object =new JSONObject(new Gson().toJson(remoteMessage.getNotification()));
            Log.e("isPeram",object.toString());
            String title =object.getString("zza");
            String message =object.getString("zzd");
            showNotifications(getString(R.string.app_name), title, message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showNotifications(String title, String title1, String message) {

        Intent intent = new Intent(this, DashboardActivity.class);

        String channelId = "channel-01";
        String channelName = "Channel Name";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            manager.createNotificationChannel(mChannel);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(getIcon())
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setContentTitle(title1)
                    .setContentText(message);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(intent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            mBuilder.setContentIntent(resultPendingIntent);

            manager.notify(NOTIFICATION_ID, mBuilder.build());

        } else {

            PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentText(message)
                    .setContentTitle(title1)
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(getIcon())
                    .build();

            manager.notify(NOTIFICATION_ID, notification);
        }

        NOTIFICATION_ID++;
    }

    private int getIcon() {
        int icon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? R.drawable.ic_small_notification : R.drawable.ic_small_notification;
        return icon;
    }

    private int getLargeIcon() {
        int icon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? R.drawable.ic_small_notification : R.drawable.ic_small_notification;
        return icon;
    }

    private static boolean checkIfKeyExists(String response, String key) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();
        return jsonObject.has(key);
    }

    private class sendNotification extends AsyncTask<String, Void, Bitmap> {

        Context ctx;
        String message,title, imageUrl;

        public sendNotification(Context context, String title, String message, String imageUrl) {
            super();
            this.ctx = context;
            this.title = title;
            this.message = message;
            this.imageUrl = imageUrl;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            InputStream in;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            try {
                Intent intent = new Intent(ctx, DashboardActivity.class);
                String channelId = "channel-01";
                String channelName = "Channel Name";
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
                    manager.createNotificationChannel(mChannel);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, channelId)
                            .setSmallIcon(getIcon())
                            .setAutoCancel(true)
                            .setColor(ContextCompat.getColor(ctx, R.color.colorAccent))
                            .setLargeIcon(result)
                            .setContentTitle(title)
                            .setContentText(message);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
                    stackBuilder.addNextIntent(intent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
                    mBuilder.setContentIntent(resultPendingIntent);
                    manager.notify(NOTIFICATION_ID, mBuilder.build());
                } else {
                    PendingIntent pendingIntent = PendingIntent.getActivity(ctx, REQUEST_CODE,
                            intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Notification notification = new NotificationCompat.Builder(ctx)
                            .setContentText(message)
                            .setContentTitle(title)
                            .setAutoCancel(true)
                            .setColor(ContextCompat.getColor(ctx, R.color.colorAccent))
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(getIcon())
                            .setLargeIcon(result)
                            .build();
                    manager.notify(NOTIFICATION_ID, notification);
                }
                NOTIFICATION_ID++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private int getIcon() {
            int icon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? R.drawable.ic_small_notification : R.drawable.ic_small_notification;
            return icon;
        }
    }
}