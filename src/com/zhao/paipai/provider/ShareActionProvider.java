package com.zhao.paipai.provider;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.view.ActionProvider;
import com.zhao.paipai.MainActivity;
import com.zhao.paipai.PicsFragment;
import com.zhao.paipai.R;
import com.zhao.paipai.utils.AppEnv;


public class ShareActionProvider extends ActionProvider{
	private Context context;
	private Intent intent;
	public ShareActionProvider(Context context) {
		super(context);
		intent = new Intent(Intent.ACTION_SEND_MULTIPLE);  
        intent.setType("image/*");  
		this.context = context;
	}
	
	public void setData(String filename){
		intent.putExtra(Intent.EXTRA_STREAM, filename); 
	}
	
	@Override
	public View onCreateActionView() {
		View view = View.inflate(context, R.layout.share_bar, null);
		view.findViewById(R.id.iv_share).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				performAction();
			}
		});
 
		return view;
	}
	
	public void performAction(){
		String filename = ((PicsFragment)((MainActivity)AppEnv.AppContext).mFragments.get(1)).getCurPicture();
		intent.putExtra(Intent.EXTRA_STREAM, filename); 
		context.startActivity(intent);
	}
	
}
