package org.myftp.kss;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * The point to show current location
 */
public class LocationPoint extends ItemizedOverlay {	
	public ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;

	public LocationPoint(Drawable defaultMarker) {
		// super(defaultMarker);
		super(boundCenterBottom(defaultMarker));
		// TODO Auto-generated constructor stub
	}

	public void addOverlay(OverlayItem overlay) {
		mOverlays.clear();
		mOverlays.add(overlay);
		populate();
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

	public LocationPoint(Drawable defaultMarker, Context context) {
		// super(defaultMarker);
		this(defaultMarker);
		// mContext = context;
		this.mContext = context;
	}

}