package com.nishithkumar.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Nishith on 10-04-2016.
 */
public class MoviesContract {

    /********* Content URIs *********
     * REFERENCE - http://www.grokkingandroid.com/android-tutorial-content-provider-basics/
     * URIs for content providers look like this:
     * content://authority/optionalPath/optionalId
     * They contain four parts: The scheme to use, an authority, an optional path and an optional id.
     * 1. The scheme for content providers is always “content”.
     * 2. Authorities have to be unique for every content provider.
     *    Android documentation recommends to use the fully qualified class name.
     * 3. The optional path, is used to distinguish the kinds of data your content provider offers.
     * 4. The optional id, if present – must be numeric. The id is used to access a single record.
     **/

    // Define a Content Authority.
    public static final String CONTENT_AUTHORITY = "com.nishithkumar.popularmovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's for the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Define valid URI strings  for the tables in the DB
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_MOVIE_DETAILS = "movies";
    public static final String PATH_TRAILERS = "trailers";
    public static final String PATH_REVIEWS = "reviews";


    /* Define an inner class for each table in the DB that contains all the column names */
    /* Inner class that defines the table contents of the movies table */
    public static final class MoviesTable implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        /* Android MIME types for Cursor objects */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_DETAILS;

        // Table name
        public static final String TABLE_NAME = "movies";

        //Define columns in the table
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_TITLE = "movie_title";
        public static final String COLUMN_POSTER_URL = "poster_url";
        public static final String COLUMN_POSTER_LOCAL = "poster_local";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_PLOT = "plot";
        public static final String COLUMN_FAVOURITE = "favourite";
        public static final String COLUMN_POPULAR = "popular";
        public static final String COLUMN_TOP_RATED = "top_rated";

        public static Uri buildMovieDetailsUri(Long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    /* Inner class that defines the table contents of the trailers table */
    public static final class TrailersTable implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();

        /* Android MIME types for Cursor objects */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;

        // Table name
        public static final String TABLE_NAME = "trailers";

        //Foreign Key
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TRAILER_NAME = "trailer_name";
        public static final String COLUMN_TRAILER_URL = "trailer_url";

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    /* Inner class that defines the table contents of the movies table */
    public static final class ReviewsTable implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();

        /* Android MIME types for Cursor objects */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        // Table name
        public static final String TABLE_NAME = "reviews";

        //Foreign Key
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_REVIEW = "review";
        public static final String COLUMN_AUTHOR = "author";

        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }



}
