package com.microsoftBand.collectionapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.SampleRate;
import com.microsoft.band.sensors.UVIndexLevel;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class MainActivity extends Activity implements HeartRateConsentListener{
    private BandClient client = null;
    //button to start the conncetion activity
    private Button btnStart,btnStop,btnViewTable;
    //The text view that displays the current status of the all the sensors
    private TextView AccelerometerTextView;
    private TextView GyroscopeTextView;
    private TextView speedTextView;
    private TextView temperatureTextView;
    private TextView heartRateTextView,UVTextView;
    // a dropdown list to select which activities to use as a label
    private Spinner activitiesToTrack;
    //database thread to store data to database
    private databaseThread dbThread;
    //database helper creates a database helper class that control all database related functions
    public static DatabaseHelper databaseHelper;
    //to prevent the screen from closing
    private PowerManager pm ;
    private PowerManager.WakeLock wakeLock;
    //the variables will store the values of each sensor
    public float accelerometerX=0,accelerometerY=0,accelerometerZ=0,gyroscopeX=0,gyroscopeY=0, gyroscopeZ=0, temperature=0,speed=0;
    //the variable will store the level of uv index
    public UVIndexLevel uv =UVIndexLevel.NONE;
    //radio group that has the sampling rates
    private RadioGroup samplingRates;
    //the three different sapming radio buttons
    private RadioButton samplerate62,samplerate32,samplerate7;

    public int heartRate=0;
    public String label;
    private long timeStamp;
    public volatile static BlockingQueue<BandData> queueForBandData;
    private SampleRate theBandSampleRate;
    private CursorAdapter spinnerCursorAdapter;
    String[] adapterCols=new String[]{"label"};
    int[] adapterRowViews=new int[]{android.R.id.text1};
    private String[] menuList;

//    private DrawerLayout mDrawerLayout;
//    private ListView mDrawerList;
//    private ArrayAdapter<String> mAdapter;
//    private ActionBarDrawerToggle mDrawerToggle;
//    private String mActivityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        AccelerometerTextView = (TextView) findViewById(R.id.AccelerometerStatus);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop=(Button) findViewById(R.id.btnstop);
        btnViewTable = (Button) findViewById(R.id.btnview);
        activitiesToTrack = (Spinner) findViewById(R.id.spinner);
        GyroscopeTextView = (TextView) findViewById(R.id.SecondStatusGyroscope);
        speedTextView=(TextView) findViewById(R.id.speedStatus);
        temperatureTextView = (TextView) findViewById(R.id.tempratureStatus);
        heartRateTextView = (TextView) findViewById(R.id.heartRateStatus);
        UVTextView= (TextView) findViewById(R.id.UVStatus);
        pm=(PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"wake tag");

        //queue for the data collected from the band
        queueForBandData = new LinkedBlockingQueue<BandData>();

        //this responsible of doing the database stuff
        databaseHelper=new DatabaseHelper(this);
        databaseHelper.getWritableDatabase();
        theBandSampleRate = SampleRate.MS16;
        samplingRates = (RadioGroup) findViewById(R.id.samplingRates);
        samplerate62=(RadioButton) findViewById(R.id.ms16);
        samplerate32 = (RadioButton) findViewById(R.id.ms32);
        samplerate7 = (RadioButton) findViewById(R.id.ms128);
        samplingRates.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.ms16) {
                    theBandSampleRate = SampleRate.MS16;
                } else if (checkedId == R.id.ms32) {
                    theBandSampleRate = SampleRate.MS32;
                } else {
                    theBandSampleRate = SampleRate.MS128;
                }
            }
        });
        samplerate62.setChecked(true);
        label="walking";

//        mDrawerList = (ListView)findViewById(R.id.left_drawer);
//        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
//        mActivityTitle = getTitle().toString();

