package otgc.com.merchant.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import otgc.com.merchant.R;
import otgc.com.merchant.models.TransactionModel;

public class TransactionListViewAdapter extends BaseAdapter {
    List<TransactionModel> data;
    Context context;

    public TransactionListViewAdapter(List<TransactionModel> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.cell_transaction, parent, false);

        TextView title = (TextView) convertView.findViewById(R.id.transaction_title);
        TextView date = (TextView) convertView.findViewById(R.id.transaction_date);

        TransactionModel model = data.get(position);

        title.setText(model.title);
        date.setText(model.date);

        return convertView;
    }
}
