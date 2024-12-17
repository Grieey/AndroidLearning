package com.learning.androidlearning.sample.schedule.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {CourseJava.class}, version = 1)
@TypeConverters({ConvertersJava.class})
public abstract class CourseDatabaseJava extends RoomDatabase {
    public abstract CourseDaoJava courseDao();

    private static volatile CourseDatabaseJava INSTANCE;

    public static CourseDatabaseJava getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (CourseDatabaseJava.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            CourseDatabaseJava.class,
                            "course_database_java"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
} 