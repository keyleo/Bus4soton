package org.myftp.kss;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Utility {

	public final static String DATABASE_PATH = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/sotonbus"
			+ "/";
	public final static String DATABASE_FILENAME = "busespro.db";
	public final static String FULLPATH = Utility.DATABASE_PATH
			+ Utility.DATABASE_FILENAME;
	
	/**
	 * The value of curretnDbVersion should be common to the getDbVersion()
	 * since it is a method for identify whether the current db is still available
	 */
	public final static String curretnDbVersion = "7";
	/**
	 * The value of currentAppVersion should be common to the getAppVersion()
	 * since it is a method for identify whether the current app is still available
	 */
	public final static String currentAppVersion = "9";

	/**
	 * Response for GET method£¬return whole page
	 * 
	 * @param url
	 * @return
	 */
	public static String getUrl(String url) {

		String content = null;
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(httpclient.getParams(),
					30 * 1000);
			HttpConnectionParams
					.setSoTimeout(httpclient.getParams(), 30 * 1000);
			// MOCK FIREFOX BROWSER
			HttpProtocolParams
					.setUserAgent(
							httpclient.getParams(),
							"Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.9) Gecko/20100315 Firefox/3.5.9");
			HttpGet httpget = new HttpGet();
			content = "";
			httpget.setURI(new URI(url));
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			content = null;
			if (entity != null) {
				content = EntityUtils.toString(entity);
				httpget.abort();
				httpclient.getConnectionManager().shutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * 
	 * @return read and return the current version of database in table info
	 */
	public String getDbVersion() {
		String result = "0";
		SQLiteDatabase dbBuses = SQLiteDatabase.openOrCreateDatabase(
				Utility.FULLPATH, null);
		Cursor cursor = dbBuses.rawQuery("select dbVersion from info",
				new String[] {});
		cursor.moveToFirst();

		if (!cursor.isAfterLast()) {
			result = cursor.getString(0);
		}
		cursor.close();
		dbBuses.close();
		return result;
	}

	/**
	 * 
	 * @return read and return the current version of app in table info
	 */
	public String getAppVersion() {
		String result = "0";
		SQLiteDatabase dbBuses = SQLiteDatabase.openOrCreateDatabase(
				Utility.FULLPATH, null);
		Cursor cursor = dbBuses.rawQuery("select appId from info",
				new String[] {});
		cursor.moveToFirst();

		if (!cursor.isAfterLast()) {
			result = cursor.getString(0);
		}
		cursor.close();
		dbBuses.close();
		return result;
	}

}
