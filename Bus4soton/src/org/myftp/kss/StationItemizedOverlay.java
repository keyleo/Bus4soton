package org.myftp.kss;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * 
 * Bus stop markers
 */
public class StationItemizedOverlay extends ItemizedOverlay {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	ProgressDialog m_pDialog;
	AlertDialog.Builder dialog;
	Cursor cursor ;
	String timeInfo = "";
	Intent itShowTime;

	public StationItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	public StationItemizedOverlay(Drawable defaultMarker, Context context) {
		this(defaultMarker);
		this.mContext = context;
				
	}

	@Override
	/**
	 * Click event for onTap to show the current real time information on ShowTime intent
	 */
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		
		final String tid = item.getSnippet();
		itShowTime =new Intent();
		itShowTime.setClass(mContext,ShowTime.class);

		Bundle bundle=new Bundle();
		bundle.putString("bid",tid);
		bundle.putString("bname",item.getTitle());
		
		itShowTime.putExtras(bundle);
		mContext.startActivity(itShowTime);
		return true;
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}

	public void addOverlay(List<StationBean> stations) {
		mOverlays.clear();
		for (StationBean s : stations) {
			try {
				mOverlays.add(new OverlayItem(new GeoPoint(s.getBlatitude(), s
						.getBlongitude()), s.getBname(), s.getBid()));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		populate();
	}

	

}
