/**
 * @author Jonathan Uy - jonathan.j.uy@gmail.com
 * ShareSomething app - a Do Something code sample.
 * 
 * ImgUtils.java
 * Utility class offering helper methods to set images on layout views
 */

package com.jonuy.ShareSomething;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ImgUtils {
	// Logging tag
	private static final String TAG = "ImgUtils";

	/** Displays selected image on a given ImageView */
	public static void setImage(ImageView imgView, String imgPath) {
		if( imgView != null ) {
			LayoutParams frame = imgView.getLayoutParams();
			
			// Find size of the view we're drawing into and request a bitmap
			// appropriate for that size
			int req_size = frame.width > frame.height ? frame.width : frame.height;
			Bitmap selectedImg = decodeFile(imgPath, req_size);

			imgView.setImageBitmap(selectedImg);
		}
	}
	
	/**
	 * Returns a Bitmap that's a sub-sample of the original image.  Allows
	 * us to retrieve an image that's appropriate for the view size and
	 * requires less memory.
	 */
	public static Bitmap decodeFile(String filePath, int req_size){
		Bitmap img = null;
		
		try {
			// Decode image size
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
		
			FileInputStream fileStream = new FileInputStream(filePath);
			BitmapFactory.decodeStream(fileStream, null, opt);
			fileStream.close();

			// Calculate new sample size based off of the req_size
			int scale = 1;
			if (opt.outHeight > req_size || opt.outWidth > req_size) {
				scale = (int)Math.pow(2, (int)Math.round(Math.log(req_size 
						/ (double)Math.max(opt.outHeight, opt.outWidth)) / Math.log(0.5)));
			}
		
			// Decode with inSampleSize
			BitmapFactory.Options scaledOpt = new BitmapFactory.Options();
			scaledOpt.inSampleSize = scale;
			fileStream = new FileInputStream(filePath);
			img = BitmapFactory.decodeStream(fileStream, null, scaledOpt);
			fileStream.close();
		}
		catch (FileNotFoundException e) {
			Log.e(TAG, "Image file not found during decoding: "+filePath);
		}
		catch (IOException e) {
			Log.e(TAG, "Image IOException during decoding: "+filePath);
		}
		
		return img;
	}
}
