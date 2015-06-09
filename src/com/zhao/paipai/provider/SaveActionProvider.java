package com.zhao.paipai.provider;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import com.actionbarsherlock.view.ActionProvider;
import com.zhao.paipai.R;
import com.zhao.paipai.EditActivity.EditBroadcastReceiver;
 

public class SaveActionProvider extends ActionProvider{
	private Context context;
	private Intent intent;
	private View view ;
	public SaveActionProvider(Context context) {
		super(context);
		this.context = context;
		this.intent = new Intent(EditBroadcastReceiver.ACTION_SAVE);
	}
	
	public View getView(){
		 return view;
	}
	
	@Override
	public View onCreateActionView() {
		view = View.inflate(context, R.layout.save_bar, null);
		view.findViewById(R.id.iv_save).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				performAction();
			}
		});
 
		return view;
	}
	
	public void performAction(){
		 context.sendBroadcast(intent);
	}
	
}
