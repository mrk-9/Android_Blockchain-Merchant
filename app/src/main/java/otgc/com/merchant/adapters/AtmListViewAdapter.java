package otgc.com.merchant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import otgc.com.merchant.R;
import otgc.com.merchant.models.AtmModel;


public class AtmListViewAdapter extends BaseAdapter {

    List<AtmModel> data;
    Context context;

    public AtmListViewAdapter(List<AtmModel> data, Context context) {
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
        convertView = LayoutInflater.from(context).inflate(R.layout.cell_atm, parent, false);

        TextView title = (TextView) convertView.findViewById(R.id.atm_title);
        TextView address = (TextView) convertView.findViewById(R.id.atm_address);
        TextView operating = (TextView) convertView.findViewById(R.id.atm_operating);
        TextView tag = (TextView) convertView.findViewById(R.id.atm_tag);
        CircleImageView profile_view = (CircleImageView) convertView.findViewById(R.id.atm_profile_image);

        AtmModel model = data.get(position);

        profile_view.setImageResource(model.logo);
        title.setText(model.title);
        address.setText(model.address);
        operating.setText(model.operating);
        tag.setText(model.tag);

        return convertView;
    }
}
