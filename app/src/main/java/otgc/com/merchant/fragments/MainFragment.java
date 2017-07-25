package otgc.com.merchant.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Timer;

import otgc.com.merchant.CurrencyExchange;
import otgc.com.merchant.R;
import otgc.com.merchant.ReceiveActivity;
import otgc.com.merchant.util.AppUtil;
import otgc.com.merchant.util.MonetaryUtil;
import otgc.com.merchant.util.PrefsUtil;
import otgc.com.merchant.util.SnackCustom;
import otgc.com.merchant.util.ToastCustom;


public class MainFragment extends Fragment implements View.OnClickListener{

    private View rootView = null;
    private TextView btnCancel = null;
    private TextView btnPay = null;
    TextView et_subtotal, et_tip, et_total;
    RelativeLayout subTotalContainer, tipContainer;

    public static String AMOUNT_PAYABLE_FIAT = "AMOUNT_PAYABLE_FIAT";
    public static String AMOUNT_PAYABLE_BTC = "AMOUNT_PAYABLE_BTC";

    public double amountPayableFiat = 0.0;
    public double amountPayableBtc = 0.0;

    private final int DECIMAL_PLACES_FIAT = 2;
    private final int DECIMAL_PLACES_BTC = 8;
    public static final String DEFAULT_CURRENCY_FIAT = "USD";
    public static final String DEFAULT_CURRENCY_BTC = "BTC";

    private boolean isBtc = false;
    private int allowedDecimalPlaces = DECIMAL_PLACES_FIAT;
    private final double bitcoinLimit = 21000000.0;

    private NumberFormat nf = null;

    private static Timer timer = null;

    public static final int RECEIVE_RESULT = 1122;

    private String strDecimal = null;

    private boolean isTip = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        nf = NumberFormat.getInstance(Locale.getDefault());
        strDecimal = Character.toString(MonetaryUtil.getInstance().getDecimalFormatSymbols().getDecimalSeparator());
        ((TextView)rootView.findViewById(R.id.decimal)).setText(strDecimal);

        et_subtotal = (TextView) rootView.findViewById(R.id.main_subtotal_edit);//tvAmount
        et_tip = (TextView) rootView.findViewById(R.id.main_tip_edit);
        et_total = (TextView) rootView.findViewById(R.id.main_total_edit);
        btnCancel = (TextView) rootView.findViewById(R.id.main_cancel_button);
        btnPay = (TextView) rootView.findViewById(R.id.main_pay_button);//ivCharge
        subTotalContainer = (RelativeLayout) rootView.findViewById(R.id.subtotal_container);
        tipContainer = (RelativeLayout) rootView.findViewById(R.id.tip_container);

        btnCancel.setOnClickListener(this);
        btnPay.setOnClickListener(this);
        et_subtotal.setOnClickListener(this);
        et_tip.setOnClickListener(this);

        initPadClickListeners();

