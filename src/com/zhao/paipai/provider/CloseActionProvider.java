package com.zhao.paipai.provider;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import com.actionbarsherlock.view.ActionProvider;
import com.zhao.paipai.R;
import com.zhao.paipai.EditActivity.EditBroadcastReceiver;


public class CloseActionProvider extends ActionProvider{
	private Context context;
	private Intent intent;
	public CloseActionProvider(Context context) {
		super(context);
		this.context = context;
		this.intent = new Intent(EditBroadcastReceiver.ACTION_CLOSE);
	}
	
	public void setData(String filename){
		intent.putExtra(Intent.EXTRA_STREAM, filename); 
	}
	
	@Override
	public View onCreateActionView() {
		View view = View.inflate(context, R.layout.close_bar, null);
		view.findViewById(R.id.iv_close).setOnClickListener(new OnClickListener(){
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
