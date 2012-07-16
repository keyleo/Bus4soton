package org.myftp.kss;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * This code was referenced Nono_Love_Lilith's blog
 * Referenced from http://blog.csdn.net/nono_love_lilith/article/details/7074800
 */
public class PublicNoticeView extends LinearLayout {

	private Context mContext;
	private ViewFlipper viewFlipper;
	private View scrollTitleView;
	String[] messages;
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 1:

				bindNotices();
				break;

			case -1:
				break;
			}
		}
	};

	/**
	 * @param context
	 */
	public PublicNoticeView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public PublicNoticeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();

	}

	/**
	 * Refresh rolling subtitles when online bulletin avaible
	 */
	protected void bindNotices() {
		// TODO Auto-generated method stub
		if (messages.length > 1) {
			viewFlipper.removeAllViews();
			int i = 0;
			while (i < messages.length && i < 10) {

				String text = messages[i];
				TextView textView = new TextView(mContext);
				textView.setText(text);

				i = i + 1;
				if (!messages[i].equals("null")) {
					textView.setText(Html.fromHtml("<u>" + text + "</u>"));
					textView.setOnClickListener(new NoticeTitleOnClickListener(
							mContext, messages[i]));
				}
				LayoutParams lp = new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				viewFlipper.addView(textView, lp);
				i++;
			}
		}
	}

	private void init() {
		bindLinearLayout();
		getPublicNotices();
	}

	/**
	 * Initialize layout
	 */
	public void bindLinearLayout() {
		scrollTitleView = LayoutInflater.from(mContext).inflate(
				R.layout.main_public_notice_title, null);
		LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		addView(scrollTitleView, layoutParams);

		viewFlipper = (ViewFlipper) scrollTitleView
				.findViewById(R.id.flipper_scrollTitle);
		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext,
				android.R.anim.slide_in_left));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext,
				android.R.anim.slide_out_right));
		viewFlipper.startFlipping();
		View v = viewFlipper.getCurrentView();

	}

	/**
	 * Acquire online bulletin
	 */
	public void getPublicNotices() {
		new Thread() {
			public void run() {

				String var = Utility
						.getUrl("http://users.ecs.soton.ac.uk/yl25e11/busBulletin.php");
				messages = var.split("\n");
				mHandler.sendEmptyMessage(1);
			}
		}.start();

	}

	/**
	 * Click on the subtitle
	 * 
	 */
	class NoticeTitleOnClickListener implements OnClickListener {
		private Context context;
		private String titleid;

		public NoticeTitleOnClickListener(Context context, String whichText) {
			this.context = context;
			this.titleid = whichText;
		}

		public void onClick(View v) {
			// TODO Auto-generated method stub
			disPlayNoticeContent(context, titleid);
		}

	}

	/**
	 * show Internet bulletin detail base on the url via browser
	 * 
	 */
	public void disPlayNoticeContent(Context context, String titleid) {
		// TODO Auto-generated method stub
		Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(titleid));
		it.setClassName("com.android.browser",
				"com.android.browser.BrowserActivity");
		getContext().startActivity(it);
	}

}