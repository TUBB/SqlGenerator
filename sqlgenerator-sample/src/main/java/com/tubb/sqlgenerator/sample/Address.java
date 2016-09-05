package com.tubb.sqlgenerator.sample;

import com.tubb.sqlgenerator.annotation.PrimaryKey;
import com.tubb.sqlgenerator.annotation.Table;

/**
 * Created by tubingbing on 16/9/5.
 */
@Table(name = "Address")
public class Address {
    public long id;
    @PrimaryKey
    public String name;
}
