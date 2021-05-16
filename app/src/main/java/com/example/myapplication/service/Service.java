package com.example.myapplication.service;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Service extends JobService {
    public static Context mcontext;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        return getData(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean getData(Context context) {
        if (isNetworkConnected(context)) {
            JSONObject jsonObject = new JSONObject();
            mcontext=context;
            MyTask mt = new MyTask();
            mt.execute();
            try {
                jsonObject = mt.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            int confirmedJson = 0;
            int recoveredJson= 0;
            int criticalJson= 0;
            int deathsJson= 0;
            try {
                confirmedJson = jsonObject.getInt("confirmed");
                recoveredJson = jsonObject.getInt("recovered");
                criticalJson = jsonObject.getInt("critical");
                deathsJson = jsonObject.getInt("deaths");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SharedPreferences sPref = context.getSharedPreferences("confirmed", MODE_PRIVATE);
            int confirmed = sPref.getInt("confirmed", 0);
            sPref = context.getSharedPreferences("recovered", MODE_PRIVATE);
            int recovered = sPref.getInt("recovered", 0);
            sPref = context.getSharedPreferences("critical", MODE_PRIVATE);
            int critical = sPref.getInt("critical", 0);
            sPref = context.getSharedPreferences("deaths", MODE_PRIVATE);
            int deaths = sPref.getInt("deaths", 0);
            sPref = context.getSharedPreferences("country", MODE_PRIVATE);
            String savedText = sPref.getString("country", "");
            if (confirmed!=confirmedJson || deaths!=deathsJson|| recovered!=recoveredJson|| critical!=criticalJson ){
                SharedPreferences preferences = context.getSharedPreferences("confirmed", MODE_PRIVATE);
                SharedPreferences.Editor ed = preferences.edit();
                ed.putInt("confirmed", confirmedJson);
                ed.apply();
                preferences = context.getSharedPreferences("recovered", MODE_PRIVATE);
                ed = preferences.edit();
                ed.putInt("recovered", recoveredJson);
                ed.apply();
                preferences = context.getSharedPreferences("critical", MODE_PRIVATE);
                ed = preferences.edit();
                ed.putInt("critical", criticalJson);
                ed.apply();
                preferences = context.getSharedPreferences("deaths", MODE_PRIVATE);
                ed = preferences.edit();
                ed.putInt("deaths", deathsJson);
                ed.apply();
                if (!MainActivity.isAppForeground()){
                    sendNotification(confirmedJson,recoveredJson,criticalJson,deathsJson,savedText,context);
                }
            }
            return true;
        } else return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    private static boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void sendNotification(int confirmedJson, int recoveredJson, int criticalJson, int deathsJson, String savedText, Context context) {
        final PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 1, new Intent(
                        context, MainActivity.class).putExtra("From", "notifyFrag"), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1500});
        notificationManager.createNotificationChannel(notificationChannel);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(pendingIntent)
                .setContentTitle("Data updated")
                .setContentText("Tap to open application")
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine("Confirmed: " + confirmedJson)
                        .addLine("Recovered: " + recoveredJson)
                        .addLine("Critical: " + criticalJson)
                        .addLine("Deaths: " + deathsJson)
                        .setBigContentTitle(savedText + " COVID-19 case count")
                );
        notificationManager.notify(5666, notificationBuilder.build());
    }

    public static class MyTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            SharedPreferences sPref = mcontext.getSharedPreferences("country", MODE_PRIVATE);
            String savedText = sPref.getString("country", "");
            String saved = savedText.replaceAll("\\s+", "%20");
            JSONObject jsonObject = new JSONObject();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://covid-19-data.p.rapidapi.com/country?name=" + saved + "&format=json")
                    .get()
                    .addHeader("x-rapidapi-key", "d5951c8ffbmshf67964952e02ed6p1c3c15jsnc5c0c90f926c")
                    .addHeader("x-rapidapi-host", "covid-19-data.p.rapidapi.com")
                    .build();
            String string;
            try (Response response = client.newCall(request).execute()) {
                string = response.body().string();
                JSONArray jsonArray = new JSONArray(string);
                jsonObject = jsonArray.getJSONObject(0);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }
    }
    public static class getCountries extends AsyncTask<Void, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Void... params) {
            JSONArray jsonArray = new JSONArray();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://covid-19-data.p.rapidapi.com/help/countries?format=json")
                    .get()
                    .addHeader("x-rapidapi-key", "d5951c8ffbmshf67964952e02ed6p1c3c15jsnc5c0c90f926c")
                    .addHeader("x-rapidapi-host", "covid-19-data.p.rapidapi.com")
                    .build();
            String string = "";
            try {
                Response response = client.newCall(request).execute();
                string = response.body().string();
                jsonArray = new JSONArray(string);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return jsonArray;
        }
    }
}