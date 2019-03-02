package leorchn.lib;

import com.LEORChn.SimpleBili.*;

public interface Consts{
	public static String DEBUG_MODE_FILE_TAG = "/sdcard/Android/SB_DEBUG"; // 公开出去以方便修改启动时是否直接进入调试模式
	public static boolean IS_DEBUG_VERSION = BuildConfig.DEBUG, // 程序是否以调试版签名运行
		DEBUG_MODE_INIT = IS_DEBUG_VERSION || new java.io.File(DEBUG_MODE_FILE_TAG).exists(); // 程序启动时调试模式的初始状态
	//protected static Icon icon;
	public static R.id id;
	public static R.layout layout;
	public static R.drawable drawable,draw,d;//可以给 R 类定义快捷方式和多个别名而且不用额外声明，超爽
	public static R.color color;
	public static R.menu menu;
	public static R.string string;
	//public static R.array array;
	
	public static final String
		PERMISSION_NAME_INSTALL_SHORTCUT="com.android.launcher.permission.INSTALL_SHORTCUT",
		ACTION_IMAGE_CROP="com.android.camera.action.CROP";
}
