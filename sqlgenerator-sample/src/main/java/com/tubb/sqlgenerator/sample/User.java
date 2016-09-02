package com.tubb.sqlgenerator.sample;

import com.tubb.sqlgenerator.annotation.PrimaryKey;
import com.tubb.sqlgenerator.annotation.Table;

/**
 * Created by tubingbing on 16/9/2.
 */
@Table(name = "User")
public class User {

    @PrimaryKey
    public long id;
    public String name;
    public int age;

}
