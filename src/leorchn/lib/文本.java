package leorchn.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class 文本 {
		public static String 读取(String 路径){
		try {
			File file=new File(路径);
			if(file.isFile() && file.exists()){ //判断文件是否存在
				//sys.o.pl("读取到文本的编码是 "+encoding);
				InputStreamReader read = new InputStreamReader(
					new FileInputStream(file),
					new FileReader(file).getEncoding());//考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null,tmp="";
				while((lineTxt = bufferedReader.readLine()) != null)
					tmp+=(lineTxt);
				read.close();
				return tmp;
			}else{System.out.println("找不到指定的文件 "+路径);
			}
		}catch(Exception e){
			System.out.println("读取文件内容出错 "+路径);	e.printStackTrace();
		}
		return "";
	}
	public static boolean 写出(String data,String path,String encode){
		try{
			PrintWriter pw = new PrintWriter(new FileWriter(path));
			pw.print(data);
			pw.close();
			return true;
		}catch(Exception e){}return false;
	}
}
