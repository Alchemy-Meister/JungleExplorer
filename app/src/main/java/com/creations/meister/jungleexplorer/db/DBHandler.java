package com.creations.meister.jungleexplorer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.domain.Expert;
import com.creations.meister.jungleexplorer.domain.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by meister on 3/30/16.
 */
public class DBHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "JungleExplorer.db";

    // Table Names
    private static final String TABLE_ANIMAL = "animal";
    private static final String TABLE_GROUP = "group";
    private static final String TABLE_ANIMAL_GROUP = "animal_group";
    private static final String TABLE_EXPERT = "expert";
    private static final String TABLE_ANIMAL_EXPERT = "animal_expert";

    // Common table column names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHOTO_ID = "photo_id";

    // ANIMAL Table - column names
    private static final String KEY_LOCATION_TEXT = "location_text";
    private static final String KEY_DESCRIPTION = "description";

    // Common relation table column names
    private static final String KEY_ANIMAL_ID = "animal_id";

    // ANIMAL_GROUP Table - column names
    private static final String KEY_GROUP_ID = "group_id";

    // ANIMAL_EXPERT Table - column names
    private static final String KEY_EXPERT_ID = "expert_id";

    // Table Create Statements
    // Animal table create statement
    private static final String CREATE_TABLE_ANIMAL = "CREATE TABLE "
            + TABLE_ANIMAL + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PHOTO_ID
            + " INTEGER," + KEY_NAME + " TEXT," + KEY_LOCATION_TEXT + " TEXT,"
            + KEY_DESCRIPTION + " TEXT)";

    private static final String CREATE_TABLE_GROUP = "CREATE TABLE "
            + TABLE_GROUP + "(" + KEY_ID + "INTEGER PRIMARY KEY," + KEY_PHOTO_ID
            + " INTEGER," + KEY_NAME + " TEXT)";

    private static final String CREATE_TABLE_ANIMAL_GROUP = "CREATE TABLE "
            + TABLE_ANIMAL_GROUP + "(" + KEY_ANIMAL_ID + "REFERENCES " + TABLE_ANIMAL
            + "(" + KEY_ID + ")," + KEY_GROUP_ID + "REFERENCES " + TABLE_GROUP
            + "(" + KEY_ID + "),PRIMARY KEY(" + KEY_ANIMAL_ID + "," + KEY_GROUP_ID + "))";

    private static final String CREATE_TABLE_EXPERT = "CREATE TABLE "
            + TABLE_EXPERT + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PHOTO_ID
            + " INTEGER," + KEY_NAME + " TEXT)";

    private static final String CREATE_TABLE_ANIMAL_EXPERT = "CREATE TABLE "
            + TABLE_ANIMAL_EXPERT + "(" + KEY_ANIMAL_ID + "REFERENCES " + TABLE_ANIMAL
            + "(" + KEY_ID + ")," + KEY_EXPERT_ID + "REFERENCES " + TABLE_EXPERT
            + "(" + KEY_ID + "),PRIMARY KEY(" + KEY_ANIMAL_ID + "," + KEY_EXPERT_ID + "))";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ANIMAL);
        db.execSQL(CREATE_TABLE_GROUP);
        db.execSQL(CREATE_TABLE_ANIMAL_GROUP);
        db.execSQL(CREATE_TABLE_EXPERT);
        db.execSQL(CREATE_TABLE_ANIMAL_EXPERT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + TABLE_ANIMAL);
        db.execSQL("DROP TABLE IF EXIST " + TABLE_GROUP);
        db.execSQL("DROP TABLE IF EXIST " + TABLE_ANIMAL_GROUP);
        db.execSQL("DROP TABLE IF EXIST " + TABLE_EXPERT);
        db.execSQL("DROP TABLE IF EXIST " + TABLE_ANIMAL_EXPERT);

        this.onCreate(db);
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    public long createAnimal(Animal animal) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, animal.getName());
        values.put(KEY_PHOTO_ID, animal.getPhotoId());
        values.put(KEY_LOCATION_TEXT, animal.getLocationText());
        values.put(KEY_DESCRIPTION, animal.getDescription());

        long animal_id = db.insert(TABLE_ANIMAL, null, values);

        // TODO Insert all the groups and experts.

        return animal_id;
    }

    public List<Animal> getAllAnimals() {
        List<Animal> animals = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ANIMAL;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Animal animal = new Animal();
                animal.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                animal.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                animal.setPhotoId(c.getString(c.getColumnIndex(KEY_PHOTO_ID)));
                animal.setLocationText(c.getString(c.getColumnIndex(KEY_LOCATION_TEXT)));
                animal.setDescription(c.getString(c.getColumnIndex(KEY_DESCRIPTION)));

                // TODO get and set all the groups and experts.

                // adding to tags list
                animals.add(animal);
            } while (c.moveToNext());
        }
        return animals;
    }

    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GROUP;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Group group = new Group();
                group.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                group.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                group.setPhotoId(c.getString(c.getColumnIndex(KEY_PHOTO_ID)));

                // TODO get and set all the groups and experts.

                // adding to tags list
                groups.add(group);
            } while (c.moveToNext());
        }
        return groups;
    }

    public List<Expert> getAllExperts() {
        List<Expert> experts = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EXPERT;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Expert expert = new Expert();
                expert.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                expert.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                expert.setPhotoId(c.getString(c.getColumnIndex(KEY_PHOTO_ID)));

                // TODO get and set all the animals.

                // adding to tags list
                experts.add(expert);
            } while (c.moveToNext());
        }
        return experts;
    }
}