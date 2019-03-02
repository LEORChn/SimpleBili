package leorchn.lib;
import android.os.*;
import java.util.*;

// 通常用于桌面图标的 Activity，确保该 Activity 不会重复启动并且回到之前最顶层的 Activity
public abstract class BottomActivity extends Activity1{
	@Override protected void onCreate(Bundle sis){
		if(isCurrentActivityInstanced()){
			super.onCreate();
			finish();
		}else
			super.onCreate(sis);
	}
	private static ArrayList<BottomActivity> ins=new ArrayList<>();
	private boolean isCurrentActivityInstanced(){
		for(BottomActivity b:ins){
			pl(string("ins.size= ",ins.size()," ;b.class= ",b.getClass().toString()," ;this.class= ",this.getClass().toString()));
			if(b.getClass().isAssignableFrom(this.getClass())) return true;
		}
		ins.add(this);
		return false;
	}
	@Override public void finish(){
		ins.remove(this);
		super.finish();
	}
	
}
