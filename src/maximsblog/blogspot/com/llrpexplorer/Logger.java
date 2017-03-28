package maximsblog.blogspot.com.llrpexplorer;

import android.util.Log;

public class Logger {

	private String mTag;
	
	public Logger(Class clazz) {
		mTag = clazz.getName();
	}
	
	public Logger(String tag) {
		mTag = tag;
	}
	
	public void warn(String msg){
		Log.d(mTag, msg);
	}
	public void info(String msg){
		Log.i(mTag, msg);
	}
	public void debug(String msg){
		Log.d(mTag, msg);
	}
	public static Logger getLogger(Class clazz) {
		return new Logger(clazz);
		
	}
	
	public static Logger getLogger(String tag) {
		return new Logger(tag);
		
	}

	public void error(String msg) {
		Log.e(mTag, msg);
	}

	public boolean isDebugEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isInfoEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isWarnEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isErrorEnabled() {
		// TODO Auto-generated method stub
		return true;
	}
}
