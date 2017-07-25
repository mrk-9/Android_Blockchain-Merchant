package otgc.com.merchant.fragments;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import otgc.com.merchant.R;
import otgc.com.merchant.adapters.ContactListViewAdapter;
import otgc.com.merchant.models.ContactModel;

public class ContactFragment extends Fragment {

    private static final String URL_WEBSITE = "http://onthegocoins.com/";
    private static final String URL_EMAIL = "support@onthegocoins.com";
    private static final String URL_PHONE = "+15148276696";
    private static final String URL_FACEBOOK = "https://www.facebook.com/OnTheGoCoins/";
    private static final String URL_TWITTER = "https://twitter.com/OnTheGoCoins";
    private static final String URL_GOOGLE = "https://plus.google.com/+BitcoinATMOnTheGoCoinsLaval";
    private static final String URL_YOUTUBE = "";

    ListView listView;
    ArrayList<ContactModel> data;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        listView = (ListView) view.findViewById(R.id.contact_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ////
                String url;
                Intent browserIntent;

                if (position == 0) { //website
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_WEBSITE));
                    startActivity(browserIntent);
                } else if (position == 1) { //email
                    browserIntent = new Intent(Intent.ACTION_SEND);
                    browserIntent.setType("message/rtc822");
                    browserIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{URL_EMAIL});
                    browserIntent.putExtra(Intent.EXTRA_SUBJECT, "OTGC Merchant");
                    browserIntent.putExtra(Intent.EXTRA_TEXT, "Please write here...");
                    try {
                        startActivity(Intent.createChooser(browserIntent, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                    }
                } else if (position == 2) { //phone
                    browserIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel" + URL_PHONE));
                    try {
                        startActivity(browserIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), "There is not available phone call.", Toast.LENGTH_SHORT).show();
                    }
                } else if (position == 3) { //facebook
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_FACEBOOK));
                    startActivity(browserIntent);
                } else if (position == 4) { //twitter
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_TWITTER));
                    startActivity(browserIntent);
                } else if (position == 5) { //google
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_GOOGLE));
                    startActivity(browserIntent);
                } else {
                    //Youtube not ready from client
                }
            }
        });

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
            int requestCode = 0;
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, requestCode);
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

        String[] titles = getResources().getStringArray(R.array.contacts);
        int[] imgs = getProfileImage();

        for (int i = 0; i < titles.length; i ++)    {
            ContactModel model = new ContactModel();
            model.title = titles[i];
            model.logo = imgs[i];
            data.add(model);
        }
    }

    private int[] getProfileImage() {
        int[] imgs = {R.mipmap.icons8_0, R.mipmap.icons8_1, R.mipmap.icons8_2, R.mipmap.icons8_3, R.mipmap.icons8_4, R.mipmap.icons8_5, R.mipmap.icons8_6};
        return imgs;
    }

    private void setListView()  {
        ContactListViewAdapter adapter = new ContactListViewAdapter(data, getContext());
        listView.setAdapter(adapter);
    }

}
