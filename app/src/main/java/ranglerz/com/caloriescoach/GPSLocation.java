package ranglerz.com.caloriescoach;

/**
 * Created by User-10 on 21-Dec-16.
 */
import android.Manifest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;


public class GPSLocation extends AppCompatActivity{

    TextView totalTime, totalDistance, totalCalories, totalSteps;
    int count = 0;
    Button share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpslocation);

        share = (Button)findViewById(R.id.bt_share);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2735690748218549/7817468515");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        totalCalories = (TextView)findViewById(R.id.totalCaloriesBurn);
        totalTime = (TextView)findViewById(R.id.totalTime);
        totalDistance = (TextView)findViewById(R.id.totalDistance);
        totalSteps = (TextView)findViewById(R.id.totalSteps);

        countNumberofImageInDatabse();
        gettingDistanceFromDB();

        Intent intent = getIntent();
        double mTotalTime = intent.getDoubleExtra("TOTALTIME", 0.0);
        double mTotalCalories =  intent.getDoubleExtra("TOTALCALORIESBURN", 0.0);
        int mTotalSteps =  intent.getIntExtra("TOTALSTEPS", 0);


        mTotalCalories = round(mTotalCalories, 2);
        mTotalTime = round(mTotalTime, 1);


        totalCalories.setText("Calories Burned: "+mTotalCalories + " grams");
        totalTime.setText("Total Time: "+mTotalTime + " minuts");
        totalSteps.setText("Total Steps Taken: " + mTotalSteps + " Steps");

        Log.i("Broadcast Values", "Calories: " + mTotalCalories + "grams");
        Log.i("Broadcast Values", "Time: " +mTotalTime);
        Log.i("Broadcast Values", "Stpes: " + mTotalSteps);

        exitApp();


    }


    public void gettingDistanceFromDB(){
        String query = "SELECT * FROM location WHERE ID="+count;
        DataBaseClass dataBaseClass = new DataBaseClass(this);
        SQLiteDatabase db = dataBaseClass.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        while (c.moveToNext()){
            String sDistance = c.getString(c.getColumnIndex("distance"));
            totalDistance.setText("Total Distance Covered: " + sDistance + " meters");
            db.close();
        }

    }

    public void countNumberofImageInDatabse() {
        //couting number of images in databse
        DataBaseClass dataBaseClass = new DataBaseClass(this);
        SQLiteDatabase dbcount = dataBaseClass.getReadableDatabase();
        try {

            Cursor cursor = dbcount.rawQuery("SELECT * FROM location", null);

            String TAG = "Count";
            count = cursor.getCount();
            String cunt = Integer.toString(count);
            Log.i(TAG, cunt);
            // Toast.makeText(getApplicationContext(), "Count data " + cunt, Toast.LENGTH_SHORT).show();
            dbcount.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        dbcount.close();
    }

    //rouding double
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public void exitApp(){
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareOnSocial();
            }
        });
    }

    //exiting app on backPress


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void shareOnSocial(){
        String tTime, tDistance, tCalories, tSteps;
        tTime = totalTime.getText().toString();
        tDistance = totalDistance.getText().toString();
        tCalories = totalCalories.getText().toString();
        tSteps = totalSteps.getText().toString();
        composeEmail("Total Time: " + tTime + "\n"
                        + "Total Distance: " + tDistance + "\n"
                        + " Total Calories Burned: " + tCalories + "\n"
                        + " Total Steps Taken: " + tSteps);

    }


    public void composeEmail(String text){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        if (intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
        }
    }

}