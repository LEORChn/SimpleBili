package leorchn.lib;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import appforms.*;
import java.io.*;
import static formevents.用户事件.*;
import simplebili.lib.*;
public class WidgetOverride{
	public static boolean visible(View v){return v.getVisibility()==View.VISIBLE;}
	public static void visible(final View v,final boolean visible){new t(){void r(){v.setVisibility(visible?View.VISIBLE:View.GONE);}};}
	public static void setImage(final ImageView v,String path){final File f=new File(path);if(f.exists())new t(){void r(){v.setImageURI(Uri.fromFile(f));}};}
	public static void init_userhead(ImageView iv){
		iv.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
			用户头像_点击();
		}});
	}
	static int scrollparam0=0,scrollparam1=0;
	public static void init(FeedListControl fl){
		ListView l=fl.getMainView();
		l.setOnScrollListener(new AbsListView.OnScrollListener(){
				public void onScrollStateChanged(AbsListView p1, int p2) {
					if(p2==0)列表滚动(scrollparam0,scrollparam1);
				}
				public void onScroll(AbsListView p1, int p2, int p3, int p4) {
					scrollparam0=p2;scrollparam1=p3;
				}
			});
	}
	public static void init(WebView w,final ProgressBar pb){
		WebSettings s=w.getSettings();
		s.setJavaScriptEnabled(true);
		s.setAppCacheMaxSize(1048576);
		//s.setSupportMultipleWindows(true);
		w.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		w.setWebChromeClient(new WebChromeClient(){
			//public void onCreateWindow(
			public void onProgressChanged(WebView w,int p){
				visible(pb,p<100);
				pb.setProgress(p<5?5:p);
					//Toast.makeText(Main_Feeds.getContext(),w.getUrl()+":"+p,0).show();
			}
		});
		w.setWebViewClient(new WebViewClient(){
			public void onPageStarted(WebView w,String u,Bitmap icon){
				网页开始载入(u);
			}
			public void onPageFinished(WebView w,String u){
				网页载入完成(u);
			}
			public boolean shouldOverrideUrlLoading(WebView w,String u){
				w.loadUrl(u);
				return true;
			}
		});
	}
	abstract static class t{
		abstract void r();
		public t(){
			new Handler(Looper.getMainLooper()).post(new Runnable(){public void run(){r();}});
		}
	}
}
