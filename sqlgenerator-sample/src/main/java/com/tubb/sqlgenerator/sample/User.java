package com.tubb.sqlgenerator.sample;

import com.tubb.sqlgenerator.annotation.Column;
import com.tubb.sqlgenerator.annotation.ForeignKey;
import com.tubb.sqlgenerator.annotation.Index;
import com.tubb.sqlgenerator.annotation.Mapping;
import com.tubb.sqlgenerator.annotation.PrimaryKey;
import com.tubb.sqlgenerator.annotation.Serializer;
import com.tubb.sqlgenerator.annotation.Table;

import java.io.File;

/**
 * Created by tubingbing on 16/9/2.
 */
@Table(name = "User")
@Index(name = "name_age_index", columns = {"name", "age"}, unique = true)
public class User {

    @PrimaryKey
    public long id;

    @Column(unique = true, notNULL = true)
    public String name;

    @Serializer(
            serializedTypeCanonicalName = String.class,
            serializerCanonicalName = FileSerializer.class)
    public File avtar;

    @Column(check = "age>16")
    public int age;

    @Column(name = "address_id")
    public Address address;

    @ForeignKey(referenceTableName = "Group", referenceColumnName = "id", action = "ON UPDATE CASCADE")
    public long groupId;
}
