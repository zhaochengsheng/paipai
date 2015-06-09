package com.zhao.paipai.provider;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.view.ActionProvider;
import com.zhao.paipai.MainActivity;
import com.zhao.paipai.PicsFragment;
import com.zhao.paipai.R;
import com.zhao.paipai.utils.AppEnv;


public class DeleteActionProvider extends ActionProvider{
	private Context context;
	public DeleteActionProvider(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public View onCreateActionView() {
		View view = View.inflate(context, R.layout.delete_bar, null);
		view.findViewById(R.id.iv_delete).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				performAction();
			}
		});
		return view;
	}
	
	public void performAction(){
		((PicsFragment)((MainActivity)AppEnv.AppContext).mFragments.get(1)).delPic();
	}
}
