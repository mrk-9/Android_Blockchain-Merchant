package otgc.com.merchant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import otgc.com.merchant.fragments.AboutFragment;
import otgc.com.merchant.fragments.AtmFragment;
import otgc.com.merchant.fragments.ContactFragment;
import otgc.com.merchant.fragments.MainFragment;
import otgc.com.merchant.fragments.NewsFragment;
import otgc.com.merchant.fragments.SettingsFragment;
import otgc.com.merchant.fragments.TransactionFragment;
import otgc.com.merchant.service.WebSocketHandler;
import otgc.com.merchant.service.WebSocketListener;
import otgc.com.merchant.util.SSLVerifierThreadUtil;

public class MainActivity extends AppCompatActivity
        implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback, WebSocketListener, NavigationView.OnNavigationItemSelectedListener {

    private WebSocketHandler webSocketHandler = null;

    public static final String ACTION_INTENT_SUBSCRIBE_TO_ADDRESS = "otgc.com.merchant.MainActivity.SUBSCRIBE_TO_ADDRESS";
    public static final String ACTION_INTENT_INCOMING_TX = "otgc.com.merchant.MainActivity.ACTION_INTENT_INCOMING_TX";
    public static final String ACTION_INTENT_RECONNECT = "otgc.com.merchant.MainActivity.ACTION_INTENT_RECONNECT";
    private static int PIN_ACTIVITY 		= 2;

    String current_tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        addFragment(new MainFragment(), "Main");
        current_tag = "Main";

        SSLVerifierThreadUtil.getInstance(MainActivity.this).validateSSLThread();

        webSocketHandler = new WebSocketHandler();
        webSocketHandler.addListener(this);
        webSocketHandler.start();
    }

    ////////////////////////////////

    @Override
    protected void onResume() {
        super.onResume();
        if (current_tag.equals("Settings"))
            addFragment(new SettingsFragment(), "Settings");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {

        if(Build.VERSION.SDK_INT < 16){
            return;
        }

        final String eventString = "onNdefPushComplete\n" + event.toString();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), eventString, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        //Stop websockets
        webSocketHandler.stop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);

        super.onDestroy();
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        if(Build.VERSION.SDK_INT < 16){
            return null;
        }

        NdefRecord rtdUriRecord = NdefRecord.createUri("market://details?id=info.blockchain.merchant");
        NdefMessage ndefMessageout = new NdefMessage(rtdUriRecord);
        return ndefMessageout;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);

        View view = this.getCurrentFocus();

        if (view instanceof EditText) {
            View w = this.getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }

        return ret;
    }

    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {

            if (ACTION_INTENT_SUBSCRIBE_TO_ADDRESS.equals(intent.getAction())) {
                webSocketHandler.subscribeToAddress(intent.getStringExtra("address"));
            }

            //Connection re-established
            if (ACTION_INTENT_RECONNECT.equals(intent.getAction())) {
                if(webSocketHandler != null && !webSocketHandler.isConnected()){
                    webSocketHandler.start();
                }
            }
        }
    };

    @Override
    public void onIncomingPayment(String addr, long paymentAmount) {

        //New incoming payment - broadcast message
        Intent intent = new Intent(MainActivity.ACTION_INTENT_INCOMING_TX);
        intent.putExtra("payment_address",addr);
        intent.putExtra("payment_amount",paymentAmount);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    ///////////////////////

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_charge) {
            //home screen
            addFragment(new MainFragment(), "Main");
        } else if (id == R.id.nav_transaction) {
            addFragment(new TransactionFragment(), "Transaction");
        } else if (id == R.id.nav_atms) {
            addFragment(new AtmFragment(), "ATM");
        } else if (id == R.id.nav_news) {
            addFragment(new NewsFragment(), "News");
        } else if (id == R.id.nav_contact) {
            addFragment(new ContactFragment(), "Contact");
        } else if (id == R.id.nav_aboutus) {
            addFragment(new AboutFragment(), "About");
            current_tag = "About";
        } else if (id == R.id.nav_setttings) {
            Intent intent = new Intent(MainActivity.this, PinActivity.class);
            intent.putExtra("create", false);
            startActivityForResult(intent, PIN_ACTIVITY);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void addFragment(Fragment fragment, String commit)  {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, fragment, commit)
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == PIN_ACTIVITY && resultCode == RESULT_OK) {
            current_tag = "Settings";
        }
    }
}
