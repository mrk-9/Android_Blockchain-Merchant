package otgc.com.merchant.service;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class WebService {
    private static final String URL_NOTIFICATION = "https://onesignal.com/api/v1/notifications?app_id=75c07185-bc78-4940-885f-2c8d03f25dde";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void getAllNotifications(RequestParams params, AsyncHttpResponseHandler handler){
        client.get(URL_NOTIFICATION, params, handler);
    }
}
