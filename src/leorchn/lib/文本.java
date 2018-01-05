package leorchn.lib;

public class 文本 extends Text{
	public static String 读取(String path){
		return read(path);
	}
	public static boolean 写出(String data,String path,String encode){
		return write(data,path,encode);
	}
}
