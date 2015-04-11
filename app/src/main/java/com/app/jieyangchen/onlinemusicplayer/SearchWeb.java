package com.app.jieyangchen.onlinemusicplayer;

import com.app.jieyangchen.onlinemusicplayer.*;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class SearchWeb extends Activity {
	private WebView broswer;
    private EditText address;
    private Button gobutton;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web);
        broswer=(WebView)findViewById(R.id.webView1);
        address=(EditText)findViewById(R.id.addressText);
        gobutton=(Button)findViewById(R.id.Go_Button);


        gobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                broswer.loadUrl(address.getText().toString());
                WebSettings mWebSettings = broswer.getSettings();
                mWebSettings.setJavaScriptEnabled(true);
                broswer.setWebViewClient(new WebViewClient(){
                    public boolean shouldOverrideUrlLoading(WebView view, String url){
                        Log.d("TEST","URL:"+url);
                        if(url.endsWith(".mp3")){
                            Intent intent = new Intent();
                            intent.setClass(SearchWeb.this, MusicUI.class);
                            intent.putExtra("url", url);
                            intent.putExtra("isOnline", 1);
                            startActivity(intent);
                            finish();
                        }
                        return false;
                    }
                });
            }
        });
	}
}
