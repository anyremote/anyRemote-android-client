//
// anyRemote android client
// a bluetooth/wi-fi remote control for Linux.
//
// Copyright (C) 2011-2016 Mikhail Fedotov <anyremote@mail.ru>
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//

package anyremote.client.android;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.Vector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import anyremote.client.android.util.Address;
import anyremote.client.android.util.ProtocolMessage;
import anyremote.client.android.R;
 
public class anyRemote extends Activity 
                       implements Handler.Callback {
	public static final int DISCONNECTED  = 0;
	public static final int CONNECTING    = 1;
    public static final int CONNECTED     = 2;
	public static final int LOSTFOCUS     = 4;
	public static final int COMMAND       = 5;
	public static final int DO_EXIT       = 6;
	public static final int DO_CONNECT    = 7;
	public static final int DO_DISCONNECT = 8;
	//public static final int SHOW_LOG      = 8;

	public static final int SWIPE_MIN_DISTANCE = 120;
	public static final int SWIPE_THRESHOLD_VELOCITY = 200; 

	static final int  NO_FORM       = 0;
	static final int  SEARCH_FORM   = 1;
	static final int  CONTROL_FORM  = 2;
	static final int  FMGR_FORM     = 3;
	static final int  TEXT_FORM     = 4;
	static final int  LIST_FORM     = 5;
	static final int  EDIT_FORM     = 6;
	static final int  WMAN_FORM     = 7;
    static final int  LOG_FORM      = 8;
    static final int  MOUSE_FORM    = 9;
    static final int  KEYBOARD_FORM = 10;
    static final int  WEB_FORM      = 11;
	static final int  DUMMY_FORM    = 12;

	static final int  LOG_CAPACITY = 16384;

	static final String  CONN_ADDR      = "ADR";
	static final String  CONN_NAME      = "CNM";
	static final String  CONN_PASS      = "CNP";
	static final String  ACTION         = "ACT";
	static final String  SWITCHTO       = "SWT";

	private static final int PERMISSION_REQUEST_CODE = 1;

	int         prevForm = NO_FORM;
	private static int  currForm = NO_FORM;
	static int         status;
	static Dispatcher  protocol;

	public static boolean finishFlag   = false;
	public static boolean firstConnect = true;
	
	static TreeMap<String,Bitmap> iconMap = new TreeMap<String,Bitmap>();
	static TreeMap<String,Bitmap> coverMap = new TreeMap<String,Bitmap>();

	private static Handler globalHandler     = null;

	private static DateFormat now_format = new SimpleDateFormat("HH:mm:ss");
	private static Date teraz = new Date();
	
	// Logging stuff
	public static StringBuilder logData = null;
	
	// Wait indicator stuff
	private static ProgressDialog waiting = null;

	private static int numeratorVar = 0;

    private static final Object syncObj = new Object();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
			logData = new StringBuilder(LOG_CAPACITY);
			
			_log("onCreate "+android.os.Build.MODEL+ " " +android.os.Build.VERSION.CODENAME+" "+android.os.Build.VERSION.RELEASE);
			
			protocol = new Dispatcher(this);

			//requestWindowFeature(Window.FEATURE_NO_TITLE);
			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

			setContentView(R.layout.main);

			currForm        = DUMMY_FORM;
			status          = DISCONNECTED;
			finishFlag      = false;
			
			if (globalHandler == null) {
					globalHandler = new Handler(this);
			}

			MainLoop.enable();
		} else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
			new AlertDialog.Builder(this)
					.setTitle("Phone State Needed")
					.setMessage("This app needs to know the state of your phone")
					.setPositiveButton("OK", (dialog, which) -> {
						ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_PHONE_STATE }, PERMISSION_REQUEST_CODE);
					})
					.setNegativeButton("Cancel", (dialog, which) -> {
						dialog.dismiss();
					})
					.create()
					.show();
		} else {
			ActivityCompat.requestPermissions(this, new String[]{ android.Manifest.permission.READ_PHONE_STATE }, PERMISSION_REQUEST_CODE);
		}
	}

	@Override
	protected void onStart() {
		_log("onStart "+currForm);		
		super.onStart();
	}
	
	@Override
	protected void onPause() {
		_log("onPause "+currForm);		
		super.onPause();
	}

	@Override
	protected void onResume() {
		//logData = ""; // remove old log
		_log("onResume "+currForm);	
		super.onResume();

		if (finishFlag) {
			doExit();
			return;
		}

		if (currForm != LOG_FORM && status == DISCONNECTED) {
		    currForm = NO_FORM;
		    setCurrentView(SEARCH_FORM,"");
		}
	}

	@Override
	protected void onStop() {
		_log("onStop");		
	    super.onStop();	
	}

	@Override
	protected void onDestroy() {	
		_log("onDestroy");

		super.onDestroy();
		
		finishFlag = true;
		currForm = NO_FORM;
		status = DISCONNECTED;
		protocol.disconnect(true);
		MainLoop.disable();
	}

	public void setPrevView(int which) {
		_log("setPrevView " + which);
		prevForm = which;
	}

	public void setCurrentView(int which, String subCommand) {
		_log("setCurrentView " + getScreenStr(which) + " (was " + getScreenStr(currForm) + ") finish="+finishFlag);
		
        if (which == LOG_FORM ||
	        which == MOUSE_FORM ||
	        which == KEYBOARD_FORM ||
	        which == WEB_FORM) {
            _log("setCurrentView wrong switch option. Skip it.");
            return; 
        }

		if (finishFlag) {
            return; // on destroy
        }

		if (currForm == which) {
			_log("setCurrentView TRY TO SWITCH TO THE SAME FORM ???");
			//if (currForm != SEARCH_FORM) {
				_log("setCurrentView SKIP SWITCH TO THE SAME FORM ???");
				return;
			//}
		}
		
		prevForm = currForm;
		currForm = which;

		if (protocol == null) {
			return;
		}

		if (currForm != prevForm) {
						
			// finish current form
			switch (prevForm) { 
			case SEARCH_FORM:
				_log("[AR] setCurrentView mess SEARCH_FORM with some other");
				break;
	
			case CONTROL_FORM:
			case LIST_FORM:
			case TEXT_FORM:
			case WMAN_FORM:
				_log("setCurrentView stop "+prevForm);
				protocol.sendToActivity(prevForm, Dispatcher.CMD_CLOSE,ProtocolMessage.FULL);
				
	            break;
	
			//case LOG_FORM:
	        //case MOUSE_FORM:
	        //case KEYBOARD_FORM:
	        //case WEB_FORM:
			case DUMMY_FORM:
				break;
	
			}
		}
		
		if (prevForm != LOG_FORM      && 
		    prevForm != MOUSE_FORM    &&
		    prevForm != KEYBOARD_FORM &&
		    prevForm != WEB_FORM) {
	     	protocol.menuReplaceDefault(currForm);
		}

		switch (currForm) { 
		case SEARCH_FORM:
			final Intent doSearch = new Intent(getBaseContext(), SearchForm.class);
			String id = String.format("%d",numerator());
			doSearch.putExtra("SUBID", id);
			_log("setCurrentView start SearchForm "+id);
			startActivity(doSearch); 
			break;

		case CONTROL_FORM:
			_log("setCurrentView start ControlScreen");
			final Intent control = new Intent(getBaseContext(), ControlScreen.class);
			startActivity(control); 
			break;

		case LIST_FORM:
			_log("setCurrentView start ListScreen");
			final Intent showList = new Intent(getBaseContext(), ListScreen.class);
			startActivity(showList); 
			break;

		case TEXT_FORM:
			_log("setCurrentView start TextScreen");
			final Intent showText = new Intent(getBaseContext(), TextScreen.class);
			showText.putExtra("SUBID", subCommand);
			startActivity(showText); 
			break;
			
		case WMAN_FORM:
			_log("setCurrentView start WinManager");
			final Intent showWman = new Intent(getBaseContext(), WinManager.class);
			startActivity(showWman); 
			break;

		/* ???
        case MOUSE_FORM:
            _log("setCurrentView start MouseWin");
            final Intent showMou = new Intent(getBaseContext(), MouseScreen.class);
            startActivity(showMou); 
            break;

		case KEYBOARD_FORM:
            _log("setCurrentView start KeyboardWin");
            final Intent showKbd = new Intent(getBaseContext(), KeyboardScreen.class);
            startActivity(showKbd); 
            break;

		case LOG_FORM:
			_log("setCurrentView start TextScreen (LOG)");
			final Intent showLog = new Intent(getBaseContext(), TextScreen.class);
			showLog.putExtra("SUBID", "__LOG__");
			startActivity(showLog); 
			break;
        */
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) { 

		menu.clear();
		MenuInflater mi = getMenuInflater();

		if (status == DISCONNECTED) { 
			mi.inflate(R.menu.menu, menu);
		} else {
			mi.inflate(R.menu.menu2, menu);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {

		case R.id.connect_main:
			_log("onOptionsItemSelected connect_main");
			setCurrentView(SEARCH_FORM,"");
			return true;

		case R.id.disconnect_main:
		    _log("onOptionsItemSelected disconnect_main");
			protocol.disconnect(true);
			return true;

		case R.id.exit_main:
			_log("onOptionsItemSelected exit_main");
			doExit();
			return true;

		case R.id.log_main:
			_log("onOptionsItemSelected log_main");
			setCurrentView(LOG_FORM,"");
			return true;					
		}

		// else - user defined items
		//cScreen.commandAction(item.getTitle().toString());
		return true;
	}

	public static void sendGlobal(int id, Object obj) {
		anyRemote._log("sendGlobal: "+id);
		if (globalHandler != null) {
			Message msg = globalHandler.obtainMessage(id, obj);
		    msg.sendToTarget();
		}
	}
	
	// get messages sent from sendGlobal()
	public boolean handleMessage(Message msg) {

		switch(msg.what){
		
		case CONNECTED:
			
			anyRemote._log("handleMessage: CONNECTED");
			//Toast.makeText(client, R.string.connection_successful, Toast.LENGTH_SHORT).show();
			
			try {
			    protocol.connected((Connection) msg.obj);
			} catch (Exception e) {  // once got ClassCastException here
			    anyRemote._log("handleMessage: CONNECTED got exception ");
			    protocol.disconnect(true);
				return true;
			}
			handleEvent(CONNECTED);
			break;
		
        case CONNECTING:
			
			anyRemote._log("handleMessage: CONNECTING");
			handleEvent(CONNECTING);
			break;
			
		case DISCONNECTED:
			
			anyRemote._log("handleMessage: DISCONNECTED");
			
			if (msg.obj != null ) {
				Resources res = getResources();
				
				String alert = res.getString(R.string.connection_failed);
				if (((String) msg.obj).length() > 0) {
					alert += "\n"+(String) msg.obj;
				}
			    Toast.makeText(this, alert, Toast.LENGTH_LONG).show();
			}
			
			protocol.disconnected(true);
			handleEvent(DISCONNECTED);
			break;	
			
		case LOSTFOCUS:
			
			anyRemote._log("handleMessage: LOST FOCUS");
			
			protocol.disconnected(false);
			handleEvent(LOSTFOCUS);
			break;	
			
		case anyRemote.COMMAND:
			
			anyRemote._log("handleMessage: COMMAND");
			protocol.handleCommand((ProtocolMessage) msg.obj);
			break;			

		case anyRemote.DO_CONNECT:
			
			anyRemote._log("handleMessage: DO_CONNECT");
			if (msg.obj != null ) {
				Address conn = (Address) msg.obj;
				setProgressBarIndeterminateVisibility(true);
                status = CONNECTING;
				protocol.doConnect(conn.name, conn.URL, conn.pass);
			} else {
				setCurrentView(DUMMY_FORM, "");
			}
			break;			

		case anyRemote.DO_EXIT:

			anyRemote._log("handleMessage: DO_EXIT");
			//doExit(); -- do exit from onResume()
			currForm = NO_FORM;
			finishFlag = true;

			break;			

		case anyRemote.DO_DISCONNECT:
			
			anyRemote._log("handleMessage: DO_DISCONNECT");
			protocol.disconnect(true);
			break;			

		/*case anyRemote.SHOW_LOG:
			
			anyRemote._log("handleMessage: SHOW_LOG");
			setCurrentView(LOG_FORM, "");
			break;*/			
		}
		return true;
	}
	
	private void handleEvent(int what) {
		
		_log("handleEvent");
		switch (what) {
			case CONNECTED:
			
				_log("handleEvent: Connection established");
		
				status = CONNECTED;
				setProgressBarIndeterminateVisibility(false);
		
				if (currForm != LOG_FORM) {
					_log("handleEvent: switch to CONTROL_FORM");
				    setCurrentView(CONTROL_FORM,"");
				}
				break;

			case CONNECTING:   // should not happens (did not send such messages)
				
			    status = CONNECTING;

			case DISCONNECTED:
			case LOSTFOCUS:

				_log("handleEvent: Connection or focus lost");
				status = DISCONNECTED;
				//protocol.closeCurrentScreen(currForm);
				
				if (!finishFlag) {   // this happens on exit
					
					// send quit to all registered activity
					protocol.sendToActivity(-1, Dispatcher.CMD_CLOSE,ProtocolMessage.FULL);
		
					if (currForm != LOG_FORM) {
						currForm = DUMMY_FORM;  // trick
					}
					
					if (currForm != LOG_FORM) {
						_log("handleEvent: switch to SEARCH_FORM");
						setCurrentView(SEARCH_FORM,"");
					}
				}
		
				break;
			default:
				_log("handleEvent: unknown event");
		}
	}

	public static int icon2int(String btn) {

		if (btn == null) return R.drawable.icon;

		if (btn.equals("default"))  return R.drawable.def;
		if (btn.equals("down")) 	return R.drawable.down;
		if (btn.equals("file")) 	return R.drawable.file;
		if (btn.equals("fit")) 		return R.drawable.fit;
		if (btn.equals("folder")) 	return R.drawable.folder;
		if (btn.equals("forward")) 	return R.drawable.forward;
		if (btn.equals("fullscreen")) return R.drawable.fullscreen;
		if (btn.equals("info")) 	return R.drawable.info;
		if (btn.equals("left")) 	return R.drawable.left;
		if (btn.equals("minus")) 	return R.drawable.minus;
		if (btn.equals("mute")) 	return R.drawable.mute;
		if (btn.equals("next")) 	return R.drawable.next;
		if (btn.equals("no")) 		return R.drawable.no;
		if (btn.equals("pause")) 	return R.drawable.pause;
		if (btn.equals("play")) 	return R.drawable.play;
		if (btn.equals("plus")) 	return R.drawable.plus;
		if (btn.equals("prev")) 	return R.drawable.prev;
		if (btn.equals("question")) return R.drawable.question;
		if (btn.equals("refresh")) 	return R.drawable.refresh;
		if (btn.equals("rewind")) 	return R.drawable.rewind;
		if (btn.equals("right")) 	return R.drawable.right;
		if (btn.equals("stop")) 	return R.drawable.stop;
		if (btn.equals("up")) 		return R.drawable.up;
		if (btn.equals("vol_down")) return R.drawable.vol_down;
		if (btn.equals("vol_up")) 	return R.drawable.vol_up;
		
		if (btn.equals("click_icon")) return R.drawable.click_icon;
		if (btn.equals("transparent")) return R.drawable.transparent;

		return R.drawable.icon;
	}

	public static Bitmap getIconBitmap(Resources resources, String icon) {
		
		if (icon.equals("none")) {	
			return null;
		}
		
		synchronized (syncObj) {
		
			if (iconMap.containsKey(icon)) {
				return (Bitmap) iconMap.get(icon);
			}
			
			int iconId = icon2int(icon);
	
			//_log("getIconBitmap "+icon+" "+iconId);
			if (iconId == R.drawable.icon) {
	
				File dir = Environment.getExternalStorageDirectory();
				File iFile = new File(dir, "Android/data/anyremote.client.android/files/icons/"+icon+".png");
	
				if(iFile.canRead()) {
					_log("getIconBitmap", icon+" found on SDCard"); 
					Bitmap ic = BitmapFactory.decodeFile(iFile.getAbsolutePath());
					if (ic == null) {
						_log("getIconBitmap", "seems image "+icon+" is broken");	
						iFile.delete();
					} else {
					    iconMap.put(icon,ic);
					}
					return ic;
				} else {
					_log("getIconBitmap", iFile.getAbsolutePath()+" absent on SDCard");
					
					// try to auto upload it
					protocol.autoUploadIcon(icon);
					return null;
				}
			}
			
			Bitmap ic = BitmapFactory.decodeResource(resources, icon2int(icon));
			iconMap.put(icon,ic);
			return ic;
		}
	}
	
	public static Bitmap getCoverBitmap(Resources resources, String name) {
		
		if (name.equals("none")) {	
			return null;
		}
		
		synchronized (syncObj) {
		
			if (coverMap.containsKey(name)) {
				return (Bitmap) coverMap.get(name);
			}
	
			File dir = Environment.getExternalStorageDirectory();
			File iFile = new File(dir, "Android/data/anyremote.client.android/files/covers/"+name+".png");

			if(iFile.canRead()) {
				_log("getCoverBitmap", name+" found on SDCard"); 
				Bitmap ic = BitmapFactory.decodeFile(iFile.getAbsolutePath());
				if (ic == null) {
					_log("getCoverBitmap", "seems image "+name+" is broken");	
					iFile.delete();
				} else {
				    coverMap.put(name,ic);
				}
				return ic;
			}
			_log("getCoverBitmap", iFile.getAbsolutePath()+" absent on SDCard");
		}
		
		// try to auto upload it
		protocol.autoUploadCover(name);
		return null;
	}

	public static void clearCache() {
		
		synchronized (syncObj) {
			iconMap.clear();
		}	
		synchronized (syncObj) {
			coverMap.clear();	
		}
	}

	public static int parseColor(Vector vR, int start) {
		if (vR.size() < start + 3) {
			// what about "yellow" ?
			//if (!c.startsWith("#")) {
			//	c = "#" + c;
			//}
			try {
		        return Color.parseColor((String) vR.elementAt(start));
		    } catch (Exception e) {
		    	return Color.parseColor("#000000");
		    }
		}
		return parseColor((String) vR.elementAt(start),
                          (String) vR.elementAt(start+1),
                          (String) vR.elementAt(start+2));
	}
	
	private static int parseColor(String r, String g, String b) {
		int[] RGB = new int[3];
		try {
			RGB[0] = Integer.parseInt(r);
			RGB[1] = Integer.parseInt(g);
			RGB[2] = Integer.parseInt(b);

			int i;
			for (i=0;i<2;i++) {
				if (RGB[i]<0  ) RGB[i] = 0;
				if (RGB[i]>255) RGB[i] = 255;
			}
		} catch (Exception e) { 
			RGB[0] = 0;
			RGB[1] = 0;
			RGB[2] = 0;
		}
		return Color.rgb(RGB[0], RGB[1], RGB[2]);
	}	

	void doExit() {
		// how to do exit ?
		_log("doExit");
		finishFlag = true;
		currForm = NO_FORM;
		protocol.disconnect(true);
		//super.onBackPressed();
		finish();
	}

	public static int getCurScreen() {
		return currForm;
	}
	
	public static String getScreenStr(int form) {
		switch (form) {
            case NO_FORM:      return "NO";
            case SEARCH_FORM:  return "SEARCH";
            case CONTROL_FORM: return "CONTROL";
            case FMGR_FORM:    return "FMGR";
            case TEXT_FORM:    return "TEXT";
            case LIST_FORM:    return "LIST";
            case EDIT_FORM:    return "EDIT";
            case WMAN_FORM:    return "WMAN";
            case LOG_FORM:     return "LOG";
            case WEB_FORM:     return "WEB";
            case DUMMY_FORM:   return "DUMMY";
		}
		return "UNKNOWN";
	}

	public static void popup(Activity cxt, boolean show, boolean update, String msg) {
		_log("popup " + show + " " +msg);

		//cxt.setProgressBarIndeterminateVisibility(show);
		if (show && !update && waiting != null) {  // do not recreate
		    return;	
		}
		if (waiting != null) {
			waiting.dismiss();
			waiting = null; 
		}
		if (show) {
			waiting = new ProgressDialog(cxt, ProgressDialog.STYLE_HORIZONTAL);
			waiting.setMessage(msg);
			waiting.show();
		} 
	}
	
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					
					if (!(inetAddress.isLoopbackAddress() || 
					     inetAddress.getHostAddress().contains(":"))) {  // avoid IPv6 addresses
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			return null;
		}
		return null;
	}

	public static boolean logVisible() {
		return (currForm == LOG_FORM);
	}

	private static void _log(String log) {
		_log("anyRemote", log);	
	}

	public static void _log(String prefix, String msg) {
		
        //synchronized (logData) {
		//if (logData == null) {
		//	return;
		//}
        
            if (logData != null && logData.length() > LOG_CAPACITY) {
			    logData.delete(0,LOG_CAPACITY);		
		    }

		    teraz.setTime(java.lang.System.currentTimeMillis());
		    if (logData != null) {
		    	logData.append("\n").append("[").append(now_format.format(teraz)).append("] [").append(prefix).append("] ").append(msg);
			}
		    Log.i(prefix,msg);
        //}
	}
	
	public static int numerator() {
		numeratorVar++;
		return numeratorVar;
	}
}
