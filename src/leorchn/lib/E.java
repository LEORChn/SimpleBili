package leorchn.lib;
import java.io.*;
public class E{
	public static String trace(Throwable e){
		Writer rs = new StringWriter();
		e.printStackTrace(new PrintWriter(rs));
		return rs.toString();
	}
}
