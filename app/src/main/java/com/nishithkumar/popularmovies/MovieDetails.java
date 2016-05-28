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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nishithkumar.popularmovies.data.MoviesContract;
import com.nishithkumar.popularmovies.data.MoviesDBHelper;
import com.squareup.picasso.Picasso;

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
import java.util.PriorityQueue;

public class MovieDetails extends AppCompatActivity {
    private Context mContext;
    private String title;
    private String poster;
    private String plot;
    private String releaseDate ;
    private String voteAverage;
    private int movieID;
    private static int MAX_LIMIT = 20;

    /* Trailers and reviews */
    static final int TRAILERS = 0;
    static final int REVIEWS = 1;
    ArrayAdapter<String> trailerItemsAdapter;
    ArrayAdapter<String> reviewItemsAdapter;
    ArrayList youtubeKeys = new ArrayList<String>();
    ArrayList trailerNames = new ArrayList<String>();
    ArrayList reviews = new ArrayList<String>();
    ToggleButton favButton;

    private static String TAG = "Popular Movies";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_movie_details);

        mContext = getApplicationContext();

        //Intent which started this activity
        Intent detailsIntent = getIntent();

        title = detailsIntent.getStringExtra("movie_title");
        poster = detailsIntent.getStringExtra("movie_poster");
        plot = detailsIntent.getStringExtra("movie_plot");
        releaseDate = detailsIntent.getStringExtra("movie_release_date");
        voteAverage = detailsIntent.getStringExtra("movie_vote_average");
        movieID = detailsIntent.getIntExtra("movie_id", 0);


        Log.d(TAG, " title: " + title);
        Log.d(TAG, " poster: " + poster);
        Log.d(TAG, " plot: " + plot);
        Log.d(TAG, " releaseDate: " + releaseDate);
        Log.d(TAG, " movieID: " + movieID);


        /* Populate the view */
        TextView titleText = (TextView) findViewById(R.id.title);
        TextView plotText = (TextView) findViewById(R.id.plot);
        TextView releaseDateText = (TextView) findViewById(R.id.releaseDate);
        TextView voteAvgText = (TextView) findViewById(R.id.voteAverage);
        ImageView posterView = (ImageView) findViewById(R.id.poster);

        titleText.setText(title);
        plotText.setText(plot);
        releaseDateText.setText(releaseDate);
        voteAvgText.setText(voteAverage + "/10");
        Picasso.with(this).load(poster).into(posterView);


        /* Set list view adapter */
       trailerItemsAdapter =
                new ArrayAdapter<String>(this,R.layout.layout_trailer_list_item,trailerNames);
        ListView listView = (ListView) findViewById(R.id.trailers_listView);
        listView.setAdapter(trailerItemsAdapter);


        /* Set list view adapter */
        reviewItemsAdapter =
                new ArrayAdapter<String>(this,R.layout.layout_trailer_list_item,reviews);
        ListView reviewListView = (ListView) findViewById(R.id.reviews_listView);
        reviewListView.setAdapter(reviewItemsAdapter);


        /* Get the trailer & reviews  */
        //Start background task to download Movie list
        FetchTrailerTask fetchTrailersTask = new FetchTrailerTask();
        fetchTrailersTask.execute(movieID,TRAILERS);

        FetchTrailerTask fetchReviewsTask = new FetchTrailerTask();
        fetchReviewsTask.execute(movieID, REVIEWS);


        /* set item click listeners */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(getApplication(),"Opening Trailer",Toast.LENGTH_SHORT).show();

                String trailerURL = (String)youtubeKeys.get(position);
                //trailerURL = "http://www.google.com";
                Intent browserIntent =  new Intent(Intent.ACTION_VIEW, Uri.parse(trailerURL));
                startActivity(browserIntent);
            }

        });


        /* set-up the favourite button */
        favButton = (ToggleButton) findViewById(R.id.favouriteButton);
        //favButton.setText("Favourite");

        MoviesDBHelper dbHelper = new MoviesDBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String columns[] = {MoviesContract.MoviesTable.COLUMN_MOVIE_ID} ;
        String whereColumn = MoviesContract.MoviesTable.COLUMN_MOVIE_ID ;
        String[] selectionArgs = { Integer.toString(movieID)};



        Cursor cursor = db.query(
                MoviesContract.MoviesTable.TABLE_NAME,  // Table to Query
                columns, // all columns
                whereColumn + "=?", // Columns for the "where" clause
                selectionArgs, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );


        if(cursor.getCount() == 1 ){
            //Already added to favourites
            favButton.setChecked(true);
        }else{
            //Not added to favourites
            favButton.setChecked(false);
        }

        //Close DB connection to avoid leaks
        cursor.close();
        dbHelper.close();

        favButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // First step: Get reference to writable database
                MoviesDBHelper dbHelper = new MoviesDBHelper(mContext);
                SQLiteDatabase db = dbHelper.getWritableDatabase();


                if (isChecked) {
                    // The toggle is enabled

                    //Add oly upto MAX_LIMIT
                    String columns[] = {MoviesContract.MoviesTable.COLUMN_MOVIE_ID} ;

                    Cursor cursor = db.query(
                            MoviesContract.MoviesTable.TABLE_NAME,  // Table to Query
                            columns, // all columns
                            null + "=?", // Columns for the "where" clause
                            null, // Values for the "where" clause
                            null, // columns to group by
                            null, // columns to filter by row groups
                            null // sort order
                    );


                    if(cursor.getCount() <= MAX_LIMIT) {

                        /* check if already present */

                        Toast.makeText(getApplicationContext(), "Added to Favourites", Toast.LENGTH_SHORT).show();
                        // Second Step: Create ContentValues of what you want to insert
                        ContentValues testValues = getMovieValues();
                        db.insert(MoviesContract.MoviesTable.TABLE_NAME, null, testValues);
                        // Insert Trailers
                        // Insert Reviews
                    }else{
                        Toast.makeText(getApplicationContext(), "Favourites Max Limit reached!", Toast.LENGTH_LONG).show();
                    }


                } else {
                    // The toggle is disabled
                    // Delete the favourite
                    String whereClause = MoviesContract.MoviesTable.COLUMN_MOVIE_ID;
                    String whereArgs[]={Integer.toString(movieID)};
                    Toast.makeText(getApplicationContext(),"Removed from Favourites",Toast.LENGTH_SHORT).show();
                    db.delete(
                            MoviesContract.MoviesTable.TABLE_NAME,  // Table to Query
                            whereClause+"=?", // all columns
                            whereArgs // Columns for the "where" clause
                    );

                }

                //Close DB connection to avoid leaks
                dbHelper.close();
            }
        });
    }


    /* Helper function to populate row values into Movies Table */
     ContentValues getMovieValues() {
        ContentValues movieValues = new ContentValues();

         movieValues.put(MoviesContract.MoviesTable.COLUMN_MOVIE_ID, movieID);
         movieValues.put(MoviesContract.MoviesTable.COLUMN_MOVIE_TITLE, title);
         movieValues.put(MoviesContract.MoviesTable.COLUMN_POSTER_URL,poster);
         movieValues.put(MoviesContract.MoviesTable.COLUMN_POSTER_LOCAL, poster);
         movieValues.put(MoviesContract.MoviesTable.COLUMN_RELEASE_DATE, releaseDate);
         movieValues.put(MoviesContract.MoviesTable.COLUMN_RATING, voteAverage);
         movieValues.put(MoviesContract.MoviesTable.COLUMN_PLOT, plot );
         movieValues.put(MoviesContract.MoviesTable.COLUMN_FAVOURITE, 1);
         movieValues.put(MoviesContract.MoviesTable.COLUMN_POPULAR, 0);
         movieValues.put(MoviesContract.MoviesTable.COLUMN_TOP_RATED, 0);

        return movieValues;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /* if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }



    /* Custom class to run Background task */
    class FetchTrailerTask extends AsyncTask<Integer,Void,Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        private boolean processDataFromJson(String moviesJsonStr)
                throws JSONException {

            final String BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String YOU_TUBE_URL = "https://www.youtube.com/watch?v=";
             JSONArray resultsArray ;

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            resultsArray = moviesJson.getJSONArray("results");

            try {
            /* Get poster-path for each entry in results */
                for (int i = 0; i < resultsArray.length(); i++) {
                    // Get the JSON object representing the movie
                    JSONObject trailerDetails = resultsArray.getJSONObject(i);

                    String trailerKey = trailerDetails.getString("key");
                    Log.d(TAG," Movie Trailer Key "+i+ " = " + trailerKey);

                    String trailerName = trailerDetails.getString("name");
                    Log.d(TAG,"  Trailer Name "+i+ " = " + trailerName);

                    //save in result array
                    if (trailerKey != null) {
                        youtubeKeys.add(YOU_TUBE_URL  + trailerKey);
                        trailerNames.add(trailerName);
                        //Log.d(TAG, "Trailer URL " = " + YOU_TUBE_URL+trailerKey);
                    }

                }
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }

            return true;
        }


        /* Helper function to extract review data from JSON */
        private boolean processReviewsFromJson(String jsonStr)
                throws JSONException {

            JSONArray resultsArray ;

            JSONObject moviesJson = new JSONObject(jsonStr);
            resultsArray = moviesJson.getJSONArray("results");

            try {
            /* Get poster-path for each entry in results */
                for (int i = 0; i < resultsArray.length(); i++) {
                    // Get the JSON object representing the movie
                    JSONObject reviewDetails = resultsArray.getJSONObject(i);

                    String reviewContent = reviewDetails.getString("content");
                    Log.d(TAG," Review Content "+ i + " = " + reviewContent);

                    String reviewAuthor = reviewDetails.getString("author");
                    Log.d(TAG," Review Author "+ i + " = " + reviewAuthor);

                    //save in result array
                    if (reviewContent != null) {
                        reviewContent = reviewContent + "\n - " + reviewAuthor;
                        reviews.add(reviewContent);
                        //Log.d(TAG, "Trailer URL " = " + YOU_TUBE_URL+trailerKey);
                    }

                }
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            int movieID = params[0];
            int downloadType = params[1];
            String jsonStr ;
            String reviewsJsonStr ;

            Log.e(TAG," ***** DownloadType = "+ downloadType + " -- movieID=" + movieID + " ***** ");


            try {
                // Construct the URL for the query

                final String API_KEY = "<---YOUR-API-KEY--->";
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" ;

                final String TRAILER_URL = BASE_URL + movieID + "/videos?api_key=" + API_KEY;
                final String REVIEWS_URL = BASE_URL + movieID + "/reviews?api_key=" + API_KEY;

                Uri builtUri = null;



                /* STEP-1: Get trailers */
                if(downloadType == TRAILERS)
                    builtUri = Uri.parse(TRAILER_URL).buildUpon().build();
                else
                    builtUri = Uri.parse(REVIEWS_URL).buildUpon().build();
                
                URL url = new URL(builtUri.toString());

                // Create the request to TheMovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    jsonStr = null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    Log.e(TAG,"Response Stream empty!");
                    return null;
                }

                //Success : print the received string
                jsonStr = buffer.toString();
                //Log.d(TAG, "Jason String : " + moviesJsonStr);



            }catch (IOException e) {
                Log.e(TAG, "Error downloading data ", e);
                // If the code didn't successfully get the data, there's no point in attempting
                // to parse it.
                jsonStr = null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }



            /* Extract required data from JSON response */
            try {
                if(downloadType == TRAILERS)
                    processDataFromJson(jsonStr);
                else
                    processReviewsFromJson(jsonStr);
            }catch(Exception e){
                Log.e(TAG, " processDataFromJson Error ", e);
                return false;
            }

            return true;

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            /* For debug only */
            /*
            for (int i=0; i<youtubeKeys.size() ; i++){
                Log.e(TAG, " Trialer path: " + youtubeKeys.get(i) );
            }

            for (int i=0; i<reviews.size() ; i++){
                Log.e(TAG, " Review : " + reviews.get(i) );
            }
            */


            trailerItemsAdapter.notifyDataSetChanged();
            reviewItemsAdapter.notifyDataSetChanged();

        }


    }


}
