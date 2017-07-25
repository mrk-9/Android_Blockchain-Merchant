package otgc.com.merchant.util;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import otgc.com.merchant.R;

public class ToastCustom{

    public static final String TYPE_ERROR = "TYPE_ERROR";
    public static final String TYPE_GENERAL = "TYPE_GENERAL";
    public static final String TYPE_OK = "TYPE_OK";

    public static final int LENGTH_SHORT = 0;
    public static final int LENGTH_LONG = 1;

    private static Toast toast = null;

    public static void makeText(final Context context,final  CharSequence text,final  int duration,final  String type) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                if(toast!=null)toast.cancel();
                toast = Toast.makeText(context, text, duration);

                LayoutInflater inflate = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = inflate.inflate(R.layout.transient_notification, null);
                TextView tv = (TextView) v.findViewById(R.id.message);
                tv.setText(text);

                if (type.equals(TYPE_ERROR)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        tv.setBackground(context.getResources().getDrawable(R.drawable.rounded_view_toast_error));
                    else
                        tv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.rounded_view_toast_error));
                    tv.setTextColor(context.getResources().getColor(R.color.toast_error_text));

                }else if(type.equals(TYPE_GENERAL)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        tv.setBackground(context.getResources().getDrawable(R.drawable.rounded_view_toast_warning));
                    else
                        tv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.rounded_view_toast_warning));
                    tv.setTextColor(context.getResources().getColor(R.color.toast_warning_text));

                }else if(type.equals(TYPE_OK)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        tv.setBackground(context.getResources().getDrawable(R.drawable.rounded_view_toast_info));
                    else
                        tv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.rounded_view_toast_info));
                    tv.setTextColor(context.getResources().getColor(R.color.toast_info_text));
                }
                toast.setView(v);
                toast.show();

                Looper.loop();
            }
        }).start();
    }
}
