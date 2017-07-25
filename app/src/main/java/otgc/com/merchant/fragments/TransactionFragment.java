package otgc.com.merchant.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import otgc.com.merchant.NotificationData;
import otgc.com.merchant.R;
import otgc.com.merchant.adapters.TransactionListViewAdapter;
import otgc.com.merchant.db.DBControllerV2;
import otgc.com.merchant.models.TransactionModel;
import otgc.com.merchant.util.DateUtil;
import otgc.com.merchant.util.MonetaryUtil;
import otgc.com.merchant.util.PrefsUtil;
import otgc.com.merchant.util.TypefaceUtil;

public class TransactionFragment extends Fragment {
    ListView listView;
    ArrayList<TransactionModel> data;

    private static String merchantXpub = null;
    private List<ContentValues> mListItems;
    private TransactionAdapter adapter = null;
    private NotificationData notification = null;
    private boolean push_notifications = false;
    private Timer timer = null;
    private boolean doBTC = false;
    private Typeface btc_font = null;
    private Activity thisActivity = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        listView = (ListView) view.findViewById(R.id.transaction_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ////
                doBTC = !doBTC;
                adapter.notifyDataSetChanged();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                doTxTap(id);
                return true;
            }
        });

        merchantXpub = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
        push_notifications = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_PUSH_NOTIFS, false);
        doBTC = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY_DISPLAY, false);

        btc_font = TypefaceUtil.getInstance(getActivity()).getTypeface();
        thisActivity = getActivity();

        return view;
    }

    private void loadData() {
        data = new ArrayList<>();
        ////
    }

    private void setListView()  {
        TransactionListViewAdapter adapter = new TransactionListViewAdapter(data, getContext());
        listView.setAdapter(adapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser) {

            merchantXpub = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
            push_notifications = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_PUSH_NOTIFS, false);
            doBTC = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY_DISPLAY, false);

            if(push_notifications) {
                if(timer == null) {
                    timer = new Timer();
                    try {
                        timer.scheduleAtFixedRate(new TimerTask() {
                            public void run() {
                                new GetDataTask().execute();
                            }
                        }, 500L, 1000L * 60L * 2L);
                    }
                    catch(IllegalStateException ise) {
                        ;
                    }
                    catch(IllegalArgumentException iae) {
                        ;
                    }
                }
            }

        }
        else {
            ;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        merchantXpub = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
        push_notifications = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_PUSH_NOTIFS, false);
        doBTC = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY_DISPLAY, false);
        new GetDataTask().execute();

//        loadData();
//        setListView();
    }

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {

            if(merchantXpub != null && merchantXpub.length() > 0) {

                // get updated list from database
                DBControllerV2 pdb = new DBControllerV2(getActivity());
                ArrayList<ContentValues> vals = pdb.getAllPayments();

                if(vals.size() > 0) {
                    mListItems.clear();
                    mListItems.addAll(vals);

                    thisActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {

            super.onPostExecute(result);
        }
    }

    private class TransactionAdapter extends BaseAdapter {

        private LayoutInflater inflater = null;
        private SimpleDateFormat sdf = null;

        TransactionAdapter() {
            inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mListItems.size();
        }

        @Override
        public String getItem(int position) {
            return mListItems.get(position).getAsString("iad");
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view;

            if (convertView == null) {
                view = inflater.inflate(R.layout.cell_transaction, parent, false);
            } else {
                view = convertView;
            }

            ContentValues vals = mListItems.get(position);

            String date_str = DateUtil.getInstance().formatted(vals.getAsLong("ts"));
            SpannableStringBuilder ds = new SpannableStringBuilder(date_str);
            if(date_str.indexOf("@") != -1) {
                int idx = date_str.indexOf("@");
                ds.setSpan(new StyleSpan(Typeface.NORMAL), 0, idx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ds.setSpan(new RelativeSizeSpan(0.75f), idx, date_str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            TextView tvDate = (TextView)view.findViewById(R.id.transaction_date);
            tvDate.setText(ds);
            tvDate.setAlpha(0.7f);

            TextView tvAmount = (TextView)view.findViewById(R.id.transaction_title);
            if(doBTC) {
                String displayValue = null;
                long amount = Math.abs(vals.getAsLong("amt"));
                displayValue = MonetaryUtil.getInstance(getActivity()).getDisplayAmountWithFormatting(amount);

                SpannableStringBuilder cs = new SpannableStringBuilder(getActivity().getResources().getString(R.string.bitcoin_currency_symbol));
                cs.setSpan(new RelativeSizeSpan((float) 0.75), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvAmount.setText(displayValue + " " + cs);
            }
            else {
                SpannableStringBuilder cs = new SpannableStringBuilder(vals.getAsString("famt").subSequence(0, 1));
                cs.setSpan(new RelativeSizeSpan((float) 0.75), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvAmount.setText(cs + " " + vals.getAsString("famt").substring(1));
            }

            return view;
        }

    }

    private void doTxTap(final long item)	{

        final ContentValues val = mListItems.get((int) item);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setItems(new CharSequence[]
                        { "View transaction" },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent( Intent.ACTION_VIEW , Uri.parse("https://blockchain.info/address/" + val.getAsString("iad")));
                                startActivity(intent);
                                break;
                        }
                    }
                });
        builder.create().show();
    }

}
