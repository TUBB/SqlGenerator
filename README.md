SqlGenerator
=====================
APP比较简单或者APP数据库操作比较少, 简单的几张表, 少量的数据存储。 这个时候其实没有必要去使用第三方的ORM库
 
 * 需要去学习第三方ORM库, 增加学习成本, 也会一定程度上增加APP的复杂度
 * 如果使用或者选择不当的话, 可能影响APP整体的性能, 好多ORM库其实是用运行时注解和反射来实现的, 对性能会有一定的影响

`SqlGenerator`致力于在不使用ORM库的情况下, 通过生成一些`boilerplate code`方式简化数据库操作, 从而减轻工作量

现在`SqlGenerator`已经支持根据Model的定义自动生成相应的Table定义, 避免编写枯燥的代码

 * 根据Model的属性名生成Table的列名, 并且可以配置列的约束
 * 根据Model的定义生成建表SQL, 支持外键
 * 生成建索引SQL
 * 支持复杂类型的映射
 
Model定义如下

```java
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
```

最终会生成表的`外观`类

```java
public final class UserContract {
    public static final String TABLE_NAME = "User";
    public static final String ID_COLUMN = "id";
    public static final String NAME_COLUMN = "name";
    public static final String AVTAR_COLUMN = "avtar";
    public static final String AGE_COLUMN = "age";
    public static final String ADDRESS_ID_COLUMN = "address_id";
    public static final String GROUPID_COLUMN = "groupId";
    public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS User(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE,avtar TEXT,age INTEGER CHECK(age>16),address_id TEXT,groupId INTEGER,FOREIGN KEY(groupId) REFERENCES Group(id) ON UPDATE CASCADE)";
    public static final String CREATE_NAME_AGE_INDEX_SQL = "CREATE INDEX name_age_index ON User(name,age)";

    public UserContract() {
    }

    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS User(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE,avtar TEXT,age INTEGER CHECK(age>16),address_id TEXT,groupId INTEGER,FOREIGN KEY(groupId) REFERENCES Group(id) ON UPDATE CASCADE)");
    }

    public static void createIndex(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX name_age_index ON User(name,age)");
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS User");
    }
}
```


Download
--------

```groovy
buildscript {
  repositories {
    mavenCentral()
   }
  dependencies {
    classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
  }
}

apply plugin: 'com.neenbedankt.android-apt'

dependencies {
  compile 'com.github.tubb.sqlgenerator:sqlgenerator-annotations:0.0.3-SNAPSHOT'
  apt 'com.github.tubb.sqlgenerator:sqlgenerator-compiler:0.0.3-SNAPSHOT'
}
```

Usage
-----

定义好Model类之后重新构建应用, 会生成以ModelNameContract命名的表`外观`类, 可以在`moduleName/build/intermediates/classes`目录下查看

```xml
./gradlew clean build
```

Note
----
`SqlGenerator`还处于开发阶段, 功能不是很稳定, 正式项目请谨慎使用。非常希望有兴趣的朋友来一起完善这个library。

License
-------

    Copyright 2016 TUBB

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
