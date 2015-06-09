package com.zhao.paipai.view;

public interface UiCallback {
	public final static int STATE_SURFACE_CREATED = 0;
	public final static int STATE_SURFACE_DESTROY = 1;
	public final static int STATE_OP_ADD = 2;
	public final static int STATE_OP_REMOVE = 3;
	public final static int STATE_SAVED = 4;
	
	public void OnStateChanged(int state,Object o);
	public void OnError(String err);
}
