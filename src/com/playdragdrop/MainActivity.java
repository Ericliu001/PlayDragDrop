package com.playdragdrop;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int ACTIVITY_SELECT_IMAGE = 0;

	protected static final String IMAGE_PATH = "image_path";

	Button btCancel, btDelete, btFinishSorting;
	TextView tvDragHint;
	LinearLayout layoutDelete, layoutSort;

	// The list of added ImageViews
	ArrayList<ImageView> listImageView;

	//The number of photos being added
	private int numPhoto = 0;
	
	
	// The list of containers
	ArrayList<RelativeLayout> listParent;

	// The list of checkboxes
	ArrayList<CheckBox> listChkbox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();
	}

	/**
	 * Initiate the Views and the Lists that save Views
	 */
	private void initViews() {
		listImageView = new ArrayList<ImageView>();
		listParent = new ArrayList<RelativeLayout>();
		listChkbox = new ArrayList<CheckBox>();

		btCancel = (Button) findViewById(R.id.btCancel);
		btDelete = (Button) findViewById(R.id.btDelete);
		btFinishSorting = (Button) findViewById(R.id.btFinishSorting);
		
		
		tvDragHint = (TextView) findViewById(R.id.tvHint);
		layoutDelete = (LinearLayout) findViewById(R.id.layoutDelete);
		layoutSort   = (LinearLayout) findViewById(R.id.layoutSort);

		listParent.add((RelativeLayout) findViewById(R.id.row0col0));
		listParent.add((RelativeLayout) findViewById(R.id.row0col1));
		listParent.add((RelativeLayout) findViewById(R.id.row0col2));

		listParent.add((RelativeLayout) findViewById(R.id.row1col0));
		listParent.add((RelativeLayout) findViewById(R.id.row1col1));
		listParent.add((RelativeLayout) findViewById(R.id.row1col2));

		listParent.add((RelativeLayout) findViewById(R.id.row2col0));
		listParent.add((RelativeLayout) findViewById(R.id.row2col1));
		listParent.add((RelativeLayout) findViewById(R.id.row2col2));
	}

	/**
	 * reset all display, clear all listeners.
	 */
	private void resetDisplay() {
		// remove all views
		for (RelativeLayout parent : listParent) {
			parent.removeAllViews();
		}


		for (int i = 0; i < listImageView.size(); i++) {
			RelativeLayout parent = listParent.get(i);
			ImageView iv = listImageView.get(i);
			parent.addView(iv);
			
			iv.setOnTouchListener(null);
			iv.setOnClickListener(new ViewImageClickListener());
		}
		
		

		// clear the checkbox List or the status will be wrong
		listChkbox.clear();
		layoutDelete.setVisibility(View.GONE);
		layoutSort.setVisibility(View.GONE);
		tvDragHint.setVisibility(View.GONE);

	}

	private ImageView addImageToImageView(MyImage image) {
		
//		This is where the ImageView is created. 
		ImageView iv = new ImageView(this);
		iv.setImageBitmap(image.yourSelectedImage);
		iv.setLayoutParams(new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		iv.setTag(image);
		iv.setOnClickListener(new ViewImageClickListener());
		return iv;
	}
	
	
	private class ViewImageClickListener implements View.OnClickListener{

		/**
		 * Catch the click event of each ImageView;
		 */
		@Override
		public void onClick(View v) {
			MyImage image = (MyImage) v.getTag();
			File imgFile = new File(image.filePath);
			if (imgFile.exists()) {
				Intent viewImgIntent = new Intent(MainActivity.this, ViewImageActivity.class);
				viewImgIntent.putExtra(IMAGE_PATH, image.filePath);
				startActivity(viewImgIntent);
			}
		}
	}

	/**
	 * Start the state of drag and drop
	 */
	private void startDragAndDrop() {
		
		if (listImageView.size() < 2) {
			Toast.makeText(this, "Add more photos before sorting", Toast.LENGTH_SHORT).show();
			return;
		}
		
		tvDragHint.setVisibility(View.VISIBLE);
		layoutSort.setVisibility(View.VISIBLE);
		btFinishSorting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				resetDisplay();
			}
		});

		for (ImageView iv : listImageView) {
			// Set up listeners to receive drop event
			RelativeLayout parent = (RelativeLayout) iv.getParent();
			parent.setOnDragListener(new MyDragListener());
			
			iv.setOnClickListener(null);
			iv.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						v.performClick();
					}

					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						View.DragShadowBuilder shadowBuilder = new DragShadowBuilder(
								v);

						v.startDrag(null, shadowBuilder, v, 0);
						v.setVisibility(View.INVISIBLE);
						return true;
					} else {
						return false;
					}
				}
			});

		}
	}

	private void startMultiSelect() {

		layoutDelete.setVisibility(View.VISIBLE);
		btDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				deletePhotos();
				finishMultiSelect();
			}
		});
		
		
		btCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finishMultiSelect();
			}
		});
		
		

		listChkbox.clear();
		for (ImageView iv : listImageView) {

			RelativeLayout parent = (RelativeLayout) iv.getParent();
			CheckBox chkbox = new CheckBox(this);
			chkbox.setTag(iv);

			// add a Checkbox onto the top of every photo
			parent.addView(chkbox);
			// save the Checkbox into an ArrayList
			listChkbox.add(chkbox);
			// very important or you will get ArrayIndexOutOfBoundsException
			chkbox.setClickable(false);

			iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					CheckBox chkBox = listChkbox.get(listImageView.indexOf(v));
					if (chkBox.isChecked()) {
						chkBox.setChecked(false);
					} else {
						chkBox.setChecked(true);
					}
				}
			});
		}

	}

	protected void deletePhotos() {
		for (CheckBox chkbox: listChkbox) {
			if (chkbox.isChecked()) {
				listImageView.remove(chkbox.getTag());
			}
		}
	}

	protected void finishMultiSelect() {
		resetDisplay();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem item = menu.findItem(R.id.action_manipulate_photo);
		item.setActionProvider(new MyActionProvider(this));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {

		case R.id.action_add_photo:
			resetDisplay();
			goToGallery();
			break;

		case R.id.action_sort:
			resetDisplay();
			startDragAndDrop();
			break;

		case R.id.action_delete_photo:
			resetDisplay();
			startMultiSelect();
			break;

		default:
			break;
		}

		return true;
	}

	private void goToGallery() {
		if (listImageView.size() < 8) {

			Intent i = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
		} else {
			Toast.makeText(this, "8 photos at most", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		if (requestCode == ACTIVITY_SELECT_IMAGE && resultCode == RESULT_OK) {
			Uri selectedImage = imageReturnedIntent.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String filePath = cursor.getString(columnIndex);
			cursor.close();

			Bitmap yourSelectedImage = ImageProcessUtil
					.decodeSampledBitmapFromFile(filePath, 100, 100);
			if (yourSelectedImage != null) {
				MyImage image = new MyImage(yourSelectedImage, filePath);
				addSelectedImageToTheDisplay(image);

			}
		}
	}

	private class MyImage {
		private Bitmap yourSelectedImage;
		private String filePath;

		public MyImage(Bitmap yourSelectedImage, String filePath) {
			this.yourSelectedImage = yourSelectedImage;
			this.filePath = filePath;
		}
	}

	private void addSelectedImageToTheDisplay(MyImage image) {

		if (listImageView.size() < 8) {

			ImageView iv = addImageToImageView(image);
			listImageView.add(iv);
			refreshtContainer(listImageView.indexOf(iv), iv);
		}

	}

	private void refreshtContainer(int index, ImageView iv) {
		RelativeLayout parent = listParent.get(index);
		parent.removeAllViews();
		parent.addView(iv);
	}

	private class MyDragListener implements OnDragListener {

		@Override
		public boolean onDrag(View dropView, DragEvent event) {
			int action = event.getAction();
			View dragView = (View) event.getLocalState();

			switch (action) {
			case DragEvent.ACTION_DRAG_STARTED:

				break;

			case DragEvent.ACTION_DRAG_ENDED:
				dragView.setVisibility(View.VISIBLE);
				dragView.invalidate();
				break;

			case DragEvent.ACTION_DROP:
				// dropped, reassign View to a place

				int positionDrag = listImageView.indexOf(dragView);
				int positionDrop = listParent.indexOf(dropView);
				Log.d("eric", "drag:" + positionDrag + " ; " + " drop:"
						+ positionDrop);

				if (positionDrag > positionDrop) {
					for (int i = positionDrag; i >= positionDrop; i--) {
						if (i != positionDrop) {

							listImageView.set(i, listImageView.get(i - 1));
						} else {

							listImageView.set(positionDrop,
									(ImageView) dragView);
						}

						RelativeLayout parent = listParent.get(i);
						parent.removeAllViews();
					}

					for (int i = positionDrag; i >= positionDrop; i--) {
						listParent.get(i).addView(listImageView.get(i));
					}

				} else if (positionDrag < positionDrop) {

					for (int i = positionDrag; i <= positionDrop; i++) {
						if (i != positionDrop) {
							listImageView.set(i, listImageView.get(i + 1));
						} else {
							listImageView.set(positionDrop,
									(ImageView) dragView);
						}

						RelativeLayout parent = listParent.get(i);
						parent.removeAllViews();
					}

					for (int i = positionDrag; i <= positionDrop; i++) {
						listParent.get(i).addView(listImageView.get(i));
					}

				}

				break;

			default:
				break;
			}

			return true; // Must return true here to continue to receive
							// DragEvents
		}

	}
}
