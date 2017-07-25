package otgc.com.merchant;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import net.sourceforge.zbar.Symbol;

import info.blockchain.wallet.util.FormatsUtil;
import otgc.com.merchant.util.PrefsUtil;
import otgc.com.merchant.util.ToastCustom;

public class RegisterActivity extends AppCompatActivity {

    EditText bus_name, bus_currency, bus_address, bus_pin;
    ImageView bt_qr;
    private static int ZBAR_SCANNER_REQUEST = 2026;

    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            int requestCode = 0;
            ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.CAMERA}, requestCode);
        }

        bus_name = (EditText) findViewById(R.id.name_reg);
        bus_currency = (EditText) findViewById(R.id.currency_reg);
        bus_address = (EditText) findViewById(R.id.address_reg);
        bus_pin = (EditText) findViewById(R.id.pin_reg);
        bt_qr = (ImageView) findViewById(R.id.camera_reg);

        bt_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, ZBarScannerActivity.class);
                intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
                startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
            }
        });

        bus_currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] currencies = getResources().getStringArray(R.array.currencies);
                String strCurrency = PrefsUtil.getInstance(RegisterActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "CAD");
                int sel = -1;
                for (int i = 0; i < currencies.length; i++) {
                    if (currencies[i].endsWith(strCurrency)) {
                        sel = i;
                        break;
                    }
                }
                if (sel == -1) {
                    sel = currencies.length - 1;    // set to USD
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setTitle(R.string.options_local_currency);
                builder.setSingleChoiceItems(currencies, sel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                PrefsUtil.getInstance(RegisterActivity.this).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, currencies[which]);
                                bus_currency.setText(currencies[which]);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        bus_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, PinActivity.class);
                intent.putExtra("create", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume()  {
        super.onResume();
        String strCurrency = PrefsUtil.getInstance(RegisterActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "CAD");
        bus_currency.setText(strCurrency);

        String pin = PrefsUtil.getInstance(RegisterActivity.this).getValue(PrefsUtil.MERCHANT_KEY_PIN, "");
        bus_pin.setText(pin);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                onSave();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void onSave()   {
        if (!isValidation())    {
            return;
        }

        onSuccess();
    }

    private boolean isValidation()  {
        return bus_name.getText().toString().trim().length() > 0 && bus_pin.getText().toString().trim().length() > 0 && bus_address.getText().toString().trim().length() > 0 && bus_pin.getText().toString().trim().length() > 0;
    }

    private void onSuccess()  {
        String name = bus_name.getText().toString();
        String currency = bus_currency.getText().toString();
        String address = bus_address.getText().toString();
        String pin = bus_pin.getText().toString();

        PrefsUtil.getInstance(RegisterActivity.this).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, name);
        PrefsUtil.getInstance(RegisterActivity.this).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, address);
        PrefsUtil.getInstance(RegisterActivity.this).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, currency);
        PrefsUtil.getInstance(RegisterActivity.this).setValue(PrefsUtil.MERCHANT_KEY_PIN, pin);
        PrefsUtil.getInstance(RegisterActivity.this).setValue(PrefsUtil.MERCHANG_KEY_LOGGED_IN, true);

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void onFail()   {
        PrefsUtil.getInstance(RegisterActivity.this).setValue(PrefsUtil.MERCHANG_KEY_LOGGED_IN, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == Activity.RESULT_OK && requestCode == ZBAR_SCANNER_REQUEST)	{

            String scanResult = data.getStringExtra(ZBarConstants.SCAN_RESULT);

            if(scanResult.startsWith("bitcoin:"))    {
                scanResult = scanResult.substring(8);
            }

            if(FormatsUtil.getInstance().isValidXpub(scanResult) || FormatsUtil.getInstance().isValidBitcoinAddress(scanResult))    {
                bus_address.setText(scanResult);
            }
            else{
                ToastCustom.makeText(this, getString(R.string.unrecognized_xpub), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            }
        }
    }
}
