package leorchn.lib;
import java.io.*;
public class E{
	public static String trace(Throwable e){
		Writer rs = new StringWriter();
		if(e==null) return "null";
		e.printStackTrace(new PrintWriter(rs));
		return rs.toString();
	}
}
