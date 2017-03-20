package ranglerz.com.caloriescoach;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import android.location.Location;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {


    EditText age, weight, hieght;
    Button startSensor, currentLocation, bMale, bFemale;
    Spinner selectGender, exerciseType;
    DataBaseClass dataBaseClass;

    double finalDistanceInMeters = 0;

    String start = "START";
    String end = "End";



    int count = 0;

    private static final String TAG = GPSLocation.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    private double currentLat, currentLng;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    private SensorManager sensorManager;
    private Sensor sensorCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialization();


        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2735690748218549/7817468515");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }

        checkIfServiceRunning();
        displayLocation();
        startBurning();
        maleFemaleButtonHandling();
        // stopService();

    }//end of onCreate

    public void initialization() {
        age = (EditText) findViewById(R.id.et_age);
        weight = (EditText) findViewById(R.id.et_weight);
        hieght = (EditText) findViewById(R.id.et_hieght);
        startSensor = (Button) findViewById(R.id.start_burning);
        bMale = (Button) findViewById(R.id.bt_male);
        bFemale = (Button) findViewById(R.id.bt_female);

        //selectGender = (Spinner) findViewById(R.id.sp_gender);
        exerciseType = (Spinner) findViewById(R.id.sp_exercise_type);
        dataBaseClass = new DataBaseClass(this);

    }//end of initialization


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void checkIfServiceRunning(){
        boolean isServiceRunning =  isMyServiceRunning(StepCounterService.class);

        if (isServiceRunning){
            startSensor.setText(end);
        }else {
            startSensor.setText(start);
        }

    }


    public void startBurning() {
        startSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                sensorCount = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

                // checking the availability of sensors in device
                if (sensorCount == null) {
                    Toast.makeText(getApplicationContext(), "This Device not support Step count Sensor Try on Other Device", Toast.LENGTH_LONG).show();
                    finish();
                } else {

                    if (startSensor.getText().equals(start)) {

                        if (age.getText().length()==0 || weight.getText().length()==0 || hieght.getText().length()==0){
                            Toast.makeText(getApplicationContext(), "Please Provide Information first ", Toast.LENGTH_SHORT).show();
                        }else {
                            //starting services
                            long currentTime = Calendar.getInstance().getTimeInMillis();
                            String personAge;
                            String personWeight;
                            String personHieght;

                            DataBaseClass dbClass = new DataBaseClass(MainActivity.this);

                            Log.e("TAG", "Current Time is: " + currentTime);

                            personAge = age.getText().toString();
                            personWeight = weight.getText().toString();
                            personHieght = hieght.getText().toString();


                            Helper helper = new Helper();
                            helper.setAge(personAge);
                            helper.setWeight(personWeight);
                            helper.setHieght(personHieght);

                            //inserting values to database
                            long isInserted = dbClass.createPost(helper);
                            Log.d("MainActivity ", "Values inserted to database person Table: ");

                            //start service
                            Intent serviceIntent = new Intent(MainActivity.this, StepCounterService.class);
                      /*  serviceIntent.putExtra("AGE", personAge);
                        serviceIntent.putExtra("WEIGHT", personWeight);
                        serviceIntent.putExtra("HIEGHT", personHieght);
                        serviceIntent.putExtra("TIME", currentTime);
*/



                            startService(serviceIntent);
                            Toast.makeText(getApplicationContext(), "Sensor Service Started...", Toast.LENGTH_SHORT).show();

                            //inserting locatoin lat long in db table


                            String stringLat = Double.toString(currentLat);
                            String stringLng = Double.toString(currentLng);

                            helper.setCurrentLat(stringLat);
                            helper.setCurrentLng(stringLng);
                            Log.i("MainActivity: ", "String Latitude: " + stringLat);
                            Log.i("MainActivity: ", "String Longitude: " + stringLng);

                            long result = dbClass.insertToLocationTable(helper);
                            if (result > -1) {
                                //Toast.makeText(getApplicationContext(), "Inserted Successfully To Location Table", Toast.LENGTH_SHORT).show();


                            } else {
                                // Toast.makeText(getApplicationContext(), "Failed to insert", Toast.LENGTH_SHORT).show();
                            }
                            //setting text as end after starting
                            startSensor.setText(end);}



                    } else if (startSensor.getText().equals(end)) {

                        //counting number of rows in table
                        countNumberofImageInDatabse();
                        DataBaseClass dataBaseClass = new DataBaseClass(MainActivity.this);
                        SQLiteDatabase db = dataBaseClass.getReadableDatabase();
                        String query = "SELECT * FROM location WHERE ID=" + count;
                        Cursor c = db.rawQuery(query, null);
                        while (c.moveToNext()) {
                            String sLat = c.getString(c.getColumnIndex("latitude"));
                            String sLng = c.getString(c.getColumnIndex("longitude"));
                            Double dbLat = Double.parseDouble(sLat);
                            Double dbLng = Double.parseDouble(sLng);
                            Log.d("MainActivity: ", "LATLNG FROM DB: " + dbLat);
                            Log.d("MainActivity: ", "LATLNG FROM DB: " + dbLng);

                            LatLng dbLatLng = new LatLng(dbLat, dbLng);
                            LatLng currentLatLng = new LatLng(currentLat, currentLng);

                            Double distane = SphericalUtil.computeDistanceBetween(dbLatLng, currentLatLng);
                            Log.d("Distance", "DDDDDDD: " + distane);
                            double finalDistance = truncateTo(distane, 1);
                            finalDistanceInMeters = finalDistance;
                            // Toast.makeText(getApplicationContext(), "Distance Coverd: " + finalDistance, Toast.LENGTH_SHORT).show();
                        }//end of while
                        Intent intent = new Intent(MainActivity.this, StepCounterService.class);
                        stopService(intent);

                        String fDistance = Double.toString(finalDistanceInMeters);
                        boolean isUpdate = dataBaseClass.updateLocationTable(count, fDistance);
                        if (isUpdate) {
                        } else {
                            // Toast.makeText(getApplicationContext(), "Update Faile", Toast.LENGTH_SHORT).show();
                        }

                        clearEditTextFields();

                        //setting text as START after ending services
                        startSensor.setText(start);


                        //finishing activity
                        finish();

                    }


                }


            }

        });


    }


    public void maleFemaleButtonHandling() {
        bMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bMale.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.skyBlueLow));
                bMale.setTextSize(12f);
                bFemale.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.skyBlue));
                bFemale.setTextSize(14f);
            }
        });//end of bMale button

        bFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bMale.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.skyBlue));
                bMale.setTextSize(14f);
                bFemale.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.skyBlueLow));
                bFemale.setTextSize(12f);

            }
        });

    }

    public void stopService() {
        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                sensorCount = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

                // checking the availability of sensors in device
                if (sensorCount == null) {
                    Toast.makeText(getApplicationContext(), "This Device not support Step count Sensor Try on Other Device", Toast.LENGTH_LONG).show();
                    finish();
                } else {


                }
            }
        });


    }

    public void clearEditTextFields() {
        age.setText("");
        weight.setText("");
        hieght.setText("");
    }


    static double truncateTo(double unroundedNumber, int decimalPlaces) {
        int truncatedNumberInt = (int) (unroundedNumber * Math.pow(10, decimalPlaces));
        double truncatedNumber = (double) (truncatedNumberInt / Math.pow(10, decimalPlaces));
        return truncatedNumber;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            currentLat = latitude;
            currentLng = longitude;

            Toast.makeText(getApplicationContext(), latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
          //  Toast.makeText(MainActivity.this, "" + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();

        } else {
           // Toast.makeText(getApplicationContext(), "Couldn't get the location. Make sure location is enabled on the device", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
       /* int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }*/

            else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
try {
    LocationServices.FusedLocationApi.removeLocationUpdates(
            mGoogleApiClient, this);
}catch (IllegalStateException e){
    e.printStackTrace();
}

    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();
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
            //  Toast.makeText(getApplicationContext(), "Count data " + cunt, Toast.LENGTH_SHORT).show();
            dbcount.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
