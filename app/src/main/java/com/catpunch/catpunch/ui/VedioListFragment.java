package com.catpunch.catpunch.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.catpunch.catpunch.R;
import com.catpunch.catpunch.util.LogUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.GenericArrayType;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.net.HttpCookie.parse;

/**
 * Created by vic on 2015/8/19.
 */
public class VedioListFragment extends Fragment {
    private static final String TAG = VedioListFragment.class.getSimpleName();

    private Activity mActivity;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ProgressBar mProgressbar;
    private TextView mTextSeries;
    private TextView mTextDescription;
    private ImageView mTextThumbnail;

    private String mSeries = null;
    private String mDesc = null;
    private String mThumbnail = null;
    private JSONArray mJsonArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();

        Bundle args = getArguments();
        if (args != null) {
            try {
                Log.i("Vedio's jsonString = ", args.getString("jsonString"));
                String jsonstring = args.getString("jsonString");
                JSONObject jsonObject = new JSONObject(jsonstring);
                mJsonArray = jsonObject.getJSONArray("list");//*
                Log.i("Vedio's mJsonArray = ", mJsonArray.toString());
                mSeries = args.getString("series");
                mDesc = args.getString("description");
                mThumbnail = args.getString("thumbnail");
            } catch (Exception e) {
//                LogUtils.d(TAG, e, e.getMessage());
                e.printStackTrace();
            }
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vedio_list, container, false);
        mProgressbar = (ProgressBar) view.findViewById(R.id.progressbar);
        mTextSeries = (TextView) view.findViewById(R.id.series);
        mTextDescription = (TextView) view.findViewById(R.id.description);
        mTextThumbnail = (ImageView) view.findViewById(R.id.thumbnail);

