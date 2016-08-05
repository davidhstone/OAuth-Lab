package com.example.davidstone.twitter_oauthlab;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private Button mButton;
    private EditText mEditText;
    private ListView mListView;

    ArrayList<String> mTweetList;
    ArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.button1);
        mEditText = (EditText) findViewById(R.id.search_edittext);
        mListView = (ListView) findViewById(R.id.listview);
        mTweetList = new ArrayList<>();
        mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTweetList.clear();
                new DownloadTask().execute("http://api.walmartlabs.com" +
                        "/v1/search?apiKey=tp3ecfpvms4jjtmyj9rt2wqg&format=json&query=cereal");
            }
        });

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            Toast.makeText(this, "You're connected",
                    Toast.LENGTH_LONG).show();
        }
        else {
            //the connection is not available
            Toast.makeText(this, "You're not connected",
                    Toast.LENGTH_LONG).show();
        }
    }



    public void downloadUrl(String myUrl) throws IOException, JSONException {
        InputStream is = null;
        try {
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            is = conn.getInputStream();

            String contentAsString = readIt2(is);
            parseJson(contentAsString);

        } finally{
            if (is != null){
                is.close();
            }
        }
    }

    private String readIt2(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String read;

        while((read = br.readLine()) != null) {
            sb.append(read);
        }
        return sb.toString();
    }

    private void parseJson(String contentAsString) throws JSONException {

        JSONObject search = new JSONObject(contentAsString);
        JSONArray items = search.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {

            JSONObject item = items.getJSONObject(i);
            mTweetList.add(item.getString("name"));
        }
    }
    private class DownloadTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();

            } catch (JSONException e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {

            super.onPostExecute(s);
            mAdapter.notifyDataSetChanged();
        }
    }
}
