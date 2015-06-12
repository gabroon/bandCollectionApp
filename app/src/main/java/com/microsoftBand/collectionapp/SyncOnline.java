package com.microsoftBand.collectionapp;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohamed on 11/06/2015.
 */
public class SyncOnline {
    private String url;
    List<NameValuePair> nameValuePairs;
//
    public SyncOnline(String url){
        this.url=url;
        nameValuePairs=  new ArrayList<NameValuePair>(2);
    }

    public void postData() {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        try {
            // Add your data
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
        } catch (ClientProtocolException e) {

        } catch (IOException e) {
            
        }
    }
    public void resetArrayList(){
        nameValuePairs=new ArrayList<NameValuePair>(2);
    }

    public void addValuesToList(String name,String value){
        nameValuePairs.add(new BasicNameValuePair(name,value));
    }
}
