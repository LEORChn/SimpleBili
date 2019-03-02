package leorchn.lib;

import java.math.*;
import java.security.*;

public class Crypt {
	public static String MD5(String s){ return MD5(s).toUpperCase(); }
	public static String md5(String s){
		try{
			return new BigInteger(1,MessageDigest.getInstance("MD5").digest(s.getBytes("utf-8"))).toString(16).toLowerCase();
		}catch(Throwable e){}
		return "";
	}
}
