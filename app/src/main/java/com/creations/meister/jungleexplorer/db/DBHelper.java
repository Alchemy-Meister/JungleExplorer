package com.creations.meister.jungleexplorer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.domain.Expert;
import com.creations.meister.jungleexplorer.domain.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by meister on 3/30/16.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper instance;

    // Database Version
    private static final int DATABASE_VERSION = 3;

    // Database Name
    private static final String DATABASE_NAME = "JungleExplorer.db";

    // Table Names
    private static final String TABLE_ANIMAL = "animals";
    private static final String TABLE_GROUP = "groups";
    private static final String TABLE_ANIMAL_GROUP = "animals_groups";
    private static final String TABLE_EXPERT = "experts";
    private static final String TABLE_ANIMAL_EXPERT = "animals_experts";

    // Common table column names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHOTO_ID = "photo_id";

    // ANIMAL Table - column names
    private static final String KEY_FAVORITE = "favorite";
    private static final String KEY_LOCATION_TEXT = "location_text";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    // Common relation table column names
    private static final String KEY_ANIMAL_ID = "animal_id";

    // ANIMAL_GROUP Table - column names
    private static final String KEY_GROUP_ID = "group_id";

    // EXPERT Table - column names
    private static final String KEY_CONTACT_URI = "contact_uri";

    // ANIMAL_EXPERT Table - column names
    private static final String KEY_EXPERT_ID = "expert_id";

    // Table Create Statements
    // Animal table create statement
    private static final String CREATE_TABLE_ANIMAL = "CREATE TABLE "
            + TABLE_ANIMAL + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PHOTO_ID
            + " TEXT," + KEY_NAME + " TEXT," + KEY_FAVORITE + " INTEGER DEFAULT 0,"
            + KEY_LOCATION_TEXT + " TEXT," + KEY_DESCRIPTION + " TEXT,"
            + KEY_LATITUDE + " REAL," + KEY_LONGITUDE + " REAL)";

    private static final String CREATE_TABLE_GROUP = "CREATE TABLE "
            + TABLE_GROUP + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PHOTO_ID
            + " TEXT," + KEY_NAME + " TEXT)";

    private static final String CREATE_TABLE_ANIMAL_GROUP = "CREATE TABLE "
            + TABLE_ANIMAL_GROUP + "(" + KEY_ANIMAL_ID + " REFERENCES " + TABLE_ANIMAL
            + "(" + KEY_ID + ")," + KEY_GROUP_ID + " REFERENCES " + TABLE_GROUP
            + "(" + KEY_ID + "),PRIMARY KEY(" + KEY_ANIMAL_ID + "," + KEY_GROUP_ID + "))";

    private static final String CREATE_TABLE_EXPERT = "CREATE TABLE "
            + TABLE_EXPERT + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PHOTO_ID
            + " TEXT," + KEY_NAME + " TEXT," + KEY_CONTACT_URI + " TEXT)";

    private static final String CREATE_TABLE_ANIMAL_EXPERT = "CREATE TABLE "
            + TABLE_ANIMAL_EXPERT + "(" + KEY_ANIMAL_ID + " REFERENCES " + TABLE_ANIMAL
            + "(" + KEY_ID + ")," + KEY_EXPERT_ID + " REFERENCES " + TABLE_EXPERT
            + "(" + KEY_ID + "),PRIMARY KEY(" + KEY_ANIMAL_ID + "," + KEY_EXPERT_ID + "))";



    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DBHelper getHelper(Context context) {
        if (instance == null)
            instance = new DBHelper(context);
        return instance;
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANIMAL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANIMAL_GROUP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPERT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANIMAL_EXPERT);

        this.onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    public long createAnimal(@NonNull  Animal animal) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, animal.getName());
        values.put(KEY_PHOTO_ID, animal.getPhotoId());
        values.put(KEY_LOCATION_TEXT, animal.getLocationText());
        values.put(KEY_DESCRIPTION, animal.getDescription());
        values.put(KEY_FAVORITE, animal.getFavorite());
        values.put(KEY_LATITUDE, animal.getLatitude());
        values.put(KEY_LONGITUDE, animal.getLongitude());

        long id = db.insert(TABLE_ANIMAL, null, values);

        animal.setId((int) id);

        for(Expert expert : animal.getAnimalExperts()) {
            this.createAnimalExpert(animal, expert);
        }
        // TODO Insert all the groups.
        return id;
    }

    public long createExpert(@NonNull  Expert expert) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, expert.getName());
        values.put(KEY_PHOTO_ID, expert.getPhotoId());
        values.put(KEY_CONTACT_URI, expert.getContactUri().toString());

        return db.insert(TABLE_EXPERT, null, values);
    }

    public long createAnimalExpert(@NonNull Animal animal, @NonNull Expert expert) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ANIMAL_ID, animal.getId());
        values.put(KEY_EXPERT_ID, expert.getId());

        return db.insert(TABLE_ANIMAL_EXPERT, null, values);
    }

    public boolean expertExists(@NonNull Expert expert) {
        String selectQuery = "SELECT * FROM " + TABLE_EXPERT + " WHERE "
                + KEY_CONTACT_URI + "=?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[] {expert.getContactUri().toString()});
        if(c.moveToFirst()) {
            c.close();
            return true;
        }
        c.close();
        return false;
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
                animal.setFavorite(c.getInt(c.getColumnIndex(KEY_FAVORITE)));
                animal.setLatitude(c.getDouble(c.getColumnIndex(KEY_LATITUDE)));
                animal.setLongitude(c.getDouble(c.getColumnIndex(KEY_LONGITUDE)));

                // TODO get and set all the groups and experts.

                // adding to tags list
                animals.add(animal);
            } while (c.moveToNext());
        }
        c.close();
        return animals;
    }

    public List<Animal> getFavorites() {
        List<Animal> animals = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ANIMAL + " WHERE " + KEY_FAVORITE + "=1";

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
                animal.setLatitude(c.getDouble(c.getColumnIndex(KEY_LATITUDE)));
                animal.setLongitude(c.getDouble(c.getColumnIndex(KEY_LONGITUDE)));

                // TODO get and set all the groups and experts.

                // adding to tags list
                animals.add(animal);
            } while (c.moveToNext());
        }
        c.close();
        return animals;
    }

    public ArrayList<Group> getAllGroups() {
        ArrayList<Group> groups = new ArrayList<>();
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
        c.close();
        return groups;
    }

    public ArrayList<Expert> getAllExperts() {
        ArrayList<Expert> experts = new ArrayList<>();
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
                expert.setContactUri(Uri.parse(c.getString(c.getColumnIndex(KEY_CONTACT_URI))));

                // TODO get and set all the animals.

                // adding to tags list
                experts.add(expert);
            } while (c.moveToNext());
        }
        c.close();
        return experts;
    }

    public ArrayList<Expert> getAllAnimalExperts(@NonNull  Animal animal) {
        ArrayList<Expert> experts = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ANIMAL_EXPERT + " WHERE "
                + KEY_ANIMAL_ID + "=" + animal.getId();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if(c.moveToFirst()) {
            do {
                Expert expert = this.getExpert(c.getLong(c.getColumnIndex(KEY_EXPERT_ID)));
                if(expert != null) {
                    experts.add(expert);
                }
            } while(c.moveToNext());
        }
        c.close();
        return experts;
    }

    public ArrayList<Animal> getAnimalsWithinRadius(Location location, int radius) {
        ArrayList<Animal> returnAnimals = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ANIMAL;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        if(c.moveToFirst()) {
            do {
                Double latitude = c.getDouble(c.getColumnIndex(KEY_LATITUDE));
                Double longitude = c.getDouble(c.getColumnIndex(KEY_LONGITUDE));

                Animal animal = new Animal();
                animal.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                animal.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                animal.setPhotoId(c.getString(c.getColumnIndex(KEY_PHOTO_ID)));
                animal.setLocationText(c.getString(c.getColumnIndex(KEY_LOCATION_TEXT)));
                animal.setDescription(c.getString(c.getColumnIndex(KEY_DESCRIPTION)));
                animal.setFavorite(c.getInt(c.getColumnIndex(KEY_FAVORITE)));
                animal.setLatitude(latitude);
                animal.setLongitude(longitude);

                if(latitude != null & longitude != null) {
                    Location aLocation = new Location("");
                    aLocation.setLatitude(latitude);
                    aLocation.setLongitude(longitude);
                    float distance = location.distanceTo(aLocation);

                    if(distance <= radius) {
                        returnAnimals.add(animal);
                    }
                }

            }while(c.moveToNext());
        }
        return returnAnimals;
    }

    public Animal getNearestAnimal(Location location) {
        Float minDistance = null;
        Animal returnAnimal = null;
        String selectQuery = "SELECT * FROM " + TABLE_ANIMAL;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        if(c.moveToFirst()) {
            do {
                Double latitude = c.getDouble(c.getColumnIndex(KEY_LATITUDE));
                Double longitude = c.getDouble(c.getColumnIndex(KEY_LONGITUDE));

                Animal animal = new Animal();
                animal.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                animal.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                animal.setPhotoId(c.getString(c.getColumnIndex(KEY_PHOTO_ID)));
                animal.setLocationText(c.getString(c.getColumnIndex(KEY_LOCATION_TEXT)));
                animal.setDescription(c.getString(c.getColumnIndex(KEY_DESCRIPTION)));
                animal.setFavorite(c.getInt(c.getColumnIndex(KEY_FAVORITE)));
                animal.setLatitude(latitude);
                animal.setLongitude(longitude);

                if(latitude != null & longitude != null) {
                    Location aLocation = new Location("");
                    aLocation.setLatitude(latitude);
                    aLocation.setLongitude(longitude);
                    float distance = location.distanceTo(aLocation);
                    if((minDistance != null && distance < minDistance) || minDistance == null) {
                        minDistance = distance;
                        returnAnimal = animal;
                    }
                }
            } while(c.moveToNext());
        }
        return returnAnimal;
    }

    public Expert getExpert(Long id) {
        Expert expert = null;
        String selectQuery = "SELECT * FROM " + TABLE_EXPERT + " WHERE " + KEY_ID + "=?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[] {String.valueOf(id)});
        if(c.moveToFirst()) {
            expert = new Expert();
            expert.setId(c.getInt((c.getColumnIndex(KEY_ID))));
            expert.setName(c.getString(c.getColumnIndex(KEY_NAME)));
            expert.setPhotoId(c.getString(c.getColumnIndex(KEY_PHOTO_ID)));
            expert.setContactUri(Uri.parse(c.getString(c.getColumnIndex(KEY_CONTACT_URI))));
        }
        return expert;
    }

    public void  updateAnimal(@NonNull Animal animal) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_NAME, animal.getName());
        cv.put(KEY_PHOTO_ID, animal.getPhotoId());
        cv.put(KEY_LOCATION_TEXT, animal.getLocationText());
        cv.put(KEY_DESCRIPTION, animal.getDescription());
        cv.put(KEY_FAVORITE, animal.getFavorite());
        if(animal.getLatitude() != null)
            cv.put(KEY_LATITUDE, animal.getLatitude());
        else
            cv.putNull(KEY_LATITUDE);
        if(animal.getLongitude() != null)
            cv.put(KEY_LONGITUDE, animal.getLongitude());
        else
            cv.putNull(KEY_LONGITUDE);

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_ANIMAL, cv,KEY_ID + "=" + animal.getId(), null);

        this.removeAnimalExperts(animal);

        for(Expert expert : animal.getAnimalExperts()) {
            this.createAnimalExpert(animal, expert);
        }
    }

    public void removeAnimal(@NonNull  Animal animal) {
        SQLiteDatabase db = this.getWritableDatabase();

        this.removeAnimalExperts(animal);
        db.delete(TABLE_ANIMAL, KEY_ID + "=" + animal.getId(), null);
    }

    public void removeAnimalExperts(@NonNull Animal animal) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ANIMAL_EXPERT, KEY_ANIMAL_ID + "=" + animal.getId(), null);
    }
}