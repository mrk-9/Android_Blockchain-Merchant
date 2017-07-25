package otgc.com.merchant.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import otgc.com.merchant.R;
import otgc.com.merchant.models.NewsModel;
import otgc.com.merchant.util.DateUtil;

public class NewsListViewAdapter extends BaseAdapter {
    List<NewsModel> data;
    Context context;

    public NewsListViewAdapter(List<NewsModel> data, Context context) {
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
        convertView = LayoutInflater.from(context).inflate(R.layout.cell_news, parent, false);

        TextView title = (TextView) convertView.findViewById(R.id.news_title);
        TextView date = (TextView) convertView.findViewById(R.id.news_date);

        NewsModel model = data.get(position);

        title.setText(model.title);
        long unixScconds = Long.parseLong(model.date);
        String date_str = DateUtil.getInstance().formatted(unixScconds);
        date.setText(date_str);


        return convertView;
    }
}
