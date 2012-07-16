package org.myftp.kss;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * 
 * @author John Main class of bus4soton
 */
public class SotonBus extends MapActivity {

	/**
	 * Sqlite connection instance
	 */
	SQLiteDatabase dbBuses;

	// From google map api
	MapView mapView;
	MapController mymapController;
	Location location;
	LocationManager loctionManager;
	Criteria criteria;

	// Pre-prepared Dialog
	AlertDialog GPSAlert;
	AlertDialog dlgRoute;
	AlertDialog.Builder editionDialog;

	LocationPoint pointMe;
	Intent itMyFavor;
	Drawable drawable;

	// Used in dialog of initRoute
	String[] areaIds;
	String[] areaNames;
	boolean[] areaState;

	String provider;
	int GPSStatus;
	boolean doingLocation = false;
	boolean GPSinitial = false;
	boolean routeInitailed = false;
	boolean isDebug = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Some Initial operate like initial mapView, GPS, Routes

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		pointMe = new LocationPoint(this.getResources().getDrawable(
				R.drawable.marker_green));

		drawable = this.getResources().getDrawable(R.drawable.bus);
		mymapController = mapView.getController();

		// Display point in the center of screen
		GeoPoint point = new GeoPoint(50936263, -1396830);
		mymapController.setCenter(point);
		mymapController.setZoom(16);

		// Init MyFavor Intent
		itMyFavor = new Intent();
		itMyFavor.setClass(SotonBus.this, MyFavor.class);
		editionDialog = new AlertDialog.Builder(this);
		initGps();

		final StationItemizedOverlay itemizedoverlay = new StationItemizedOverlay(
				drawable, SotonBus.this);

