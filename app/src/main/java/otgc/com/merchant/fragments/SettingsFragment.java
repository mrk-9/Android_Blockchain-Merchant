package otgc.com.merchant.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import net.sourceforge.zbar.Symbol;

import info.blockchain.wallet.util.FormatsUtil;
import otgc.com.merchant.PinActivity;
import otgc.com.merchant.R;
import otgc.com.merchant.util.PrefsUtil;
import otgc.com.merchant.util.ToastCustom;

public class SettingsFragment extends Fragment {

    EditText st_name, st_address, st_city, st_province, st_zip, st_phone, st_note, st_wallet,st_currency, st_pin;
    ImageView bt_qr;
    private static int ZBAR_SCANNER_REQUEST = 2026;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        st_name = (EditText) view.findViewById(R.id.setting_name);
        st_address = (EditText) view.findViewById(R.id.setting_address);
        st_city = (EditText) view.findViewById(R.id.setting_city);
        st_province = (EditText) view.findViewById(R.id.setting_province);
        st_zip = (EditText) view.findViewById(R.id.setting_zipcode);
        st_phone = (EditText) view.findViewById(R.id.setting_phone);
        st_note = (EditText) view.findViewById(R.id.setting_note);
        st_wallet = (EditText) view.findViewById(R.id.setting_wallet);
        st_currency = (EditText) view.findViewById(R.id.setting_currency);
        st_pin = (EditText) view.findViewById(R.id.setting_pin);
        bt_qr = (ImageView) view.findViewById(R.id.setting_camera);

        bt_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ZBarScannerActivity.class);
                intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
                startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
            }
        });

        st_currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] currencies = getResources().getStringArray(R.array.currencies);
                String strCurrency = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "CAD");
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

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.options_local_currency);
                builder.setSingleChoiceItems(currencies, sel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, currencies[which]);
                                st_currency.setText(currencies[which]);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        st_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PinActivity.class);
                intent.putExtra("create", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        String wallet = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
        st_wallet.setText(wallet);
        String name = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, "");
        st_name.setText(name);
        String address = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANG_KEY_BUSINESS_ADDRESS, "");
        st_address.setText(address);
        String zipcode = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANG_KEY_BUSINESS_ZIPCODE, "");
        st_zip.setText(zipcode);
        String city = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANG_KEY_BUSINESS_CITY, "");
        st_city.setText(city);
        String province = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANG_KEY_BUSINESS_PROVINCE, "");
        st_province.setText(province);
        String phone = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANG_KEY_PHONE, "");
        st_phone.setText(phone);
        String note = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANG_KEY_NOTE, "");
        st_note.setText(note);

        return view;
    }

    @Override
    public void onResume()  {
        super.onResume();
        String strCurrency = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "CAD");
        st_currency.setText(strCurrency);

        String pin = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_PIN, "");
        st_pin.setText(pin);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.register_menu, menu);
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
        String name = st_name.getText().toString();
        String currency = st_currency.getText().toString();
        String wallet = st_wallet.getText().toString();
        String pin = st_pin.getText().toString();

        String address = st_address.getText().toString();
        String city = st_city.getText().toString();
        String province = st_province.getText().toString();
        String zipcode = st_zip.getText().toString();
        String phone = st_phone.getText().toString();
        String note = st_note.getText().toString();


        PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, name);
        PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, wallet);
        PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, currency);
        PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANT_KEY_PIN, pin);
        PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANG_KEY_BUSINESS_ADDRESS, address);
        PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANG_KEY_BUSINESS_CITY, city);
        PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANG_KEY_BUSINESS_PROVINCE, province);
        PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANG_KEY_BUSINESS_ZIPCODE, zipcode);
        PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANG_KEY_PHONE, phone);
        PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANG_KEY_NOTE, note);

        Toast.makeText(getActivity(), "Successfully saved.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == Activity.RESULT_OK && requestCode == ZBAR_SCANNER_REQUEST)	{

            String scanResult = data.getStringExtra(ZBarConstants.SCAN_RESULT);

            if(scanResult.startsWith("bitcoin:"))    {
                scanResult = scanResult.substring(8);
            }

            if(FormatsUtil.getInstance().isValidXpub(scanResult) || FormatsUtil.getInstance().isValidBitcoinAddress(scanResult))    {
                st_wallet.setText(scanResult);
            }
            else{
                ToastCustom.makeText(getActivity(), getString(R.string.unrecognized_xpub), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            }
        }
    }
}
