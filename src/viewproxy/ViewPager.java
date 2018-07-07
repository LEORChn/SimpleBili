package viewproxy;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class ViewPager extends HorizontalScrollView implements MessageQueue.IdleHandler{
	public ViewPager(Context c){
		super(c);
		init();
	}
	public ViewPager(Context c, AttributeSet attrs){
		super(c,attrs);
		init();
	}
	public ViewPager(Context c, AttributeSet attrs, int defStyle){
		super(c,attrs,defStyle);
		init();
	}//类所需api 3，以上强行劫持所有构造器
	@Override public void addView(View child){ inner.addView(child); }
	@Override public void addView(View child,ViewGroup.LayoutParams params){ inner.addView(child,params); }
	@Override public void addView(View child,int index){ inner.addView(child,index); }
	@Override public void addView(View child,int width,int height){ inner.addView(child,width,height); }
	//@Override public void addView(View child,int index,ViewGroup.LayoutParams params){ inner.addView(child,index,params); }
	//不要覆盖上面这个addView，否则用xml初始化会出现问题
	
	//@Override public void smoothScrollBy(int x,int y){}//该类为final，无法覆盖
	@Override protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec,heightMeasureSpec);
		int w=getWindowWidth();
		if(length_page_width==w){
			addIdleTodo(IDLE_TODO_SET_CURRENT_PAGE);//通常在下方更新宽度后会重新调用一次onMeasure，所以需要在宽度没有更新后回到原来的页面
			return;//因为页面宽度并未更新，所以不需更新子页面宽度
		}
		length_page_width=w;
		length_minside=Math.min(w,getWindowHeight());
		addIdleTodo(IDLE_TODO_UPDATE_WIDTH);
	}
	
	//以上覆盖
	LinearLayout inner;
	void init(){ //构造器共享初始化方法
		inner=new LinearLayout(getContext());
		inner.setOrientation(LinearLayout.HORIZONTAL);
		super.addView(inner,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
		//sv.setKeepScreenOn(true);//api 1
		setFillViewport(true);
	}
	int length_minside=0,length_page_width=0;//页面更新相关的变量存储
	int idleTodo,
		scroll_to_page_when_idle;//空闲待办相关的变量存储
	static final int
		IDLE_TODO_UPDATE_WIDTH=1,
		IDLE_TODO_SET_CURRENT_PAGE=2,
		IDLE_TODO_REMEMBER_CURRENT_PAGE=4;
	@Override public boolean queueIdle(){
		if(idleTodo==0)return false;
		int r=0;
		int[]todolist={1,2,4};//每次调用空闲待办时，按事件优先级每次只做一件事
		for(int i=0,len=todolist.length;i<len;i++)
			if((idleTodo&todolist[i])>0){
				r=todolist[i];
				break;
			}
		switch(r){
			case IDLE_TODO_UPDATE_WIDTH:
				for(int i=0,len=inner.getChildCount();i<len;i++){
					View inner=this.inner.getChildAt(i);
					LinearLayout.LayoutParams l= (LinearLayout.LayoutParams)inner.getLayoutParams();
					l.width=getMeasuredWidth();
					inner.setLayoutParams(l);
				}
				break;
			case IDLE_TODO_SET_CURRENT_PAGE:
				smoothScrollTo(scroll_to_page_when_idle*getWindowWidth(),0);
				break;
			case IDLE_TODO_REMEMBER_CURRENT_PAGE:
				scroll_to_page_when_idle=getCurrentPage();
				break;
		}
		idleTodo-=r;
		return idleTodo>0;
	}
	void addIdleTodo(int taskid){
		idleTodo|=taskid;
		Looper.myQueue().addIdleHandler(this);
	}
	public void clear(){ inner.removeAllViews(); }
	public void removePage(int i){ inner.removeViewAt(i); }
	public View getPage(int i){ return inner.getChildAt(i); }
	public void setPage(int i){
		scroll_to_page_when_idle=i;//调用此方法的很可能是机器行为，因此要直接控制变量
		addIdleTodo(IDLE_TODO_SET_CURRENT_PAGE);
	}
	public void setPageVisible(int i){ getPage(i).setVisibility(View.VISIBLE); }
	public int getCurrentPage(){ return scroll_to_page_when_idle; }
	public int getMaxPage(){ return Math.max(0,inner.getChildCount()-1); }
	public int getMaxScroll(){ return inner.getMeasuredWidth(); }
	public int getWindowHeight(){ return getMeasuredHeight(); }
	public int getWindowWidth(){ return getMeasuredWidth(); }
	public ViewGroup getInner(){ return inner; }
	//以下 手势判断相关，是本类，也就是Pager的核心
	float from=0,to=0;
	boolean fliped=false;
	@Override public boolean onTouchEvent(MotionEvent p2) {
		switch(p2.getAction()){
			case p2.ACTION_MOVE:
				to=p2.getX();
				if(from==0)from=to;
				if(Math.abs(from-to)>length_minside/5)
					FlipPage();
				break;
			case p2.ACTION_UP:
				if(Math.abs(from-to)>length_minside/20)
					FlipPage();
				ResetCanFlipPage();
				break;
			default:
				return true;
		}
		return false;
	}
	void ResetCanFlipPage(){
		fliped=false;
		from=0;
	}
	void FlipPage(){
		if(fliped)return;
		fliped=true;
		if(from<to) PrevPage();
		else NextPage();
		addIdleTodo(IDLE_TODO_REMEMBER_CURRENT_PAGE);//调用此方法的很可能是人类行为
	}
	void PrevPage(){ smoothScrollBy(-getWindowWidth(),0); }
	void NextPage(){ smoothScrollBy(getWindowWidth(),0); }
}

