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

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.TableLayout;
import android.widget.TextView;
//import android.text.method.ScrollingMovementMethod;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.KeyEvent;
import anyremote.client.android.util.InfoMessage;
import anyremote.client.android.util.ProtocolMessage;

public class ControlScreen 
       extends arActivity 
       implements View.OnClickListener,
                  // View.OnTouchListener,
                  KeyEvent.Callback, OnGestureListener {

    static final int KEY_NUM1 = 1;
    static final int KEY_NUM2 = 2;
    static final int KEY_NUM3 = 3;
    static final int KEY_NUM4 = 4;
    static final int KEY_NUM5 = 5;
    static final int KEY_NUM6 = 6;
    static final int KEY_NUM7 = 7;
    static final int KEY_NUM8 = 8;
    static final int KEY_NUM9 = 9;
    static final int KEY_STAR = 10;
    static final int KEY_NUM0 = 0;
    static final int KEY_POUND = 12;
    static final int KEY_UNKNOWN = -1;

    static final String STR_NUM1 = "1";
    static final String STR_NUM2 = "2";
    static final String STR_NUM3 = "3";
    static final String STR_NUM4 = "4";
    static final String STR_NUM5 = "5";
    static final String STR_NUM6 = "6";
    static final String STR_NUM7 = "7";
    static final String STR_NUM8 = "8";
    static final String STR_NUM9 = "9";
    static final String STR_STAR = "*";
    static final String STR_NUM0 = "0";
    static final String STR_POUND = "#";
    static final String STR_COVER = "COVER";
    static final String STR_UNKNOWN = "";
 
    static final int SK_DEFAULT = 0;
    static final int SK_BOTTOMLINE = 1;

    static final int NUM_ICONS = 12;
    static final int NUM_ICONS_BTM = 7;

    // private static final int SWIPE_MAX_OFF_PATH = 250;

    static final int[] btns3x4 = { R.id.b1, R.id.b2, R.id.b3, R.id.b4, R.id.b5, R.id.b6, R.id.b7, R.id.b8, R.id.b9,
            R.id.b10, R.id.b0, R.id.b11 };

    static final int[] lbtns3x4 = { R.id.tl_b1, R.id.tl_b2, R.id.tl_b3, R.id.tl_b4, R.id.tl_b5, R.id.tl_b6, R.id.tl_b7,
            R.id.tl_b8, R.id.tl_b9, R.id.tl_b10, R.id.tl_b11, R.id.tl_b12 };

    static final int[] btns3x4_r90 = { R.id.b1_r90, R.id.b2_r90, R.id.b3_r90, R.id.b4_r90, R.id.b5_r90, R.id.b6_r90,
            R.id.b7_r90, R.id.b8_r90, R.id.b9_r90, R.id.b10_r90, R.id.b11_r90, R.id.b12_r90 };

    static final int[] lbtns3x4_r90 = { R.id.tl_b1_r90, R.id.tl_b2_r90, R.id.tl_b3_r90, R.id.tl_b4_r90, R.id.tl_b5_r90,
            R.id.tl_b6_r90, R.id.tl_b7_r90, R.id.tl_b8_r90, R.id.tl_b9_r90, R.id.tl_b10_r90, R.id.tl_b11_r90,
            R.id.tl_b12_r90 };

    static final int[] btns3x4_r270 = { R.id.b1_r270, R.id.b2_r270, R.id.b3_r270, R.id.b4_r270, R.id.b5_r270,
            R.id.b6_r270, R.id.b7_r270, R.id.b8_r270, R.id.b9_r270, R.id.b10_r270, R.id.b11_r270, R.id.b12_r270 };

    static final int[] lbtns3x4_r270 = { R.id.tl_b1_r270, R.id.tl_b2_r270, R.id.tl_b3_r270, R.id.tl_b4_r270,
            R.id.tl_b5_r270, R.id.tl_b6_r270, R.id.tl_b7_r270, R.id.tl_b8_r270, R.id.tl_b9_r270, R.id.tl_b10_r270,
            R.id.tl_b11_r270, R.id.tl_b12_r270 };

    static final int[] btns7x1 = { R.id.bb1, R.id.bb2, R.id.bb3, R.id.bb4, R.id.bb5, R.id.bb6, R.id.bb7 };

    static final int[] lbtns7x1 = { R.id.tl_bb1, R.id.tl_bb2, R.id.tl_bb3, R.id.tl_bb4, R.id.tl_bb5, R.id.tl_bb6,
            R.id.tl_bb7 };

    boolean fullscreen     = false;
    boolean ignoreOneClick = false;
    boolean switchedToPrivateScreen = false;
    Dispatcher.ArHandler hdlLocalCopy;

    ImageButton[] buttons;
    LinearLayout[] buttonsLayout;
    private GestureDetector gestureScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefix = "ControlScreen"; // log stuff
        log("onCreate");

        buttons = new ImageButton[NUM_ICONS];
        buttonsLayout = new LinearLayout[NUM_ICONS];
 
        gestureScanner = new GestureDetector(this);

        hdlLocalCopy = new Dispatcher.ArHandler(anyRemote.CONTROL_FORM, new Handler(this));
        anyRemote.protocol.addMessageHandler(hdlLocalCopy);
    }

    /*
     * @Override protected void onStart() { log("onStart"); super.onStart(); }
     */

    @Override
    protected void onPause() {
        log("onPause");
        hidePopup();
        super.onPause();
    }

    @Override
    protected void onResume() {
        log("onResume");

        super.onResume();

        switchedToPrivateScreen = false;

        if (anyRemote.status == anyRemote.DISCONNECTED) {
            log("onResume no connection");
            doFinish("");
            return;
        }

        redraw();

        exiting = false;
    }

    @Override
    protected void onStop() {
        log("onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        log("onDestroy");
        anyRemote.protocol.removeMessageHandler(hdlLocalCopy);
        super.onDestroy();
    }

    @Override
    protected void onUserLeaveHint() {
        log("onUserLeaveHint");
        // no time to sending events
        // commandAction(anyRemote.protocol.context.getString(R.string.disconnect_item));
        if (!switchedToPrivateScreen && !exiting && anyRemote.protocol.messageQueueSize() == 0) {
            log("onUserLeaveHint - make disconnect");
            anyRemote.protocol.disconnect(false);
        }
    }

    public void handleEvent(InfoMessage data) {

        log("handleEvent " + Dispatcher.cmdStr(data.id));

        // checkPopup();

        if (data.stage != ProtocolMessage.FULL && // process only full commands
                data.stage == ProtocolMessage.FIRST) {
            return;
        }

        if (handleCommonCommand(data.id)) {
            return;
        }

        if (data.id == Dispatcher.CMD_VOLUME) {
            Toast.makeText(this, "Volume is " + anyRemote.protocol.cfVolume + "%", Toast.LENGTH_SHORT).show();
            return;
        } else if (data.id == Dispatcher.CMD_IMAGE || // some icon was uploaded,
                                                      // need to redraw
                data.id == Dispatcher.CMD_REPAINT) {
            log("handleEvent need to update icons");
        }

        redraw();
    }

    private void setTitleField() {
        // log("setTitleField "+anyRemote.protocol.cfTitle);
        TextView title = (TextView) findViewById(getTitleId());
        title.setText(anyRemote.protocol.cfTitle);
        title.setSelected(true);
        //title.setMovementMethod(new ScrollingMovementMethod());
    }

    private void setStatusField() {
        // log("setStatusField "+anyRemote.protocol.cfStatus);
        TextView status = (TextView) findViewById(getStatusId());
        status.setText(anyRemote.protocol.cfStatus);
        status.setSelected(true);
        //status.setMovementMethod(new ScrollingMovementMethod());
    }

    private void setSkinSimple() {

        // log("setSkinSimple ");

        Display display = getWindowManager().getDefaultDisplay();

        int h = display.getHeight();
        int w = display.getWidth();

        int pd = anyRemote.protocol.cfPadding;

        if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE) {

            // log("setSkin SK_BOTTOMLINE");

            setContentView(R.layout.control_form_bottomline);

            for (int b = 0; b < 7; b++) {
                buttons[b] = (ImageButton) findViewById(btns7x1[b]);
                buttonsLayout[b] = (LinearLayout) findViewById(lbtns7x1[b]);
            }
            for (int b = 7; b < 12; b++) {
                buttons[b] = null;
                buttonsLayout[b] = null;
            }

            int realCnt = 0;
            for (int i = 0; i < NUM_ICONS_BTM; i++) {
                Bitmap ic = anyRemote.getIconBitmap(getResources(), anyRemote.protocol.cfIcons[i]);

                buttons[i].setImageBitmap(ic);

                if (ic == null) {
                    buttons[i].setVisibility(View.GONE);
                    log("setSkin SK_BOTTOMLINE hide " + i);
                } else {
                    buttons[i].setVisibility(View.VISIBLE);
                    realCnt++;
                }

                buttons[i].setOnClickListener(this);
                // buttons[i].setOnTouchListener(this);
            }

            if (realCnt == 0) {
                realCnt = 7;
            }

            anyRemote.protocol.cfIconSize = w / realCnt;

            int isz = (anyRemote.protocol.cfIconSizeOverride >= 0 ? anyRemote.protocol.cfIconSizeOverride
                    : anyRemote.protocol.cfIconSize);

            log("setSkin set icon size to " + isz);

            for (int i = 0; i < NUM_ICONS_BTM; i++) {

                buttons[i].setMaxHeight(isz);
                buttons[i].setMaxWidth(isz);
                buttons[i].setMinimumHeight(isz);
                buttons[i].setMinimumWidth(isz);

                buttonsLayout[i].setPadding(pd, pd, pd, pd);
            }

            int sz = (w > h ? h : w);
            ImageView cover = (ImageView) findViewById(R.id.cover);
            cover.setMaxHeight((2 * sz) / 3);
            cover.setMaxWidth((2 * sz) / 3);
            cover.setBackgroundColor(anyRemote.protocol.cfBkgr);
            cover.setOnClickListener(this);

            if (anyRemote.protocol.cfInitFocus >= 0 && anyRemote.protocol.cfInitFocus < NUM_ICONS_BTM) {
                buttons[anyRemote.protocol.cfInitFocus].requestFocus();
                buttons[anyRemote.protocol.cfInitFocus].requestFocusFromTouch();
            }
        } else {

            log("setSkinSimple SK_DEFAULT ");
            boolean landscape = false;

            if (display.getOrientation() == Surface.ROTATION_90) {

                setContentView(R.layout.control_form_default_r90);

                landscape = true;

                for (int b = 0; b < 12; b++) {
                    buttons[b] = (ImageButton) findViewById(btns3x4_r90[b]);
                    buttonsLayout[b] = (LinearLayout) findViewById(lbtns3x4_r90[b]);
                }

            } else if (display.getOrientation() == Surface.ROTATION_270) {

                setContentView(R.layout.control_form_default_r270);

                landscape = true;

                for (int b = 0; b < 12; b++) {
                    buttons[b] = (ImageButton) findViewById(btns3x4_r270[b]);
                    buttonsLayout[b] = (LinearLayout) findViewById(lbtns3x4_r270[b]);
                }

            } else {

                setContentView(R.layout.control_form_default);

                for (int b = 0; b < 12; b++) {
                    buttons[b] = (ImageButton) findViewById(btns3x4[b]);
                    buttonsLayout[b] = (LinearLayout) findViewById(lbtns3x4[b]);
                }
            }

            h = landscape ? h / 5 : h / 6; // 3 or 4 rows with icons and 2 line
                                           // of text + gaps
            w = landscape ? w / 4 : w / 3; // 3 or 4 columns with icons

            anyRemote.protocol.cfIconSize = (w > h ? h : w);

            int isz = (anyRemote.protocol.cfIconSizeOverride >= 0 ? anyRemote.protocol.cfIconSizeOverride
                    : anyRemote.protocol.cfIconSize);

            for (int i = 0; i < NUM_ICONS; i++) {

                // if (anyRemote.protocol.cfIcons[i] != "none") {
                // log("setSkinSimple get bitmap "+anyRemote.protocol.cfIcons[i]);
                // }
                Bitmap ic = anyRemote.getIconBitmap(getResources(), anyRemote.protocol.cfIcons[i]);
                if (ic == null) { // no to squeze
                    if (anyRemote.protocol.cfIcons[i] != "none") {
                        log("setSkinSimple failed to get bitmap " + anyRemote.protocol.cfIcons[i]);
                    }
                    ic = anyRemote.getIconBitmap(getResources(), "transparent");
                }

                buttons[i].setImageBitmap(ic);

                buttons[i].setVisibility(View.VISIBLE);
                buttons[i].setOnClickListener(this);
                // buttons[i].setOnTouchListener(this);

                buttons[i].setMaxHeight(isz);
                buttons[i].setMaxWidth(isz);
                buttons[i].setMinimumHeight(isz);
                buttons[i].setMinimumWidth(isz);

                buttonsLayout[i].setPadding(pd, pd, pd, pd);
            }

            if (anyRemote.protocol.cfInitFocus >= 0 && anyRemote.protocol.cfInitFocus < NUM_ICONS_BTM) {
                buttons[anyRemote.protocol.cfInitFocus].requestFocus();
                buttons[anyRemote.protocol.cfInitFocus].requestFocusFromTouch();
            }
        }
    }

    private void redraw() {

        anyRemote.protocol.setFullscreen(this);

        synchronized (anyRemote.protocol.cfTitle) {

            setTitle(anyRemote.protocol.cfCaption);

            setSkinSimple();
            setFont();
            setTextColor();
            setBackground();

            setTitleField();
            setStatusField();

            setCover();
        }
    }

    private void setBackground() {

        if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE) {

            ImageView cover = (ImageView) findViewById(R.id.cover);
            cover.setBackgroundColor(anyRemote.protocol.cfBkgr);

            RelativeLayout ll = (RelativeLayout) findViewById(R.id.skin_bottomline);
            ll.setBackgroundColor(anyRemote.protocol.cfBkgr);

        } else {
            LinearLayout ll = (LinearLayout) findViewById(getLayoutId());
            ll.setBackgroundColor(anyRemote.protocol.cfBkgr);

            TableLayout tl = (TableLayout) findViewById(getIconLayoutId());
            tl.setBackgroundColor(anyRemote.protocol.cfBkgr);
        }
    }

    private void setTextColor() {

        TextView title = (TextView) findViewById(getTitleId());
        TextView status = (TextView) findViewById(getStatusId());

        title.setTextColor(anyRemote.protocol.cfFrgr);
        status.setTextColor(anyRemote.protocol.cfFrgr);
    }

    private void setCover() {
        if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE) {
            ImageView cover = (ImageView) findViewById(R.id.cover);

            if (anyRemote.protocol.cfNamedCover.length() == 0) {
                log("setCover " + (anyRemote.protocol.cfCover == null ? "NULL" : "Set"));
                cover.setImageBitmap(anyRemote.protocol.cfCover);
            } else {
                log("setCover by_name " + anyRemote.protocol.cfNamedCover);
                Bitmap ic = anyRemote.getCoverBitmap(getResources(), anyRemote.protocol.cfNamedCover);
                if (ic != null) {
                    cover.setImageBitmap(ic);
                }
            }
        }
    }

    private void setFont() {

        TextView title = (TextView) findViewById(getTitleId());
        TextView status = (TextView) findViewById(getStatusId());

        title.setTypeface(anyRemote.protocol.cfTFace);
        status.setTypeface(anyRemote.protocol.cfTFace);

        title.setTextSize(anyRemote.protocol.cfFSize);
        status.setTextSize(anyRemote.protocol.cfFSize);
    }

    public void onClick(View v) {
        // public boolean onTouch (View v, MotionEvent e) {

        String key = "";
        // int action = e.getAction();

        switch (v.getId()) {

        case R.id.b1:
        case R.id.b1_r90:
        case R.id.b1_r270:
        case R.id.bb1:
            key = STR_NUM1;
            break;

        case R.id.b2:
        case R.id.b2_r90:
        case R.id.b2_r270:
        case R.id.bb2:
            key = STR_NUM2;
            break;

        case R.id.b3:
        case R.id.b3_r90:
        case R.id.b3_r270:
        case R.id.bb3:
            key = STR_NUM3;
            break;

        case R.id.b4:
        case R.id.b4_r90:
        case R.id.b4_r270:
        case R.id.bb4:
            key = STR_NUM4;
            break;

        case R.id.b5:
        case R.id.b5_r90:
        case R.id.b5_r270:
        case R.id.bb5:
            key = STR_NUM5;
            break;

        case R.id.b6:
        case R.id.b6_r90:
        case R.id.b6_r270:
        case R.id.bb6:
            key = STR_NUM6;
            break;

        case R.id.b7:
        case R.id.b7_r90:
        case R.id.b7_r270:
        case R.id.bb7:
            key = STR_NUM7;
            break;

        case R.id.b8:
        case R.id.b8_r90:
        case R.id.b8_r270:
            key = STR_NUM8;
            break;

        case R.id.b9:
        case R.id.b9_r90:
        case R.id.b9_r270:
            key = STR_NUM9;
            break;

        case R.id.b10:
        case R.id.b10_r90:
        case R.id.b10_r270:
            key = STR_STAR;
            break;

        case R.id.b0:
        case R.id.b11_r90:
        case R.id.b11_r270:
            key = STR_NUM0;
            break;

        case R.id.b11:
        case R.id.b12_r90:
        case R.id.b12_r270:
            key = STR_POUND;
            break;

        case R.id.cover:
            key = STR_COVER;
            break;

        default:
            log("onClick: Unknown button");
            return;
            // return false;
        }

        clickOn(key);
        /*
         * if (action == MotionEvent.ACTION_DOWN) { iconPressed(key); return
         * true; } else if (action == MotionEvent.ACTION_UP) {
         * iconReleased(key); return true; } return false;
         */
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (gestureScanner.onTouchEvent(ev)) {
             return true;
        }
        super.dispatchTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (gestureScanner.onTouchEvent(me)) {
             return true;
        }
        return super.onTouchEvent(me);
    }

    private String key2str(int keyCode) {

        log("key2str " + keyCode);

        switch (keyCode) {
        case KeyEvent.KEYCODE_1:
            return "1";
        case KeyEvent.KEYCODE_2:
            return "2";
        case KeyEvent.KEYCODE_3:
            return "3";
        case KeyEvent.KEYCODE_4:
            return "4";
        case KeyEvent.KEYCODE_5:
            return "5";
        case KeyEvent.KEYCODE_6:
            return "6";
        case KeyEvent.KEYCODE_7:
            return "7";
        case KeyEvent.KEYCODE_8:
            return "8";
        case KeyEvent.KEYCODE_9:
            return "9";
        case KeyEvent.KEYCODE_STAR:
            return "*";
        case KeyEvent.KEYCODE_0:
            return "0";
        case KeyEvent.KEYCODE_POUND:
            return "#";
        case KeyEvent.KEYCODE_MENU:
        case KeyEvent.KEYCODE_HOME:
        case KeyEvent.KEYCODE_BACK:
            return ""; // do not process them
        case KeyEvent.KEYCODE_VOLUME_UP:
            return "VOL+";
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            return "VOL-";
        case KeyEvent.KEYCODE_DPAD_UP:
            return (!anyRemote.protocol.cfUseJoystick || anyRemote.protocol.cfSkin == SK_BOTTOMLINE ? anyRemote.protocol.cfUpEvent
                    : ""); // do not process them if joystick_only param was set
        case KeyEvent.KEYCODE_DPAD_DOWN:
            return (!anyRemote.protocol.cfUseJoystick || anyRemote.protocol.cfSkin == SK_BOTTOMLINE ? anyRemote.protocol.cfDownEvent
                    : "");
        case KeyEvent.KEYCODE_DPAD_LEFT:
            return (!anyRemote.protocol.cfUseJoystick ? "LEFT" : ""); // do not
                                                                      // process
                                                                      // them
                                                                      // if
                                                                      // joystick_only
                                                                      // param
                                                                      // was
                                                                      // set
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            return (!anyRemote.protocol.cfUseJoystick ? "RIGHT" : "");
        case KeyEvent.KEYCODE_DPAD_CENTER:
            return (!anyRemote.protocol.cfUseJoystick ? "FIRE" : "");
        case KeyEvent.KEYCODE_SEARCH:
            return "SEARCH";
        default:
            if (keyCode >= 0 && keyCode < 10) {
                return "K" + String.valueOf(keyCode);
            }
            return String.valueOf(keyCode);
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            
        	new Handler().postDelayed(new Runnable() { 
                public void run() { 
                    openOptionsMenu(); 
                  } 
               }, 1000);
        	
        	longPress = true;
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        log("onKeyUp " + keyCode + " " + event.isTracking() +" " + event.isCanceled());
        
        boolean lp = longPress;
        longPress = false;
            
        if (keyCode == KeyEvent.KEYCODE_BACK) { // && event.isTracking() && !event.isCanceled()) {
            if (lp) {
            	/* do nothing - jusr skip
                // show menu
                log("onKeyUp KEYCODE_BACK long press - show menu");
                 
                new Handler().postDelayed(new Runnable() { 
                    public void run() { 
                        openOptionsMenu(); 
                      } 
                   }, 1000); 
                */
            } else {
                log("onKeyUp KEYCODE_BACK do disconnect");
                commandAction(anyRemote.protocol.context.getString(R.string.disconnect_item));
            }
            return true;
        }

        String key = key2str(keyCode);
        if (key.length() > 0) {
            anyRemote.protocol.queueCommand(key, false);
            return true;
        }
        log("onKeyUp TRANSFER " + keyCode);
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        log("onKeyDown " + keyCode + " " + event.isTracking() +" " + event.isCanceled());
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            event.startTracking();
            return true;
        }
        String key = key2str(keyCode);
        if (key.length() > 0) {
            anyRemote.protocol.queueCommand(key, true);
            return true;
        }
        log("onKeyDown TRANSFER " + keyCode);
        return false;
    }

    /*public void iconPressed(String key) {
        log("iconPressed " + key);
        anyRemote.protocol.queueCommand(key, true);
    }

    public void iconReleased(String key) {
        log("iconReleased " + key);
        anyRemote.protocol.queueCommand(key, false);
    }*/

    public void clickOn(String key) {
        // log("clickOn "+key);
        anyRemote.protocol.queueCommand(key, true);
        anyRemote.protocol.queueCommand(key, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        commandAction(item.getTitle().toString());
        return true;
    }

    // @Override
    // public void onBackPressed() {
    // log("onBackPressed");
    // commandAction(anyRemote.protocol.context.getString(R.string.disconnect_item));
    // }

    public void commandAction(String command) {
        log("commandAction " + command);

        if (command.equals(anyRemote.protocol.context.getString(R.string.exit_item))) {
            anyRemote.sendGlobal(anyRemote.DO_EXIT, null);
        } else if (command.equals(anyRemote.protocol.context.getString(R.string.disconnect_item))) {
            anyRemote.sendGlobal(anyRemote.DO_DISCONNECT, null);
        } else if (command.equals(anyRemote.protocol.context.getString(R.string.log_item))) {
            switchedToPrivateScreen = true;
            showLog();
        } else if (command.equals(anyRemote.protocol.context.getString(R.string.mouse_item))) {
            switchedToPrivateScreen = true;
            showMouse();
        } else if (command.equals(anyRemote.protocol.context.getString(R.string.keyboard_item))) {
            switchedToPrivateScreen = true;
            showKbd();
        } else if (command.equals(anyRemote.protocol.context.getString(R.string.back_item))) {
            anyRemote.protocol.queueCommand("Back"); // avoid national alphabets
        } else {
            anyRemote.protocol.queueCommand(command);
        }
    }

    @Override
    protected void doFinish(String action) {

        log("doFinish " + action);
 
        final Intent intent = new Intent();
        intent.putExtra(anyRemote.ACTION, action);
        setResult(RESULT_OK, intent);

        finish();
    }

    private int getLayoutId() {

        Display display = getWindowManager().getDefaultDisplay();

        int id = R.id.skin_default;
        if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE) {
            id = R.id.skin_bottomline;
        } else if (display.getOrientation() == Surface.ROTATION_90) {
            id = R.id.skin_default_r90;
        } else if (display.getOrientation() == Surface.ROTATION_270) {
            id = R.id.skin_default_r270;
        }
        return id;
    }

    private int getTitleId() {

        Display display = getWindowManager().getDefaultDisplay();

        int t = R.id.cf_title;
        if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE) {
            t = R.id.cf_btitle;
        } else if (display.getOrientation() == Surface.ROTATION_90) {
            t = R.id.cf_title_r90;
        } else if (display.getOrientation() == Surface.ROTATION_270) {
            t = R.id.cf_title_r270;
        }
        return t;
    }

    private int getStatusId() {

        Display display = getWindowManager().getDefaultDisplay();

        int t = R.id.cf_status;
        if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE) {
            t = R.id.cf_bstatus;
        } else if (display.getOrientation() == Surface.ROTATION_90) {
            t = R.id.cf_status_r90;
        } else if (display.getOrientation() == Surface.ROTATION_270) {
            t = R.id.cf_status_r270;
        }
        return t;
    }

    private int getIconLayoutId() {

        Display display = getWindowManager().getDefaultDisplay();

        int id = R.id.icons;
        if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE) {
            // no such element
        } else if (display.getOrientation() == Surface.ROTATION_90) {
            id = R.id.icons_r90;
        } else if (display.getOrientation() == Surface.ROTATION_270) {
            id = R.id.icons_r270;
        }
        return id;
    }

    public boolean onDown(MotionEvent e) {
        //log("onDown");
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // log("onFling " + e1.getX() + " " + e1.getY() + " "
        // + e2.getX() + " " + e2.getY() + " "
        // + velocityX + " " + velocityY);
        try {
            // if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
            // return false;
            // }

            // right to left swipe
            if (e1.getX() - e2.getX() > anyRemote.SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > anyRemote.SWIPE_THRESHOLD_VELOCITY) {
                clickOn("SlideLeft");
                ignoreOneClick = true;
            } else if (e2.getX() - e1.getX() > anyRemote.SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > anyRemote.SWIPE_THRESHOLD_VELOCITY) {
                clickOn("SlideRight");
                ignoreOneClick = true;
           } else if (e1.getY() - e2.getY() > anyRemote.SWIPE_MIN_DISTANCE
                    && Math.abs(velocityY) > anyRemote.SWIPE_THRESHOLD_VELOCITY) {
                clickOn("SlideUp");
                ignoreOneClick = true;
           } else if (e2.getY() - e1.getY() > anyRemote.SWIPE_MIN_DISTANCE
                    && Math.abs(velocityY) > anyRemote.SWIPE_THRESHOLD_VELOCITY) {
                clickOn("SlideDown");
                ignoreOneClick = true;
           }
        } catch (Exception e) {
            // nothing
        }

        return true;
    }

    public void onLongPress(MotionEvent e) {
        //log("onLongPress");
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //log("onScroll");
        return false;
    }

    public void onShowPress(MotionEvent e) {
        //log("onShowPress");
    }

    public boolean onSingleTapUp(MotionEvent e) {
        //log("onSingleTapUp");
        return false;
    }
}
