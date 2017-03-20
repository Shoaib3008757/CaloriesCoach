package ranglerz.com.caloriescoach;

/**
 * Created by User-10 on 21-Dec-16.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Shoaib Anwar on 12/4/2016.
 */
public class DataBaseClass extends SQLiteOpenHelper{

    Context context;
    public static final String DATABASE_NAME = "person.db";
    private static final int Database_Version = 1;
    private static final String TABLE_NAME = "personrecord";
    public static final String Col_1 = "ID";
    public static final String Col_2 = "age";
    public static final String Col_3 = "weight";
    public static final String Col_4 = "hieght";
    public static final String Col_5 = "startTime";
    public static final String Col_6 = "endTime";
    public static final String Col_7 = "startStepsCount";
    public static final String Col_8 = "endStepsCounts";
    public static final String Col_9 = "distanceCoverd";
    public static final String Col_10 = "exerciseType";
    public static final String Col_11 = "currentLng";
    public static final String Col_12 = "currentLat";





    String CREATE_TABLE_CALL = "CREATE TABLE " + TABLE_NAME + "("
            + Col_1 + " integer PRIMARY KEY AUTOINCREMENT,"
            + Col_2 + " TEXT, "
            + Col_3 + " TEXT, "
            + Col_4 + " TEXT, "
            + Col_5 + " TEXT, "
            + Col_6 + " TEXT, "
            + Col_7 + " TEXT, "
            + Col_8 + " TEXT, "
            + Col_9 + " TEXT, "
            + Col_10 + " TEXT, "
            + Col_11 + " TEXT, "
            + Col_12 + " TEXT, "
            + "TAG TEXT" + ")";

    //table for location cordinates
    private static final String LOCATION_TABLE = "location";
    public static final String LCol_1 = "ID";
    public static final String LCol_2 = "latitude";
    public static final String LCol_3 = "longitude";
    public static final String LCol_4 = "distance";

    String CREATE_TABLE_LOCATION_CALL = "CREATE TABLE " + LOCATION_TABLE + "("
            + LCol_1 + " integer PRIMARY KEY AUTOINCREMENT,"
            + LCol_2 + " TEXT, "
            + LCol_3 + " TEXT, "
            +LCol_4 + " TEXT, "
            + "TAG TEXT" + ")";



    //constructor
    DataBaseClass(Context context) {
        super(context, DATABASE_NAME, null, Database_Version);
        this.context = context;

    }



    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_CALL);
        db.execSQL(CREATE_TABLE_LOCATION_CALL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE);

    }

    //inserting post in databse
    public long createPost(Helper helper) {
        long c;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Col_2, helper.getAge());
        values.put(Col_3, helper.getWeight());
        values.put(Col_4, helper.getHieght());
        values.put(Col_5, helper.getStartTime());
        values.put(Col_6, helper.getEndTime());
        values.put(Col_7, helper.getStartStepsCounts());
        values.put(Col_8, helper.getEndStepsCounts());
        values.put(Col_9, helper.getDistanceCoverd());
        values.put(Col_10, helper.getExerciseType());
        values.put(Col_11, helper.getCurrentLng());
        values.put(Col_12, helper.getCurrentLat());

        //inserting valuse
        //inserting valuse
        c = db.insert(TABLE_NAME, null, values);
        db.close();
        return c;

    }

    public long insertToLocationTable(Helper h){
        long c;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LCol_2, h.getCurrentLat());
        values.put(LCol_3, h.getCurrentLng());
        values.put(LCol_4, h.getDistanceCoverd());

        c = db.insert(LOCATION_TABLE, null, values);
        db.close();
        return c;
    }

    /* Method for fetching record from Database */
    public ArrayList<Helper> getAllRecords() {
        String query = "SELECT * FROM " + TABLE_NAME;
        ArrayList<Helper> recordList = new ArrayList<Helper>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        if (c != null) {
            while (c.moveToNext()) {
                String personAge = c.getString(c.getColumnIndex(Col_2));
                String personWeight = c.getString(c.getColumnIndex(Col_3));
                String startTime = c.getString(c.getColumnIndex(Col_5));
                String endTime = c.getString(c.getColumnIndex(Col_6));
                String startStepsCount = c.getString(c.getColumnIndex(Col_7));
                String endStepsCount = c.getString(c.getColumnIndex(Col_8));
                String distanceCoverd = c.getString(c.getColumnIndex(Col_9));
                String excerciseType = c.getString(c.getColumnIndex(Col_10));


                Helper recordHelper = new Helper();

                recordHelper.setAge(personAge);
                recordHelper.setWeight(personWeight);
                recordHelper.setStartTime(startTime);
                recordHelper.setEndTime(endTime);
                recordHelper.setStartStepsCounts(startStepsCount);
                recordHelper.setEndStepsCounts(endStepsCount);
                //recordHelper.setDistanceCoverd(distanceCoverd);
                recordHelper.setExerciseType(excerciseType);

                recordList.add(recordHelper);
            }
        }

        db.close();
        return recordList;

    }

    public ArrayList<Helper> getLastRow(int id ){
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE ID="+id;
        ArrayList<Helper> resultList = new ArrayList<Helper>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        if (c!=null){
            while (c.moveToNext()){
                String personAge = c.getString(c.getColumnIndex(Col_2));
                String personWeight = c.getString(c.getColumnIndex(Col_3));
                String startTime = c.getString(c.getColumnIndex(Col_5));
                String endTime = c.getString(c.getColumnIndex(Col_6));
                String startStepsCount = c.getString(c.getColumnIndex(Col_7));
                String endStepsCount = c.getString(c.getColumnIndex(Col_8));
                String distanceCoverd = c.getString(c.getColumnIndex(Col_9));
                String excerciseType = c.getString(c.getColumnIndex(Col_10));


                Helper recordHelper = new Helper();

                recordHelper.setAge(personAge);
                recordHelper.setWeight(personWeight);
                recordHelper.setStartTime(startTime);
                recordHelper.setEndTime(endTime);
                recordHelper.setStartStepsCounts(startStepsCount);
                recordHelper.setEndStepsCounts(endStepsCount);
                recordHelper.setDistanceCoverd(distanceCoverd);
                recordHelper.setExerciseType(excerciseType);

                resultList.add(recordHelper);
            }
        }
        db.close();
        return resultList;
    }

    //Updatating post
    public boolean updateData(int id, String age, String weight, String hieght, long startTime, long endTime, int startSteps, int endSteps, int distance, String exerciseType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Col_2, age);
        contentValues.put(Col_3, weight);
        contentValues.put(Col_4, hieght);
        contentValues.put(Col_5, startTime);
        contentValues.put(Col_6, endTime);
        contentValues.put(Col_7, startSteps);
        contentValues.put(Col_8, endSteps);
        contentValues.put(Col_9, distance);
        contentValues.put(Col_10, exerciseType);


        db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{Integer.toString(id)});
        db.close();
        return true;
    }

    public boolean updateLocationTable(int id, String distance){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LCol_4, distance);
        db.update(LOCATION_TABLE, contentValues, "ID = ?", new String[]{Integer.toString(id)});
        db.close();
        return true;
    }
}
