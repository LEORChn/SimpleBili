/* Created by LEOR_Chn
 * License Copyright(R) LEOR_Chn (2014-2017)
 * LEOR_Chn Soft.
 */
package leorchn.lib;
import java.util.regex.*;
public class Regex {
	public static String match(String text,String regex){return match(text,regex,0);}
	//0为全文匹配，有小括号后从1开始为匹配小括号
	public static String match(String text,String regex,int index){
		Matcher m = Pattern.compile(regex).matcher(text);
		return m.find()&&m.groupCount()>=index?m.group(index):"";
	}
}
