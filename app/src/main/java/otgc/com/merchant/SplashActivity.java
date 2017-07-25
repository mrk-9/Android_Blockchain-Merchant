package otgc.com.merchant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.Window;
import android.view.WindowManager;

import otgc.com.merchant.util.PrefsUtil;

//import otgc.com.merchant.CurrencyExchange;

public class SplashActivity extends Activity	{

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    this.setContentView(R.layout.activity_splash);
	    
    	new Thread() {
    		public void run() {
    			Looper.getMainLooper().prepare();
    			
    			try {
        			sleep(3 * 1000);
    			}
    			catch(InterruptedException ie) {
    				;
    			}

//    		    CurrencyExchange.getInstance(SplashActivity.this);
				boolean is_logged_in = PrefsUtil.getInstance(SplashActivity.this).getValue(PrefsUtil.MERCHANG_KEY_LOGGED_IN, false);

				if (is_logged_in) {
					Intent intent = new Intent(SplashActivity.this, MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				} else {
					startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
				}

//				Intent intent = new Intent(SplashActivity.this, PinActivity.class);
//				intent.putExtra("create", true);
//				startActivityForResult(intent, PIN_ACTIVITY);

    			Looper.getMainLooper().loop();
    		}
		        }.start();

    }
}
