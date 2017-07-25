package otgc.com.merchant.service;

public interface WebSocketListener {
    void onIncomingPayment(String addr, long paymentAmount);
}
