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
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.GridView;
import android.widget.Toast;

import com.nishithkumar.popularmovies.data.MoviesContract;
import com.nishithkumar.popularmovies.data.MoviesDBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "Popular Movies";
    public String[] moviePoster;
    private int[] movieID;
    private ImageAdapter mImageAdapter;
    private GridView gridView;
    private String moviesJsonStr ;
    private static int MAX_RESULTS = 20;
    private final static String FAVOURITES = "FAVOURITES";
    private final static String TOP_RATED = "TOP_RATED";
    private final static String POPULAR = "POPULAR";
    private JSONArray resultsArray ;
    private String lastSelection ;
    private boolean isFavourite;
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        moviePoster = new String[MAX_RESULTS];
        movieID = new int[MAX_RESULTS];
        lastSelection = "TOP_RATED";
        isFavourite = false;

        Log.d(TAG, "onCreate! ");

        /* If saved last selection populate it */
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        lastSelection  = sharedPref.getString("SELECTION","TOP_RATED");

        gridView = (GridView) findViewById(R.id.gridview);
        mImageAdapter = new ImageAdapter(this, moviePoster);
        gridView.setAdapter(mImageAdapter);

        if(savedInstanceState != null){
            //get your data saved and populate the adapter here
            moviePoster = savedInstanceState.getStringArray("MOVIES");
            mImageAdapter.updateGrid(moviePoster);
            mImageAdapter.notifyDataSetChanged();
            Log.d(TAG, "Restore savedInstanceState : Retrieve Movie List!");
        }else{

            Log.d(TAG, "savedInstanceState is Null!");

            if(lastSelection.compareTo(FAVOURITES) == 0){
                //Display favourites
                getFavourites();
            }else {
                //Start background task to download Movie list
                FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
                fetchMoviesTask.execute(lastSelection);
            }
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)  {
                JSONObject movieDetails=null;
                String title = null ;
                String poster = null ;
                String plot = null ;
                String releaseDate = null ;
                String voteAverage = null ;
                int movieIDVal = 0;

                if(isFavourite){
                    Toast.makeText(getApplicationContext()," Details" ,Toast.LENGTH_SHORT).show();

                    // First step: Get reference to writable database
                    MoviesDBHelper dbHelper = new MoviesDBHelper(getApplicationContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    String whereColumn = MoviesContract.MoviesTable.COLUMN_MOVIE_ID ;
                    String[] selectionArgs = { Long.toString(movieID[position])};


                    Cursor cursor = db.query(
                            MoviesContract.MoviesTable.TABLE_NAME,  // Table to Query
                            null, // all columns
                            whereColumn + "=?", // Columns for the "where" clause
                            selectionArgs, // Values for the "where" clause
                            null, // columns to group by
                            null, // columns to filter by row groups
                            null // sort order
                    );



                    //Log.d(TAG, "Num roes returned : " +cursor.getCount() );
                    cursor.moveToFirst();
                    int index = cursor.getColumnIndex(MoviesContract.MoviesTable.COLUMN_MOVIE_TITLE);
                    title = cursor.getString(index);

                    index = cursor.getColumnIndex(MoviesContract.MoviesTable.COLUMN_POSTER_LOCAL);
                    poster = cursor.getString(index);

                    index = cursor.getColumnIndex(MoviesContract.MoviesTable.COLUMN_PLOT);
                    plot = cursor.getString(index);

                    index = cursor.getColumnIndex(MoviesContract.MoviesTable.COLUMN_RELEASE_DATE);
                    releaseDate =  cursor.getString(index);

                    index = cursor.getColumnIndex(MoviesContract.MoviesTable.COLUMN_RATING);
                    voteAverage = Long.toString(cursor.getLong(index));

                    index = cursor.getColumnIndex(MoviesContract.MoviesTable.COLUMN_MOVIE_ID);
                    movieIDVal = cursor.getInt(index);



                    //Close DB connection to avoid leaks
                    cursor.close();
                    dbHelper.close();

                }else {

                    try {
                        movieDetails = resultsArray.getJSONObject(position);
                        title = movieDetails.getString("title");
                        poster = moviePoster[position];
                        plot = movieDetails.getString("overview");
                        releaseDate = movieDetails.getString("release_date");
                        voteAverage = movieDetails.getString("vote_average");
                        movieIDVal = movieDetails.getInt("id");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {

                    }

                }//end else

                /* Start new activity */
                if (movieIDVal != 0) {
                    Intent detailsIntent = new Intent(getApplicationContext(), MovieDetails.class);
                    detailsIntent.putExtra("movie_title", title);
                    detailsIntent.putExtra("movie_poster", poster);
                    detailsIntent.putExtra("movie_plot", plot);
                    detailsIntent.putExtra("movie_release_date", releaseDate);
                    detailsIntent.putExtra("movie_vote_average", voteAverage);
                    detailsIntent.putExtra("movie_id", movieIDVal);
                    startActivity(detailsIntent);
                }

            } //end function
        });

        /* end onCreate() */
    }


