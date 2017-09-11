package leorchn.lib;
import org.json.JSONArray;
import org.json.JSONObject;
public class FSON {
	boolean baseisobj=false,usable=false;
	String name=null;
	JSONObject baseobj;JSONArray basearr;
	public FSON(String json){init(json,"");}
	public FSON(String json,String name){init(json,name);}
	public FSON(JSONObject json){baseobj=json;baseisobj=true;usable=true;}
	private void init(String json,String name){
		try{
			baseobj=new JSONObject(json);
			baseisobj=true;
		}catch(Exception e){
			try{
				basearr=new JSONArray(json);
			}catch(Exception e2){
				return;//just return and fuck who new this class within that json text
			}
		}
		this.name=name;
		usable=true;
	}
	public boolean canRead(){return usable;}
	public boolean exists(String path){	
		int lastIndexOfShortPath=path.replaceAll("\\\\","/").lastIndexOf("/");
		try{
			return getObject(path.substring(0,lastIndexOfShortPath))
				.baseobj.has(path.substring(lastIndexOfShortPath+1));
		}catch(Exception e){}
		return false;
	}
	public static FSON fromXML(String xml){
		try{
			return new FSON(org.json.XML.toJSONObject(xml));
		}catch(Exception e){}
		return new FSON("");
	}
	public <E extends Object> E get(String key,E defval){
		try{
			if(usable && baseisobj && baseobj.has(key)){
				Object val = baseobj.opt(key);
				if(JSONObject.NULL.equals(val))return defval;
				if(defval.getClass().isAssignableFrom(val.getClass()))//checked
					return (E)val;
				if(defval.getClass()==Double.class && val.getClass()==Integer.class){
					Double d=Double.parseDouble(val.toString());
					return (E)d;}
				if(defval.getClass()==String.class)//checked
					return (E)val.toString();
			}
		}catch(Exception e){}
		return defval;
	}
	public <E extends Object> E get(int index,E defval){
		try{
			if(usable && (!baseisobj) && basearr.length()>index){
				Object val = basearr.opt(index);
				if(JSONObject.NULL.equals(val))return defval;
				if(defval.getClass().isAssignableFrom(val.getClass()))//checked
					return (E)val;
				if(defval.getClass()==Double.class && val.getClass()==Integer.class){
					Double d=Double.parseDouble(val.toString());
					return (E)d;}
				if(defval.getClass()==String.class)//checked
					return (E)val.toString();
			}
		}catch(Exception e){}
		return defval;
	}
	public boolean set(String key,Object value){//return true if successfully set the value
		try{
			baseobj.put(key,value);
			return true;
		}catch(Exception e){}return false;
	}
	public String getName(){return name;}
/*	public FSON getObjectPossible(String path){
		JSONObject tmp=baseobj;
		int deep=0;String[]tp2=null;
		try{
			if(!(usable && baseisobj))return null;
			String[] tp=path.replaceAll("\\\\","/").split("/");tp2=tp;
			for(String tp1 : tp){
				tmp = tmp.getJSONObject(tp1);
				deep++;
			}
			return new FSON(tmp.toString(0),tp[tp.length-1]);
		}catch(Exception e){}
		if(deep==0 || tp2==null)return this;
		return new FSON(tmp.toString(0),tp2[deep-1]);
	}*/
	public FSON getObject(String path){
		try{
			if(!(usable && baseisobj))return null;
			String[] tp=path.replaceAll("\\\\","/").split("/");
			JSONObject tmp=baseobj;
			for(String tp1 : tp)
				if(!tp1.equals(""))tmp = tmp.getJSONObject(tp1);
			return new FSON(tmp.toString(0),tp[tp.length-1]);
		}catch(Exception e){}
		return null;
	}
	public FSON getObject(int index){
		try{
			if(!baseisobj && usable)
				return new FSON(basearr.getJSONObject(index).toString(0));
		}catch(Exception e){}
		return null;
	}
	public FSON getList(String key){
		try{
			if(baseisobj && usable)
				return new FSON(baseobj.getJSONArray(key).toString(0),key);
		}catch(Exception e){}
		return null;
	}
	public FSON getList(int index){
		try{
			if(!baseisobj && usable)
				return new FSON(basearr.getJSONArray(index).toString(0));
		}catch(Exception e){}
		return null;
	}
	public boolean isList(){return !baseisobj;}
	public boolean isObject(){return baseisobj;}
	public int length(){
		if(baseisobj){
			return baseobj.length();
		}else{
			return basearr.length();
		}
	}
	@Override public String toString() {
		try{
			if (baseisobj) {
				return baseobj.toString(0);
			} else {
				return basearr.toString(0);
			}
		}catch(Exception e){}return "";
	}
}
