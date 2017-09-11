package appforms;

import com.LEORChn.SimpleBili.R;
import android.app.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import leorchn.lib.*;

public class UpZone extends Activity implements OnClickListener,MessageQueue.IdleHandler {
	Activity This;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CrashHandlerReg.reg(this);
		This=this;
		Looper.myQueue().addIdleHandler(this);
	}
	public boolean queueIdle(){
		
		return false;
	}
	
	public void onClick(View p1) {
		// TODO: Implement this method
	}
}
