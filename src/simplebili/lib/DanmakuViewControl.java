package simplebili.lib;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import com.LEORChn.SimpleBili.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import android.os.*;
import static leorchn.lib.网络.*;
import static leorchn.lib.Global.*;

//usage://setContentView( new DanmakuViewControl(this,R.layout.,R.id.,50).getBackRootView() );
public class DanmakuViewControl {
	DanmakuViewControl This=this; OnDanmakuLogListener dll=null; static Rect rect=new Rect();
	public DanmakuViewControl(Activity a,int layoutId,int danmakuViewId,int danmakuViewCount){
		vroot=(ViewGroup) LayoutInflater.from(a).inflate(layoutId,null);
		danmakuView=(ViewGroup) vroot.findViewById(danmakuViewId);
		tx=new DMText[danmakuViewCount<1?50:danmakuViewCount];//开始填充弹幕组件池，如果数量无效则默认50
		for(int i=0;i<tx.length;i++) tx[i]=new DMText(a,danmakuView,rect); 
	}
	public ViewGroup getBackRootView(){return vroot;}
	public void setOnDanmakuLogListener(OnDanmakuLogListener dllobj){dll=dllobj;}
	public void updateDanmaku(long time){
		while(time *1000 > timepool[danprog]){
			tx[whatnext].scroll(pool.get(timepool[danprog]),linenext);
			danprog++;
			if(whatnext<tx.length-1)whatnext++;else whatnext=0;
			linenext[0]++;
		}
	}
	public void onVideoSeekto(long time){
		for(int i=0,len=timepool.length;i<len-1;i++)
			if(time<timepool[i]){
				danprog=i;
				break;
			}
	}
	ViewGroup danmakuView,vroot; DMText tx[];
	int whatnext=0;//当前所操作的组件的索引值
	int[]linenext={0};//当前操作的组件应在的行数
	class DMText extends TextView implements Animation.AnimationListener{
		Rect gt; int[]outside; int myline=0;
		public DMText(Context c,ViewGroup root,Rect graphicTool){ super(c);
			setTextAppearance(c,R.style.danmakuText);
			root.addView(this);
			gt=graphicTool;
		}
		public void scroll(String text,int[]outline){
			String[]tparam=text.split(desplit); if(tparam.length<3)return;
			text=tparam[2]; int c=Integer.valueOf(tparam[1]); if(c<0x01000000)c+=0xFF000000;//防止了在一次测试过程中偶发的int溢出错误
			setText(text); setTextColor(c);
			outside=outline; myline=outline[0];
			getPaint().getTextBounds(text,0,text.length(),gt);//计算文字宽度和高度
			AnimationSet an1=new AnimationSet(true); //Toast.makeText(This,This.danmakuView.getWidth()+","+(0-gt.width()),0).show();
			int linepix=gt.height()*myline;
			if(linepix+gt.height()>danmakuView.getHeight()){
				linepix=0;
				outline[0]=0;
			}
			TranslateAnimation ta=new TranslateAnimation(This.danmakuView.getWidth(),0-gt.width(),linepix,linepix);
			an1.addAnimation((Animation)ta);
			an1.setFillAfter(true);
			ta.setDuration(10000);
			ta.setAnimationListener(this);
			startAnimation(an1);
		}
		public void onAnimationEnd(Animation p){ //Toast.makeText(This,"myline: "+myline+"outside "+outside[0],0).show();
			if(myline<outside[0])outside[0]=myline;
		}
		public void onAnimationStart(Animation p){}public void onAnimationRepeat(Animation p){}
	}
	public interface OnDanmakuLogListener{
		void onDanmakuLog(String log)
		void onDanmakuLoadCompleted()
		void onDanmakuLoadError(int errcode,String errmsg)// 1=download,2=format,4=contentRead
	}
	HashMap<Long,String>pool=new HashMap<Long,String>();
	Long[]timepool; int danprog=0;
	static final String split="^;;",desplit="\\^;;";
	public void downAndLoadDanmaku(final String url){
		new AsyncTask<String,Integer,String>(){
			protected void onPreExecute(){ log("\n正在下载弹幕..."+url); }
			protected String doInBackground(String[] p) {
				InputStream input=getdataInputStream("GET",p[0],"","");
				return deflateDecoder(input);
			}
			@Override protected void onPostExecute(String r){
				log("弹幕下载完成，装填中...");
				loadDanmakuString(r);
			}
		}.execute(url);
	}
	public void loadDanmakuString(String content){
		Node doc=getNodeFromXmlString(content);
		if(doc==null){
			log("弹幕数据读取错误。");
			error(2,content);
			return;
		}int location=0; 
		try{
			ArrayList<Long>timelist=new ArrayList<Long>();
			Node[]ds=getNodeArray(getNodeArray(doc,"i")[0],"d");
			pool.clear();
			for(int i=0,len=ds.length;i<len;i++){
				location=i;
				String[]daninf=getNodeParam(ds[i],"p").split(",");
				timelist.add(addtopool(Double.valueOf(daninf[0]),daninf[1],daninf[3],getNodeText(ds[i])));
			}
			timelist.add(0xffffffffffl);//最后一个时间点永远不允许被超越！！
			timepool=timelist.toArray(new Long[timelist.size()]);
		}catch(Exception e){
			log("弹幕数据格式错误。");
			error(4,location+" :location\n"+e+Arrays.toString(e.getStackTrace())+content);
			return;
		}
		Arrays.sort(timepool);
		//pool.put(0l,"");//add while zero exception handler
		log("弹幕装填完毕。");
		completed();
	}
	long addtopool(double time,String mode,String color,String text){
		long serial=((long)(time*100))*10000+pool.size();
		pool.put(serial,mode+split+color+split+text);
		return serial;
	}
	void log(String s){ if(dll!=null)dll.onDanmakuLog(s+"\n"); }
	void error(int i,String s){ if(dll!=null)dll.onDanmakuLoadError(i,s); }
	void completed(){ if(dll!=null)dll.onDanmakuLoadCompleted(); }
	
	Node getNodeFromXmlString(String XmlString){
		try{
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(XmlString.getBytes()));
		}catch(Exception e){}
		return null;
	}
	Node getNode(Node n,String path){
		String[]tp=path.replaceAll("\\\\","/").split("/",2);
		return tp.length==2?getNode(getNodeArray(n,tp[0])[0],tp[1]):getNodeArray(n,tp[0])[0];
	}
	Node[]getNodeArray(Node n,String name){
		NodeList nl=n.getChildNodes();
		ArrayList<Node>na=new ArrayList<Node>();
		for(int i=0,len=nl.getLength();i<len;i++)
			if(nl.item(i).getNodeName().equals(name))
				na.add(nl.item(i));
		return na.toArray(new Node[na.size()]);
	}
	String getNodeText(Node n){ return n.getTextContent(); }
	String getNodeParam(Node n,String name){ return n.getAttributes().getNamedItem(name).getNodeValue(); }
}
/* strings.xml

	<style name="danmakuText">
		<item name="android:singleLine">true</item>
		<item name="android:layout_height">wrap_content</item>
		<item name="android:textAppearance">?android:attr/textAppearanceSmall</item>
		<item name="android:layout_width">wrap_content</item>
		<item name="android:shadowColor">#808080</item>
		<item name="android:shadowDx">0</item>
		<item name="android:shadowDy">0</item>
		<item name="android:shadowRadius">3</item>
	</style>
*/
