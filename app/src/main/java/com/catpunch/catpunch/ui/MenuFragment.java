package com.catpunch.catpunch.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.catpunch.catpunch.R;
import com.catpunch.catpunch.util.LogUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by vic on 2015/8/19.
 */
public class MenuFragment extends Fragment {
    private static final String TAG = MenuFragment.class.getSimpleName();

    private Activity mActivity;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ProgressBar mProgressbar;

    private JSONArray mMenuJsonArray;

    public String path = "http://t3.catpunch.co/menu.json";
    public String mSeries;
    public String mDesc;
    public String mThumbnail;

    private String defaultThumbnail = "http://forum.catpunch.co/user_avatar/forum.catpunch.co/catpunch/120/10_1.png";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        mProgressbar = (ProgressBar) view.findViewById(R.id.progressbar);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        new GetMenuJsonTask().execute();
        return view;
    }

    public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_menu, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            try {
                final JSONObject jsonObject = mMenuJsonArray.getJSONObject(position);
                holder.mTitle.setText(jsonObject.getString("series"));
                try {
                    if (jsonObject.has("description")) {
                        holder.mDesc.setText(jsonObject.getString("description"));//*
                    }else{holder.mDesc.setText(null);}
                    if (jsonObject.has("thumbnail")) {
                        String thumbnail = jsonObject.getString("thumbnail");//*
                        if (thumbnail != null && !thumbnail.equals("")) {
                            Picasso.with(mActivity).load(thumbnail).into(holder.mThumbnail);
                        } else {
                            Picasso.with(mActivity).load(defaultThumbnail).into(holder.mThumbnail);
                        }
                    }else{String thumbnail = null;}
                } catch (Exception e) {
                    e.printStackTrace();
                }
                holder.mCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            JSONObject jObj = mMenuJsonArray.getJSONObject(position);
                            mSeries = jObj.getString("series");
//                            mDesc = jObj.getString("description");
//                            System.out.print("mdesc:" + mDesc);
//                            mThumbnail = jObj.getString("thumbnail");
//                            System.out.print("mthumbail :" + mThumbnail);
//                            if (mThumbnail.equals("")) {
//                                mThumbnail = defaultThumbnail;
//                            }
                            new GetVedioListTask().execute(jsonObject.getString("hash"), null, null);
                        } catch (JSONException e) {
                            // LogUtils.d(TAG, e, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
//                LogUtils.d(TAG, e, e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            int count = 0;
            if (mMenuJsonArray != null) {
                count = mMenuJsonArray.length();
            }
            return count;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CardView mCardView;
            public ImageView mThumbnail;
            public TextView mTitle;
            public TextView mDesc;

            public ViewHolder(View v) {
                super(v);
                mCardView = (CardView) v.findViewById(R.id.card_view);
                mThumbnail = (ImageView) v.findViewById(R.id.thumbnail);
                mTitle = (TextView) v.findViewById(R.id.title);
                mDesc = (TextView) v.findViewById(R.id.description);
            }
        }
    }

    public class GetMenuJsonTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            mProgressbar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(path);
                try {
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    try {
                        con.setRequestMethod("GET");
                        con.setReadTimeout(15000);
                        switch (con.getResponseCode()) {
                            case 200:
                                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                                StringBuilder sb = new StringBuilder();
                                String data = null;
                                while ((data = br.readLine()) != null) {
                                    sb.append(data);
                                }
                                String jsonString = sb.toString();
                                try {
                                    mMenuJsonArray = new JSONArray(jsonString);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    //LogUtils.d(TAG, e, e.getMessage());
                                }
                                break;
                        }
                    } finally {
                        con.disconnect();
                    }
                } catch (IOException e) {
                    LogUtils.d(TAG, e, e.getMessage());
                }
            } catch (MalformedURLException e) {
                LogUtils.d(TAG, e, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter = new MenuAdapter();
            mRecyclerView.setAdapter(mAdapter);
            mProgressbar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }

    public class GetVedioListTask extends AsyncTask<String, Void, Void> {
        String jsonString;

        @Override
        protected Void doInBackground(String... params) {
            String path = "http://t3.catpunch.co/menu/" + params[0];
            try {
                URL url = new URL(path);
                try {
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    try {
                        con.setRequestMethod("GET");
                        con.setReadTimeout(15000);
                        switch (con.getResponseCode()) {
                            case 200:
                                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                                StringBuilder sb = new StringBuilder();
                                String data = null;
                                while ((data = br.readLine()) != null) {
                                    sb.append(data);
                                }
                                jsonString = sb.toString();

                                break;
                        }
                    } finally {
                        con.disconnect();
                    }
                } catch (IOException e) {
                    LogUtils.d(TAG, e, e.getMessage());
                }
            } catch (MalformedURLException e) {
                LogUtils.d(TAG, e, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Bundle args = new Bundle();
            args.putString("series", mSeries);
            Log.i("Menu's series = ", mSeries);
            args.putString("description", mDesc);
            args.putString("thumbnail", mThumbnail);
            args.putString("jsonString", jsonString);
            Log.i("Menu's jsonString =", jsonString);
            Fragment fragment = new VedioListFragment();
            fragment.setArguments(args);
            mActivity.getFragmentManager().beginTransaction()
                    .replace(R.id.main_content_container, fragment)
                    .addToBackStack(null)
                    .commit();
            super.onPostExecute(aVoid);
        }
    }
}
