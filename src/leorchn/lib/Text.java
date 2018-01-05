package leorchn.lib;

import java.io.*;

public class Text{
	public static String read(String path){
		try {
			File file=new File(path);
			if(file.isFile() && file.exists() && file.canRead()){ //判断文件存在和可读
				//sys.o.pl("读取到文本的编码是 "+encoding);
				//InputStreamReader read = new InputStreamReader(
				InputStream is=new FileInputStream(file);
				String encode=new FileReader(file).getEncoding();//考虑到编码格式
				/*BufferedReader bufferedReader = new BufferedReader(read);
				 String lineTxt = null;
				 StringBuilder tmp=new StringBuilder();
				 while((lineTxt = bufferedReader.readLine()) != null)
				 tmp.append("\n").append(lineTxt);
				 read.close();*/
				ByteArrayOutputStream bytes=new ByteArrayOutputStream();
				int i=-1;
				while((i=is.read())!=-1) bytes.write(i);
				return new String(bytes.toByteArray(),encode);
			}else System.out.println("找不到指定的文件 "+path);
		}catch(Exception e){
			System.out.println("读取文件内容出错 "+path); e.printStackTrace();
		}
		return "";
	}
	public static boolean write(String data,String path,String encode){
		try{
			PrintWriter pw = new PrintWriter(new FileWriter(path));
			pw.print(data);
			pw.close();
			return true;
		}catch(Exception e){}return false;
	}
}
