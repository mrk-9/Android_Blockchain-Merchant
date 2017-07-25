package otgc.com.merchant.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import otgc.com.merchant.R;

public class SSLVerifierThreadUtil {

    private static SSLVerifierThreadUtil instance = null;
    private static Context context = null;
    private int strikeCount = 0;
    private int strikeLimit = 5;

    private SSLVerifierThreadUtil() { ; }

    public static SSLVerifierThreadUtil getInstance(Context ctx) {

        context = ctx;

        if(instance == null) {
            instance = new SSLVerifierThreadUtil();
        }

        return instance;
    }

    public void validateSSLThread() {

        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                if(ConnectivityStatus.hasConnectivity(context)) {

                    if(!SSLVerifierUtil.getInstance(context).isValidHostname()) {

                        strikeCount++;
                        if(strikeCount < strikeLimit){

                            //Possible connection issue, retry  ssl verify
                            try{Thread.sleep(1000);}catch (Exception e){}
                            validateSSLThread();
                            return;

                        }
                        else{

                            //ssl verify failed 5 time - something is wrong, warn user
                            final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                            final String message = context.getString(R.string.ssl_hostname_invalid);

                            if(!((Activity) context).isFinishing()) {
                                builder.setMessage(message)
                                        .setCancelable(false)
                                        .setPositiveButton(R.string.dialog_continue,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface d, int id) {
                                                        d.dismiss();
                                                    }
                                                });

                                builder.create().show();
                            }
                        }
                    }

                    if(!SSLVerifierUtil.getInstance(context).certificatePinned()) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        final String message = context.getString(R.string.ssl_pinning_invalid);

                        if(!((Activity) context).isFinishing()) {
                            builder.setMessage(message)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.dialog_continue,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface d, int id) {
                                                    d.dismiss();
                                                }
                                            });

                            builder.create().show();
                        }
                    }

                    strikeCount = 0;
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ;
                    }
                });

                Looper.loop();

            }
        }).start();
    }

}
