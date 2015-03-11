package com.example.accelerometerapplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

import android.support.v7.app.ActionBarActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

	private SensorManager senSensorManager;
	private Sensor senAccelerometer;
	private Sensor senCompass;
	
	private long lastUpdate = 0;
	private long lastUpdateMag = 0;
	//private float last_x, last_y, last_z;
	//private float mLastValues[] = new float[10];
	//private static final int SHAKE_THRESHOLD = 600;
	private boolean record = false;
	private String accelData = " ";
	private float accelerationVal = 0;
	private float maxVal = 0.75f;
	private float minVal = -0.75f;
	//private int mNumMaxs =0;
	private int k;
	private long stepDetecTime;
	private long stepEndTime;
	private float stepTimeDiff;

	
	//MAGNETIC FIELD VALUES
	private float lastHeadingNorth = 0;
	private float lastHeadingWest = 0;
	private int turns = 0;
	private int mDegTurn = 8;
	private int mCheckSensorDelay = 1000;
	private TextView tvHeading;
	

	private int mNumSteps = 0;
	//private int index =0; 
	private ArrayList<Float> mLastValues = new ArrayList<Float>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		 	senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		    senCompass = senSensorManager
					.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			senSensorManager.registerListener(this, senCompass,
					SensorManager.SENSOR_DELAY_NORMAL);
		    senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		/*if (id == R.id.action_settings) {
			return true;
		}*/
		
		return super.onOptionsItemSelected(item);
	}

	protected void onPause() {
	    super.onPause();
	    senSensorManager.unregisterListener(this);
	}
	protected void onResume() {
	    super.onResume();
	    senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
	    senSensorManager.registerListener(this, senCompass, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	public void btnReset_OnClick(View view){
		//Button btnReset = (Button)findViewById(R.id.btnReset);
		turns = 0;
		TextView tv4 = (TextView)findViewById(R.id.txtCorner);
		tv4.setText("Number of Turns: " + String.valueOf(turns));
		mNumSteps = 0;
		TextView tvStep = (TextView)findViewById(R.id.textViewCounter);
		tvStep.setText("Steps: " + String.valueOf(mNumSteps));
		maxVal = 0.75f;
		minVal = -0.75f;
	}
	public void btnRecord_OnClick(View view){
		if(record == false){
			record = true;
		}else{
			record = false;
			WriteData(accelData);
		}
		
		Button btn = (Button) findViewById(R.id.btnRecord);
		
		
		if(record == false){
			btn.setText("Record Data");
		}else{
			btn.setText("Stop Recording");
		}
		
		
		
	}
	public void WriteData(String data){
		String filename = "filename.txt";
		File file = new File(Environment.getExternalStorageDirectory(), filename);
		FileOutputStream fos;
		
		byte[] data2 = data.getBytes();
		try {
		    fos = new FileOutputStream(file);
		    fos.write(data2);
		    fos.flush();
		    fos.close();
		    Toast toast = Toast.makeText(getApplicationContext(), "Saved File Success", Toast.LENGTH_LONG);
		    toast.show();

		} catch (FileNotFoundException e) {
		    // handle exception
			Toast.makeText(getApplicationContext(), "File Not Found", Toast.LENGTH_LONG).show();
		} catch (IOException e) {
		    // handle exception
			Toast.makeText(getApplicationContext(), "IO Error", Toast.LENGTH_LONG).show();
		}
	}
	public void onAccelerationChanged(float x, float y, float z){
		TextView labelX = (TextView)findViewById(R.id.lableXView);
		TextView labelY = (TextView)findViewById(R.id.labelYVal);
		TextView labelZ = (TextView)findViewById(R.id.labelZView);
		TextView labelAccel = (TextView)findViewById(R.id.txtAcceleration);

		
		labelX.setText(String.valueOf(x));
		labelY.setText(String.valueOf(y));
		labelZ.setText(String.valueOf(z));
		labelAccel.setText("Acceleration: " + String.valueOf(accelerationVal));
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor mySensor = event.sensor;
		 long curTime = System.currentTimeMillis();
		 
		 /**THE HEADING CHANGE PART**/
		 synchronized (this){
		if(mySensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
			Log.d("MAG_FIELD", "ITS A MAG FIELD");
			float degree1 = Math.round(event.values[0]);
			float degree2 = Math.round(event.values[2]);
			tvHeading = (TextView)findViewById(R.id.txtHeading);
			float testDeg = Math.round((event.values[0] * event.values[1]));
			tvHeading.setText("Test Heading: " + String.valueOf(testDeg)); 
			//tvHeading.setText("Heading North: " + String.valueOf(degree1) + "Heading Y: " + String.valueOf(degree2));
			TextView tvLastHead = (TextView)findViewById(R.id.tvLastHead);
			tvLastHead.setText("Last HeadingNorth: " + lastHeadingNorth + "Last Heading Roll: " + lastHeadingWest); 
			if ((curTime - lastUpdateMag) > mCheckSensorDelay) {
				Log.d("MAG_FIELD", "CHECKING IF CURTIME - LASTUPDATE");
				lastUpdateMag = curTime;
				if (lastHeadingNorth + mDegTurn <= degree1 || lastHeadingNorth - mDegTurn >= degree1) {
					turns++;
					lastHeadingNorth = degree1;
					lastHeadingWest = degree2;
				}else if(lastHeadingWest + 15 <= degree2 || lastHeadingWest - 15 >= degree2) {
					turns++;
					lastHeadingNorth = degree1;
					lastHeadingWest = degree2;
				}
				else {
					//lastHeading = degree1;
				}
			}
			TextView tv4 = (TextView)findViewById(R.id.txtCorner);
			tv4.setText("Number of Turns: " + String.valueOf(turns));
		}
		
		
		/**THE ACCELEROMETER PART**/
		else if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
		        float x = event.values[0];
		        float y = event.values[1];
		        float z = event.values[2];
		 
		       
		        float flotsam = (float) 0;
		        if ((curTime - lastUpdate) > 150) {
		            long diffTime = (curTime - lastUpdate);
		            lastUpdate = curTime;
		            accelerationVal = (float) (Math.sqrt((x*x) + (y*y) + (z*z)) - (float)9.8);
		            onAccelerationChanged(x,y,z);
		            int test = mLastValues.size();
		            Log.d("test Value", "Num: " + test);
		            mLastValues.add(accelerationVal);
		            
		            for(int i=0;i<mLastValues.size();i++){
		            	if(mLastValues.get(i) >= maxVal){
		            		Log.d("Location","> maxVal");
		            		//stepDetecTime = System.currentTimeMillis();
		            		stepDetecTime = System.nanoTime();
		            		//arrayChecker();
		            		for(int j=i;j< mLastValues.size();j++){
		            			if(mLastValues.get(j)<= minVal){
		            				Log.d("Location2", "<=minVal");
		            				//arrayChecker();
		            				for(k=j; k< mLastValues.size();k++){
		            					if(mLastValues.get(k)>=maxVal){
		            						//STEP DETECTED
		            						Log.d("STEP_MAX","A maximum step has been acheived");
		            						//stepEndTime = System.currentTimeMillis();
		            						stepEndTime = System.nanoTime();
		            						stepCounter();
		            						arrayDealer();
		            						//arrayChecker();
		            					}
		            				}
		            			}
		            		}
		            	}else if(mLastValues.get(i) <= minVal){
		            		Log.d("Location","< minVal");
		            		//stepDetecTime = System.currentTimeMillis();
		            		stepDetecTime = System.nanoTime();
		            		for(int j=i;j<mLastValues.size();j++){
		            			if(mLastValues.get(j)>=maxVal){
		            				//arrayChecker();
		            				for(k=j;k<mLastValues.size();k++){
		            					if(mLastValues.get(k)<=minVal){
		            						//arrayChecker();
		            						//stepEndTime = System.currentTimeMillis();
		            						stepEndTime = System.nanoTime();
		            						Log.d("STEP_MIN","A minimum step has been acheived");
		            						arrayDealer();
		            						stepCounter();
		            						
		            					}
		            				}
		            			}
		            		}
		            	}
		            }
		            
		            /**Recording acceleration data point **/
		            /*if(record == true){
		            	
		            		accelData += x + "," + y +","+ z + "," + accelerationVal+";"; 
						
		            }*/
		        }
		        if(mLastValues.size() >= 25){
		        	k=1;
		        	arrayDealer();
		        	}

		       // index++;
		    }
		 }

		}
	
	
	private void arrayDealer(){
		Log.d("arrayDealer", "Got to ArrayDealer");
		
		for(int i = k ;i< mLastValues.size();i++){
			mLastValues.remove(i);
		}
		
		String values = "Values: ";
		for(int i=0;i<mLastValues.size();i++){
			values += " " + mLastValues.get(i);
		}
		Log.d("ArrayVals",values);
	}
	
	
	/**For testing purposes*/
	private void arrayChecker(){
		String values = "Values: ";
		for(int i = 0; i<mLastValues.size();i++){
			values += " " + mLastValues.get(i);
		}
		Log.d("ArrayVals_ArrayChecker", values);
	}
	
	private void stepCounter(){
		
		
		stepTimeDiff = (float)(stepEndTime - stepDetecTime);
		if(stepTimeDiff < 200000){
			mNumSteps++;
		}
		Log.d("Step Time Diff", String.valueOf(stepTimeDiff));
		//stepTimeDiff = stepTimeDiff/1000;
		/**Recording Step Time Differences**/
		 if(record == true){
         	accelData += stepTimeDiff +","; 
		 }
		 if(stepTimeDiff > 1)
		if(stepTimeDiff >= 60000 && stepTimeDiff <= 92000){
			//THIS SHOULD BE THE OPTIMUM POINT? (OR MY HEIGHT??)
			maxVal = 0.75f;
			minVal = -0.75f;
		}
		else if(stepTimeDiff < 60000 && maxVal < 1.5f){//Some random value, will need to actually get data first
			//maxVal = maxVal + 0.1f;
			//minVal = minVal - 0.1f;
			maxVal = 0.9f;
			minVal = -0.9f;
		}else if(stepTimeDiff > 92000 && minVal > -1.5f){//Some random value, will need to actually get data first
			//maxVal = maxVal - 0.1f;
			//minVal = minVal + 0.1f;
			maxVal = 0.65f;
			minVal = -0.65f;
		}
		TextView tvStep = (TextView)findViewById(R.id.textViewCounter);
		tvStep.setText("Steps: " + String.valueOf(mNumSteps));
	}
}
