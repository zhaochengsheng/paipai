package com.zhao.paipai.bianhuan;

public interface UiCallback {
	public final static int STATE_CHANGED = 0;
	public final static int STATE_PERFORMED = 1;
	
	public void OnStateChanged(int state,Object o);
	public void OnError(String err);
}