        mTextSeries.setText(mSeries);
        mTextDescription.setText(mDesc);
        if (mThumbnail != null && !mThumbnail.equals("")) {
            Picasso.with(mActivity)
                    .load(mThumbnail)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(mTextThumbnail);
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(mActivity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mProgressbar = (ProgressBar) view.findViewById(R.id.progressbar);

        return view;
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private int selected = -1;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public CardView mCardView;
            public ImageButton mThumbnail;
            public TextView mTitle;

            public ViewHolder(View v) {
                super(v);
                mTitle = (TextView) v.findViewById(R.id.title);
                mThumbnail = (ImageButton) v.findViewById(R.id.thumbnail);
                mCardView = (CardView) v.findViewById(R.id.card_view);
            }
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_vedio, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            try {
                final JSONObject jsonObject = mJsonArray.getJSONObject(position);
                holder.mTitle.setText(jsonObject.getString("title"));
                holder.mCardView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mProgressbar.setVisibility(View.VISIBLE);
                        try {
                            new RequestTask().execute(jsonObject.getString("hash"), jsonObject.getString("title"));
                        } catch (JSONException e) {
//                            LogUtils.d(TAG, e, e.getMessage());
                            e.printStackTrace();
                        }
                        setSelected(position);
                        notifyDataSetChanged();
                    }
                });
            } catch (JSONException e) {
//                LogUtils.d(TAG, e, e.getMessage());
                e.printStackTrace();
            }

            if (position == selected) {
                holder.mCardView.setCardBackgroundColor(getResources().getColor(R.color.green));
            } else {
                holder.mCardView.setCardBackgroundColor(getResources().getColor(R.color.white));
            }

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            int count = 0;
            if (mJsonArray != null) {
                count = mJsonArray.length();
            }
            return count;
        }

        public void setSelected(int pos) {
            selected = pos;
        }

    }

    private class RequestTask extends AsyncTask<String, Void, Void> {
        public String jsonString = null;
        public String vedioUrl = null;
        public String track = null;
        public String sub = null;
        public JSONArray jAry;
        public JSONArray jAryTrack;
        public JSONObject jObj;
        public JSONObject jObjTrack;
        public Uri subUri = null;
        public String Cookies = null;
        public String hash = null;
        public URL urll = null;
        public String RedirectString = null;


        @Override
        protected Void doInBackground(String... params) {
            hash = params[0];
            try {
                String path = "http://t3.catpunch.co/gs/" + params[0];
                Log.i("PATH = ", path);
                URL url = new URL(path);
                System.out.print("URL = " + url.toString());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                Cookies = con.getHeaderField("Set-Cookie");
                Log.i("Cookie ===== ", Cookies);
                con.setRequestMethod("GET");
                con.setReadTimeout(15000);
                try {
                    switch (con.getResponseCode()) {
                        case 200:
                            LogUtils.d(TAG, "response: 200");
                            InputStream is = con.getInputStream();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = is.read(buffer)) != -1) {
                                baos.write(buffer, 0, len);
                            }
                            jsonString = baos.toString();
                            jAry = new JSONArray(jsonString);
                            jObj = jAry.getJSONObject(0);
                            vedioUrl = jObj.getString("file");
                            track = jObj.getString("tracks");
                            jAryTrack = new JSONArray(track);
                            jObjTrack = jAryTrack.getJSONObject(0);
                            sub = jObjTrack.getString("file");
                            subUri = Uri.parse(sub);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri videoUrl = Uri.parse(vedioUrl);
                            intent.setDataAndType(videoUrl, "application/x-mpegURL");
                            intent.setPackage("com.mxtech.videoplayer.ad");
                            intent.putExtra("subs", new Parcelable[]{subUri});
                            startActivity(intent);
                            baos.close();
                            is.close();
                            break;
                        case 403:
                            LogUtils.d(TAG, "response: 403");
                            String path1 = "http://t3.catpunch.co/gs/" + params[0];
                            Log.i("PATH1 = ", path1);
                            URL url_1 = new URL(path1);
                            urll = url_1;
                            HttpURLConnection ucon = (HttpURLConnection) url_1.openConnection();
                            ucon.addRequestProperty("Cookie", Cookies);//*
                            try {
                                ucon.setRequestMethod("POST"); //
                                ucon.setReadTimeout(15000);
                                ucon.setInstanceFollowRedirects(false);
                                switch (ucon.getResponseCode()) {

                                    case 202:
                                        Log.d(TAG, "response: 202");
                                        String path2 = ucon.getHeaderField("cptv-redirect");
                                        RedirectString = path2;
                                        URL url2 = new URL(path2);
//                                        HttpURLConnection cookie = (HttpURLConnection) url2.openConnection();
//                                        cookie.addRequestProperty("Cookie", Cookies);
                                        HttpURLConnection redirCon = (HttpURLConnection) url2.openConnection();
                                        try {

                                            redirCon.setRequestMethod("GET");
                                            redirCon.setReadTimeout(15000);
                                            redirCon.setRequestProperty("referer", "http://localhost");
                                            Log.i("redirCon ==", String.valueOf(redirCon.getResponseCode()));
                                            Log.i("redirConString ==", redirCon.toString());
//                                            BufferedReader bw = new BufferedReader(new InputStreamReader(redirCon.getInputStream()));
                                            InputStream in = new BufferedInputStream(redirCon.getInputStream());//*
                                            String crlf = "\r\n";
                                            String twoHyphens = "--";
                                            String boundary = "*****";
                                            String path3 = " http://t3.catpunch.co/gs/" + params[0];
                                            URL url3 = new URL(path3);
                                            HttpURLConnection conn = (HttpURLConnection) url3.openConnection();
                                            conn.setRequestProperty("Cookie", Cookies);
                                            try {
                                                conn.setRequestMethod("POST");
                                                conn.setReadTimeout(15000);
                                                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="
                                                        + boundary);
                                                conn.setDoInput(true);
                                                conn.setDoOutput(true);
                                                conn.setChunkedStreamingMode(0);
                                                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                                                int read1;
                                                String data;
                                                byte[] buf1 = new byte[4 * 1024];
                                                out.writeBytes(twoHyphens + boundary + crlf);
                                                out.writeBytes("Content-Disposition: form-data; name=\""
                                                        + "file" + "\";filename=\"" + "file" + "\"" + crlf);
                                                out.writeBytes(crlf);
                                                while ((read1 = in.read(buf1)) != -1) {
                                                    out.write(buf1, 0, read1);
                                                }
                                                out.writeBytes(crlf);
                                                out.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
                                                out.flush();
                                                out.close();
                                                Log.i("CONN ==", String.valueOf(conn.getResponseCode()));
                                                switch (conn.getResponseCode()) {
                                                    case 200:
                                                        Log.d(TAG, "reponse: 200");
//                                                        InputStream is1 = conn.getInputStream();
//                                                        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
//                                                        byte[] buffer1 = new byte[1024];
//                                                        int len1;
//                                                        while ((len1 = is1.read(buffer1)) != -1) {
//                                                            baos1.write(buffer1, 0, len1);
//
//                                                        }
                                                        BufferedReader br = new BufferedReader(
                                                                new InputStreamReader(conn.getInputStream()));
                                                        StringBuilder sb = new StringBuilder();
                                                        String data1 = null;
                                                        while ((data1 = br.readLine()) != null) {
                                                            sb.append(data1);
                                                        }
                                                        jsonString = sb.toString();
                                                        Log.i("jsonString == ", jsonString);
                                                        jAry = new JSONArray(jsonString);//*
                                                        jObj = jAry.getJSONObject(0);
                                                        Log.i("jsonString === ", jObj.toString());
                                                        vedioUrl = jObj.getString("file");
                                                        track = jObj.getString("tracks");
                                                        Log.i("track = ", track);
                                                        jAryTrack = new JSONArray(track);
                                                        if (jAryTrack.optBoolean(0)) {
                                                            jObjTrack = jAryTrack.getJSONObject(0);
                                                            sub = jObjTrack.getString("file");
                                                            subUri = Uri.parse(sub);
                                                        }
                                                        Uri videoUri = Uri.parse(vedioUrl);
                                                        Intent intent1 = new Intent(Intent.ACTION_VIEW, videoUri);
                                                        intent1.setDataAndType(videoUri, "application/x-mpegURL");
                                                        intent1.setPackage("com.mxtech.videoplayer.ad");
                                                        if (subUri != null) {
                                                            intent1.putExtra("subs", new Parcelable[]{subUri});
                                                        }
                                                        intent1.putExtra("title", params[1]);
                                                        startActivity(intent1);
//                                                        baos1.close();
//                                                        is1.close();
                                                        break;
                                                    case 202:
                                                        String path4 = conn.getHeaderField("cptv-redirect");
                                                        RedirectString = path4;
                                                        URL url4 = new URL(path4);
                                                        HttpURLConnection redirCon1 = (HttpURLConnection) url4.openConnection();
                                                        try {
                                                            redirCon1.setRequestMethod("GET");
                                                            redirCon1.setReadTimeout(15000);
                                                            redirCon1.setRequestProperty("referer", "http://localhost");
                                                            Log.i("redirCon1 ==", String.valueOf(redirCon1.getResponseCode()));
                                                            Log.i("redirConString ==", redirCon1.toString());
                                                            InputStream in1 = new BufferedInputStream(redirCon1.getInputStream());//*
                                                            String crlf1 = "\r\n";
                                                            String twoHyphens1 = "--";
                                                            String boundary1 = "*****";
                                                            String path5 = " http://t3.catpunch.co/gs/" + params[0];
                                                            URL url5 = new URL(path5);
                                                            HttpURLConnection reconn = (HttpURLConnection) url5.openConnection();
                                                            reconn.setRequestProperty("Cookie", Cookies);
                                                            try {
                                                                reconn.setRequestMethod("POST");
                                                                reconn.setReadTimeout(15000);
                                                                reconn.setRequestProperty("Content-Type", "multipart/form-data;boundary="
                                                                        + boundary1);
                                                                System.out.print("test");
                                                                reconn.setDoInput(true);
                                                                reconn.setDoOutput(true);
                                                                reconn.setChunkedStreamingMode(0);
                                                                DataOutputStream reout = new DataOutputStream(reconn.getOutputStream());
                                                                int read2;
                                                                buf1 = new byte[4 * 1024];
                                                                reout.writeBytes(twoHyphens1 + boundary1 + crlf1);
                                                                reout.writeBytes("Content-Disposition: form-data; name=\""
                                                                        + "file" + "\";filename=\"" + "file" + "\"" + crlf1);
                                                                reout.writeBytes(crlf1);
                                                                while ((read2 = in1.read(buf1)) != -1) {
                                                                    reout.write(buf1, 0, read2);
                                                                }
                                                                reout.writeBytes(crlf1);
                                                                reout.writeBytes(twoHyphens1 + boundary1 + twoHyphens1 + crlf1);
                                                                reout.flush();
                                                                reout.close();
                                                                Log.i("CONN ==", String.valueOf(reconn.getResponseCode()));
                                                                switch (reconn.getResponseCode()){
                                                                    case 200:
                                                                        BufferedReader rebr = new BufferedReader(
                                                                                new InputStreamReader(reconn.getInputStream()));
                                                                        StringBuilder sb2 = new StringBuilder();
                                                                        String data2 = null;
                                                                        while ((data2 = rebr.readLine()) != null) {
                                                                            sb2.append(data2);
                                                                            Log.i("DATA = ",sb2.toString());
                                                                        }
                                                                        jsonString = sb2.toString();
                                                                        Log.i("jsonString == ", jsonString);
                                                                        jAry = new JSONArray(jsonString);//*
                                                                        jObj = jAry.getJSONObject(0);
                                                                        Log.i("jsonString === ", jObj.toString());
                                                                        vedioUrl = jObj.getString("file");
                                                                        track = jObj.getString("tracks");
                                                                        Log.i("track = ", track);
                                                                        jAryTrack = new JSONArray(track);
                                                                        if (jAryTrack.optBoolean(0)) {
                                                                            jObjTrack = jAryTrack.getJSONObject(0);
                                                                            sub = jObjTrack.getString("file");
                                                                            subUri = Uri.parse(sub);
                                                                        }
                                                                        Uri videoUri2 = Uri.parse(vedioUrl);
                                                                        Intent intent12 = new Intent(Intent.ACTION_VIEW, videoUri2);
                                                                        intent12.setDataAndType(videoUri2, "application/x-mpegURL");
                                                                        intent12.setPackage("com.mxtech.videoplayer.ad");
                                                                        if (subUri != null) {
                                                                            intent12.putExtra("subs", new Parcelable[]{subUri});
                                                                        }
                                                                        intent12.putExtra("title", params[1]);
                                                                        startActivity(intent12);
                                                                        break;
                                                                    case 202:
                                                                        String rePath = reconn.getHeaderField("cptv-redirect");
                                                                        RedirectString = rePath;
                                                                        URL reurl = new URL(rePath);
                                                                        HttpURLConnection redirCon2 = (HttpURLConnection) reurl.openConnection();
                                                                        try{
                                                                            redirCon2.setRequestMethod("GET");
                                                                            redirCon2.setReadTimeout(15000);
                                                                            redirCon2.setRequestProperty("referer", "http://localhost");
                                                                            Log.i("redirCon1 ==", String.valueOf(redirCon2.getResponseCode()));
                                                                            Log.i("redirConString ==", redirCon2.toString());
                                                                            InputStream in2 = new BufferedInputStream(redirCon2.getInputStream());//*
                                                                            String crlf2 = "\r\n";
                                                                            String twoHyphens2 = "--";
                                                                            String boundary2 = "*****";
                                                                            String path6 = " http://t3.catpunch.co/gs/" + params[0];
                                                                            URL url6 = new URL(path6);
                                                                            HttpURLConnection reconn1 = (HttpURLConnection) url6.openConnection();
                                                                            reconn1.setRequestProperty("Cookie", Cookies);
                                                                            try{
                                                                                reconn1.setRequestMethod("POST");
                                                                                reconn1.setReadTimeout(15000);
                                                                                reconn1.setRequestProperty("Content-Type", "multipart/form-data;boundary="
                                                                                        + boundary2);
                                                                                reconn1.setDoInput(true);
                                                                                reconn1.setDoOutput(true);
                                                                                reconn1.setChunkedStreamingMode(0);
                                                                                DataOutputStream reout2 = new DataOutputStream(reconn1.getOutputStream());
                                                                                int read3;
                                                                                buf1 = new byte[4 * 1024];
                                                                                reout2.writeBytes(twoHyphens2 + boundary2 + crlf2);
                                                                                reout2.writeBytes("Content-Disposition: form-data; name=\""
                                                                                        + "file" + "\";filename=\"" + "file" + "\"" + crlf2);
                                                                                reout2.writeBytes(crlf2);
                                                                                while ((read3 = in2.read(buf1)) != -1) {
                                                                                    reout2.write(buf1, 0, read3);
                                                                                }
                                                                                reout2.writeBytes(crlf2);
                                                                                reout2.writeBytes(twoHyphens2 + boundary2 + twoHyphens2 + crlf2);
                                                                                reout2.flush();
                                                                                reout2.close();
                                                                                Log.i("RECONN ==", String.valueOf(reconn1.getResponseCode()));
                                                                                switch ((reconn1.getResponseCode())){
                                                                                    case 200:
                                                                                        BufferedReader rebr2 = new BufferedReader(
                                                                                                new InputStreamReader(reconn1.getInputStream()));
                                                                                        StringBuilder sb3 = new StringBuilder();
                                                                                        String data3 = null;
                                                                                        while ((data3 = rebr2.readLine()) != null) {
                                                                                            sb3.append(data3);
                                                                                            Log.i("DATA = ",sb3.toString());
                                                                                        }
                                                                                        jsonString = sb3.toString();
                                                                                        Log.i("jsonString == ", jsonString);
                                                                                        jAry = new JSONArray(jsonString);//*
                                                                                        jObj = jAry.getJSONObject(0);
                                                                                        Log.i("jsonString === ", jObj.toString());
                                                                                        vedioUrl = jObj.getString("file");
                                                                                        track = jObj.getString("tracks");
                                                                                        Log.i("track = ", track);
                                                                                        jAryTrack = new JSONArray(track);
                                                                                        if (jAryTrack.optBoolean(0)) {
                                                                                            jObjTrack = jAryTrack.getJSONObject(0);
                                                                                            sub = jObjTrack.getString("file");
                                                                                            subUri = Uri.parse(sub);
                                                                                        }
                                                                                        Uri videoUri3 = Uri.parse(vedioUrl);
                                                                                        Intent intent13 = new Intent(Intent.ACTION_VIEW, videoUri3);
                                                                                        intent13.setDataAndType(videoUri3, "application/x-mpegURL");
                                                                                        intent13.setPackage("com.mxtech.videoplayer.ad");
                                                                                        if (subUri != null) {
                                                                                            intent13.putExtra("subs", new Parcelable[]{subUri});
                                                                                        }
                                                                                        intent13.putExtra("title", params[1]);
                                                                                        startActivity(intent13);
                                                                                }
                                                                            }catch (ActivityNotFoundException e){
                                                                                e.printStackTrace();
                                                                                mUiHandler.sendEmptyMessage(NO_INSTALL_MXPLAYER);
                                                                            }catch (Exception e){
                                                                                e.printStackTrace();
                                                                            }
                                                                        }catch (Exception e){
                                                                            e.printStackTrace();
                                                                        }
                                                                        break;
                                                                    case 500:
                                                                        LogUtils.d(TAG,"reponse 500");
                                                                        break;
                                                                }
                                                            }catch (ActivityNotFoundException e){
                                                                e.printStackTrace();
                                                                mUiHandler.sendEmptyMessage(NO_INSTALL_MXPLAYER);
                                                            }catch (Exception e){
                                                                e.printStackTrace();
                                                            }
                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                        }
                                                        break;
                                                    case 500:
                                                        LogUtils.d(TAG, "response 500");
                                                        break;
                                                }
                                            } finally {
                                                conn.disconnect();
                                            }
                                        } finally {
                                            redirCon.disconnect();
                                        }
                                        break;
                                    case 404:
                                        LogUtils.d(TAG, "response: 404");
                                        break;
                                }
                            } finally {
                                ucon.disconnect();
                            }
                            break;
                        case 404:
                            LogUtils.d(TAG, "response: 404");
                            break;
                    }
                } finally {
                    con.disconnect();
                }
            } catch (UnknownHostException e) {
                mUiHandler.sendEmptyMessage(NO_NETWORK_CONNECTION);
                e.printStackTrace();
            } catch (ActivityNotFoundException e) {
                mUiHandler.sendEmptyMessage(NO_INSTALL_MXPLAYER);
                e.printStackTrace();
            } catch (Exception e) {
                LogUtils.d(TAG, e.toString());
                e.printStackTrace();
            }
            return null;
        }



            @Override
        protected void onPostExecute(Void aVoid) {
            mProgressbar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }
    public static final int NO_INSTALL_MXPLAYER = 1000;
    public static final int NO_NETWORK_CONNECTION = 2000;

    public Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int msgId = msg.what;
            switch (msgId) {
                case NO_INSTALL_MXPLAYER:
                    Toast.makeText(mActivity, "請先安裝  MX Player 及其解碼包", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case NO_NETWORK_CONNECTION:
                    Toast.makeText(mActivity, "請確認網路是否連線", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
