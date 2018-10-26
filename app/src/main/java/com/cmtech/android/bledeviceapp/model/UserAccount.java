package com.cmtech.android.bledeviceapp.model;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class UserAccount  extends LitePalSupport implements Serializable{
    // 数据库保存的字段
    // id
    private int id;

    private String name = "";

    private String password = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
