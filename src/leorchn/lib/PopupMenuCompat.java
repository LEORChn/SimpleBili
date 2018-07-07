package leorchn.lib;
import android.view.*;
import android.content.*;
import java.util.*;
import android.widget.*;
import android.app.*;
/*	-Here's how to init after setContentView() in activity:
	PopupMenu popupMenu=new PopupMenuCompat(this,fv(id.main_menu),menu.main_nav);
	
	-And using this at onClick() in the onClickListener:
	popupMenu.show();
	
	-And using this if wants to override system menu key:
	@Override public boolean onPrepareOptionsMenu(Menu menu) {
		popupMenu.show();
		return !super.onPrepareOptionsMenu(menu);//这里一个感叹号阻止 OptionMenu 转而使用自定义的 PopupMenu
	}
	
	-And write what to do after selected a menu item here:
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			//dont forgot to add android:id="@+id/menuitem_*" each menu item 
		}
		return super.onOptionsItemSelected(item);
	}
*/
public class PopupMenuCompat extends PopupMenu implements PopupMenu.OnMenuItemClickListener{
	Activity act;
	public PopupMenuCompat(Activity c,View view,int menuId){
		super(c,view);
		get(this,menuId);
		act=c;
		setOnMenuItemClickListener(this);
	}
	@Override public boolean onMenuItemClick(MenuItem p1){
		act.onContextItemSelected(p1);
		act.onOptionsItemSelected(p1);
		return false;
	}
	// 以下-----静态方法区
	public static PopupMenu get(PopupMenu p,int menuId){
		MenuInflater mi=p.getMenuInflater();
		mi.inflate(menuId,p.getMenu());
		exec(p.getMenu());
		return p;
	}
	public static Menu get(Context c,int menuId,Menu m){
		MenuInflater mi=new MenuInflater(c);
		mi.inflate(menuId,m);
		return exec(m);
	}
	public static Menu exec(Menu menu){
		do{
			if(Sys.apiLevel()<14) break; // 系统在4.0以下时禁用
			List<Menu>l=new ArrayList<>();
			submenulooper(menu,l);
			try{
				Class clz=Class.forName("com.android.internal.view.menu.MenuBuilder");
				java.lang.reflect.Method m=clz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
				m.setAccessible(true);
				for(int i=0,len=l.size();i<len;i++)
					m.invoke(l.get(i), true);
			}catch(Throwable e){}
		}while(false);
		return menu;
	}
	static void submenulooper(Menu menu,List<Menu>l){
		l.add(menu);
		for(int i=0,len=menu.size();i<len;i++)
			if(menu.getItem(i).hasSubMenu())
				submenulooper(menu.getItem(i).getSubMenu(),l);
	}
}
