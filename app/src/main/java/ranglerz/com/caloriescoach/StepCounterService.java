package ranglerz.com.caloriescoach;

/**
 * Created by User-10 on 21-Dec-16.
 */
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class StepCounterService extends Service implements SensorEventListener {


    Handler handler;
    int count;
    long serviceStartTime;
    long serviceEndTime;

    private SensorManager sensorManager;
    private Sensor sensorCount;
    private Sensor sensorDetect;
    long previousTime = 0, prevTimeSpan = 0, prevCountPerSec = 0, currenTime = 0;
    int currentSteps = 0;
    DataBaseClass dataBaseClass;

    private boolean isSensorAvailaable = true;

    Helper helper;


    final static String MY_ACTION = "MY_ACTION";


    public StepCounterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        countNumberofImageInDatabse();

        serviceStartTime = Calendar.getInstance().getTimeInMillis();


        PackageManager pm = getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
            Toast.makeText(getApplicationContext(), "Device support Step count Sensor", Toast.LENGTH_SHORT).show();
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorCount = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorDetect = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        sensorManager.registerListener(this, sensorCount, (sensorManager.SENSOR_DELAY_NORMAL * 10) / 2);
        sensorManager.registerListener(this, sensorDetect, (sensorManager.SENSOR_DELAY_NORMAL * 10) / 2);// noral has 2,00,000 mirosecond so i am converting it to sescond, one second = 1,000,000 micro secon


        // checking the availability of sensors in device
        if (sensorCount==null){
            Toast.makeText(getApplicationContext(), "Device not support Step count Sensor", Toast.LENGTH_SHORT).show();
            isSensorAvailaable = false;
            stopSelf();

        }else {

            String weight, hieght;

            SharedPreferences sharedPreferences = getSharedPreferences("tab", 0);
            int cCurrentSteps = sharedPreferences.getInt("STEPS", 0);
           currentSteps = cCurrentSteps;

            DataBaseClass dataBaseClass = new DataBaseClass(this);
            String query = "SELECT * FROM personrecord WHERE ID=" + count;
            SQLiteDatabase db = dataBaseClass.getReadableDatabase();
            Cursor c = db.rawQuery(query, null);
            if (c != null) {
                while (c.moveToNext()) {
                    //String personAge = c.getString(c.getColumnIndex(Col_2));
                    weight = c.getString(c.getColumnIndex("weight"));
                    hieght = c.getString(c.getColumnIndex("hieght"));


                    /*String age = intent.getStringExtra("AGE");
            String weight = intent.getStringExtra("WEIGHT");
            String hieght = intent.getStringExtra("HIEGHT");
            */


                    //time = intent.getLongExtra("TIME", 0);
                    String time1 = Long.toString(serviceStartTime);

                    String timeInHMS =  convertingMilisecondToHMS(serviceStartTime);
                    //  Toast.makeText(getApplicationContext(), "time: "+timeInHMS+"/nHieght: "+hieght+"\nWeight: "+weight+"/nAge:" + age, Toast.LENGTH_SHORT).show();*//**//**//**//*
                    Log.i("Information: ", "Time: " + timeInHMS);
                    // Log.i("Information: ", "Age: " + age);
                    Log.i("Information: ", "Weight: " + weight);
                    Log.i("Information: ", "Hieght: " + hieght);

                    helper = new Helper();
                    helper.setHieght(hieght);
                    helper.setWeight(weight);
                    //helper.setAge(age);
                    String cSteps = Integer.toString(currentSteps);
                    helper.setStartStepsCounts(cSteps);
                    //helper.setEndStepsCounts("400");
                    helper.setStartTime(time1);
                    // helper.setEndTime(time2);

                    dataBaseClass = new DataBaseClass(this);
                    long isInserted = dataBaseClass.createPost(helper);
                    if (isInserted > -1) {
                        // Toast.makeText(getApplicationContext(), "Inserted Successfully To PersonTable", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "Fail To Start", Toast.LENGTH_SHORT).show();
                        //stop service
                    }


                }
                db.close();
            }


        }

        return super.onStartCommand(intent, flags, startId);



    }//end of onStarCommand




    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;
        if (event!=null){
            float[] values = event.values;
            int value = -1;
            if (values.length > 0) {
                value = (int) values[0];
                currentSteps = value;
               // Toast.makeText(StepCounterService.this, "Current Steps: " + currentSteps, Toast.LENGTH_SHORT).show();


            }
            if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                long currentTime = Calendar.getInstance().getTimeInMillis();
                long timeSpan = currentTime - previousTime;
                long currentCountPerSec = 1/timeSpan;

                if(currentCountPerSec>prevCountPerSec)
                {
                    Toast.makeText(getApplicationContext(), "I am Running", Toast.LENGTH_SHORT).show();

                }
                prevCountPerSec = currentCountPerSec;
                previousTime = currentTime;


            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //converting millisecond to house minumts and second
    public String convertingMilisecondToHMS(long milisecond ){
        // New date object from millis
        Date date = new Date(milisecond);
        // formattter
        SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss");
        // Pass date object
        String formatted = formatter.format(date);
        return formatted;

    }


    @Override
    public void onDestroy() {



        if (isSensorAvailaable == true){
            serviceEndTime = Calendar.getInstance().getTimeInMillis();
            super.onDestroy();



            countNumberofImageInDatabse();

            DataBaseClass dataBaseClass = new DataBaseClass(this);
            String query = "SELECT * FROM personrecord WHERE ID=" + count;
            SQLiteDatabase db = dataBaseClass.getReadableDatabase();
            Cursor c = db.rawQuery(query, null);
            if (c != null) {
                while (c.moveToNext()) {
                    //String personAge = c.getString(c.getColumnIndex(Col_2));
                    String personWeight = c.getString(c.getColumnIndex("weight"));
                    String startTime = c.getString(c.getColumnIndex("startTime"));
                    String startStepsCount = c.getString(c.getColumnIndex("startStepsCount"));
                    // String endStepsCount = c.getString(c.getColumnIndex("endStepsCounts"));

                    Log.i("Values Strings: ", "Weight: " + personWeight);
                    Log.i("Values Strings: ", "Start Time: " + startTime);
                    Log.i("Values Strings: ", "End Time: " + serviceEndTime);
                    Log.i("Values Strings: ", "Start Steps: " + startStepsCount);
                    // Log.i("Values Strings: ", "Endt Steps: " + endStepsCount);

                    Log.i("Service: ", "Stop Service Called");
                    db.close();

                    int sSteps, iWeight;
                    long sTime, eTime;

                    sTime = Long.parseLong(startTime);
                    //eTime = Long.parseLong(endTime);
                    eTime = serviceEndTime;
                    sSteps = Integer.parseInt(startStepsCount);
                    // eSteps = Integer.parseInt(endStepsCount);
                    iWeight = Integer.parseInt(personWeight);

                    long timeTaken = eTime - sTime;

                    double finalTimeInMinuts = ((timeTaken / 1000) / 60);
                    int finalSteps = currentSteps - sSteps;

                    double burnCalories = coutCaloriesBurn(iWeight, finalTimeInMinuts, finalSteps);


                    Log.i("Values Integer: ", "Weight: " + iWeight);
                    Log.i("Values Integer: ", "Start Time: " + sTime);
                    Log.i("Values Integer: ", "End Time: " + eTime);
                    Log.i("Values Integer: ", "Start Steps: " + sSteps);
                    //Log.i("Values Integer: ", "Endt Steps: " + eSteps);
                    Log.i("Values Integer: ", "Time Taken: " + timeTaken);
                    Log.i("Values Integer: ", "Final Time: " + finalTimeInMinuts + " Minuts");


                    Log.i("Values Integer: ", "Calories Burn: " + burnCalories);

                    Toast.makeText(getApplicationContext(), "Total Calories Burn: " + burnCalories, Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "Total Time Taken: " + finalTimeInMinuts + " m", Toast.LENGTH_SHORT).show();

                    Intent showResults = new Intent(StepCounterService.this, GPSLocation.class);
                    showResults.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    showResults.putExtra("TOTALTIME", finalTimeInMinuts);
                    showResults.putExtra("TOTALCALORIESBURN", burnCalories);
                    showResults.putExtra("TOTALSTEPS", finalSteps);
                    startActivity(showResults);


                    sensorManager.unregisterListener(this, sensorCount);
                    sensorManager.unregisterListener(this, sensorDetect);


                    //putting values of current Steps in sharePreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("tab", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("STEPS", currentSteps);
                    editor.commit();
                }
            }
        } else {
            super.onDestroy();

        }
    }

    public void countNumberofImageInDatabse() {
        //couting number of images in databse
        DataBaseClass dataBaseClass = new DataBaseClass(this);
        SQLiteDatabase dbcount = dataBaseClass.getReadableDatabase();
        try {

            Cursor cursor = dbcount.rawQuery("SELECT * FROM personrecord", null);

            String TAG = "Count";
            count = cursor.getCount();
            String cunt = Integer.toString(count);
            Log.i(TAG, cunt);
            // Toast.makeText(getApplicationContext(), "Count data " + cunt, Toast.LENGTH_SHORT).show();


        } catch (SQLException e) {
            e.printStackTrace();
        }
        dbcount.close();
    }

    public double coutCaloriesBurn(int weight, double finalTime, int finalSteps){


        double finalresult = finalSteps/((finalTime+1)*(weight));


        return finalresult;
    }


}
