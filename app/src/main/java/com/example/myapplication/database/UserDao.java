package com.example.myapplication.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM user")
    List<User> getAll();

    @Query("SELECT * FROM user WHERE login = :login")
    List<User> getByLogin(String login);
    @Query("SELECT * FROM user WHERE login = :login and password = :password")
    List<User> getUser(String login,String password);
    @Insert
    void insert(User user);
}
