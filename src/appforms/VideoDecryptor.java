package appforms;

import leorchn.lib.*;
import android.view.*;
import android.content.*;
import android.app.*;
import android.widget.*;

public class VideoDecryptor extends Activity1 {
	static Key key = Key.getNextKey(null);
	public static void start(Activity1 a, String aid, String cid){
		a.startActivity(new Intent(a, VideoDecryptor.class)
						.putExtra("aid", aid)
						.putExtra("cid", cid));
	}
	@Override public void onClick(View v) {
		// TODO: Implement this method
	}
	TextView log;
	@Override protected void oncreate(){
		Window w = getWindow();
		WindowManager.LayoutParams wp = w.getAttributes();
		wp.alpha = .8f; // 0全透明 1不透明
		w.setAttributes(wp);
		
		log=new TextView(this);
		log.setText("", TextView.BufferType.EDITABLE);
		setContentView(log);
		
		key=Key.getNextKey(null);
		log(string("加载默认密钥：", key.name()));
	}
	
	int tries=5;
	@Override protected boolean onIdle(){
		Intent i = getIntent();
		final String aid = i.getStringExtra("aid"),
			cid = i.getStringExtra("cid");
		log("开始解析...");
		new Http("get", getDecryptorUrl(cid), getDecryptorHeader(aid), ""){
			@Override public void onload(String d){
				FSON j=new FSON(d), root=j;
				if(j.canRead()){
					j=j.getList("durl");
					if(j==null || j.length()==0){ log("durl 数据空白.."); r(d,root); return; }
					VideoPlaySimple.start(VideoDecryptor.this, j.getObject(0).get("url", ""), aid, cid, false);
					finish();
					return;
				}
				r(d,j);
			}
			void r(String data, FSON j){
				handleTheError(j);
				retry(data,aid,cid);
			}
		};
		return false;
	}
	void retry(String d, String aid, String cid){
		tries--;
		if(tries==0){
			log("无法解析视频...");
			log(string("aid=", aid, ", cid=", cid));
			log(d);
		}else{
			addIdleHandler();
		}
	}
	static String getDecryptorUrl(String cid){
		String quality = "64",
			p = String.format("appkey=%s&cid=%s&otype=json&qn=%s&quality=%s&type=", key.appkey, cid, quality, quality);
		return string("http://interface.bilibili.com/playurl?",getChecksumMixed(p));
	}
	static String getDecryptorHeader(String aid){
		return string("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36",
			"\nCookie: CURRENT_FNVAL=16",
			"\nReferer: https://www.bilibili.com/video/av", aid, "/");
	}
	static String getChecksumMixed(String param){
		return string(param, "&sign=", getChecksum(string(param, key.seckey)));
	}
	static String getChecksum(String param){
		return Crypt.md5(param);
	}
	void handleTheError(FSON j){
		if(j==null || !j.canRead() || j.get("code", 0)==0) return;
		Error errtype=Error.getErrorByCode(j.get("code", 0));
		if(errtype != null) log(string("===== ", errtype.msgcn));
		switch(errtype){
			case INVALID_SIGNATURE:
			case INVALID_APPKEY:
				key=Key.getNextKey(key);
				log(string("尝试切换到密钥：", key.name()));
				if(key != Key.FINAL) tries++;
				break;
			default:
				log(string(j.get("result","")," code ", j.get("code",0), ": ",j.get("message", "null?")));
		}
	}
	boolean log(String s){
		if(isFinishing()) return false;
		log.append(string(s, "\n"));
		return true;
	}
}
enum Key{ // 要确保谷歌搜不到代码里包含的 key 才行
	OLD("84 95 65 60 bc 02 8e b7", "94 ab a5 4a f9 06 5f 71 de 72 f5 50 8f 1c d4 2e"), // 版本未知，19年2月失效，具体时间不记得
	UNK1("f3 bb 20 8b 3d 08 1d c8", "1c 15 88 8d c3 16 e0 5a 15 fd d0 a0 2e d6 58 4f"), // 版本未知，来源丢失
	NORMAL_411007("c1 b1 07 42 8d 33 79 28", "ea 85 62 4d fc f1 2d 7c c7 b2 b3 a9 4f ac 1f 2c"), // 普通版v4.11.7
	NORMAL_519000("1d 8b 6e 7d 45 23 34 36", "56 0c 52 cc d2 88 fe d0 45 85 9e d1 8b ff d9 73"), // 普通版v5.19甚至v5.27还在用
	TV("44 09 e2 ce 8f fd 12 b8", "59 b4 3e 04 ad 69 65 f3 43 19 06 2b 47 8f 83 dd"), // TV版，版本未知，来源 https://blog.kaaass.net/archives/947
	FINAL("iV GU Tj sx vp Le uD Cf", "aH Rm hW ML kd eM uI Lq OR nY Zo cw MB pM EO dt"); // 版本未知，来源 you-get
	public final String appkey, seckey, name;
	Key(String appkey, String seckey){
		this.appkey=appkey.replaceAll("\\s",""); this.seckey=seckey.replaceAll("\\s","");
		this.name=new StringBuilder(name()).append(String.format("(%s)", appkey.substring(0,4))).toString();
	}
	public static Key getNextKey(Key prev){
		boolean pickthis=false;
		for(Key k:$VALUES) if(pickthis) return k; else if(k==prev) pickthis = true;
		return $VALUES[0];
	}
}
enum Error{
	INVALID_SIGNATURE(10002, "Invalid signature parameter.", "签名参数无效或计算错误，或此签名无权解析此视频。"),
	INVALID_APPKEY   (10003, "Invalid appkey parameter.",    "公钥无效或不存在。"),
	VIDEO_IS_HIDDEN  (10004, "Video is hidden.",             "视频已经被隐藏，无法解析。");
	public final int code;
	public final String message, msgcn;
	Error(int code, String message, String cn){
		this.code=code; this.message=message; this.msgcn=cn;
	}
	/*Error(int code){
		this.code=code; this.message=msgcn=new StringBuilder(code).append(" - VideoDecryptor unknown error").toString();
	}*/
	public static Error getErrorByCode(int c){
		for(Error e:$VALUES) if(e.code==c) return e;
		return null;
	}
}
