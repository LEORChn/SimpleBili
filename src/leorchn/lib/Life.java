package leorchn.lib;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;

public class Life{
//public static void end(Activity act){ // 这个应该放在 BaseActivity 的覆写 finish() 方法中，覆写的该方法应包含本方法及 super.finish()
//	if(act == null || act.isFinishing() || act.isDestroyed()) return;
//	act.setContentView(new View(act));
//}
	public static void end(ImageView imgv){ // 希望通过这个来正确移除 imageview
		if(imgv == null) return;
		end((BitmapDrawable)imgv.getDrawable());
		imgv.setImageBitmap(null);
		rm(imgv);
	}
	public static void end(BitmapDrawable bd){
		if(bd == null) return;
		end(bd.getBitmap());
	}
	public static void end(Bitmap btm){ // 希望通过这个来正确移除 bitmap
		if(btm == null || btm.isRecycled()) return;
		btm.recycle();
	}
	public static void rm(View v){ // 把一个 view 从它的父 view 中移除
		if(v == null) return;
		ViewParent vp=v.getParent();
		if(vp == null) return;
		ViewGroup vg=(ViewGroup)vp;
		vg.removeView(v);
	}
}
