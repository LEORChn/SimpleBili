package appforms;
import android.content.*;
import android.net.*;
import android.view.*;
import leorchn.lib.*;

public class VideoDetailEntrance extends Activity1 {
	@Override protected void oncreate(){
		Intent i = getIntent();
		try{
			String s = null;
			if(Intent.ACTION_SEND.equals(i.getAction()))
				s = i.getStringExtra(Intent.EXTRA_TEXT);
			if(Intent.ACTION_VIEW.equals(i.getAction())){
				s = i.getData().getPath();
				if("bilibili".equals(i.getScheme()))
					s = s.replace("/", "/av");
			}
			s = Regex.match(s, "/av([0-9]*)", 1);
			startActivity(new Intent(this, VideoDetail.class)
				.putExtra("vid", s));
		}catch(Throwable e){
			tip(string("文本中没有包含有效的视频链接:\n", i.getStringExtra(Intent.EXTRA_TEXT), E.trace(e)));
		}
		finish();
	}
	
	@Override public void onClick(View v) {
	}
	
	@Override protected boolean onIdle(){
		return false;
	}
}