		/**
		 * Initial all station asynchronous and when it is finished sent message
		 * to handel
		 */
		new Thread() {
			public void run() {
				List<StationBean> stations = getStations();
				itemizedoverlay.addOverlay(stations);
				mapView.getOverlays().add(itemizedoverlay);
				hander.sendEmptyMessage(1);
			}
		}.start();

	}

	@Override
	protected void onResume() {
		/**
		 * onResume is is always called after onStart, even if the app hasn't
		 * been paused
		 * 
		 * add location listener and request updates every 1000ms or 10m
		 */

		provider = loctionManager.getBestProvider(criteria, true);
		if (provider != null) {
			if (provider.equals(LocationManager.NETWORK_PROVIDER))
				GPSStatus = 0;
			else if (provider.equals(LocationManager.GPS_PROVIDER))
				GPSStatus = 1;
			// Listen on time 2000ms or distance changes greater than 10 meters
			loctionManager.requestLocationUpdates(provider, 2000, 10,
					locationListener);
		} else {
			pointMe.mOverlays.clear();
			Toast.makeText(
					SotonBus.this,
					"GPS service is not available, Open GPS can helps you to fetch location more accurately!",
					Toast.LENGTH_SHORT).show();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		/** GPS, as it turns out, consumes battery like crazy */
		loctionManager.removeUpdates(locationListener);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		loctionManager.removeUpdates(locationListener);
		if (dbBuses != null) {
			dbBuses.close();
		}
		super.onDestroy();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * Get Displayed Bus Stations List from database
	 * 
	 * @return
	 */
	public List<StationBean> getStations() {
		List<StationBean> result = new ArrayList<StationBean>();
		dbBuses = openDatabase();
		Log.i("dbBuses State in getStations", dbBuses+"");
		Cursor cursor = dbBuses
				.rawQuery(
						"select buses.bid, buses.bname, buses.GLatitude, buses.Glongitude "
								+ "from routestop inner join routes on routestop.rsid = routes.rid "
								+ "inner join buses on routestop.bid = buses.bid "
								+ "where routes.isDeleted=0", new String[] {});

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			result.add(new StationBean(cursor.getString(0),
					cursor.getString(1), cursor.getInt(2), cursor.getInt(3)));
			cursor.moveToNext();
		}
		cursor.close();
		dbBuses.close();
		return result;
	}

	/**
	 * Initial database or open get the SqliteDatabase interface
	 * 
	 * @return SQLiteDatabase interface
	 */
	private SQLiteDatabase openDatabase() {

		SQLiteDatabase database;
		try {
			String databaseFilename = Utility.FULLPATH;
			File dir = new File(Utility.DATABASE_PATH);

			if (!dir.exists())
				dir.mkdir();

			if (!(new File(databaseFilename)).exists()) {
				copyDatabase();
			} else // have database file
			{
				byte[] bytes;
				if (new File(this.getFilesDir(), "dbVersion").exists()) {
					RandomAccessFile f = new RandomAccessFile(new File(
							this.getFilesDir(), "dbVersion"), "r");
					bytes = new byte[(int) f.length()];
					f.readFully(bytes);
					f.close();

					String Oldver = new String(bytes);

					Log.i("equl", (Oldver.equals(Utility.curretnDbVersion))
							+ "");
					if (!Oldver.equals(Utility.curretnDbVersion)) {
						(new File(databaseFilename)).delete();
						copyDatabase();
					}
				} else {
					(new File(databaseFilename)).delete();
					copyDatabase();
				}

			}

			database = SQLiteDatabase.openOrCreateDatabase(Utility.FULLPATH,
					null);

			return database;
		} catch (Exception e) {
			Log.i("Exception on openDatabase", e.toString());
		}
		return null;
	}

	/**
	 * Initial database when installing
	 * 
	 * @throws IOException
	 */
	public void copyDatabase() throws IOException {
		String databaseFilename = Utility.FULLPATH;
		InputStream is = getResources().openRawResource(R.raw.busespro);
		FileOutputStream fos = new FileOutputStream(databaseFilename);
		byte[] buffer = new byte[8192];
		int count = 0;
		while ((count = is.read(buffer)) > 0) {
			fos.write(buffer, 0, count);
		}
		fos.close();
		is.close();
		File installation = new File(this.getFilesDir(), "dbVersion");
		FileOutputStream out = new FileOutputStream(installation);
		String id = Utility.curretnDbVersion;
		out.write(id.getBytes());
		out.close();
	}

	/**
	 * Create menu
	 */
	public boolean onCreateOptionsMenu(Menu aMenu) {

		super.onCreateOptionsMenu(aMenu);
		aMenu.add(0, Menu.FIRST, 0, "Locate Me");
		aMenu.add(0, Menu.FIRST + 1, 0, "FavorList");
		aMenu.add(0, Menu.FIRST + 2, 0, "Route");
		aMenu.add(0, Menu.FIRST + 3, 0, "About");
		if (isDebug)
			aMenu.add(0, Menu.FIRST + 4, 0, "Debug");

		return true;

	}

	/**
	 * Menu event
	 */
	public boolean onOptionsItemSelected(MenuItem aMenuItem) {

		switch (aMenuItem.getItemId()) {
		case Menu.FIRST:
			if (doingLocation == true)
				return true;
			doLocationMe();
			break;

		case Menu.FIRST + 1:
			startActivity(itMyFavor);
			break;
		case Menu.FIRST + 2:
			if (!routeInitailed) {
				AlertDialog.Builder m2dialog = new AlertDialog.Builder(this);

				m2dialog.setTitle("Bus4Soton");
				m2dialog.setIcon(R.drawable.logo64); 
				m2dialog.setMessage("Routes is initialing, please try again later!");
				m2dialog.show();
			} else
				dlgRoute.show();
			break;
		case Menu.FIRST + 3:
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Bus4Soton");
			dialog.setIcon(R.drawable.logo64);
			dialog.setPositiveButton("Please give me a review!",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface,
								int i) {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse("market://details?id="
									+ getPackageName()));
							startActivity(intent);
						}
					});
			dialog.setMessage("This application was created by Lin Yongkai\nEmail:swe06030@gmail.com\n"
					+ "The Real-time information was provided by "
					+ "\nUniversity of Southampton "
					+ "\nand "
					+ "Southampton City Council ROMANSE office."
					+ "\nHowever, it is not an official Application From either of them.\n");
			dialog.show();
			break;
		case Menu.FIRST + 4:
			AlertDialog.Builder debugdialog = new AlertDialog.Builder(this);

			debugdialog.setTitle("Bus4Soton");
			debugdialog.setIcon(R.drawable.logo64);
			debugdialog.setMessage("Database version: "
					+ new Utility().getDbVersion() + "\n App version: "
					+ new Utility().getAppVersion());
			debugdialog.show();
			break;

		}
		return super.onOptionsItemSelected(aMenuItem);

	}

	/**
	 * locate user and set Mapzoom
	 */
	public void doLocationMe() {
		doingLocation = true;
		provider = loctionManager.getBestProvider(criteria, true);
		if (provider != null) {
			loctionManager.requestLocationUpdates(provider, 2000, 10,
					locationListener);
			if (provider.equals(LocationManager.NETWORK_PROVIDER))
				GPSStatus = 0;
			else if (provider.equals(LocationManager.GPS_PROVIDER))
				GPSStatus = 1;
			if (this.getCurrentGeoPoint() == null) {
				Toast.makeText(
						SotonBus.this,
						"Sotonbus is tring to locate your position, please wait...!",
						Toast.LENGTH_SHORT).show();
			} else {
				pointMe.addOverlay(new OverlayItem(this.getCurrentGeoPoint(),
						"You are here", ""));
				mapView.getOverlays().add(pointMe);
				this.mymapController.setZoom(16);
				this.mymapController.animateTo(this.getCurrentGeoPoint());

				;
				if (GPSStatus == 0) {
					Toast.makeText(
							SotonBus.this,
							"GPS service is not available, Open GPS can helps you to fetch location more accurately!",
							Toast.LENGTH_SHORT).show();
				}
			}
			doingLocation = false;

		} else {
			Toast.makeText(SotonBus.this,
					"Location service not available, please Open it!",
					Toast.LENGTH_SHORT).show();
			pointMe.mOverlays.clear();
			GPSAlert.show();
			doingLocation = false;
		}
	}

	/**
	 * Convert Current location to GeoPoint
	 * 
	 * @return Current GeoPoint
	 */
	public GeoPoint getCurrentGeoPoint() {
		if (location == null)
			return null;
		return new GeoPoint((int) (location.getLatitude() * 1e6),
				(int) (location.getLongitude() * 1e6));
	}

	/**
	 * Initial Route selection dialog
	 */
	public void initRoute() {

		dbBuses = openDatabase();
		Cursor cursor = dbBuses.rawQuery(
				"select rid, rcode, rlabel, isDeleted from routes",
				new String[] {});
		cursor.moveToFirst();
		int length = cursor.getCount();
		areaIds = new String[length];
		areaNames = new String[length];
		areaState = new boolean[length];
		int cursorIndex = 0;
		while (!cursor.isAfterLast()) {
			areaIds[cursorIndex] = cursor.getString(0);
			areaNames[cursorIndex] = cursor.getString(1) + " : "
					+ cursor.getString(2);
			areaState[cursorIndex] = cursor.getInt(3) < 1; // if isDelete=true
															// return false
			cursorIndex++;
			cursor.moveToNext();
		}
		cursor.close();
		dbBuses.close();

		dlgRoute = new AlertDialog.Builder(SotonBus.this)
				.setTitle("Select Routes for display in the map")
				.setMultiChoiceItems(areaNames, areaState,
						new DialogInterface.OnMultiChoiceClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton, boolean isChecked) {

							}
						})
				.setPositiveButton("Confirm",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String result = "'";
								for (int i = 0; i < areaIds.length; i++) {
									if (areaState[i] == true) {
										result = result + areaIds[i].toString()
												+ "','";
									}
								}
								if (result.length() > 1)
									result = (String) result.subSequence(0,
											result.length() - 2);
								// if(!dbBuses.isOpen())
								// dbBuses=SQLiteDatabase.openOrCreateDatabase(Utility.FULLPATH,
								// null);
								dbBuses = openDatabase();
								dbBuses.execSQL("update routes set isDeleted = 1");
								if (result.length() > 1)
									dbBuses.execSQL("update routes set isDeleted = 0 where rid in ("
											+ result + ")");
								dbBuses.close();
								mapView.getOverlays().clear();

								StationItemizedOverlay itemizedoverlay = new StationItemizedOverlay(
										drawable, SotonBus.this);
								;

								List<StationBean> stations = getStations();
								itemizedoverlay.addOverlay(stations);
								mapView.getOverlays().add(itemizedoverlay);
								updateWithNewLocation(location);
								mapView.invalidate();// Re Draw
								dialog.dismiss();
							}
						}).setNegativeButton("Cancel", null).create();
		routeInitailed = true;
	}

	/**
	 * Inition Gps variable
	 */
	public void initGps() {
		String contextService = Context.LOCATION_SERVICE;
		loctionManager = (LocationManager) getSystemService(contextService);

		// obtain GPS service from system
		// String provider=LocationManager.GPS_PROVIDER;
		// Location location = loctionManager.getLastKnownLocation(provider);

		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);// 高精度
		criteria.setAltitudeRequired(false);// 不要求海拔
		criteria.setBearingRequired(false);// 不要求方位
		criteria.setCostAllowed(true);// 允许有花费
		criteria.setPowerRequirement(Criteria.POWER_LOW);// 低功耗

		provider = loctionManager.getBestProvider(criteria, true);

		if (provider != null) {
			if (provider.equals(LocationManager.NETWORK_PROVIDER))
				GPSStatus = 0;
			else if (provider.equals(LocationManager.GPS_PROVIDER))
				GPSStatus = 1;
			loctionManager.requestLocationUpdates(provider, 2000, 10,
					locationListener);
		} else {
			pointMe.mOverlays.clear();
			Toast.makeText(
					SotonBus.this,
					"GPS service is not available, Open GPS can helps you to fetch location more accurately!",
					Toast.LENGTH_SHORT).show();
		}
		GPSinitial = true;
	}

	/**
	 * Initial LocationListener
	 */
	private final LocationListener locationListener = new LocationListener() {
		@Override
		public void onProviderDisabled(String provider) {
			/* this is called if/when the GPS is disabled in settings */
			/* bring up the GPS settings */
			Toast.makeText(
					SotonBus.this,
					"GPS service is not available, Open GPS can helps you to fetch location more accurately!",
					Toast.LENGTH_SHORT).show();
			GPSStatus = 0;
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(SotonBus.this, "GPS Available Now",
					Toast.LENGTH_SHORT).show();
			GPSStatus = 1;

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			/* This is called when the GPS status alters */
			switch (status) {
			case LocationProvider.OUT_OF_SERVICE:

				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:

				break;
			case LocationProvider.AVAILABLE:

				break;
			}
		}

		@Override
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}
	};

	/**
	 * Update value of location variable
	 * 
	 * @param location
	 */
	private void updateWithNewLocation(Location location) {
		this.location = location;
		if (location == null) {
			Toast.makeText(SotonBus.this,
					"Location service not available, please try later!",
					Toast.LENGTH_SHORT).show();
		} else {
			if (pointMe.mOverlays.size() == 0) {
				pointMe.addOverlay(new OverlayItem(this.getCurrentGeoPoint(),
						"You are here", ""));
				mapView.getOverlays().add(pointMe);
				this.mymapController.setZoom(16);
				this.mymapController.animateTo(this.getCurrentGeoPoint());

				;
			}

		}
	}

	/**
	 * Initial GPS Dialog
	 */
	public void initGPSEnaber() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Would you like to open Location service now?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent intent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(intent);
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		GPSAlert = builder.create();

	}

	private Handler hander = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				initRoute();
				// create gps enaber dialog but not showed immediate
				initGPSEnaber(); 

				/**
				 * obtain edition and compared with the newest edition from service
				 * if the APP is not newest pop update message to user
				 */
				String edition = Utility
						.getUrl("http://users.ecs.soton.ac.uk/yl25e11/busVersion.php");

				Log.i("edition", edition);
				if (edition != null && edition.length() > 0) {
					int old = Integer.parseInt(Utility.currentAppVersion);
					int newVersion = Integer.parseInt(edition.split("\n")[0]);
					if (old < newVersion) {

						editionDialog.setTitle("New version available!");
						editionDialog.setIcon(R.drawable.logo64);
						editionDialog.setPositiveButton("Update!",
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialoginterface,
											int i) {
										Intent intent = new Intent(
												Intent.ACTION_VIEW);
										intent.setData(Uri
												.parse("market://details?id="
														+ getPackageName()));
										startActivity(intent);
									}
								});
						editionDialog
								.setMessage("New version available! Please update to obtain new feature!");
						editionDialog.show();
					}
				}
				break;

			}

		}

	};

}