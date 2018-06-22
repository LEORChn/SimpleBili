package appforms;

import android.app.*;
import android.content.*;
import android.os.*;
import java.util.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import leorchn.lib.*;
import static leorchn.lib.HttpRequest.*;

public class DailyQuest extends Service1 implements DialogInterface.OnClickListener{
	@Override public int onStartCommand(Intent i, int flags, int startId) {//适应多帐号，此方法只用于添加帐号信息
		String[]coks=i.getStringArrayExtra("cookie");
		//coks=Text.read(getExternalCacheDir().getPath()+"/cookie.txt").split("/n");
		_onekey(coks);
		return super.onStartCommand(i, flags, startId);
	}
	static void p(Object...s){log.set(0,string(buildstring(log.get(0)),s).toString());}
	static void pl(Object...s){log(s);}
	static void log(Object...s){
		log.add(0,string(buildstring(date.format(System.currentTimeMillis())," "),s).toString());
		if(log.size()>49) log.remove(49);
	}
	static SimpleDateFormat date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	void _onekey(String...coks){//<editor-fold defaultstate="collapsed">
		Task[]t=new Task[coks.length];
		for(int i=0,len=t.length;i<len;i++)t[i]=new Task(coks[i]);
		_onekey(t);
	}//</editor-fold>
	void _onekey(Task...tsk){/*//<editor-fold desc="来呀，来双飞呀" defaultstate="collapsed">*/pl("    :start_");
		for(Task t:tsk) ts.add(t);
		exec();
	}
	static ArrayList<String>log=new ArrayList<>();
	static ArrayList<Task>ts=new ArrayList<>();
	static AsyncTask<Void,Void,Void> runtime;
	void exec(){
		runtime=new AsyncTask<Void,Void,Void>(){
			@Override protected Void doInBackground(Void[] p1) {
				while(true){
					for(int i=ts.size()-1;i>-1;i--){
						ts.get(i).print();
						if(ts.get(i)._onekey()>1)ts.remove(i);
					}if(ts.isEmpty())break;
					try{ Thread.sleep(46000);}catch(InterruptedException ex){}
				}
				return null;
			}
		}.execute();
		notice();
	}
	void notice(){
		Notification n=new 通知(){void onclick(){ showcontrol(); }}
			.提示("正在执行任务计划")
			.标题("任务计划")
			.说明("日常持续中")
			.图标ID(draw.seed_silver)
			.运行中(true).自动注销(false)
			.创建();
		this.startForeground(0xC179B30A,n);//dailyquest的九宫格输入的16进制
		//pl(date.format(System.currentTimeMillis())+" stat="+new StringBuilder(Integer.toBinaryString(t.check())).reverse()+",sign="+t.sign+",metalgift="+t.medalgift+",log_web="+t.log_web+",log_mob="+t.log_mob+",v_watch="+t.v_watch+",v_share="+t.v_share+",single="+t.single_stat+",double="+t.double_stat);
	}//</editor-fold >
	boolean alreadyShowControls=false;
	String[]m;
	void showcontrol(){
		if(alreadyShowControls)return;alreadyShowControls=true;
		java.util.List<String>l=new java.util.ArrayList<String>();
		l.add("退出挂机"); //l.add("注销(todo)");
		for(int i=0,len=log.size();i<len;i++)
			l.add(log.get(i));
		m=l.toArray(new String[1]);
		AlertDialog d=new AlertDialog.Builder(this)/*.setTitle("控制面板")*/
			.setCancelable(false).setItems(m, this).create();
		d.getWindow().setType(android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		d.show();
	}
	public void onClick(DialogInterface v,int i){
		if(i==0)confirmExit();
	}
	void confirmExit(){
		new Msgbox("确认退出？","","是","取消"){
			@Override void onClick(int i){
				if(i==vbyes)
					android.os.Process.killProcess(android.os.Process.myPid());
			}};
	}
	static class Task{
		public Task(String cookie){ cok=cokfix(cookie); }
		static HashMap<Integer,Integer>pm;//pm=PriceMap<GiftId,SilverPrice>
		String cok; Tax tl;//stat 1|=network 2|=loginStat 4|=liveApi 8|=homeApi
		int stat=0,sign=0,medalgift=0,log_web=0,log_mob=0,v_watch=0,v_share=0,single_stat=0,double_stat=0;
		public int check(){/*//<editor-fold desc="检查网络、帐号以及俩站API的状态" defaultstate="collapsed">*/pl("    : check");
			if((stat&3)==1)return stat;//network ok but cookie timeout
			int net=0,log=0,liveapi=0,homeapi=0;
			String s=http("get","http://api.live.bilibili.com/i/api/taskInfo",cok,"");
			FSON j=new FSON(s);
			if(j.canRead()){ net|=1;//network ok
				if(j.get("code",-1)==0){ log|=2;//login success
					j=j.getObject("data");
					if(j!=null){ liveapi|=4;//api using success
						FSON sinf=j.getObject("sign_info"),
							linf=j.getObject("double_watch_info");
						sign=sinf.get("status", 0);
						log_web=linf.get("web_watch", 0);
						log_mob=linf.get("mobile_watch", 0);
						double_stat=linf.get("status", 0);//1=待领奖 2=已领奖
						single_stat=j.getObject("watch_info").get("status", 0);
					}
				}//cookie timeout or account is not usable
			}
			s=http("get","http://account.bilibili.com/home/reward",cok,"");
			j=new FSON(s);
			if(j.canRead()){ net|=1;//network ok
				if(j.get("code",-1)==0){ log|=2;//login success
					j=j.getObject("data");
					if(j!=null){ homeapi|=8;//api using success
						v_watch=j.get("watch_av", false)?1:v_watch;//由于此任务有几小时延迟，在做完任务后不会立即展现
						v_share=j.get("share_av", false)?1:0;
						//if(j.get("watch_av", false))pl(j.toString());
					}
				}
			}
			if((sign & v_share & double_stat) ==0){//重置部分无法检测的任务的状态，如粉丝奖励和应援团亲密
				medalgift=0;
			}
			return(stat=net|log|liveapi|homeapi);
		}//</editor-fold >
		public void sign(){/*//<editor-fold desc="签到" defaultstate="collapsed">*/pl("    : sign");
			if((stat&3)<3)return;
			String s=http("get","http://api.live.bilibili.com/sign/doSign",cok,"");
			FSON j=new FSON(s);
			if(j.canRead())
				switch(j.get("code", -1)){
					case 0: case -500:sign=1;
				}
		}//</editor-fold >
		public String checkbag(){/*//<editor-fold defaultstate="collapsed">*/pl("    : check_bag");
			if((stat&3)==1)return"包裹：请重新登录。";
			String s=http("get","http://api.live.bilibili.com/gift/v2/gift/bag_list",cok,"");
			FSON j=new FSON(s);
			if(j.canRead()&&false)
				if(j.get("code", -1)==0){
					try{
						j=j.getObject("data").getList("list");
						int len=j.length();
						String s2="";
						if(len>0) for(int i=0;i<len;i++){
								FSON li=j.getObject(i);
								s2+=li.get("gift_num", 0)+li.get("gift_name", "")+" ";
							};
						return s;
					}catch(Exception e){}
				}
			return s;
		}//</editor-fold>
		public String todaygift(){//<editor-fold defaultstate="collapsed">
			FSON j=new FSON(checkbag()),l,li,op=new FSON("{}");;
			if(j.canRead()){
				if(j.get("code", -1)==0)
					try{
						j=j.getObject("data");
						l=j.getList("list");
						int price=0,snack=0,soil=0,kitty=0; long serverNow=j.get("time", 0);
						for(int i=0,len=l.length();i<len;i++){
							li=l.getObject(i);
							if(1l+li.get("expire_at", 0)>serverNow+29*86400){
								switch(li.get("gift_id", 0)){
									case 1: snack+=li.get("gift_num", 0);break;
									case 3: soil+=li.get("gift_num", 0);break;
									case 4: kitty+=li.get("gift_num", 0);break;
								}
							}
						} price=100*snack + 9900*soil + 5000*kitty;
						pl(price+"="+snack+"/"+soil+"/"+kitty);
						return op.toString();//未完成，此处该返回由总计价值及其他3种奖品的个数
					}catch(Exception e){}
				return "{'code':-2}";
			}return "{'code':-1}";
		}//</editor-fold >
		public String dailybag(){/*//<editor-fold desc="等级、勋章和社团的每日和每周奖品" defaultstate="collapsed">*/pl("    : get_bag:"+medalgift);
			if((stat&3)<3)return"{'code':-2,'msg':'每日礼物：请重新登录。','data':[]}";
			String s=http("get","http://api.live.bilibili.com/giftBag/getSendGift",cok,"");
			FSON j=new FSON(s);
			if(j.canRead() && j.get("code", -1)==0){//成功登记领取
				if(j.getList("data").length()==0 || medalgift>3)//没有新道具或者已经连续领了5次，不领了
					medalgift=1;
				else{//领到新的道具，记录所得并再领一次
					medalgift++;
					dailybag();
				}
				return s;
			}
			return j.canRead()?s:"{'code':-1,'msg':'每日礼物：网络错误。','data':[]}";
		}//</editor-fold>
		public void dailyvwatch(){/*//<editor-fold defaultstate="collapsed">*/pl("    : video_watch");
			if((stat&3)<3)return;
			String s=http("get","http://api.bilibili.com/x/v2/history?ps=1",cok,"");
			FSON j=new FSON(s);
			if(j.canRead()){
				try{
					j=j.getList("data").getObject(0);
					SimpleDateFormat df=new SimpleDateFormat("MM.dd");//空值时返回2月29号（68169600）
					if(!df.format(1000l*j.get("view_at",68169600)).equals(df.format(System.currentTimeMillis()))){
						String s2=http("POST", "http://api.bilibili.com/x/report/web/heartbeat",cok,"aid="+j.get("aid",0)+"&cid="+j.getObject("page").get("cid",0)+"&played_time="+j.get("progress",0));
						pl(s2);
						if(new FSON(s2).canRead())v_watch=1;
					}else v_watch=1;
				}catch(Exception e){}
			}
		}//</editor-fold >
		public void dailyvshare(){/*//<editor-fold defaultstate="collapsed">*/pl("    : share");
			if((stat&3)<3)return;
			String aid=rndaid(),ct_add=string("aid=",aid,"&"),ct_first=string("type=0&id=",aid,"&"),
				param="appkey=c1b107428d337928",//&build=411007&platform=android//好像不需要网址参数也行？
				s=http("POST","http://api.bilibili.com/x/share/add?"+param,cok+"\r\nContent-Length: "+ct_add.length(),ct_add);
			FSON j=new FSON(s);//s=http("POST","http://api.bilibili.com/x/share/first?"+param,cok+"\r\nContent-Length: "+ct_first.length(),ct_first);
			if(j.canRead()){

			}pl(aid+":"+s);
		}//</editor-fold >
		public void online_mobile(){/*//<editor-fold desc="手机登录5分钟" defaultstate="collapsed">*/pl("    : mob_online");
			if((stat&3)<3)return;
			String prms="appkey=c1b107428d337928",
				checksum=md5(prms+"ea85624dfcf12d7cc7b2b3a94fac1f2c").toLowerCase(),
				s=http("GET","http://live.bilibili.com/mobile/userOnlineHeart?"+prms+"&sign="+checksum,cok+
					   "\r\nUser-Agent: Mozilla/5.0 BiliDroid/4.11.7 (bbcallen@gmail.com)","");
			pl(s);
		}//</editor-fold>
		public void online_web(){/*//<editor-fold desc="电脑登录5分钟" defaultstate="collapsed">*/pl("    : web_online");
			if((stat&3)<3)return;
			String s=http("post","http://api.live.bilibili.com/User/userOnlineHeart",cok,"");
			pl(s);
		}//</editor-fold>
		public void support(){

		}
		public int _onekey(){//<editor-fold desc="一键执行所有，需要酌情在一定周期内重复调用。以后提供一个返回值，0=网络问题，1=执行中，2=帐号失效，3=执行完毕" defaultstate="collapsed">
			switch(stat&3){
				case 0:check();break;
				case 1:return 2;
			}
			if(sign==0)sign();
			if(0==0)online_mobile();
			if(log_web==0)online_web();
			if(medalgift==0)pl(dailybag());
			if(v_watch==0)dailyvwatch();
			if(v_share==0)dailyvshare();
			if((log_mob&log_web)==1){
				getReward(); return -3;
			}return 1;
		}//</editor-fold >
		public void print(){
			pl(date.format(System.currentTimeMillis())+cok);
			pl(string("stat=",buildstring(Integer.toBinaryString(check())).reverse(),",sign=",sign,",metalgift=",medalgift,",log_web=",log_web,",log_mob=",log_mob,",v_watch=",v_watch,",v_share=",v_share,",single=",single_stat,",double=",double_stat));
		}
		public void getReward(){/*<editor-fold desc="完成任务领奖" defaultstate="collapsed">*/pl("    : get_reward");
			if(single_stat==1)http("post","http://api.live.bilibili.com/activity/v1/task/receive_award",cok,"task_id=single_watch_task");
			if(double_stat==1)http("post","http://api.live.bilibili.com/activity/v1/task/receive_award",cok,"task_id=double_watch_task");
		}//</editor-fold >
		public int canfeed(int target_id){//<editor-fold desc="得到该勋章今天可增的数值" defaultstate="collapsed">
			String s=http("get","http://api.live.bilibili.com/i/api/medal?pageSize=25",cok,"");
			FSON j=new FSON(s);
			if(j.canRead() && j.get("code",-1)==0){
				j=j.getObject("data");
				int had=j.get("count",0);
				pl("had: "+had+"/"+j.get("medalCount",20));
				j=j.getList("fansMedalList");
				for(int i=0;i<had;i++){
					FSON li=j.getObject(i);
					if(li.get("target_id",0)==target_id){p(target_id+": "+li.get("dayLimit", 500));
						return li.get("dayLimit", 500)-Integer.valueOf(li.get("todayFeed", "2000"));}
				}return -2;//no medal
			}return -1;//fail to get
		}//</editor-fold >
		public HashMap<String,Integer>matchfeed(int canfeed){ return matchfeed(canfeed,10); }//0=无天数限制
		public HashMap<String,Integer>matchfeed(int canfeed,int daylimit){//内部开发。由于检查包裹需要用到动态方法，因此该方法不能声明静态
			HashMap<String,Integer>m=new HashMap<>();
			int last=canfeed; if(last<0)return m;//表示今日剩余可增亲密度，小于0表示帐号有问题
			if(updatePriceMap()){
				FSON j=new FSON(checkbag());
				if(j.canRead()){
					try{
						j=j.getObject("data");
						long serverNow=j.get("time", 0);
						j=j.getList("list"); pl("剩余亲密："+last);
						int mostprice=0;long bagid=0;//价值规则：高优先级，只有当某一组物品总值超过3万时触发（价值超过300辣条），有活动时成功率高。一旦成功，取60%
						for(int i=0,len=j.length();i<len;i++){
							FSON li=j.getObject(i);
							if(li.get("expire_at", 0)==0) continue;//没有过期时间？直接跳过
							int group=pm.get(li.get("gift_id", 0))*li.get("gift_num", 0);//物品组的总亲密度
							if(group>mostprice){mostprice=group; bagid=li.get("bag_id", 0);}
						}
						if(mostprice>30000)for(int i=0,len=j.length();i<len;i++){
								FSON li=j.getObject(i);
								if(bagid!=li.get("bag_id", 0))continue;//直接判断bagid，不符的跳过
								int giftid=li.get("gift_id", 0),
									single=pm.get(giftid)/100,//物品组的单个亲密度
									count=li.get("gift_num", 200),
									sendcount=Math.min((int)(.6f*last/single),count-200/single);
								if(sendcount>0)m.put(bagid+"i"+giftid,sendcount ); last-=sendcount*single; 
								if(li.set("gift_num", count-sendcount))j.set(i,li);
								pl(li.get("gift_name", "")+"（"+((li.get("expire_at", 0)-serverNow)/86400)+"天）"+count+" -"+sendcount+" 剩余亲密:"+last);break;//加一次到表里面之后就表示结束了，进入下一个规则
							}//-----规则分割线-----
						for(int i=0,len=j.length();i<len;i++){//日期规则：低优先级，但无论怎样都会执行
							FSON li=j.getObject(i);
							long dayleft=(li.get("expire_at", 0)-serverNow)/86400;
							if(Math.abs(dayleft)>daylimit) continue;//取绝对值的原因是，部分包裹物品没有过期时间（也就是0）会导致时间直接低于现在很多天而被处理，为了防止此事发生
							int giftid=li.get("gift_id", 0);
							switch(giftid){
								case 3: if(dayleft>7)break; //只有不足7天的B克拉才会充税
								default:
									boolean finalday=dayleft==0;//时间仅限1天则无条件赠送（以后引入single_gift_to的变量，表示仅赠送一个人时才会开启此项目）
									int single=pm.get(giftid)/100,
										count= finalday? li.get("gift_num", 0): Math.min(li.get("gift_num", 0),last / single);
									String ids=li.get("bag_id", 0)+"i"+giftid;//多个规则可能会覆写赠送数量，为解决此问题故判断containsKey
									last-=single*count;
									if(count>0)
										if(m.containsKey(ids)) m.put(ids, m.get(ids)+count);
										else m.put(ids, count);
									pl(li.get("gift_name", "")+"（"+dayleft+"天）"+li.get("gift_num", 0)+" -"+count+" 剩余亲密:"+last);
									if(finalday)continue;//可能还有最后一天的礼物，跳过检查
							}if(last<1)break;//亲密度超额检查
						}
					}catch(Exception e){e.printStackTrace();}
				}
			}
			return m;
		}
		static boolean updatePriceMap(){//<editor-fold desc="获取所有类型的道具的价值" defaultstate="collapsed">
			if(pm==null)pm=new HashMap<>();else if(!pm.containsValue(999999))return true;//价格表为空或者有无法读取的数值时，重载；否则直接返回true
			String s=http("get","http://api.live.bilibili.com/gift/v2/live/room_gift_list","","");
			FSON j=new FSON(s);
			pm.put(9, 4500); pm.put(10, 19900);//手动加入便当和胖次的瓜子数
			if(j.canRead()){
				try{
					j=j.getList("data");
					for(int i=0,len=j.length();i<len;i++){
						FSON li=j.getObject(i);
						pm.put(li.get("id", -1), li.get("price", 999999));
					}return true;
				}catch(Exception e){}
			}return false;
		}//</editor-fold>
		String rndaid(){//<editor-fold desc="根据历史记录，或者完全随机选择一个av号" defaultstate="collapsed">
			String s=http("get","http://api.bilibili.com/x/v2/history?ps=20",cok,"");
			FSON j=new FSON(s);
			java.util.Random r=new java.util.Random();
			if(j.canRead()){
				try{
					j=j.getList("data");
					int len=j.length();
					if(len>0){
						int[]vids=new int[len];
						for(int i=0;i<len;i++)
							vids[i]=j.getObject(i).get("aid",0);
						return String.valueOf(vids[r.nextInt(len)]);
					}
				}catch(Exception e){}
			}return String.valueOf(r.nextInt(20000000));//根据历史记录进行的随机失败，开始全局随机
		}//</editor-fold>
		static String cokfix(String c){//<editor-fold desc="用此修复Cookies格式问题" defaultstate="collapsed">
			String tmp=c.trim().toLowerCase();
			if(!tmp.startsWith("cookie:")) c="Cookie: "+c;
			if(!tmp.endsWith(";")) c+=";";
			return c;
		}//</editor-fold>
		public void addOnTaxListener(Tax listener){ tl=listener; }
		interface Tax{
			void onChange(String json);
		}
	}
	static String md5(String s){//<editor-fold desc="32位md5" defaultstate="collapsed">
		try{
			MessageDigest m=MessageDigest.getInstance("MD5");
			return bytestohex(m.digest((s).getBytes("utf-8")));
		}catch(Exception e){}
		return "";
	}//</editor-fold>
	static String bytestohex(byte[] bytes) {// <editor-fold desc="字节数组转换为字符串形式" defaultstate="collapsed">
		StringBuffer md5str = new StringBuffer();		// 把数组每一字节换成16进制连成md5字符串
		int digital;
		for(int i = 0;i < bytes.length;i++){
			digital = bytes[i];
			if(digital < 0){
				digital += 256;
			}
			if(digital < 16){
				md5str.append("0");
			}
			md5str.append(Integer.toHexString(digital));
		}
		return md5str.toString().toUpperCase();
	}// </editor-fold>
				
}
