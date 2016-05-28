/*
 *  Copyright 2016 Nishith Kumar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.nishithkumar.popularmovies;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Nishith on 25-03-2016.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> mMovieIds;
    private static String TAG = "Popular Movies";

    // Constructor
    public ImageAdapter(Context c, String[] movies) {


        this.mContext = c;
        this.mMovieIds = new ArrayList<String>();

    }

    public void clearGrid() {
        mMovieIds.clear();
    }


    public void updateGrid( String[] movies){
        mMovieIds.clear();

        for (int i =0; i <movies.length ; i++ )
            this.mMovieIds.add(movies[i]);
    }

    public int getCount() {
        return this.mMovieIds.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(185*2, 278*2));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);
        }
        else
        {
            imageView = (ImageView) convertView;
        }

        //Check if it is a valid  URL
        // Also add placeholder image and error image
        /*
        Picasso.with(context)
            .load(url)
            .placeholder(R.drawable.user_placeholder)
            .error(R.drawable.user_placeholder_error)
            .into(imageView);
         */

        try {
            boolean isValid = URLUtil.isValidUrl(mMovieIds.get(position));
            if(isValid)
                Picasso.with(this.mContext).load(this.mMovieIds.get(position)).into(imageView);
            else
                Log.d(TAG, "Invalid URL! - " + mMovieIds.get(position));

            Picasso.with(this.mContext).load(this.mMovieIds.get(position)).into(imageView);
            //imageView.setImageResource(mMovieIds[position]);
        }catch(Exception e){
            e.printStackTrace();
        }

        return imageView;
    }


}
