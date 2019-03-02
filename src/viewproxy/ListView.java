package viewproxy;
import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import leorchn.lib.*;

public class ListView extends android.widget.ListView implements AbsListView.OnScrollListener {
	public ListView(Context c){ super(c); init(); }
	public ListView(Context c,AttributeSet attrs){ super(c,attrs); init(); }
	public ListView(Context c,AttributeSet attrs,int defStyle){ super(c,attrs,defStyle); init(); }
	
	//以上覆盖
	void init(){
		setOnScrollListener(this); //允许用手指刷动态
		setFastScrollEnabled(true); //api 3
		if(Sys.apiLevel()>=12)
			setOnGenericMotionListener(new OnGenericMotionListener(){
				@Override public boolean onGenericMotion(View v,MotionEvent e) {
					//getAction:确保是鼠标滚轮。getAxisValue<0:确保是向下滚动
					if(lis==null)return false;
					if(e.getAction()==e.ACTION_SCROLL && e.getAxisValue(e.AXIS_VSCROLL)<0f)
						onScrollStateChanged((AbsListView)v,0);
					return false;
				}
			}); //允许用鼠标滚轮刷动态
	}
	//很纠结这段要不要因为兼容旧版而不加 @Override 
	public void setFastScrollAlwaysVisible(boolean b){
		if(Sys.apiLevel()>=11)
			super.setFastScrollAlwaysVisible(b);
	}
	public void setAdapter(View headView,BaseAdapter itemControl){ setAdapter(headView,itemControl,null); }
	public void setAdapter(BaseAdapter itemControl,View footerView){ setAdapter(null,itemControl,footerView); }
	public void setAdapter(View headView,BaseAdapter itemControl,View footerView){
		if(headView!=null) addHeaderView(headView);
		if(footerView!=null) addFooterView(footerView);
		ba=itemControl;
		setAdapter(ba);
	}
	BaseAdapter ba;
	OnListNeedsUpdateListener lis;
	public void setOnListNeedsUpdateListener(OnListNeedsUpdateListener l){ lis=l; }
	//以下 监听器区域
	@Override public void onScrollStateChanged(AbsListView a,int p2) {
		if(lis==null)return;
		if(p2==0 && (getLastVisiblePosition()+5)>getCount())
			lis.onListNeedsUpdate(this,ba);
	}
	@Override public void onScroll(AbsListView a,int b,int c,int d){}
	//监听器区域 结束
	public static interface OnListNeedsUpdateListener{
		public void onListNeedsUpdate(ListView lsv,BaseAdapter ba)
	}
}
