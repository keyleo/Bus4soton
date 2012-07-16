package org.myftp.kss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * Class of My Favorite List
 */
public class MyFavor extends ListActivity {
	SQLiteDatabase dbBuses;
	Intent itShowTime;
	SimpleAdapter tAdapter;
	Map<String, Object> tMap;
	List<Map<String, Object>> tList;

	@Override
	protected void onResume() {
		loadList();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbBuses != null) {
			dbBuses.close();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getListView().setOnCreateContextMenuListener(this);
		dbBuses = SQLiteDatabase.openOrCreateDatabase(Utility.FULLPATH, null);

		// set divider's style
		int[] colors = { 0, 0xFF006400, 0 };
		getListView().setDivider(
				new GradientDrawable(Orientation.RIGHT_LEFT, colors));
		getListView().setDividerHeight(2);

		itShowTime = new Intent();
		itShowTime.setClass(MyFavor.this, ShowTime.class);

		tList = new ArrayList<Map<String, Object>>();
		tMap = new HashMap<String, Object>();

		// call loadList function to load myfavor list
		loadList();

		//To show the real time information
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				ListView listView = (ListView) arg0;
				Map<String, Object> tMap = (Map<String, Object>) listView.getItemAtPosition(arg2);
				Bundle bundle = new Bundle();
				if (tMap.get("id").toString().equals("-1"))
					return;
				bundle.putString("bid", tMap.get("id").toString());
				bundle.putString("bname", tMap.get("name").toString());

				itShowTime.putExtras(bundle);
				startActivity(itShowTime);

			}
		});

	}

	/**
	 * Create Menu
	 */
	public boolean onCreateOptionsMenu(Menu aMenu) {
		super.onCreateOptionsMenu(aMenu);
		aMenu.add(0, Menu.FIRST + 0, 0, "Refresh");
		return true;
	}

	/**
	 * Menu items click event
	 */
	public boolean onOptionsItemSelected(MenuItem aMenuItem) {

		switch (aMenuItem.getItemId()) {
		case Menu.FIRST:
			loadList();
		}
		return super.onOptionsItemSelected(aMenuItem);
	}

	/**
	 * Load stations in marked as favor station
	 */
	public void loadList() {
		String[] tString = new String[] { "id", "name", "img" };
		int[] tInt = new int[] { R.id.ItemId, R.id.ItemText, R.id.img };
		tList.clear();

		Cursor cursor = dbBuses.query("buses", new String[] { "bid", "remark",
				"GLatitude", "Glongitude" }, "isFavor=?", new String[] { "1" },
				null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			tMap = new HashMap<String, Object>();
			tMap.put("id", cursor.getString(0));
			tMap.put("name", cursor.getString(1));
			tMap.put("img", R.drawable.listdot);
			tList.add(tMap);
			cursor.moveToNext();
		}
		cursor.close();

		if (tList.size() == 0) {
			tMap = new HashMap<String, Object>();
			tMap.put("id", "-1");
			tMap.put("name", "The FavorList is empty.");
			tMap.put("img", R.drawable.listdot);
			tList.add(tMap);
		}

		tAdapter = new SimpleAdapter(this, tList, R.layout.myfavor, tString,
				tInt);
		setListAdapter(tAdapter);
	}

}
