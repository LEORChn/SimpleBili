package appforms;
import android.graphics.Bitmap;
import android.webkit.*;
import android.widget.ProgressBar;
//import formevents.帐户登录;
import leorchn.lib.*;
import appforms.Login.WebLogin.LoginStatListener;
import android.app.*;
import java.util.*;
import android.os.*;
import android.os.Process;
import android.content.*;
import android.view.*;
public class Login extends Activity1{
	@Override protected void onCreate(Bundle savedInstanceState){
		addIdleHandler();
		setResult(-5);
		super.onCreate(savedInstanceState);
	}
	WebView w; WebLogin wl; ProgressBar p; ProgressDialog pd;
	int hasinit=0;
	@Override protected boolean onIdle(){
		switch(hasinit){
			case 0: setContentView(layout.activity_login); break;
			case 1:
				pd=new ProgressDialog(this);
				w=(WebView)fv(id.login_webview);
				p=(ProgressBar)fv(id.login_webload);
				wl=new WebLogin(w,p,l);
				btnbind(id.login_goback);
				break;
		}hasinit++;
		return hasinit<9;
	}
	@Override public void onClick(View v){
		if(v.getId()==id.login_goback)finish();
	}

	@Override protected void onDestroy() {
		//w.destroy(); w=null; wl=null; p=null; pd=null;
		System.err.println("----- Calling kill my process id... -----");
		Process.killProcess(Process.myPid());
		super.onDestroy();
	}
	/*public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==4){
			l.onLogin(
				"Cookie: DedeUserID=8232068; DedeUserID__ckMd5=306a54f5179401e0; SESSDATA=a95d8f0a%2c1517515006%2cf867ae44; bili_jct=0a48d75fd66fee9bc98c14b0fd627f5e;"
			);
		}
			
		return false;//super.onKeyDown(keyCode, event);
	}*/
	LoginStatListener l=new LoginStatListener(){
		@Override public void onLogin(final String cok){
			int res=useradd(cok);
			setResult(res,new Intent().putExtra("Cookie",cok).putExtra("user",user.toString()));
			finish();
		}
		@Override public void onLog(String text){
			pd.setMessage(text);
			if(!pd.isShowing())pd.show();
		}
	};
	
	public static class WebLogin extends WebViewClient{
		public WebLogin(WebView w,ProgressBar p,LoginStatListener lsl){
			pb=p; l=lsl;
			clean(w);
			WebSettings s=w.getSettings();
			s.setJavaScriptEnabled(true);
			s.setCacheMode(WebSettings.LOAD_NO_CACHE);
			//s.setAppCacheMaxSize(1048576);
			w.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
			w.setWebChromeClient(wcc);
			w.setWebViewClient(this);
			HashMap<String,String>extHeader=new HashMap<String,String>();
			extHeader.put("User-Agent",UA_android);
			extHeader.put("Referer", "http://m.bilibili.com/space.html");
			w.loadUrl("https://passport.bilibili.com/login", extHeader);
		}ProgressBar pb;LoginStatListener l;
		WebChromeClient wcc=new WebChromeClient(){
			public void onProgressChanged(WebView w,int p){
				pb.setVisibility(p<100?View.VISIBLE:View.INVISIBLE);
				pb.setProgress(p<5?5:p);
			}
		};
		public void onPageStarted(WebView w,String u,Bitmap icon){
			if(tms<1 && u.contains("m.bilibili.com")){
				w.setVisibility(View.GONE);
				l.onLog("加载帐户信息...");//可能会覆盖下方
			}
		}
		int tms=0;
		static final String[]verifyitem={"DedeUserID=","DedeUserID__ckMd5=","SESSDATA=","bili_jct="};
		public void onPageFinished(WebView w,String u){
			if(u.contains("bilibili.com/space")){
				//判断cookie完整性
				CookieManager cm=CookieManager.getInstance();
				String cok=cm.getCookie("bilibili.com");
				for(String each:verifyitem)
					if(!cok.contains(each)){ 
						l.onLog(each+"未找到，重试中 _"+tms); tms++;
						w.loadUrl("http://m.bilibili.com/space.html");
						return;
					}
				w.loadUrl("about:blank");
				//清除所有缓存
				w.freeMemory();
				clean(w);
				l.onLogin("Cookie: "+cok);
			}else if(u.contains("about:blank")){
				
			}
		}
		void clean(WebView w){
			w.clearCache(true);
			w.clearFormData();
			CookieManager.getInstance().removeAllCookie();
			Files.allDelete(simplebili.App.getContext().getDir("webview",0));//Context.MODE_PRIVATE
		}
		/*public boolean shouldOverrideUrlLoading(WebView w,String u){
		 w.loadUrl(u);
		 return true;
		 }*/
		public static interface LoginStatListener{
			void onLogin(String cookie)
			void onLog(String text)
		}
	}
}

