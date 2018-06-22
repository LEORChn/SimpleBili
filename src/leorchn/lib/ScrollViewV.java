package leorchn.lib;

import leorchn.lib.Sys;
import android.view.*;
import android.widget.*;
import android.widget.FrameLayout.LayoutParams;

public abstract class ScrollViewV implements Runnable, View.OnTouchListener{
	ScrollView sv;
	ViewGroup inner;
	public ScrollViewV(ScrollView root){
		sv=root;
		if(sv.getChildCount()>0){
			inner=(ViewGroup)sv.getChildAt(0);
		}else{
			inner=new LinearLayout(sv.getContext());
			sv.addView(inner,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
		}
		//sv.setKeepScreenOn(true);//api 1
		sv.setOnTouchListener(this);//api 1
		sv.setSmoothScrollingEnabled(false);//api 1 - 电视应用需要一个确定的焦点时使用
		if(Sys.apiLevel()>=12)
			sv.setOnGenericMotionListener(new View.OnGenericMotionListener(){
				@Override public boolean onGenericMotion(View p1, MotionEvent p2) {
					proxy();
					return false;
				}
			});//api 12
	}
	public void addView(View child){ inner.addView(child); }
	public void clear(){ inner.removeAllViews(); }
	public int itemCount(){ return inner.getChildCount(); }
	public int getMaxScroll(){ return inner.getHeight(); }//To ScrollViewH needs edit
	public int getScrollPos(){ return sv.getScrollY(); }//To ScrollViewH needs edit
	public int getWindowHeight(){ return sv.getHeight(); }
	public int getWindowWidth(){ return sv.getWidth(); }
	public ScrollView getRoot(){ return sv; }
	public ViewGroup getInner(){ return inner; }
	
	@Override public boolean onTouch(View p1, MotionEvent p2) {
		switch(p2.getAction()){
			case p2.ACTION_MOVE:
				proxy();
				break;
			case p2.ACTION_UP:
				flingUpdate();
		}
		return false;
	}
	@Override public void run(){
		if(proxy())flingUpdate();
	}
	void flingUpdate(){ sv.postDelayed(this,200); }
	int lastpos=0,laststop=0;
	boolean proxy(){
		boolean moving=getScrollPos()!=lastpos;
		lastpos=getScrollPos();
		if(moving)
			onScroll(laststop,lastpos);
		else{
			onStop(laststop,lastpos);
			laststop=lastpos;
		}
		return moving;
	}
	abstract public void onScroll(int from,int to)
	public void onStop(int from,int to){}
}
