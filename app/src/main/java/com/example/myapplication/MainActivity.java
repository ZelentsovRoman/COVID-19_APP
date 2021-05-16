package com.example.myapplication;
import android.app.Activity;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.example.myapplication.fragments.Login;
import com.example.myapplication.fragments.Success;
import com.example.myapplication.service.MyReceiver;
import com.example.myapplication.service.Service;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements Application.ActivityLifecycleCallbacks {
    SharedPreferences sPref;
    private static int activityCount = 0;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerActivityLifecycleCallbacks(this);
        String type = getIntent().getStringExtra("From");
        if (type != null) {
            switch (type) {
                case "notifyFrag":
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.add(R.id.main, new Success());
                    ft.commit();
                    break;
            }
        }
        else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.main, new Login());
            ft.commit();
            if (getArrayList(this, "countries") == null) {
                if (Success.isNetworkConnected(this)) {
                    ArrayList<String> countries = getcountries();
                    saveArrayList(countries, "countries");
                    new MyReceiver().onReceive(this,getIntent().setAction("firstStart"));
                } else Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ArrayList<String> getcountries() {
        ArrayList<String> countries = new ArrayList<>();
        Service.getCountries getCountries = new Service.getCountries();
        getCountries.execute();
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        try {
            jsonArray =getCountries.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int i = 0; i< jsonArray.length(); i++) {
            try {
                jsonObject = jsonArray.getJSONObject(i);
                countries.add(jsonObject.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return countries;
    }

    public void saveArrayList(ArrayList<String> list, String key){
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }
    public static ArrayList<String> getArrayList(Context context,String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onDestroy() {
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;
        for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
            if ( jobInfo.getId() == 0 ) {
                hasBeenScheduled = true;
                break ;
            }
        }
        if (!hasBeenScheduled){
            new MyReceiver().onReceive(this,getIntent().setAction("firstStart"));
        }
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }
    public static boolean isAppForeground() {
        return activityCount > 0;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        activityCount++;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        activityCount--;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}

