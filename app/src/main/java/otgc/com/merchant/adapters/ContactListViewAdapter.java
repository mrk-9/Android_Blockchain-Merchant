package otgc.com.merchant.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import otgc.com.merchant.R;
import otgc.com.merchant.models.ContactModel;

public class ContactListViewAdapter extends BaseAdapter {

    List<ContactModel> data;
    Context context;

    public ContactListViewAdapter(List<ContactModel> data, Context context) {
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
        convertView = LayoutInflater.from(context).inflate(R.layout.cell_contact, parent, false);

        TextView title = (TextView) convertView.findViewById(R.id.contact_title);
        ImageView profile_view = (ImageView) convertView.findViewById(R.id.contact_image);

        ContactModel model = data.get(position);

        profile_view.setImageResource(model.logo);
        title.setText(model.title);

        return convertView;
    }
}
