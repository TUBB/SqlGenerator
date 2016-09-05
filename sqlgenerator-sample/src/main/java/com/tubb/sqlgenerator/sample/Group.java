package com.tubb.sqlgenerator.sample;

import com.tubb.sqlgenerator.annotation.PrimaryKey;
import com.tubb.sqlgenerator.annotation.Table;

/**
 * Created by tubingbing on 16/9/5.
 */
@Table(name = "Group")
public class Group {
    @PrimaryKey
    public long id;
    public String name;
}
