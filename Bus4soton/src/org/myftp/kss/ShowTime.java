package org.myftp.kss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * To show the real time information on a List
 */
public class ShowTime extends ListActivity {
	SQLiteDatabase dbBuses;
	String bid = "";	
	String val;
	String url;
	
	Map<String, Object> tMap;
	List<Map<String, Object>> tList;	
	
	SimpleAdapter tAdapter;
	Menu theMenu;
	Bundle bundle;
	ProgressDialog progressDialog;	
	AlertDialog.Builder alert;

	private Handler hander = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				JSONObject jsonObject;
				JSONArray jsonArray;
				try {
					jsonObject = new JSONObject(val);
					jsonArray = jsonObject.getJSONArray("stops");
					tList.clear();
					tMap = new HashMap<String, Object>();
					tMap.put("name", "BUS");
					tMap.put("timelist", bundle.getString("bname"));
					tList.add(tMap);
					for (int i = 0; i < jsonArray.length(); i++) {
						tMap = new HashMap<String, Object>();
						tMap.put("name",
								jsonArray.getJSONObject(i).getString("name"));
						tMap.put("timelist", jsonArray.getJSONObject(i)
								.getString("time"));
						tList.add(tMap);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (tList.size() == 1) {
					tMap = new HashMap<String, Object>();
					tMap.put("name", "No bus");
					tMap.put("timelist",
							"Sorry there is no bus or the real-time service is unavailable now");
					tList.add(tMap);
				}

				setListAdapter(tAdapter);
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bundle = new Bundle();
		bundle = this.getIntent().getExtras();
		bid = bundle.getString("bid");
		String[] tString = new String[] { "name", "timelist" };
		int[] tInt = new int[] { R.id.ItemName, R.id.ItemText };
		tList = new ArrayList<Map<String, Object>>();
		tAdapter = new SimpleAdapter(this, tList, R.layout.showtime, tString,
				tInt);
		dbBuses = SQLiteDatabase.openOrCreateDatabase(Utility.FULLPATH, null);

		initList();

		getListView().setOnCreateContextMenuListener(this);

		// device unique id
		String uid = Installation.id(this); 
		String GETURL = "http://users.ecs.soton.ac.uk/yl25e11/bus.php?bid=";
		url = GETURL + bid + "&uid=" + uid + "&ver="
				+ new Utility().getAppVersion();
		progressDialog = ProgressDialog.show(ShowTime.this, "", "Loading...");
		progressDialog.setCancelable(true);
		new Thread() {
			public void run() {
				val = Utility.getUrl(url);
				hander.sendEmptyMessage(0);
				progressDialog.dismiss();
			}
		}.start();

		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Toast.makeText(ShowTime.this,
						"Long click can copied Timetable info to clipboard",
						Toast.LENGTH_SHORT).show();
			}
		});

		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

				Map<String, Object> tMap = (Map<String, Object>) getListView()
						.getItemAtPosition(position);

				clipboard.setText(tMap.get("name").toString() + " "
						+ tMap.get("timelist").toString());// …Ë÷√Clipboard µƒƒ⁄»›
				Toast.makeText(ShowTime.this,
						"Timetable info copied to clipboard successed",
						Toast.LENGTH_SHORT).show();
				// Return true to consume the click event. In this case the
				// onListItemClick listener is not called anymore.
				return true;
			}
		});

	}

	@Override
	protected void onResume() {
		initList();
		progressDialog.show();
		new Thread() {
			public void run() {
				val = Utility.getUrl(url);
				hander.sendEmptyMessage(0);
				progressDialog.dismiss();
			}
		}.start();
		super.onResume();
	}

	/**
	 * Create Menu
	 */
	public boolean onCreateOptionsMenu(Menu aMenu) {
		theMenu = aMenu;
		super.onCreateOptionsMenu(aMenu);
		Cursor cursor = dbBuses.query("buses", new String[] { "isFavor,bid" },
				"bid=?", new String[] { bid }, null, null, null);
		cursor.moveToFirst();
		if (cursor.getString(0).equals("0"))
			aMenu.add(0, Menu.FIRST, 0, "Send to FavorList");
		else
			aMenu.add(0, Menu.FIRST, 0, "Withdraw from favorList");
		aMenu.add(0, Menu.FIRST + 1, 0, "Refresh");

		cursor.close();
		return true;

	}

	/**
	 * Pop remark dialog which replace a specify name to a station
	 */
	public void popRemark() {
		alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		input.setText(bundle.getString("bname"));
		alert.setTitle("Set an remarkably title for the station");
		alert.setView(input);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dbBuses.execSQL(
						"update buses set isFavor = '1', remark= ? where bid=?",
						new String[] { input.getText().toString().trim(), bid });
				theMenu.getItem(0).setTitle("Withdraw from FavorList");
				Toast.makeText(ShowTime.this, "Sent to favorList success!",
						Toast.LENGTH_SHORT).show();
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
		alert.show();
	}

	/**
	 * Menu event
	 */
	public boolean onOptionsItemSelected(MenuItem aMenuItem) {

		switch (aMenuItem.getItemId()) {
		case Menu.FIRST:
			try {
				if (theMenu.getItem(0).toString().equals("Send to FavorList")) {
					popRemark();
				} else {
					dbBuses.execSQL("update buses set isFavor = '0' where bid='"
							+ bid + "'");
					theMenu.getItem(0).setTitle("Send to FavorList");
					Toast.makeText(ShowTime.this,
							"Withdraw from favorList success!",
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception ex) {
				Toast.makeText(ShowTime.this, "Operate failed!",
						Toast.LENGTH_SHORT).show();
			}
			break;

		case Menu.FIRST + 1:
			progressDialog.show();
			new Thread() {
				public void run() {
					val = Utility.getUrl(url);
					hander.sendEmptyMessage(0);
					progressDialog.dismiss();
				}
			}.start();
			break;

		}
		return super.onOptionsItemSelected(aMenuItem);

	}

	/**
	 * Initial list
	 */
	public void initList() {
		tList.clear();
		tMap = new HashMap<String, Object>();
		tMap.put("name", "BUS");
		tMap.put("timelist", bundle.getString("bname"));
		tList.add(tMap);
		tMap = new HashMap<String, Object>();
		tMap.put("name", "BusList");
		tMap.put("timelist", "Loading...");
		tList.add(tMap);
		setListAdapter(tAdapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbBuses != null) {
			dbBuses.close();
		}
	}

}