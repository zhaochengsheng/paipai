package com.zhao.paipai.provider;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.view.ActionProvider;
import com.zhao.paipai.MainActivity;
import com.zhao.paipai.R;
import com.zhao.paipai.utils.AppEnv;

public class PhotoActionProvider extends ActionProvider{
	private Context context;
	public PhotoActionProvider(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public View onCreateActionView() {
		View view = View.inflate(context, R.layout.photos_bar, null);
		view.findViewById(R.id.iv_photos).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				 performAction();
			}
		});
		AppEnv.Log("----onCreateActionView-----photos-------");
		return view;
	}
	
	public void performAction(){
		((MainActivity)AppEnv.AppContext).mViewPager.setCurrentItem(1,true);
	}
	
}
