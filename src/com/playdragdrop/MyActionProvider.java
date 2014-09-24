package com.playdragdrop;

import android.content.Context;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;

public class MyActionProvider extends ActionProvider {


	public MyActionProvider(Context context) {
		super(context);
	}

	@Override
	public View onCreateActionView() {
		return null;
	}
	
	@Override
	public boolean hasSubMenu() {
		return true;
	}
	
	@Override
	public void onPrepareSubMenu(SubMenu subMenu) {
		subMenu.clear();
		
		subMenu.add(Menu.NONE, R.id.action_add_photo, 0, "Add");
		subMenu.add(Menu.NONE, R.id.action_sort, 1, "Sort");
		subMenu.add(Menu.NONE, R.id.action_delete_photo, 2, "Delete");
	}
	
	

}