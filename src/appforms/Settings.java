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

import android.content.*;
import java.text.*;
import java.util.*;

public class Settings extends Activity1 implements View.OnClickListener{
    protected void onCreate(Bundle savedInstanceState) {
		addIdleHandler();
        super.onCreate(savedInstanceState);
		
		//startActivity(new Intent(this,TestChangeLayout.class));
	}
	AsyncTask<Void,Integer,Void> upgradetip;
	int hasinit=0;
	public boolean onIdle() {
		switch(hasinit){
			case 0: setContentView(layout.activity_settings); break;
			case 1:
				seticon(fv(id.setting_goback),icon.nav_cancel);
				seticon(fv(id.setting_extendmodule),icon.nav_plugin);
				break;
			case 2: loadUserView(); break;
			case 3:
				btnbind(id.setting_goback,id.setting_addUser,id.setting_orderUser,id.setting_addcookie);
				loadSetsView();
				break;
				//设置项_加载(new SettingListControl(this, (ListView)findViewById(R.id.setting_list)){
				//	public void onItemClick(int idx, String itemtag) {设置项_单击(idx,itemtag,this);}
				//});
			case 4://case 5:
				if(getIntent().getBooleanExtra("launchfromfeed",false)){
					Toast t=Toast.makeText(simplebili.App.getContext(),"视频观看功能正在加班升级中！\n很快就会回来的\n应该吧...",1);
						t.setGravity(Gravity.TOP,0,0);
						t.show();
				}
				
		}
		hasinit++;
		return hasinit<9;
	}
	public void onClick(View v){
		switch(v.getId()){
			case id.setting_extendmodule: 附加功能(this); return;
			case id.setting_goback: finish(); return;
			case id.setting_addUser:
				startActivityForResult(new Intent(this,Login.class),1); //startActivity(new Intent(this,Login.class));
				return;
			case id.user_star:
				if(v.getTag() instanceof Integer);else return;
				final int uid=user.getList("order").get((int)v.getTag(),0);
				final FSON su=user.getObject("users").getObject(string(uid));
				new Msgbox("更改主帐户",
					string("确定更改主帐户为 ", su.get("n","神秘用户"), " ？"),
					"设为主帐户", "取消"){
					void onClick(int i){
						if(i==vbyes){
							userset(uid);
							usersave();
							loadUserView();
						}
					}
				};return;
			case id.user_logout:
				if(v.getTag() instanceof Integer);else return;
				final int ord=(int)v.getTag();
				final int uid2=user.getList("order").get(ord,0);
				final FSON su2=user.getObject("users").getObject(string(uid2));
				new Msgbox("删除帐号",
					string("确定删除帐号 ", su2.get("n","神秘用户"), " 的记录？"),
					"删除帐号", "取消"){
					void onClick(int i){
						if(i==vbyes){
							if(user.get("main",0)==ord)userset(0);//如果删除主帐户，设主帐户为0
							else if(user.get("main",0)>ord)user.set("main",user.get("main",1)-1);
							FSON neword=user.getList("order");
							neword.remove(ord);
							user.set("order",neword);
							usersave();
							loadUserView();
						}else{
							tip(string("main=",user.get("main",0),"ord=",ord));
						}
					}
				};return;
			case id.setting_orderUser: multip("排序功能以后开放"); break;
			case id.setting_addcookie:
				//tip(useradd("")+"");
				//tip(user.toString());
				loadUserView(); return;
			case id.listsub_bg: setsitem_onclick((String)v.getTag()); return; //设置项目
			//case id.setting_starthome: startActivity(new Intent(this,Main_Feeds.class)); return;
		}
	}
	boolean setschanged=false;
	@Override protected void onDestroy() {
		if(setschanged)setssave();
		super.onDestroy();
	}
	@Override protected void onActivityResult(int reqCode, int resCode, Intent data) {
		super.onActivityResult(reqCode, resCode, data);
		if(reqCode==1){//表示是从登录窗口返回的
			switch(resCode){//0=已登录,-1=网络异常,-5=用户取消,-11=识别码错误,-12=识别码失效
				case 0://在登录完成自动返回时更新
					user=new FSON(data.getStringExtra("user"));
					usersave();//由于进程隔离，所以需要从浏览器进程拿信息来本进程保存，以保证现有信息同步
					loadUserView();
					break;
				case -5: tip("您已取消登录"); break;//用户取消登录
				default:
					retry(data.getStringExtra("Cookie"),resCode);
			}
		}
	}
	void retry(final String cok,final int reason){
		new Msgbox("","由于某些原因登录失败，代码 "+reason+"\n要重试吗？","重试","否"){
			void onClick(int i){
				if(i==vbyes){
					i=useradd(cok);
					if(i==0) loadUserView();// 此处i==0意味着操作是否成功（即等于0，失败为负数）
					else retry(cok,i);
				}
			}
		};
	}
	SimpleDateFormat year_hour=new SimpleDateFormat("yyyy-MM-dd HH");
	void loadUserView(){
		ViewGroup v=(ViewGroup)fv(id.setting_usersViewGroup);
		v.removeAllViews();
		FSON us=user.getObject("users"),od=user.getList("order");
		for(int i=0,len=od.length();i<len;i++){
			int uid=od.get(i,0);
			FSON su=us.getObject(string( uid ));
			if(!su.get("l",false))continue;//“已删除”标记，为了方便覆盖
			boolean ismain=i==user.get("main",-1);//是否是主帐户
			ViewGroup ni=(ViewGroup) LayoutInflater.from(this).inflate(// 判断帐户格局
				ismain?
					layout.listsub_user_main:
					layout.listsub_user_secondary
				,null);
			((TextView)fv(ni,id.user_name)).setText(su.get("n","神秘用户"));// 赋值帐户名称
			((TextView)fv(ni,id.user_expire)).setText(// 赋值过期信息
				string("于 ",year_hour.format(su.get("d",0l)*1000),"时 过期"));
			View switcher=fv(ni,ismain? id.user_stared: id.user_star);
			seticon(switcher,ismain? icon.stared: icon.star);// 主次帐户星标图片
			View headbox=fv(ni,id.user_head);
			if(ismain) v.addView(ni,0); else v.addView(ni);// 主要/次要帐户排序
			if(uid==0){// 隐身模式-隐藏右侧删除按钮-使用内部图片
				seticon(headbox,icon.user(0));
				fv(ni,id.user_logout_background).setVisibility(View.INVISIBLE);
			}else{
				new HeadLoader(uid, su.get("h", ""),headbox);
				View logout=fv(ni,id.user_logout);
				seticon(logout,icon.nav_cancel);
				logout.setTag(i);
				btnbind(logout);
			}
			if(!ismain){
				switcher.setTag(i);// 设定编号
				btnbind(switcher);// 次要帐户绑定切换操作
			}
		}
		//new Msgbox("",user.toString());
	}
	void loadSetsView(){
		ViewGroup v=(ViewGroup)fv(id.setting_setsViewGroup);
		v.removeAllViews();
		
		FSON ds=defSetting;
		for(int i=0,len=ds.length();i<len;i++){
			ViewGroup ni=(ViewGroup) LayoutInflater.from(this).inflate(
				layout.listsub_classic
				,null);
			FSON s=ds.getList(i);
			((TextView)fv(ni,id.listsub_title)).setText(s.get(0,""));//设置项名称
			//需要判断设置值的数据类型？
			((TextView)fv(ni,id.listsub_desc)).setText(string("当前选择 ",
				s.getList(3).get(//设置值说明（集）
					sets.get(s.get(1,""),0),//设置项id，默认0，确保是数字，
				"")));
			ni.setTag(s.get(1,""));//设其tag为项目id
			v.addView(ni);
			btnbind(fv(ni,id.listsub_bg));
		}
	}
	//FSON sc=null;//settingCenter
	String value2tip(String setkey){//获取当前配置值的提示信息
		FSON j=defSetting;
		for(int i=0,len=j.length();i<len;i++){
			FSON p=j.getList(i);
			if(p.get(0,"").equals(setkey)){
				return p.getList(2).get(
					sets.get(//获取当前设置值，失败则获取默认值，也就是p
						setkey,
						p.get(
							1,//默认数值
							new Object()//由于无法确定默认数值的数据类型
						)
					),
					"");
			}
		}
		return "";
	}
	public<E extends Object>E getDefSet(String name){
		return null;
	}
	public static String getDefaultSetting(){
		return string("[",
			"['“关注动态”列表样式(重启生效)','feedliststyle',0,['简单模式：省内存，有字无图','经典模式：有字但只显示封面图，暂不开放','标准模式：不稳定测试中']],",
			"['清晰度选择','quality',0,['省流量清晰度：MP4，不分段','高清晰度：HDMP4，不分段','原画清晰度：FLV，分段，暂不开放','省流自动模式，暂不开放','高清自动模式，暂不开放']],",
			"['视频播放节点','playport',0,['总是自动选择主节点','总是自动选择首个备用节点','总是让我自行选择']],",
			"['在线视频播放器','playeronline',0,['内置播放器：还有bug要修','第三方播放器：在线播放有点悬','浏览器：发送网址包裹','复制文本']],",
			"['Checksum错误重试次数','checksumretry',5,[0,1,2,3,4,5]],",
			"['弹幕引擎','danmakuengine',1,['禁用','瑞视1号']],",
			"['缩短弹幕的连续重复字符','danmakushortly',0,['不缩短','最多2个重复字符（23333->233）']]",
		"]");
	}
	/*void 预备设置(){//prepare setting data
		String p=setspath(),s=null,//=setsload(),// 2/5
			defdata=getDefaultSetting();
			if(s.isEmpty()){
			new File(p).delete();//maybe that's directory. who knows, whatever
			sets=new FSON(defdata);
			setssave();
			setsload();//fuck! my app has no permission to write setting
		}
	}*/
	/*void 设置项_加载(SettingListControl l){
		l.clear(); //预备设置();// 1/5
		String[]s={"feedliststyle","quality","playport","playeronline","checksumretry","danmakuengine","danmakushortly"};
		l.additem("","当前选择 "+value2tip(s[0]),s[0]);
		l.additem("","当前选择 "+value2tip(s[1]),s[1]);
		l.additem("","当前选择 "+value2tip(s[2]),s[2]);
		l.additem("","当前选择 "+value2tip(s[3]),s[3]);
		l.additem("","当前选择 "+value2tip(s[4]),s[4]);
		l.additem("","当前选择 "+value2tip(s[5]),s[5]);
		l.additem("","当前选择 "+value2tip(s[6]),s[6]);
	}*/
	void setsitem_onclick(String tag){
		//if(idx==0)return;//if add 'support me to make money' option
		FSON j=defSetting;
		String[]s=null;
		String t=null;//选择器标题
		for(int i=0,len=j.length();i<len;i++){//遍历默认配置查找配置id
			FSON p=j.getList(i);
			if(p.get(1,"").equals(tag)){//找到id
				t=p.get(0,"");
				p=p.getList(3);
				s=new String[p.length()];
				for(int i2=0,len2=s.length;i2<len2;i2++)//填充选项
					s[i2]=p.get(i2,"");
				break;
			}
		}
		if(s==null)return;
		int option=getoptions(t,s);
		if(option==-1)return;
		sets.set(tag,option);
		setschanged=true;//标记为“设置已更改”
		loadSetsView();//设置项_加载(slc);
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
}

