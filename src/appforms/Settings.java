package appforms;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.LEORChn.SimpleBili.*;
import java.io.*;
import leorchn.lib.*;
import simplebili.lib.*;
import static leorchn.lib.Global.*; 

import leorchn.lib.CrashHandlerReg;
import android.content.*;

public class Settings extends Activity implements View.OnClickListener{
	static Activity This; public static Activity getContext(){return This;}
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		CrashHandlerReg.reg(this);
		This=this;
		setContentView(R.layout.activity_settings);
		//startActivity(new Intent(this,TestChangeLayout.class));
	}
	boolean hasinit=false;
	protected void onStart() {
		super.onStart();
		if( ! hasinit){
			btnbind(R.id.setting_goback,R.id.setting_starthome);
			设置项_加载(new SettingListControl(this, (ListView)findViewById(R.id.setting_list)){
				public void onItemClick(int idx, String itemtag) {设置项_单击(idx,itemtag,this);}
			});
		}
	}
	public void onClick(View v){switch(v.getId()){
		case R.id.setting_extendmodule: 
			附加功能(this);
			return;
		case R.id.setting_goback: finish(); return;
		case R.id.setting_starthome: startActivity(new Intent(this,Main_Feeds.class));return;
	}};
	//FSON sc=null;//settingCenter
	String 解读设置(String setkey){
		switch(setkey){
			case "feedliststyle":
			case "quality":
			case "playport":
			case "player":return 获取列表(setkey)[sets.get(setkey,0)];
		}
		return "";
	}
	String[]获取列表(String sets){
		switch(sets){
			case "feedliststyle":
				return new String[]{"简单模式：省内存，有字无图",
					"经典模式：有字但只显示封面图，暂不开放",
					"标准模式：不稳定测试中"};
			case "quality":
				return new String[]{"省流量清晰度：MP4，不分段",
					"高清晰度：FLV，分段，暂不开放"};
			case "playport":
				return new String[]{"总是自动选择主节点",
					"总是自动选择首个备用节点",
					"总是让我自行选择"};
			case "player":
				return new String[]{"内置播放器：还有bug要修",
					"第三方播放器：在线播放有点悬",
					"浏览器：发送网址包裹",
					"复制文本"};
		}return new String[]{""};
	}
	void 预备设置(){//prepare setting data
		String p=setspath(),s=setsload(),
			defdata="{'feedliststyle':0,'quality':0,'playport':0,'player':0}";
		if(s.isEmpty()){
			new File(p).delete();//maybe that's directory. who knows, whatever
			setssave(defdata);
			setsload();//fuck! my app has no permission to write setting
		}
	}
	void 设置项_加载(SettingListControl l){
		l.clear(); 预备设置();
		String[]s={"feedliststyle","quality","playport","player"};
		l.additem("“关注动态”列表样式(重启生效)","当前选择 "+解读设置(s[0]),s[0]);
		l.additem("清晰度选择","当前选择 "+解读设置(s[1]),s[1]);
		l.additem("视频播放节点","当前选择 "+解读设置(s[2]),s[2]);
		l.additem("选择视频播放器","当前选择 "+解读设置(s[3]),s[3]);
	}
	void 设置项_单击(int idx,String tag,SettingListControl slc){
		//if(idx==0)return;//if add 'support me to make money' option
		int option=getoptions(slc.getTitle(idx),获取列表(tag));
		if(option==-1)return;
		sets.set(tag,option);
		setssave();设置项_加载(slc);
	}
	/*long lastReqExit=0;
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==4)
			if(System.currentTimeMillis()-lastReqExit<3000){finish();}else{
				tip("再按一次退出设置。");
				lastReqExit=System.currentTimeMillis();
			}
		return false;//super.onKeyDown(keyCode, event);
	}*/
	int getoptions(String title,String[]options){ return 列表信息框(this,title,options); }
	void btnbind(int...id){for(int btnid:id)btnbind(btnid);}
	void btnbind(int id){fv(id).setOnClickListener(this);}
	View fv(int id){return findViewById(id);}
}

