package otgc.com.merchant;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
//import android.util.Log;

import com.google.common.hash.Hashing;

import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

import otgc.com.merchant.util.PrefsUtil;
import otgc.com.merchant.util.ToastCustom;

public class PinActivity extends Activity	{

    String userEnteredPIN = "";
    String userEnteredPINConfirm = null;

    private TextView titleView = null;
    private TextView pinBox0 = null;
    private TextView pinBox1 = null;
    private TextView pinBox2 = null;
    private TextView pinBox3 = null;
    private TextView pinBox4 = null;
    private TextView pinBox5 = null;

    private final int PIN_LENGTH = 6;
    private TextView[] pinBoxArray = null;
	
	private boolean doCreate = false;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

	    setContentView(R.layout.activity_pin);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

	    setTitle(R.string.action_pincode);
	    
        Bundle extras = getIntent().getExtras();
        if(extras != null)	{
            if(extras.getBoolean("create"))	{
            	doCreate = true;
            }
        }

        // Set title state
        titleView = (TextView)findViewById(R.id.titleBox);
        if(doCreate) {
            titleView.setText(R.string.create_pin);
        }
        else {
            titleView.setText(R.string.enter_pin);
        }

        pinBox0 = (TextView)findViewById(R.id.pinBox0);
        pinBox1 = (TextView)findViewById(R.id.pinBox1);
        pinBox2 = (TextView)findViewById(R.id.pinBox2);
        pinBox3 = (TextView)findViewById(R.id.pinBox3);
        pinBox4 = (TextView)findViewById(R.id.pinBox4);
        pinBox5 = (TextView)findViewById(R.id.pinBox5);

        pinBoxArray = new TextView[PIN_LENGTH];
        pinBoxArray[0] = pinBox0;
        pinBoxArray[1] = pinBox1;
        pinBoxArray[2] = pinBox2;
        pinBoxArray[3] = pinBox3;
        pinBoxArray[4] = pinBox4;
        pinBoxArray[5] = pinBox5;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && !doCreate) {
            finish();
            return true;
        }
        return false;
    }

    public void padClicked(View view) {

        if(userEnteredPIN.length() == PIN_LENGTH) {
            return;
        }

        // Append tapped #
        userEnteredPIN = userEnteredPIN + view.getTag().toString().substring(0, 1);
        pinBoxArray[userEnteredPIN.length() - 1].setBackgroundResource(R.drawable.rounded_view_dark_blue);

        // Perform appropriate action if PIN_LENGTH has been reached
        if (userEnteredPIN.length() == PIN_LENGTH) {

            if(!doCreate){
                validatePin();

            }else if(userEnteredPINConfirm == null){
                createNewPin();

            }else if (userEnteredPINConfirm != null && userEnteredPINConfirm.equals(userEnteredPIN)){
                confirmNewPin();

            } else {
                pinCreationError();
            }
        }
    }

    private void confirmNewPin(){
        //End of Confirm - Pin is confirmed
        String hashed = Hashing.sha256().hashString(userEnteredPIN, Charset.forName("UTF8")).toString();
//      ToastCustom.makeText(PinActivity.this, hashed, ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_OK);
        PrefsUtil.getInstance(PinActivity.this).setValue(PrefsUtil.MERCHANT_KEY_PIN, hashed);

        PrefsUtil.getInstance(PinActivity.this).setValue(PrefsUtil.MERCHANT_KEY_ACCOUNT_INDEX, 0);

        setResult(RESULT_OK);
        finish();
    }

    private void validatePin(){
        // Validate
        String hashed = Hashing.sha256().hashString(userEnteredPIN, Charset.forName("UTF8")).toString();
        String stored = PrefsUtil.getInstance(PinActivity.this).getValue(PrefsUtil.MERCHANT_KEY_PIN, "");
        if(stored.equals(hashed)) {
            setResult(RESULT_OK);
            finish();
        }
        else {
            ToastCustom.makeText(PinActivity.this, getString(R.string.pin_code_enter_error), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            clearPinBoxes();
            userEnteredPIN = "";
            userEnteredPINConfirm = null;
        }
    }

    private void createNewPin(){
        //End of Create -  Change to Confirm
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                PinActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        titleView.setText(R.string.confirm_pin);
                        clearPinBoxes();
                        userEnteredPINConfirm = userEnteredPIN;
                        userEnteredPIN = "";
                    }
                });
            }
        }, 200);
    }

    private void pinCreationError(){
        //End of Confirm - Pin Mismatch
        ToastCustom.makeText(PinActivity.this, getString(R.string.pin_code_create_error), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        clearPinBoxes();
        userEnteredPIN = "";
        userEnteredPINConfirm = null;
        titleView.setText(R.string.create_pin);
    }

    public void cancelClicked(View view) {
        clearPinBoxes();
        userEnteredPIN = "";
    }

    private void clearPinBoxes(){
        if(userEnteredPIN.length() > 0)	{
            for(int i = 0; i < pinBoxArray.length; i++)	{
                pinBoxArray[i].setBackgroundResource(R.drawable.rounded_view_blue_white_border);//reset pin buttons blank
            }
        }
    }
}
