package com.Indoscan.channelbridge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.Indoscan.channelbridge.R;
import com.Indoscan.channelbridgedb.UserLogin;
import com.Indoscan.channelbridgedb.AutoSyncOnOffFlag;

public class LoginActivity extends Activity{
//
	EditText txtUserName, txtPassword;
	Button btnSignIn, btnCancel;
	AlertDialog alertDialog;
	Builder alertExit;
    Location location;
    private LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		txtUserName = (EditText) findViewById(R.id.etUserName);
		txtPassword = (EditText) findViewById(R.id.etPassword);
		btnSignIn = (Button) findViewById(R.id.bSignIn);
		btnCancel = (Button) findViewById(R.id.bCancel);
		
		txtUserName.setFocusable(true);
		txtUserName.requestFocus();

/**
 * newly added
 */
      /*  if (!new ServiceChecker().isGPSServiceRunning(getApplicationContext())) {
            startService(new Intent(LoginActivity.this, GPSAutoSynchronize.class));
            Log.w("GPS service not running so start it", "=================> OK");
        }*/

		alertDialog = new AlertDialog.Builder(this).create();

		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});

		alertExit = new AlertDialog.Builder(this)
				.setTitle("Alert")
				.setMessage(
						"Are you sure you want to exit?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});

		
		txtUserName.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!txtUserName.getText().toString().isEmpty()) {
					txtPassword.setFocusable(true);
					txtPassword.requestFocus();					
				}
			}
		});
		
		txtPassword.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				login();
			}
		});
		
		
		btnSignIn.setOnClickListener(new OnClickListener() {

			
			public void onClick(View v) {
				login();			

			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertExit.show();
			}
		});
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			alertExit.show();
		}


		return super.onKeyDown(keyCode, event);
	}
	
	public void login() {
		UserLogin userLoginObject = new UserLogin(LoginActivity.this);
		// userLoginObject.op
		// enWritableDatabase();
		
		if (txtUserName.getText().toString().contentEquals("Admin")) {
			if (txtPassword.getText().toString().contentEquals("Admin@")) {
//			if (txtPassword.getText().toString().contentEquals("a")) {
				Intent adminIntent = new Intent(LoginActivity.this, AdministratorPreference.class);
				finish();
				startActivity(adminIntent);
				
			}
			
		} else {
			userLoginObject.openReadableDatabase();
			int status = userLoginObject.isUseractive(txtUserName.getText().toString());
			userLoginObject.closeDatabase();
			
			switch (status) {

			case 0:

				alertDialog.setTitle("Warning");
				alertDialog.setMessage("Username is not valid");
				alertDialog.show();
				break;

			case 1:
				try {
					UserLogin userLogin = new UserLogin(LoginActivity.this);
					userLogin.openReadableDatabase();
					String[] userDetails = userLogin.getUserDetailsByUserName(txtUserName.getText().toString());
					userLogin.closeDatabase();




					if (userDetails[1] != null) {
						String enteredPassword = new Utility(LoginActivity.this).encryptString(txtPassword.getText()
										.toString());

						if (enteredPassword.contentEquals(userDetails[1])) {


                            AutoSyncOnOffFlag autoSyncOnOffFlag =new AutoSyncOnOffFlag(LoginActivity.this);
                            autoSyncOnOffFlag.openReadableDatabase();
                            autoSyncOnOffFlag.AutoSyncActive(0);
                            // String  dbStatus=autoSyncOnOffFlag.GetAutoSyncStatus();
                            autoSyncOnOffFlag.closeDatabase();
                           // turnGPSOn();





							Intent iternaryListActivity = new Intent(
							"com.Indoscan.channelbridge.ITINERARYLIST");
							Bundle bundleToView = new Bundle();
							bundleToView.putString("DeviceId", userDetails[2]);
							bundleToView.putString("RepId", userDetails[3]);
							bundleToView.putString("UserLogin", userDetails[4]);
							iternaryListActivity.putExtras(bundleToView);
							finish();
							startActivity(iternaryListActivity);
							
						}

						else {
							alertDialog.setTitle("Warning");
							alertDialog
									.setMessage("Password and Username do not match!");
							alertDialog.show();
						}
					} else {
						alertDialog.setTitle("Error");
						alertDialog.setMessage("Cannot retrive your password please contact Administrator");
						alertDialog.show();

					}

				} catch (Exception e) {
					alertDialog.setTitle(R.string.error);
					alertDialog.setMessage(e.toString());
					alertDialog.show();
					e.printStackTrace();
				}

				break;

			case 2:

				alertDialog.setTitle("Warning");
				alertDialog.setMessage("User Locked!");
				alertDialog.show();
				break;

			case 3:

				alertDialog.setTitle("Warning");
				alertDialog
						.setMessage("Cannot Identify the user status please contact Administrator!");
				alertDialog.show();
				break;
			case 4:

				alertDialog.setTitle("Error");
				alertDialog.setMessage("Oops! Looks Like something went wrong. Please try again or contact System Administrator");
				alertDialog.show();
				break;

			}
		}
	}


    private void turnGPSOn()
    {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!provider.contains("gps")) { //if gps is disabled
            final Intent poke = new Intent();
           poke.setClassName("com.android.settings", "com.android.settings.locationserices.UseGpssatellites");
           poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);


        /*    Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
            intent.putExtra("enabled", true);
            sendBroadcast(intent);*/




        }
    }


}
