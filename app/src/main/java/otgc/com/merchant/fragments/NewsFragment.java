package otgc.com.merchant.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import otgc.com.merchant.R;
import otgc.com.merchant.adapters.NewsListViewAdapter;
import otgc.com.merchant.models.NewsModel;

public class NewsFragment extends Fragment {

    private static final String HEADER_AUTHRICATION = "Basic ZWU0YjI1MTAtMWRmZi00YjBlLTg3YjctYWU1NTkzMjUwNWVm";
    private static final String URL_NOTIFICATION = "https://onesignal.com/api/v1/notifications?app_id=75c07185-bc78-4940-885f-2c8d03f25dde";

    ListView listView;
    ArrayList<NewsModel> data;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        listView = (ListView) view.findViewById(R.id.news_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ////
            }
        });

        return view;
    }

    @Override
    public void onResume()  {
        super.onResume();
        data = new ArrayList<>();
        loadData();
    }

    private void loadData() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Authorization", HEADER_AUTHRICATION);
        client.get(getContext(), URL_NOTIFICATION, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                JSONArray obj_array = getJSONArray(response, "notifications");
                if (obj_array != null) {
                    data = new ArrayList<NewsModel>();

                    for (int i = 0; i < obj_array.length(); i++) {
                        JSONObject obj = getJSONObject(obj_array, i);
                        if (obj != null) {
                            NewsModel model = new NewsModel();
                            JSONObject contents = getJSONObject(obj, "contents");
                            if (contents != null)   {
                                String title = getString(contents, "en");
                                if (title != null)  {
                                    model.title = title;
                                }
                            }
                            String date = getString(obj, "queued_at");
                            if (date != null)   {
                                model.date = date;
                            }

                            data.add(model);
                        }

                    }
                    setListView();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });

    }

    private void setListView()  {
        if (listView.getAdapter() != null)  {
            listView.setAdapter(null);
        }
        NewsListViewAdapter adapter = new NewsListViewAdapter(data, getContext());
        listView.setAdapter(adapter);
    }

    private JSONObject getJSONObject(JSONObject obj, String key)    {
        try {
            JSONObject result = obj.getJSONObject(key);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONObject getJSONObject(JSONArray obj_array, int position) {
        try {
            JSONObject result = obj_array.getJSONObject(position);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONArray getJSONArray(JSONObject obj, String key)  {
        try {
            JSONArray result = obj.getJSONArray(key);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getString(JSONObject obj, String key)    {
        try {
            String result = obj.getString(key);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
