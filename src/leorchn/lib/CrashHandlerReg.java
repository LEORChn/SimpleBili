package leorchn.lib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Looper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class CrashHandlerReg{
	public static boolean reg(Activity others){
		if(others==null)return false;
		CrashHandler.getInstance().init(others);
		return true;
	}
}
class CrashHandler implements Thread.UncaughtExceptionHandler {
	private CrashHandler() {}
	private static CrashHandler INSTANCE = new CrashHandler();
	public static CrashHandler getInstance() {return INSTANCE;}

	public static final String TAG = "CrashHandler";
	private Context mContext;
	private Thread.UncaughtExceptionHandler mDefaultHandler;

	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	public void uncaughtException(Thread thread, Throwable ex) {
		final Writer rs = new StringWriter();
		ex.printStackTrace(new PrintWriter(rs));
		final String errRep = rs.toString();
		new Thread() {
			@Override public void run(){
				Looper.prepare();
				new AlertDialog.Builder(mContext).setTitle("温和的错误提示")
					.setMessage("程序发生了一个未处理的错误。程序即将退出，您可将以下信息截图下来提交给客服。\n"+errRep)
					.setNeutralButton("关闭", new OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							System.exit(0);
						}
					}).create().show();
				Looper.loop();
			}
		}.start();
	}
}
