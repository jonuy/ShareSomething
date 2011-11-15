/**
 * @author Jonathan Uy - jonathan.j.uy@gmail.com
 * ShareSomething app - a Do Something code sample.
 * 
 * Influenced by some conversations I had during the interview process, I
 * created a simple app that allows the user to select an image from their 
 * phone and enter some accompanying text, and then share this via Android 
 * Intents.  It's admittedly rough around the edges, but base functionality is
 * there and has been tested successfully on Android 2.2 and 2.3 using the
 * Gmail application as the Intent of choice.
 * 
 * MainActivity.java
 * Handles interaction with the UI and the majority of app behavior.  Entirety 
 * of app life-cycle is contained within the MainActivity life-cycle.
 */

package com.jonuy.ShareSomething;

import java.io.File;
import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	// Logging tag
	private static final String TAG = "ShareSomething";
	
	// Handles to UI elements
	private ImageButton mSendImageButton;
	private EditText mSendText;
	private Button mShareButton;
	private ImageView mLastImgView;
	private TextView mLastTextView;
	private TextView mLastSentTime;
	
	// File path to selected image, if any
	private String mCurrentImgPath;
	
	// Text entered into EditText box.  Set when user executes a share activity.
	private String mCurrentText;
	
	// Used to track the last image and text shared
	private String mLastImgPath;
	private String mLastSentText;
	
    /** Called when the activity is first created */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Map elements from layout to private handles
        mSendImageButton = (ImageButton)findViewById(R.id.send_image);
        mSendText = (EditText)findViewById(R.id.send_text);
        mShareButton = (Button)findViewById(R.id.share_button);
        mLastImgView = (ImageView)findViewById(R.id.last_sent_image);
        mLastTextView = (TextView)findViewById(R.id.last_sent_msg);
        mLastSentTime = (TextView)findViewById(R.id.last_sent_time);
        
        // Setup onClick listeners
        mSendImageButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);
    }

    /** Triggered when UI elements are clicked */
	@Override
	public void onClick(View v) {
		switch( v.getId() ) {
		// User clicks on the main image button
		case R.id.send_image:
			startImgSelectActivity();
			break;
		// User clicks on the main share button
		case R.id.share_button:
			startShareActivity();
			break;
		}
	}
    
    /** Start image gallery activity through an intent to let user pick an image */
	private void startImgSelectActivity() {
		Intent i = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		i.setType("image/*");
		startActivityForResult(i, Constants.ACTIVITY_IMG_SELECT);
	}
	
	/** Creates intent and starts activity to send/share */
	private void startShareActivity() {
		// Instantiate new intent and set default type as text-only
		Intent i=new Intent(android.content.Intent.ACTION_SEND);
		i.setType("text/plain");
		
		// Set subject as our app's name
		String subject = getResources().getText(R.string.app_name).toString();
		i.putExtra(Intent.EXTRA_SUBJECT, subject);
		
		// Get current text on screen, not what's saved in the database (could be old)
		mCurrentText = mSendText.getText().toString();
		i.putExtra(Intent.EXTRA_TEXT, mCurrentText);
		
		// If an image is selected, attach it to the intent and change intent type
		if( mCurrentImgPath != null ) {
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mCurrentImgPath)));
			i.setType("image/*");
		}
		
		// Use createChooser() to allow user to select application to use for sharing
		Intent chooser = Intent.createChooser(i, 
				getResources().getText(R.string.share_intent_title));
		startActivityForResult(chooser, Constants.ACTIVITY_SHARE);
	}
	
	/** Called when activities launched from this class return with a result */
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	
    	switch( requestCode ) {
    	case Constants.ACTIVITY_IMG_SELECT:
    		if(resultCode == RESULT_OK)
    			onImgSelectComplete(intent);
    		else
    			Log.w(TAG, "ACTIVITY_IMG_SELECT returned: "+resultCode);
    		break;
    	case Constants.ACTIVITY_SHARE:
			onShareComplete();
			Log.w(TAG, "ACTIVITY_SHARE returned: "+resultCode);
    		break;
    	}
    }
	
	/** Called when image selection activity returns with an image */
	private void onImgSelectComplete(Intent intent) {
		// Determine where the selected image is stored
		Uri selectedImage = intent.getData();
		String[] filePathColumn = {MediaStore.Images.Media.DATA};
		
		Cursor cursor = getContentResolver().query(selectedImage, 
			filePathColumn, null, null, null);
		startManagingCursor(cursor);
		cursor.moveToFirst();
		
		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		mCurrentImgPath = cursor.getString(columnIndex);
		
		// Then display selected image on the ImageButton
		ImgUtils.setImage(mSendImageButton, mCurrentImgPath);
	}
	
	/** Called when a share activity executes successfully */
	private void onShareComplete() {
		// Cache the shared data
		mLastImgPath = mCurrentImgPath;
		mLastSentText = mCurrentText;
		
		// Revert input fields to their default state
		mCurrentImgPath = null;
		mCurrentText = null;
		mSendImageButton.setImageResource(R.drawable.ic_photo);
		mSendText.setText("");
		
		// Display last sent data in new fields along with current time
		ImgUtils.setImage(mLastImgView, mLastImgPath);
		mLastTextView.setText(mLastSentText);
		
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DATE);
		int year = cal.get(Calendar.YEAR);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		
		// Build string for current date and time
		String currTime = getResources().getText(R.string.time_sent).toString() 
			+ getLocalizedMonth(month) + " " + day + ", " + year
			+ " at " + hour + ":" + minute;
		
		mLastSentTime.setText(currTime);
	}
	
	/** Given a month's numerical value, returns its corresponding localized string */
	private String getLocalizedMonth(int month) {
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] strMonths = dfs.getMonths();
		
		return strMonths[month];
	}
}