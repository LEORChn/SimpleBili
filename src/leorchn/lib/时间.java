package leorchn.lib;

import java.util.Date;

public class 时间{
	public static String 简单时间差(long t){
		long last=Math.abs(System.currentTimeMillis()/1000-t);
		if(last>86400)return Math.round(last/86400)+"天前";
		if(last>3600)return Math.round(last/3600)+"小时前";
		return Math.round(last/60)+"分钟前";
	}
	public static String 动态时间差(long t){
		Date d=new Date();
		d.setTime(1000l*t);
		return 简单时间差(t)+" ("+new java.text.SimpleDateFormat("dd日 HH:mm").format(d)+")";
	} 
}
