package com.sdl.brogrammers.paytmgateway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseFunctions m_Functions;
    private final int ActivityRequestCode = 0x0abc;

    final Double TXN_AMOUNT = 1.0;
    final String ORDER_ID = "ORDER0001";
    final String CUST_ID = "athang213";
    final String EMAIL = "athang213@gmail.com";
    final String MOBILE_NO = "9819785790";

    String CHECKSUMHASH;
    String MID;
    String WEBSITE;
    String CHANNEL_ID;
    String INDUSTRY_TYPE_ID;
    String MERCHANT_KEY;
    String CALLBACK_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_Functions = FirebaseFunctions.getInstance();
    }

    public void getPaymentDetails(View view) throws JSONException {

        Map<String, Object> data = new HashMap<>();
        data.put("ORDER_ID", ORDER_ID);
        data.put("CUST_ID", CUST_ID);
        data.put("TXN_AMOUNT", TXN_AMOUNT);
        data.put("EMAIL", EMAIL);
        data.put("MOBILE_NO", MOBILE_NO);

        JsonObjectRequest checksumRequest = new JsonObjectRequest(Request.Method.POST,
                "https://us-central1-spotsale-50962.cloudfunctions.net/generate_checksum",
                new JSONObject(data),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            CHECKSUMHASH = response.getString("checksum");
                            MID = response.getString("mid");
                            WEBSITE = response.getString("website");
                            CHANNEL_ID = response.getString("channel_id");
                            INDUSTRY_TYPE_ID = response.getString("industry");
                            MERCHANT_KEY = response.getString("merchant_key");
                            CALLBACK_URL = response.getString("callback_url");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Toast.makeText(getApplicationContext(), "ChecksumHash : " + CHECKSUMHASH, Toast.LENGTH_SHORT).show();
                        Log.d("CHECKSUMHASH", CHECKSUMHASH);
                        Log.d("MID", MID);
                        Log.d("WEBSITE", WEBSITE);
                        Log.d("CHANNEL_ID", CHANNEL_ID);
                        Log.d("INDUSTRY_TYPE_ID", INDUSTRY_TYPE_ID);
                        Log.d("MERCHANT_KEY", MERCHANT_KEY);
                        Log.d("CALLBACK_URL", CALLBACK_URL);

                        startPayment();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error!", Toast.LENGTH_LONG).show();
                        Log.e("checksumRequest", error.toString());
                        VolleyLog.e("", error.getMessage());
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);

        queue.add(checksumRequest);

    }

    private void startPayment() {

        PaytmPGService paytmService = PaytmPGService.getStagingService();

        HashMap<String, String> paramMap = new HashMap<String,String>();
        paramMap.put( "MID" , MID);
        paramMap.put( "ORDER_ID" , ORDER_ID);
        paramMap.put( "CUST_ID" , CUST_ID);
        paramMap.put( "MOBILE_NO" , MOBILE_NO);
        paramMap.put( "EMAIL" , EMAIL);
        paramMap.put( "CHANNEL_ID" ,CHANNEL_ID );
        paramMap.put( "TXN_AMOUNT" , String.valueOf(TXN_AMOUNT));
        paramMap.put( "WEBSITE" , WEBSITE);
        paramMap.put( "INDUSTRY_TYPE_ID" , INDUSTRY_TYPE_ID);
        paramMap.put( "CALLBACK_URL", CALLBACK_URL);
        paramMap.put( "CHECKSUMHASH" , CHECKSUMHASH);
        PaytmOrder Order = new PaytmOrder(paramMap);

/*
        JsonObjectRequest verificationRequest = new JsonObjectRequest(Request.Method.POST,
                "https://us-central1-spotsale-50962.cloudfunctions.net/verify_checksum",
                new JSONObject(paramMap),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean verification = response.getBoolean("verification");
                            Toast.makeText(MainActivity.this, "verification:"+verification, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error!", Toast.LENGTH_LONG).show();
                        Log.e("checksumRequest", error.toString());
                        VolleyLog.e("", error.getMessage());
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(verificationRequest);
/**/


        paytmService.initialize(Order, null);
        paytmService.startPaymentTransaction(this, true, true,
                new PaytmPaymentTransactionCallback() {
                    @Override
                    public void onTransactionResponse(Bundle inResponse) {
                        Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();
                        Log.d("TRANSACTION_RESPONSE", inResponse.toString());
                    }

                    @Override
                    public void networkNotAvailable() {
                        Toast.makeText(getApplicationContext(), "Network connection error: Check your internet connectivity", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void clientAuthenticationFailed(String inErrorMessage) {
                        Toast.makeText(getApplicationContext(), "Authentication failed: Server error" + inErrorMessage.toString(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void someUIErrorOccurred(String inErrorMessage) {
                        Toast.makeText(getApplicationContext(), "UI Error " + inErrorMessage , Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                        Toast.makeText(getApplicationContext(), "Unable to load webpage " + inErrorMessage, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onBackPressedCancelTransaction() {
                        Toast.makeText(getApplicationContext(), "Transaction cancelled by user" , Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                        Toast.makeText(getApplicationContext(), "Transaction cancelled " + inErrorMessage , Toast.LENGTH_LONG).show();
                    }
                });
/**/


    }

}
