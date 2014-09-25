package com.playdragdrop;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class ViewImageActivity extends Activity {
	ImageView ivFull;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_image);
		ivFull = (ImageView) findViewById(R.id.ivFull);
		File imgFile = new File(getIntent().getStringExtra(MainActivity.IMAGE_PATH));
		if (imgFile.exists()) {
			Bitmap chosenBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			ivFull.setImageBitmap(chosenBitmap);
		}
	}

}
