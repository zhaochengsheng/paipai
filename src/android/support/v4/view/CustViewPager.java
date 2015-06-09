package android.support.v4.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class CustViewPager extends ViewPager {
	private int mVelocity = 1;
	public CustViewPager(Context context) {
		super(context);
		
	}

	public CustViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
	
	@Override
    void smoothScrollTo(int x, int y, int velocity) {
        
        super.smoothScrollTo(x, y, mVelocity);
    }

}
