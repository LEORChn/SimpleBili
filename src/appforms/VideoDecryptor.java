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
		switch(Error.getErrorByCode(j.get("code", 0))){
			case INVALID_SIGNATURE:
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
enum Key{
	TV("4409e2ce8ffd12b8", "59b43e04ad6965f34319062b478f83dd"),
	FINAL("iVGUTjsxvpLeuDCf", "aHRmhWMLkdeMuILqORnYZocwMBpMEOdt");
	public final String appkey, seckey;
	Key(String appkey, String seckey){
		this.appkey=appkey; this.seckey=seckey;
	}
	public static Key getNextKey(Key prev){
		boolean pickthis=false;
		for(Key k:$VALUES) if(pickthis) return k; else if(k==prev) pickthis = true;
		return $VALUES[0];
	}
}
enum Error{
	INVALID_SIGNATURE(10002, "Invalid signature parameter.", "签名参数无效/计算错误。");
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
