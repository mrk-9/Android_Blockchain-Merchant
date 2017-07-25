package otgc.com.merchant;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import info.blockchain.api.APIException;
import info.blockchain.api.exchangerates.Currency;
import info.blockchain.api.exchangerates.ExchangeRates;

//import android.util.Log;

public class CurrencyExchange	{

    private static CurrencyExchange instance = null;

	private static Map<String,Currency> ticker = null;
    private static HashMap<String,Double> prices = null;
    private static HashMap<String,String> symbols = null;

	private static Context context = null;
    
    private CurrencyExchange()	{ ; }

	public static CurrencyExchange getInstance(Context ctx) {
		
		context = ctx;

		if (instance == null) {

		    prices = new HashMap<String,Double>();
		    symbols = new HashMap<String,String>();

	    	instance = new CurrencyExchange();
		}

		getExchangeRates();

		return instance;
	}
	
    public Double getCurrencyPrice(String currency)	{
    	
    	if(prices.containsKey(currency) && prices.get(currency) != 0.0)	{
    		return prices.get(currency);
    	}
    	else	{
    		return 0.0;
    	}

    }

    public String getCurrencySymbol(String currency)	{
    	
    	if(symbols.containsKey(currency) && symbols.get(currency) != null)	{
    		return symbols.get(currency);
    	}
    	else	{
    		return null;
    	}

    }

	private static void getExchangeRates() {

		new Thread(new Runnable() {
			@Override
			public void run() {

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

				try	{
					ticker = ExchangeRates.getTicker();

					Set<String> keys = ticker.keySet();
					for(String key : keys)	{
						prices.put(key, Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(0.0))));
						symbols.put(key, prefs.getString(key + "-SYM", null));
					}

					for(String key : keys)	{
						prices.put(key, ticker.get(key).getLast().doubleValue());
						symbols.put(key, ticker.get(key).getSymbol());
					}

					SharedPreferences.Editor editor = prefs.edit();
					for(String key : keys)	{
						if(prices.containsKey(key) && prices.get(key) != 0.0)	{
							editor.putLong(key, Double.doubleToRawLongBits(prices.get(key)));
							editor.putString(key + "-SYM", symbols.get(key));
						}
					}
					editor.commit();

				}
				catch(APIException | IOException e)	{
					e.printStackTrace();
				}

			}
		}).start();

	}

}
