package leorchn.lib;

import java.io.*;

public class Text{
	public static String TEXT_DEFAULT_ENCODE="UTF-8";
	public static String read(String path){
		return read(path,TEXT_DEFAULT_ENCODE);
	}
	public static String read(String path,String encode){
		try {
			File file=null;
			InputStream is=null;
			if(path.startsWith("/")){
				file=new File(path);
				if(file.isFile() && file.exists() && file.canRead()){ //判断文件存在和可读
					is=new FileInputStream(file);
					encode=new FileReader(file).getEncoding();//考虑到编码格式
				}else{
					System.out.println("找不到指定的文件 "+path);
					return "";
				}
			}else{
				is=leorchn.App.getContext().getAssets().open(path);
			}
			ByteArrayOutputStream bytes=new ByteArrayOutputStream();
			int i=-1;
			while((i=is.read())!=-1) bytes.write(i);
			return new String(bytes.toByteArray(),encode);
		}catch(Throwable e){
			System.out.println("读取文件内容出错 "+path+E.trace(e));
		}
		return "";
	}
	public static boolean write(String data,String path){
		return write(data,path,TEXT_DEFAULT_ENCODE);
	}
	public static boolean write(String data,String path,String encode){
		try{
			PrintWriter pw = new PrintWriter(new FileWriter(path));
			pw.print(data);
			pw.close();
			return true;
		}catch(Throwable e){}
		return false;
	}
}
