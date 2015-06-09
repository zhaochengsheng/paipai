package com.zhao.paipai.provider;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.view.ActionProvider;
import com.zhao.paipai.EditActivity;
import com.zhao.paipai.MainActivity;
import com.zhao.paipai.PicsFragment;
import com.zhao.paipai.R;
import com.zhao.paipai.utils.AppEnv;
 
public class EditActionProvider extends ActionProvider{
	private Context context;
	private Intent intent = null;
	public EditActionProvider(Context context) {
		super(context);
		intent = new Intent(context,EditActivity.class);
		this.context = context;
	}
	
	public void setData(String filename){
		intent.putExtra("picture", filename);
	}
	
	
	@Override
	public View onCreateActionView() {
		View view = View.inflate(context, R.layout.edit_bar, null);
		view.findViewById(R.id.iv_edit).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				performAction();
			}
		});
		return view;
	}
	
	public void performAction(){
		String filename = ((PicsFragment)((MainActivity)AppEnv.AppContext).mFragments.get(1)).getCurPicture();
		intent.putExtra("picture", filename);
		//context.startActivity(intent);
		((MainActivity)AppEnv.AppContext).startActivityForResult(intent, 10);
	}
}
