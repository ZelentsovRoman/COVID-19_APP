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
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.User;
import com.example.myapplication.database.UserDao;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUp extends Fragment implements View.OnClickListener{
    Button button3;
    EditText editTextTextEmailAddress;
    EditText editTextTextPassword;
    private AppDatabase database;
    public UserDao userDao;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        button3 = (Button) view.findViewById(R.id.button3);
        button3.setOnClickListener(this);
        editTextTextEmailAddress = (EditText) view.findViewById(R.id.editTextTextEmailAddress);
        editTextTextPassword = (EditText) view.findViewById(R.id.editTextTextPassword);
        database = Room.databaseBuilder(this.getContext(), AppDatabase.class, "database").allowMainThreadQueries().build();
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
    public void onClick(View view) {
        if ((editTextTextEmailAddress.getText().length() > 0) && (editTextTextPassword.getText().length() > 0)) {
            database = Login.getInstance().getDatabase();
            userDao = database.userDao();
            User user = new User();
            user.login = convertPassMd5(editTextTextEmailAddress.getText().toString());
            user.password = convertPassMd5(editTextTextPassword.getText().toString());
            if (!userDao.getByLogin(user.login).isEmpty()) {
                Toast.makeText(this.getActivity(),"User already exist", Toast.LENGTH_LONG).show();
            } else {
                userDao.insert(user);
                Fragment login = new Login();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.main, login);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        } else {
            Toast.makeText(this.getActivity(),"Please enter your login and password", Toast.LENGTH_LONG).show();
        }
    }
}