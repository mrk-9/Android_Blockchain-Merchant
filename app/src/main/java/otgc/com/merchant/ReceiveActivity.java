package otgc.com.merchant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.encode.QRCodeEncoder;

import org.bitcoinj.core.Coin;
import org.bitcoinj.uri.BitcoinURI;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import info.blockchain.api.receive2.ReceiveV2;
import info.blockchain.api.receive2.ReceiveV2Response;
import otgc.com.merchant.api.APIFactory;
import otgc.com.merchant.db.DBControllerV2;
import otgc.com.merchant.service.ExpectedIncoming;
import otgc.com.merchant.fragments.MainFragment;
import otgc.com.merchant.util.AppUtil;
import otgc.com.merchant.util.MonetaryUtil;
import otgc.com.merchant.util.PrefsUtil;
import otgc.com.merchant.util.ToastCustom;

public class ReceiveActivity extends Activity implements View.OnClickListener{

    private TextView tvMerchantName = null;
    private TextView tvFiatAmount = null;
    private TextView tvBtcAmount = null;
    private ImageView ivReceivingQr = null;
    private LinearLayout progressLayout = null;
    private ImageView ivCancel = null;
    private ImageView ivCheck = null;
    private TextView tvStatus = null;

    private String receivingAddress = null;

    private BigInteger bamount = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_receive);

        initViews();

        //Register receiver (Listen for incoming tx)
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_INTENT_INCOMING_TX);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);

        //Incoming intent value
        double amountFiat = this.getIntent().getDoubleExtra(MainFragment.AMOUNT_PAYABLE_FIAT, 0.0);
        double amountBtc = this.getIntent().getDoubleExtra(MainFragment.AMOUNT_PAYABLE_BTC, 0.0);
        tvFiatAmount.setText(getCurrencySymbol()+" "+ MonetaryUtil.getInstance().getFiatDecimalFormat().format(amountFiat));
        tvBtcAmount.setText(MonetaryUtil.getInstance().getBTCDecimalFormat().format(amountBtc) + " " + MainFragment.DEFAULT_CURRENCY_BTC);

        getReceiveAddress(amountBtc, tvFiatAmount.getText().toString());

    }

    @Override
    protected void onDestroy() {
        //Unregister receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_cancel:onBackPressed();break;
        }
    }

    private void initViews(){
        tvMerchantName = (TextView)findViewById(R.id.tv_merchant_name);
        tvMerchantName.setText(PrefsUtil.getInstance(this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, ""));
        tvFiatAmount = (TextView)findViewById(R.id.tv_fiat_amount);
        tvBtcAmount = (TextView)findViewById(R.id.tv_btc_amount);
        ivReceivingQr = (ImageView)findViewById(R.id.qr);
        progressLayout = (LinearLayout)findViewById(R.id.progressLayout);
        ivCancel = (ImageView)findViewById(R.id.iv_cancel);
        ivCheck = (ImageView)findViewById(R.id.iv_check);
        tvStatus = (TextView)findViewById(R.id.tv_status);

        ivReceivingQr.setVisibility(View.GONE);
        ivCheck.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);

        ivCancel.setOnClickListener(this);
    }

    private void displayQRCode(long lamount) {

        try {

            bamount = MonetaryUtil.getInstance(this).getUndenominatedAmount(lamount);
            if(bamount.compareTo(BigInteger.valueOf(2100000000000000L)) == 1)    {
                ToastCustom.makeText(this, "Invalid amount", ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
                return;
            }
            if(!bamount.equals(BigInteger.ZERO)) {
                generateQRCode(BitcoinURI.convertToBitcoinURI(receivingAddress, Coin.valueOf(bamount.longValue()), "", ""));
                write2NFC(BitcoinURI.convertToBitcoinURI(receivingAddress, Coin.valueOf(bamount.longValue()), "", ""));
            }
            else {
                generateQRCode("bitcoin:" + receivingAddress);
                write2NFC("bitcoin:" + receivingAddress);
            }
        }
        catch(NumberFormatException e) {
            generateQRCode("bitcoin:" + receivingAddress);
            write2NFC("bitcoin:" + receivingAddress);
        }
    }

    private void generateQRCode(final String uri) {

        new AsyncTask<Void, Void, Bitmap>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                //Show generating QR message
                ivReceivingQr.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            }

            @Override
            protected Bitmap doInBackground(Void... params) {

                Bitmap bitmap = null;
                int qrCodeDimension = 260;

                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(uri, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimension);

                try {
                    bitmap = qrCodeEncoder.encodeAsBitmap();
                } catch (WriterException e) {
                    e.printStackTrace();
                }

                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                progressLayout.setVisibility(View.GONE);
                ivReceivingQr.setVisibility(View.VISIBLE);
                ivReceivingQr.setImageBitmap(bitmap);
            }
        }.execute();
    }

    private String getCurrencySymbol() {

        String strCurrencySymbol = "$";

        if(CurrencyExchange.getInstance(this).getCurrencySymbol(PrefsUtil.getInstance(ReceiveActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, MainFragment.DEFAULT_CURRENCY_FIAT)) != null) {
            strCurrencySymbol = CurrencyExchange.getInstance(this).getCurrencySymbol(PrefsUtil.getInstance(ReceiveActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, MainFragment.DEFAULT_CURRENCY_FIAT)).substring(0, 1);
        }

        return strCurrencySymbol;
    }

    private void getReceiveAddress(final double amountBtc, final String strFiat) {

        new AsyncTask<Void, Void, String>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                progressLayout.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... params) {

                //Generate new address/QR code for receive
                if(AppUtil.getInstance(ReceiveActivity.this).isV2API())    {
                    try {
                        ReceiveV2Response response = ReceiveV2.receive(PrefsUtil.getInstance(ReceiveActivity.this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, ""), APIFactory.getInstance().getCallback(), APIFactory.getInstance().getAPIKey());
                        receivingAddress = response.getReceivingAddress();
                    }
                    catch(Exception e) {
                        receivingAddress = null;
                        e.getMessage();
                        e.printStackTrace();
                    }
                }
                else    {
                    receivingAddress = PrefsUtil.getInstance(ReceiveActivity.this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
                }

                if(receivingAddress == null)    {
                    ToastCustom.makeText(ReceiveActivity.this, getText(R.string.unable_to_generate_address), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
                    return null;
                }

                //Subscribe to websocket to new address
                Intent intent = new Intent(MainActivity.ACTION_INTENT_SUBSCRIBE_TO_ADDRESS);
                intent.putExtra("address",receivingAddress);
                LocalBroadcastManager.getInstance(ReceiveActivity.this).sendBroadcast(intent);

                long lAmount = getLongAmount(amountBtc);
                ExpectedIncoming.getInstance().getBTC().put(receivingAddress, lAmount);
                ExpectedIncoming.getInstance().getFiat().put(receivingAddress, strFiat);

                displayQRCode(lAmount);

                return receivingAddress;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                progressLayout.setVisibility(View.GONE);
            }
        }.execute();

    }

    private long getLongAmount(double amountPayable) {

        double value = Math.round(amountPayable * 100000000.0);
        long longValue = (Double.valueOf(value)).longValue();

        return longValue;
    }

    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {

        //Catch incoming tx
        if (MainActivity.ACTION_INTENT_INCOMING_TX.equals(intent.getAction())) {
            soundAlert();

            final String addr = intent.getStringExtra("payment_address");
            final long paymentAmount = intent.getLongExtra("payment_amount", 0L);

            // underpayment
            if(paymentAmount < ExpectedIncoming.getInstance().getBTC().get(addr))    {

                final long remainder = ExpectedIncoming.getInstance().getBTC().get(addr) - paymentAmount;
                ToastCustom.makeText(ReceiveActivity.this, "Remainder:" + remainder, ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);

                final double btcAmount = Double.valueOf(remainder / 1e8);

                String strCurrency = PrefsUtil.getInstance(ReceiveActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, MainFragment.DEFAULT_CURRENCY_FIAT);
                Double currencyPrice = CurrencyExchange.getInstance(ReceiveActivity.this).getCurrencyPrice(strCurrency);
                NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
                double fiatAmount = 0.0;
                try {
                    fiatAmount = nf.parse(MonetaryUtil.getInstance().getFiatDecimalFormat().format(btcAmount * currencyPrice)).doubleValue();
                } catch (ParseException pe) {
                    fiatAmount = 0.0;
                }
                final double _fiatAmount = fiatAmount;

                StringBuilder sb = new StringBuilder();
                sb.append(ReceiveActivity.this.getText(R.string.insufficient_payment));
                sb.append("\n");
                sb.append(ReceiveActivity.this.getText(R.string.re_payment_requested));
                sb.append(": ");
                sb.append(MonetaryUtil.getInstance(ReceiveActivity.this).getDisplayAmount(ExpectedIncoming.getInstance().getBTC().get(addr)));
                sb.append(" BTC");
                sb.append("\n");
                sb.append(ReceiveActivity.this.getText(R.string.re_payment_received));
                sb.append(": ");
                sb.append(MonetaryUtil.getInstance(ReceiveActivity.this).getDisplayAmount(paymentAmount));
                sb.append(" BTC");
                sb.append("\n");
                sb.append(ReceiveActivity.this.getText(R.string.insufficient_payment_continue));

                AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveActivity.this, R.style.AppTheme);
                builder.setTitle(R.string.app_name);
                builder.setMessage(sb.toString()).setCancelable(false);
                AlertDialog alert = builder.create();

                alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.prompt_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();

                        onPaymentReceived(addr, paymentAmount);

                        Intent intent = new Intent(ReceiveActivity.this, ReceiveActivity.class);
                        intent.putExtra(MainFragment.AMOUNT_PAYABLE_FIAT, _fiatAmount);
                        intent.putExtra(MainFragment.AMOUNT_PAYABLE_BTC, btcAmount);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }
                });

                alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.prompt_ko), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        onPaymentReceived(addr, paymentAmount);
                    }
                });

                alert.show();

            }
            // overpayment
            else if(paymentAmount > ExpectedIncoming.getInstance().getBTC().get(addr))    {
                onPaymentReceived(addr, paymentAmount);
            }
            // expected amount
            else    {
                onPaymentReceived(addr, -1L);
            }

        }
        }
    };

    public void soundAlert(){
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager!=null && audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            MediaPlayer mp;
            mp = MediaPlayer.create(this, R.raw.alert);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.reset();
                    mp.release();
                }
            });
            mp.start();
        }
    }

    private void onPaymentReceived(String addr, long paymentAmount){

        ivCancel.setVisibility(View.GONE);
        ivReceivingQr.setVisibility(View.GONE);
        ivCheck.setVisibility(View.VISIBLE);
        ivCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                ReceiveActivity.this.setResult(RESULT_OK, intent);
                ReceiveActivity.this.finish();
            }
        });
        tvStatus.setText(getResources().getText(R.string.payment_received));
        tvStatus.setTextColor(getResources().getColor(R.color.blockchain_receive_green));
        tvBtcAmount.setVisibility(View.GONE);
        tvFiatAmount.setVisibility(View.GONE);

        long amount = (paymentAmount == -1L) ? ExpectedIncoming.getInstance().getBTC().get(addr) : paymentAmount;
        if(amount != ExpectedIncoming.getInstance().getBTC().get(addr))    {
            amount *= -1L;
        }

        String strCurrency = PrefsUtil.getInstance(ReceiveActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, MainFragment.DEFAULT_CURRENCY_FIAT);
        Double currencyPrice = CurrencyExchange.getInstance(ReceiveActivity.this).getCurrencyPrice(strCurrency);
        double amountPayableFiat = (Math.abs((double)amount) / 1e8) * currencyPrice;
        String strFiat  = (paymentAmount == -1L) ? ExpectedIncoming.getInstance().getFiat().get(addr) : getCurrencySymbol() + " " + MonetaryUtil.getInstance().getFiatDecimalFormat().format(amountPayableFiat);

        DBControllerV2 pdb = new DBControllerV2(ReceiveActivity.this);
        pdb.insertPayment(
                System.currentTimeMillis() / 1000,          // timestamp, Unix time
                receivingAddress,                           // receiving address
                amount,                                     // BTC amount
                strFiat,                                    // fiat amount
                -1,                                         // confirmations
                ""                                          // note, message
        );
        pdb.close();

        if(paymentAmount > ExpectedIncoming.getInstance().getBTC().get(addr))    {
            AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveActivity.this);
            TextView title = new TextView(ReceiveActivity.this);
            title.setPadding(20, 60, 20, 20);
            title.setText(R.string.app_name);
            title.setGravity(Gravity.CENTER);
            title.setTextSize(20);
            builder.setCustomTitle(title);
            builder.setMessage(R.string.overpaid_amount).setCancelable(false);
            AlertDialog alert = builder.create();

            alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.prompt_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ;
                }});

            alert.show();

        }

        setResult(RESULT_OK);
    }

    private void write2NFC(final String uri) {

        if (Build.VERSION.SDK_INT < 16){
            return;
        }

        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(ReceiveActivity.this);
        if (nfc != null && nfc.isNdefPushEnabled() ) {
            nfc.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
                @Override
                public NdefMessage createNdefMessage(NfcEvent event) {
                    NdefRecord uriRecord = NdefRecord.createUri(uri);
                    return new NdefMessage(new NdefRecord[]{ uriRecord });
                }
            }, ReceiveActivity.this);
        }

    }

}
