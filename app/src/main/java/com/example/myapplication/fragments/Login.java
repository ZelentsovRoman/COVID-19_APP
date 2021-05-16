package com.example.myapplication.fragments;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.User;
import com.example.myapplication.database.UserDao;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Login extends Fragment implements View.OnClickListener {
    Button button;
    Button buttonSignUp;
    EditText editTextTextEmailAddress3;
    EditText editTextTextPassword3;
    private AppDatabase database;
    public static Login instance;
    public UserDao userDao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }
    public AppDatabase getDatabase() {
        return database;
    }
    public static Login getInstance() {
        return instance;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(this);
        buttonSignUp = (Button) view.findViewById(R.id.button2);
        buttonSignUp.setOnClickListener(this);
        editTextTextEmailAddress3 = (EditText) view.findViewById(R.id.editTextTextEmailAddress3);
        editTextTextPassword3 = (EditText) view.findViewById(R.id.editTextTextPassword3);
        instance =this;
        database = Room.databaseBuilder(this.getContext(), AppDatabase.class, "database.db").allowMainThreadQueries().build();
    }
    public static String convertPassMd5(String pass) {
        String password = null;
        MessageDigest mdEnc;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
            mdEnc.update(pass.getBytes(), 0, pass.length());
            StringBuilder passBuilder = new StringBuilder(new BigInteger(1, mdEnc.digest()).toString(16));
            while (passBuilder.length() < 32) {
                passBuilder.insert(0, "0");
            }
            pass = passBuilder.toString();
            password = pass;
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return password;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button:
                if ((editTextTextEmailAddress3.getText().length() > 0) && (editTextTextPassword3.getText().length() > 0)) {
                    database = Login.getInstance().getDatabase();
                    userDao = database.userDao();
                    User user = new User();
                    user.login = convertPassMd5(editTextTextEmailAddress3.getText().toString());
                    user.password = convertPassMd5(editTextTextPassword3.getText().toString());
                    if (!userDao.getUser(user.login,user.password).isEmpty()){
                        if(Success.isNetworkConnected(getContext())||(MainActivity.getArrayList(getContext(),"countries")!=null)) {
                            Fragment success = new Success();
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.main, success);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        } else Toast.makeText(getContext(), "No internet connection!", Toast.LENGTH_SHORT).show();
                    } else {
                        Fragment fail = new Fail();
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.main,fail);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                } else {
                    Toast.makeText(this.getActivity(),"Please enter your login and password", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.button2:
                Fragment signup = new SignUp();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.main,signup);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
        }
    }
}