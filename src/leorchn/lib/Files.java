package leorchn.lib;
import java.io.File;

public class Files{
	public static boolean allDelete(String path){
		return allDelete(new File(path));
	}
	public static boolean allDelete(File dir){
		try{
			if(dir.isDirectory()){
				File[]fl=dir.listFiles();
				for(File f:fl) allDelete(f);
			}
			return dir.delete();
		}catch(Exception e){return false;}
	}
	public static boolean inAllDelete(String path){
		try{
			File f=new File(path);
			if(f.isDirectory()){
				File[]fs=f.listFiles();
				for(File fi:fs) allDelete(fi);
				for(File fi:fs) if(fi.exists()) return false;
				return true;
			}else return false;
		}catch(Exception e){return false;}
	}
}