        initValues();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        isBtc = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY_DISPLAY, false);

        initValues();
    }

    private void initPadClickListeners(){
        rootView.findViewById(R.id.button0).setOnClickListener(this);
        rootView.findViewById(R.id.button1).setOnClickListener(this);
        rootView.findViewById(R.id.button2).setOnClickListener(this);
        rootView.findViewById(R.id.button3).setOnClickListener(this);
        rootView.findViewById(R.id.button4).setOnClickListener(this);
        rootView.findViewById(R.id.button5).setOnClickListener(this);
        rootView.findViewById(R.id.button6).setOnClickListener(this);
        rootView.findViewById(R.id.button7).setOnClickListener(this);
        rootView.findViewById(R.id.button8).setOnClickListener(this);
        rootView.findViewById(R.id.button9).setOnClickListener(this);
        rootView.findViewById(R.id.button0).setOnClickListener(this);
        rootView.findViewById(R.id.button10).setOnClickListener(this);
        rootView.findViewById(R.id.buttonDeleteBack).setOnClickListener(this);
    }

    private void initValues(){

        formatViews();
        isBtc = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY_DISPLAY, false);

        if(et_subtotal != null)    {
            if(isBtc)    {
                allowedDecimalPlaces = DECIMAL_PLACES_BTC;
            }
            else    {
                allowedDecimalPlaces = DECIMAL_PLACES_FIAT;
            }
        }
        updateAmounts();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button1:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button2:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button3:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button4:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button5:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button6:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button7:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button8:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button9:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button10:padClicked(strDecimal);break;
            case R.id.button0:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.buttonDeleteBack:padClicked(null);break;
            case R.id.main_subtotal_edit:checkTip(false);break;
            case R.id.main_tip_edit:checkTip(true);break;
            case R.id.main_cancel_button:cancelClicked();break;
            case R.id.main_pay_button:chargeClicked(); return;
        }

        updateAmounts();
    }

    private boolean validateAmount(){

        try {
            double textParsable = nf.parse(et_subtotal.getText().toString()).doubleValue();
            if (textParsable > 0.0) {
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            return false;
        }
    }

    private void checkTip(boolean flag) {
        isTip = flag;
        if (isTip) {
            activedTipContainer();
        } else {
            activedSubTotalContainer();
        }
    }

    public void cancelClicked() {
        formatViews();
    }

    private void formatViews() {
        et_subtotal.setText("0");
        et_total.setText("");
        et_tip.setText("");

        activedSubTotalContainer();
    }

    private void activedSubTotalContainer() {
        tipContainer.setBackgroundResource(R.drawable.bg_edittext);
        subTotalContainer.setBackgroundResource(R.drawable.bg_edittext_active);
    }

    private void activedTipContainer() {
        tipContainer.setBackgroundResource(R.drawable.bg_edittext_active);
        subTotalContainer.setBackgroundResource(R.drawable.bg_edittext);
    }

    public void chargeClicked() {

        if(!AppUtil.getInstance(getActivity()).hasValidReceiver())    {
            return;
        }

        if(validateAmount()) {
            updateAmounts();
            Intent intent = new Intent(getActivity(), ReceiveActivity.class);
            intent.putExtra(AMOUNT_PAYABLE_FIAT, amountPayableFiat);
            intent.putExtra(AMOUNT_PAYABLE_BTC, amountPayableBtc);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityForResult(intent, RECEIVE_RESULT);
        }else{
            SnackCustom.make(getActivity(), getView(), getActivity().getText(R.string.invalid_amount), getActivity().getResources().getString(R.string.prompt_ok), null);
        }
    }

    private void updateAmounts(){

        if(et_subtotal==null)return;

        try {

            double amount = nf.parse(et_total.getText().toString()).doubleValue();

            String strCurrency = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, DEFAULT_CURRENCY_FIAT);
            Double currencyPrice = CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(strCurrency);

            if(isBtc) {
                amountPayableFiat = nf.parse(MonetaryUtil.getInstance().getFiatDecimalFormat().format(amount * currencyPrice)).doubleValue();
                amountPayableBtc = amount;
            }
            else {
                amountPayableFiat = amount;
                amountPayableBtc = nf.parse(MonetaryUtil.getInstance().getBTCDecimalFormat().format(amount / currencyPrice)).doubleValue();
            }
        }
        catch(ParseException pe) {
            amountPayableFiat = 0.0;
            amountPayableBtc = 0.0;
            pe.printStackTrace();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //reset amount after receive
        if(requestCode == RECEIVE_RESULT && resultCode == Activity.RESULT_OK) {
            formatViews();
        }
    }

    public void padClicked(String pad) {

        TextView selectedEditText = et_subtotal;
        if (isTip) selectedEditText = et_tip;

        // Back clicked
        if(pad == null){
            String e1 = selectedEditText.getText().toString();
            if (e1.length() > 1) {
                selectedEditText.setText(e1.substring(0, e1.length() - 1));
            }else {
                selectedEditText.setText("0");
            }
            return;
        }

        String amountText = selectedEditText.getText().toString();

        if(amountText.length() == 1 && amountText.charAt(0) == '0' && !pad.equals(strDecimal))    {
            selectedEditText.setText(pad);
            return;
        }

        //initial input
        double amount = 0.0;
        try {
            amount = nf.parse(amountText).doubleValue();
        }
        catch(ParseException pe) {
            amount = 0.0;
        }

        if(amount == 0.0 && pad.equals(strDecimal))    {
            selectedEditText.setText("0" + strDecimal);
            return;
        }

        //Don't allow multiple decimal separators
        if(amountText.contains(strDecimal) && pad.equals(strDecimal)){
            return;
        }

        //Don't allow multiple leading 0's
        if(amountText.equals("0") && pad.equals("0")){
            return;
        }

        //Get decimal places
        if(amountText.contains(strDecimal)) {

            int decimalPlaces = 0;
            amountText += pad;
            String[] result = amountText.split(strDecimal);

            if(result.length >= 2)
                decimalPlaces = result[1].length();

            if(decimalPlaces > allowedDecimalPlaces)return;
        }

        if(pad!=null) {
            // Append tapped #
            selectedEditText.append(pad);
        }

        //Check that we don't exceed bitcoin limit
        checkBitcoinLimit();
    }

    private void checkBitcoinLimit(){

        double currentValue = 0.0;
        double currentSubValue = 0.0;
        double currentTipValue = 0.0;

        try {
            currentSubValue = nf.parse(et_subtotal.getText().toString()).doubleValue();
        }
        catch(ParseException pe) {
            pe.printStackTrace();
        }

        try {
            currentTipValue = nf.parse(et_tip.getText().toString()).doubleValue();
        }
        catch(ParseException pe) {
            pe.printStackTrace();
        }

        currentValue = currentSubValue + currentTipValue;
        et_total.setText(Double.toString(currentValue));

        if(isBtc){

            if(currentValue > bitcoinLimit) {
                et_subtotal.setText(MonetaryUtil.getInstance().getBTCDecimalFormat().format(bitcoinLimit));
                et_total.setText(MonetaryUtil.getInstance().getBTCDecimalFormat().format(bitcoinLimit));
                ToastCustom.makeText(getActivity(), getResources().getString(R.string.btc_limit_reached), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            }
        }
        else if(!isBtc){

            String strCurrency = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, DEFAULT_CURRENCY_FIAT);
            Double currencyPrice = CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(strCurrency);

            double btcValue = 0.0;
            try {
                btcValue = nf.parse(MonetaryUtil.getInstance().getBTCDecimalFormat().format(currentValue / currencyPrice)).doubleValue();
            }
            catch(ParseException pe) {
                pe.printStackTrace();
            }

            if(btcValue > bitcoinLimit) {
                //This is marked by Alex because this code generates issue first time only.
//                et_subtotal.setText(MonetaryUtil.getInstance().getFiatDecimalFormat().format(bitcoinLimit * currencyPrice));
//                et_total.setText(MonetaryUtil.getInstance().getFiatDecimalFormat().format(bitcoinLimit * currencyPrice));
//                ToastCustom.makeText(getActivity(), getResources().getString(R.string.btc_limit_reached), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            }
        }
    }

}
