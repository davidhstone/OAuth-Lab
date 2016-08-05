package com.example.davidstone.twitter_oauthlab;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// import java.util.Base64;


/**
 * Created by davidstone on 8/4/16.
 */
public class AuthenticateActivity extends AppCompatActivity {
    WebView mWebView;
    //Encoder encoder;
    String BearerToken = TwitterAppData.BEARER_TOKEN_CREDENTIALS;
  //  String consumerKey = TwitterAppData.CONSUMER_KEY;
  //  String consumerSecret = TwitterAppData.CONSUMER_SECRET;

    
    private final String YOUR_AUTHORIZATION_URL = "https://api.twitter.com/oauth2/" +
            TwitterAppData.CONSUMER_SECRET; //ENTER YOUR AUTHORIZATION URL HERE
    /*
    private final String YOUR_AUTHORIZATION_URL = "https://api.Twitter.com/oauth/authorize/?" +
            "client_id=" + TwitterAppData.CLIENT_ID  + "&redirect_uri="  + TwitterAppData.CALLBACK_URL +
            "&response_type=code"; //ENTER YOUR AUTHORIZATION URL HERE
    */        
    private static final String TAG = "AuthenticateActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);
        
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.contains("code=")){ //CHECKING TO SEE IF THE URL WE HAVE IS THE ONE WE WANT
                    Log.i(TAG, "shouldOverrideUrlLoading: " + url);

                    int index = url.indexOf("=");
                    Log.i(TAG, url.substring(index+1));

                    //STRIPPING AWAY THE URL AND ONLY KEEPING THE CODE
                    String code = url.substring(index+1);
                    getBearerToken(code);
                    return true;
                }
                else {
                    return false;
                }
            }
        });
        mWebView.loadUrl(YOUR_AUTHORIZATION_URL); //WHAT DO YOU THINK THE URL SHOULD BE?
    }

    // I FOUND THE FOLLOWING CODE ONLINE WHICH WAS SUPPOSED TO, ALONG WITH THE BASE64 IMPORT,
    // ENCODE THE BEARER TOKEN TO BASE64, BUT IT ISN'T LETTING ME DO THAT
    /*
    private String getEncodedBearerTokenCredentials (String bearerToken){
        Base64.Encoder encoder = Base64.getEncoder();
        String normalString = BearerToken;
        String encodedString = encoder.encodeToString(
                normalString.getBytes(StandardCharsets.UTF_8));
        )
        return encodedString;
    }
    */
    private String authenticationPost (String myUrl) throws IOException, JSONException {
        DataOutputStream os = null;
        InputStream is = null;

        try{
            URL url = new URL (myUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            String urlParameters = "param1=a&param2=b";
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset","utf-8");
            conn.setRequestProperty("content-Length",Integer.toString(postDataLength));

            os = new DataOutputStream(conn.getOutputStream());
            os.write(postData);
            os.flush();

            is = conn.getInputStream();
            return readIt(is);
        }finally {
            if(is != null){
                is.close();
            }
            if(os != null) {
                os.close();
            }
        }
    }

    private String readIt(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String read;

        while((read = br.readLine()) != null) {
            sb.append(read);
        }
        return sb.toString();
    }

    private void getBearerToken(String code){
        //WE'LL WORK ON THIS TOGETHER
        Log.d(TAG, AuthenticateActivity.class.getName()+": Code: "+code);
//        OkHttpClient
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
              //  .add("client_id", TwitterAppData.CONSUMER_KEY)
                .add("client_secret", TwitterAppData.CONSUMER_SECRET)
               // .add("redirect_uri", TwitterAppData.CALLBACK_URL)
                .add("code", code)
                .add("grant_type", "authorization_code")
                .build();

        Request request = new Request.Builder()
                .url("https://api.Twitter.com/oauth/bearer_token")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: request Failed");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!response.isSuccessful()){
                    throw new IOException("Unexpected code " + response);
                }
                String responseString = response.body().string();
                Log.i(TAG, "onResponse: " + responseString);

                try {
                    JSONObject result = new JSONObject(responseString);
                    String accessToken = result.getString("access_token");

                    Log.i(TAG, "onResponse: access token - " + accessToken);

                    Intent intent = new Intent(AuthenticateActivity.this, MainActivity.class);
                    intent.putExtra("access_token", accessToken);
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