protected void onSaveInstanceState(Bundle outState) {
        //outState.putParcelableArrayList("MOVIES", moviePoster);
        outState.putStringArray("MOVIES", moviePoster);
        Log.d(TAG, "onSaveInstanceState : Saved movies data! ");
        super.onSaveInstanceState(outState);
        }

@Override
protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart!");
        }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause! ");
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("SELECTION", lastSelection);
        editor.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume! ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop! ");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_popularity){
            isFavourite = false;
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
            fetchMoviesTask.execute("POPULAR");
            return true;
        }else if(id == R.id.action_top_rated) {
            isFavourite = false;
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
            fetchMoviesTask.execute("TOP_RATED");
            return true;
        }else if(id == R.id.action_favourites) {
            isFavourite = true;
            Toast.makeText(MainActivity.this, "Favourites", Toast.LENGTH_SHORT).show();
            getFavourites();
            return true;
    }

    return super.onOptionsItemSelected(item);
    }


    /* Helper function to get favourites from database */
    private void getFavourites(){

        String[] columns = {MoviesContract.MoviesTable.COLUMN_MOVIE_ID,
                            MoviesContract.MoviesTable.COLUMN_POSTER_LOCAL};
        MoviesDBHelper dbHelper = new MoviesDBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        /* Save state to shared prefernces */
        lastSelection = FAVOURITES;

        Cursor cursor = db.query(
                MoviesContract.MoviesTable.TABLE_NAME,  // Table to Query
                columns, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );


        /* Clear movies list and update favourites */
        for (int i=0; i< moviePoster.length; i++)
        {
            moviePoster[i] = "c:/test/path-to-file";
        }


        cursor.moveToFirst();

        int indexMovieID = cursor.getColumnIndex(MoviesContract.MoviesTable.COLUMN_MOVIE_ID);
        int indexPoster = cursor.getColumnIndex(MoviesContract.MoviesTable.COLUMN_POSTER_LOCAL);

        Log.e(TAG, "**********************************************************************");

        int i=0;
        while (cursor.isAfterLast() == false) {
            Log.e(TAG, " Cursor:indexMovieID=" + indexMovieID + "   indexPoster="+indexPoster);
            movieID[i] = cursor.getInt(indexMovieID);
            moviePoster[i] = cursor.getString(indexPoster);
            i++;
            cursor.moveToNext();
        }

        cursor.close();


        /* Update UI */
        mImageAdapter.updateGrid(moviePoster);
        mImageAdapter.notifyDataSetChanged();

    }

  /* Custom class to run Background task */
    class FetchMoviesTask extends AsyncTask<String,Void,Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        private boolean processDataFromJson(String moviesJsonStr)
                throws JSONException {

            final String BASE_URL = "http://image.tmdb.org/t/p/";
            final String IMAGE_SIZE = "w185";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            resultsArray = moviesJson.getJSONArray("results");

            try {
            /* Get poster-path for each entry in results */
                for (int i = 0; i < resultsArray.length(); i++) {
                    // Get the JSON object representing the movie
                    JSONObject movieDetails = resultsArray.getJSONObject(i);

                    String rel_poster_path = movieDetails.getString("poster_path");
                    //Log.d(TAG,"Rel poster "+i+ " = " + rel_poster_path);


                    //save in result array
                    if (rel_poster_path != null) {
                        moviePoster[i] = BASE_URL + IMAGE_SIZE + rel_poster_path;
                        Log.d(TAG, "Full Poster path " + i + " = " +  moviePoster[i]);
                    }

                }
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }

            return true;
        }


        @Override
        protected Boolean doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String download_type = params[0];

            Log.e(TAG," ***** Download type: "+ download_type + " ***** ");

            try {
                // Construct the URL for the query

                final String API_KEY = "<---YOUR-API-KEY--->";
                final String TOP_RATED_MOVIES_URL = "http://api.themoviedb.org/3/movie/top_rated?api_key=" + API_KEY;
                final String POPULAR_MOVIES_URL =  "http://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY;
                Uri builtUri = null;

                if(download_type.compareToIgnoreCase("TOP_RATED")==0 ){
                    builtUri = Uri.parse(TOP_RATED_MOVIES_URL).buildUpon().build();
                    lastSelection = "TOP_RATED";
                }else if(download_type.compareToIgnoreCase("POPULAR")==0 ){
                    builtUri = Uri.parse(POPULAR_MOVIES_URL).buildUpon().build();
                    lastSelection = "POPULAR";
                }else{
                    builtUri = Uri.parse(POPULAR_MOVIES_URL).buildUpon().build();
                    lastSelection = "POPULAR";
                }

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
                    moviesJsonStr = null;
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
                moviesJsonStr = buffer.toString();
                //Log.d(TAG, "Jason String : " + moviesJsonStr);


            }catch (IOException e) {
                Log.e(TAG, "Error downloading data ", e);
                // If the code didn't successfully get the data, there's no point in attempting
                // to parse it.
                moviesJsonStr = null;
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

            try {
                processDataFromJson(moviesJsonStr);
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

            mImageAdapter.updateGrid(moviePoster);
            mImageAdapter.notifyDataSetChanged();
      }


    }


}
