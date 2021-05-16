package com.example.myapplication.fragments;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.service.Service;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class Success extends Fragment {
    TextView confirmed,recovered,critical,deaths;
    SharedPreferences sPref;
    Spinner spinner;
    private SwipeRefreshLayout swipeContainer;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spinner = view.findViewById(R.id.spinner);
        ArrayList<String> countries = MainActivity.getArrayList(getContext(),"countries");
        ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = view.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(listener);
        sPref = getContext().getSharedPreferences("country", MODE_PRIVATE);
        int value = sPref.getInt("countryPos",0);
        spinner.setSelection(value);
        confirmed = view.findViewById(R.id.textView9);
        recovered = view.findViewById(R.id.textView11);
        critical = view.findViewById(R.id.textView13);
        deaths = view.findViewById(R.id.textView15);
        swipeContainer = view.findViewById(R.id.swipeRefreshLayout);
        swipeContainer.setOnRefreshListener(swipe);
        if(isNetworkConnected(getContext())) {
            swipe.onRefresh();
        } else {
            sPref = Success.this.getContext().getSharedPreferences("confirmed", MODE_PRIVATE);
            int savedText = sPref.getInt("confirmed", 0);
            confirmed.setText(String.valueOf(savedText));
            sPref = Success.this.getContext().getSharedPreferences("recovered", MODE_PRIVATE);
            savedText = sPref.getInt("recovered", 0);
            recovered.setText(String.valueOf(savedText));
            sPref = Success.this.getContext().getSharedPreferences("critical", MODE_PRIVATE);
            savedText = sPref.getInt("critical", 0);
            critical.setText(String.valueOf(savedText));
            sPref = Success.this.getContext().getSharedPreferences("deaths", MODE_PRIVATE);
            savedText = sPref.getInt("deaths", 0);
            deaths.setText(String.valueOf(savedText));
            Toast.makeText(Success.this.getActivity(), "No internet connection!", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    SwipeRefreshLayout.OnRefreshListener swipe = new SwipeRefreshLayout.OnRefreshListener(){
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onRefresh() {
            Handler handler = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    if (msg.what == 11) {
                        sPref = Success.this.getContext().getSharedPreferences("confirmed", MODE_PRIVATE);
                        int savedText = sPref.getInt("confirmed", 0);
                        confirmed.setText(String.valueOf(savedText));
                        sPref = Success.this.getContext().getSharedPreferences("recovered", MODE_PRIVATE);
                        savedText = sPref.getInt("recovered", 0);
                        recovered.setText(String.valueOf(savedText));
                        sPref = Success.this.getContext().getSharedPreferences("critical", MODE_PRIVATE);
                        savedText = sPref.getInt("critical", 0);
                        critical.setText(String.valueOf(savedText));
                        sPref = Success.this.getContext().getSharedPreferences("deaths", MODE_PRIVATE);
                        savedText = sPref.getInt("deaths", 0);
                        deaths.setText(String.valueOf(savedText));
                        swipeContainer.setRefreshing(false);
                    }
                };
            };
            if (isNetworkConnected(getActivity())) {
                swipeContainer.setRefreshing(true);
                new Thread(new Runnable() {
                    public void run() {
                        Service.getData(getActivity());
                        handler.sendEmptyMessage(11);
                    }
                }).start();
            } else {
                swipeContainer.setRefreshing(false);
                Toast.makeText(Success.this.getActivity(), "No internet connection!", Toast.LENGTH_SHORT).show();
            }
        }
    };
    AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            sPref = getContext().getSharedPreferences("country", MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            int value = sPref.getInt("countryPos", 0);
            ed.putInt("countryPos", adapterView.getSelectedItemPosition());
            ed.putString("country", String.valueOf(adapterView.getItemAtPosition(i)));
            ed.apply();
            if (i!=value) {
                swipe.onRefresh();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
}