//        addDrawerItems();
//        setupDrawer();

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wakeLock.acquire();
                Log.d("Lock", "lock acquired");
                btnViewTable.setEnabled(false);
                btnStop.setEnabled(true);
                btnStart.setEnabled(false);
                samplingRates.setEnabled(false);
                samplerate62.setEnabled(false);
                samplerate32.setEnabled(false);
                samplerate7.setEnabled(false);
                AccelerometerTextView.setText("");
                //start an asynchronous class that check for the bands connection and connects to it if not connected

                //get activity label
                label=activitiesToTrack.getSelectedItem().toString();


                     new SensorSubscription().execute();
                dbThread=new databaseThread(databaseHelper);

            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbThread.stopThread();
                btnViewTable.setEnabled(true);
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                samplingRates.setEnabled(true);
                samplerate62.setEnabled(true);
                samplerate32.setEnabled(true);
                samplerate7.setEnabled(true);
               //kill the connection
            if(client!=null) {
                if (client.isConnected() == true) {
                    try {
                            client.getSensorManager().unregisterAccelerometerEventListeners();
                        if(SensorsPage.gyro_flag==true){
                            client.getSensorManager().unregisterGyroscopeEventListeners();
                        }
                        if(SensorsPage.skin_flag==true){
                            client.getSensorManager().unregisterSkinTemperatureEventListeners();
                        }
                        if(SensorsPage.heart_flag==true){
                            client.getSensorManager().unregisterHeartRateEventListeners();
                        }
                        if(SensorsPage.uv_flag==true){
                            client.getSensorManager().unregisterUVEventListeners();
                        }

                        if(SensorsPage.speed_flag==true) {
                            client.getSensorManager().unregisterDistanceEventListeners();
                        }

                    } catch (BandIOException e) {
                        e.printStackTrace();
                    }
                    client.disconnect();


                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                        Log.d("Lock", "lock released");
                    }
                    appendToMainUI("Band Disconnected");
                }
            }
            }
        });
        btnViewTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //view table activity
                Intent viewTableActivity= new Intent("android.intent.action.VIEWINFO");
                startActivity(viewTableActivity);
            }
        });

        loadSpinnerData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        AccelerometerTextView.setText("");
        loadSpinnerData();
    }

    @Override
    protected void onPause() {
        super.onPause();

//        if (client != null) {
//            try {
//                //unsubscribe from the listnening events
//                client.getSensorManager().unregisterAccelerometerEventListeners();
//                client.getSensorManager().unregisterDistanceEventListeners();
//                client.getSensorManager().unregisterGyroscopeEventListeners();
//                client.getSensorManager().unregisterSkinTemperatureEventListeners();
//                client.getSensorManager().unregisterHeartRateEventListeners();
//            } catch (BandIOException e) {
//                appendToMainUI(e.getMessage());
//            }
//            dbThread.stopThread();
//        }
    }

    private class SensorSubscription extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
            //check if  band is connected

                boolean bandConnection = getConnectedBandClient();
                if (bandConnection) {
                    appendToMainUI("Band is connected.\n");

                    client.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, theBandSampleRate);

                    if(SensorsPage.gyro_flag==true){
                        client.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, theBandSampleRate);
                    }


                    if(SensorsPage.skin_flag==true){
                        client.getSensorManager().registerSkinTemperatureEventListener(mTemperatureEventListener);
                    }
                    if(SensorsPage.uv_flag==true){
                        client.getSensorManager().registerUVEventListener(mUVEventListner);
                    }

                    if(SensorsPage.speed_flag){
                        client.getSensorManager().registerDistanceEventListener(mDistanceEventListener);
                    }

                    if(SensorsPage.heart_flag==true) {
                        if (client.getSensorManager().getCurrentHeartRateConsent() != UserConsent.GRANTED) {
                            // user has not consented, request it
                            // the calling class is both an Activity and implements
                            // HeartRateConsentListener
                            client.getSensorManager().requestHeartRateConsent(MainActivity.this, MainActivity.this);
                        } else {
                            client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListner);
                        }
                    }


                } else {
                    appendToMainUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage();
                        break;
                }
                appendToMainUI(exceptionMessage);

            } catch (InterruptedException e) {
                appendToMainUI(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dbThread.activateThread();
            if(!dbThread.isAlive()){
                dbThread.start();
            }

        }




    }

    /**
     * @param value
     * change the status text that displays errors and the accelerometer readings
     */
    private void appendToMainUI(final String value) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AccelerometerTextView.setText(value);
            }
        });
    }

    /**
     *
     * @param value
     *
     * update the TextView that displays the gyroscope values
     */
    private void appendToGyroscopeUI(final String value) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GyroscopeTextView.setText(value);
            }
        });
    }

    /**
     *
     * @param value
     *
     * update the Textview responsible of displaying the speed
     */
    private void appendToSpeedUI(final String value) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                speedTextView.setText(value);
            }
        });
    }

    /**
     *
     * @param value
     *
     * update the TextView
     */

    private void appendToTemperature(final String value) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                temperatureTextView.setText(value);
            }
        });
    }
    private void appendToUV(final String value) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UVTextView.setText(value);
            }
        });
    }

    private void appendToHeartStatus(final String value) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                heartRateTextView.setText(value);
            }
        });
    }

    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            if (event != null) {
                //update UI
                appendToMainUI(String.format(" X = %.3f g\n Y = %.3f g\n Z = %.3f g", event.getAccelerationX(),
                        event.getAccelerationY(), event.getAccelerationZ()));
                //update variables in databaseThread class
               accelerometerX=event.getAccelerationX();
                accelerometerY=event.getAccelerationY();
               accelerometerZ=event.getAccelerationZ();
                timeStamp  =event.getTimestamp();

                BandData bandData = new BandData(accelerometerX, accelerometerY,accelerometerZ,gyroscopeX,gyroscopeY, gyroscopeZ,speed,temperature,heartRate,uv,label,timeStamp);
                Log.d("Lable",label);
                queueForBandData.add(bandData);

            }
        }
    };

    private BandGyroscopeEventListener mGyroscopeEventListener = new BandGyroscopeEventListener() {
        @Override
        public void onBandGyroscopeChanged(BandGyroscopeEvent bandGyroscopeEvent) {
            if(bandGyroscopeEvent != null && SensorsPage.gyro_flag==true){
                appendToGyroscopeUI(String.format(" X Gyroscope = %.3f deg/s  \n YGyroscope = %.3f deg/s\n ZGyroscope = %.3f deg/s", bandGyroscopeEvent.getAngularVelocityX(),
                        bandGyroscopeEvent.getAngularVelocityY(), bandGyroscopeEvent.getAngularVelocityZ()));
                //update variables in databaseThread class
               gyroscopeX=bandGyroscopeEvent.getAngularVelocityX();
                gyroscopeY= bandGyroscopeEvent.getAngularVelocityY();
                gyroscopeZ=bandGyroscopeEvent.getAngularVelocityZ();
            }
        }
    };

    private BandDistanceEventListener mDistanceEventListener = new BandDistanceEventListener() {
        @Override
        public void onBandDistanceChanged(BandDistanceEvent bandDistanceEvent) {
           if(bandDistanceEvent != null){
               //change speed unit from cm/s to m/s
                speed = bandDistanceEvent.getSpeed();
               appendToSpeedUI(String.format("speed= %.3f cm/s ", speed));
           }
        }
    };

    private BandSkinTemperatureEventListener mTemperatureEventListener = new BandSkinTemperatureEventListener() {
        @Override
        public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent bandSkinTemperatureEvent) {
            if(bandSkinTemperatureEvent != null && SensorsPage.skin_flag==true){
                appendToTemperature(String.format("skin temprature= %.3f Celsius ", bandSkinTemperatureEvent.getTemperature()));
                //update variables in databaseThread class
                temperature=bandSkinTemperatureEvent.getTemperature();
            }
        }
    };
    private BandHeartRateEventListener mHeartRateEventListner = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
            if(bandHeartRateEvent != null && SensorsPage.heart_flag==true){
                appendToHeartStatus(String.format("Heart Rate= %03d", bandHeartRateEvent.getHeartRate()));
                //update variables in databaseThread class
                heartRate=bandHeartRateEvent.getHeartRate();
            }
        }
    };

    //uv section
    private BandUVEventListener mUVEventListner = new BandUVEventListener() {
        @Override
        public void onBandUVChanged(BandUVEvent bandUVEvent) {
            if(bandUVEvent != null && SensorsPage.uv_flag==true){
                appendToUV(String.format("UV= %s ", bandUVEvent.getUVIndexLevel()));
                //update variables in databaseThread class
                uv=bandUVEvent.getUVIndexLevel();
            }
        }
    };

    @Override
    public void userAccepted(boolean consentGiven) {
    // handle user's heart rate consent decision
            // check current user heart rate consent
        if(consentGiven==true){
            try {
                client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListner);
            } catch (BandException e) {
                e.printStackTrace();
            }
        }else{
            client.getSensorManager().requestHeartRateConsent(this, this);
        }

    }


    /**
     *
     * @return boolean true means connected , false not coneccted
     * @throws InterruptedException
     * @throws BandException
     *
     * The method the state of the connection between the application and the band
     */
    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        //check if the band client  is null
        if (client == null) {
            //create an instance of the BandClientManger and get the devices paired to the band
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            //check the number of devices connected to the band
            if (devices.length == 0) {
                //if no devices are connected show message and return false
                appendToMainUI("Band isn't paired with your phone.\n");
                return false;
            }
            //else create a Band Client by using the BandClientManager
            //getBaseContext get current context or use this
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
            //else if the device is connected  return true
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }
        //if the band ws just created then this message will be shown to the user
        appendToMainUI("Band is connecting...\n");
        //connect the newly created client to the band
        //and compare the result to the ConnectionState.CONNECTED state
        //await() stops the thread till  the connect is finshed
        return ConnectionState.CONNECTED == client.connect().await();
    }

    private void loadSpinnerData() {
        // Spinner Drop down elements
        List<String> labels = databaseHelper.getLabels();
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        activitiesToTrack.setAdapter(dataAdapter);
    }


//    private void addDrawerItems() {
//        String[] osArray = { "Export data", "Sensors", "Manage labels", "About"};
//        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
//        mDrawerList.setAdapter(mAdapter);
//    }
//
//    private void setupDrawer() {
//        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
//
//            /** Called when a drawer has settled in a completely open state. */
//            public void onDrawerOpened(View drawerView) {
//                super.onDrawerOpened(drawerView);
//                getSupportActionBar().setTitle("Menu");
//                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
//            }
//
//            /** Called when a drawer has settled in a completely closed state. */
//            public void onDrawerClosed(View view) {
//                super.onDrawerClosed(view);
//                getSupportActionBar().setTitle(mActivityTitle);
//                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
//            }
//        };
//
//        mDrawerToggle.setDrawerIndicatorEnabled(true);
//        mDrawerLayout.setDrawerListener(mDrawerToggle);
//    }
//
//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        // Sync the toggle state after onRestoreInstanceState has occurred.
//        mDrawerToggle.syncState();
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        mDrawerToggle.onConfigurationChanged(newConfig);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        // Activate the navigation drawer toggle
//        if (mDrawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}

//todo add uv to database