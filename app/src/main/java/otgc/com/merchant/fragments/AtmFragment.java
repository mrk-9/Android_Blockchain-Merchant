package otgc.com.merchant.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import otgc.com.merchant.MapActivity;
import otgc.com.merchant.R;
import otgc.com.merchant.adapters.AtmListViewAdapter;
import otgc.com.merchant.models.AtmModel;


public class AtmFragment extends Fragment {

    ListView listView;
    ArrayList<AtmModel> data;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_atm, container, false);
        listView = (ListView) view.findViewById(R.id.atm_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ////
                Intent intent = new Intent(getActivity(), MapActivity.class);
                intent.putExtra("index", position);
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "There is not available Google map.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            int requestCode = 0;
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
        }

        return view;
    }

    @Override
    public void onResume()  {
        super.onResume();
        loadData();
        setListView();
    }

    private void loadData() {
        data = new ArrayList<>();

        String[] titles = getResources().getStringArray(R.array.atm_titles);
        String[] addresses = getResources().getStringArray(R.array.atm_address);
        String[] operatings = getResources().getStringArray(R.array.atm_operating);
        String[] tags = getResources().getStringArray(R.array.atm_tag);
        int[] imgs = getProfileImage();

        for (int i = 0; i < titles.length; i ++)    {
            AtmModel model = new AtmModel();
            model.title = titles[i];
            model.address = addresses[i];
            model.operating = operatings[i];
            model.tag = tags[i];
            model.logo = imgs[i];
            data.add(model);
        }
    }

    private int[] getProfileImage() {
        int[] imgs = {R.mipmap.img_atm1, R.mipmap.img_atm2, R.mipmap.img_atm3, R.mipmap.img_atm4, R.mipmap.img_atm5};
        return imgs;
    }

    private void setListView()  {
        AtmListViewAdapter adapter = new AtmListViewAdapter(data, getContext());
        listView.setAdapter(adapter);
    }
}
