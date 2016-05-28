package com.nishithkumar.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.net.URI;

/**
 * Created by Nishith on 15-05-2016.
 */

/*
 * REFERENCE: http://www.grokkingandroid.com/android-tutorial-writing-your-own-content-provider/
 * Implementing a content provider involves always the following steps:
 *
 * Create a class that extends ContentProvider
 *   Create a contract class
 *   Create the UriMatcher definition
 *   Implement the onCreate() method
 *   Implement the getType() method
 *   Implement the CRUD methods
 *   Add the content provider to your AndroidManifest.xml
 * */

public class MovieDataProvider  extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDBHelper mDBHelper;
    private static String TAG = "Popular Movies";

    /* Constants for the various URIs */
    static final int MOVIES_URI = 10;
    static final int MOVIE_DETAILS_URI = 11;
    static final int TRAILERS_URI = 20;
    static final int REVIEWS_URI = 30;


    /* URIMatcher - Utility class to aid in matching URIs in content providers.
     *
     * NO_MATCH   - Creates the root node of the URI tree (Constant Value: -1)
     * "path"     - match exact path.
     * "path/#"   - match path followed by a number
     * "path/*"   - match path followed by any string
     **/
    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // WeatherContract to help define the types to the UriMatcher.
        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES_URI);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE_DETAILS + "/#", MOVIE_DETAILS_URI);
        matcher.addURI(authority, MoviesContract.PATH_TRAILERS, TRAILERS_URI);
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS, REVIEWS_URI);

        // 3) Return the new matcher!
        return matcher;
    }



    /*
     * REFERENCE: http://www.grokkingandroid.com/android-tutorial-writing-your-own-content-provider/
     * The abstract methods to implement
     *  onCreate() 	    Prepares the content provider
     *  getType() 	    Returns the MIME type for this URI
     *  query() 	    Return records based on selection criteria
     *  insert() 	    Adds records
     *  update() 	    Modifies data
     *  delete() 	    Deletes records
     **/

    @Override
    public boolean onCreate() {
        mDBHelper = new MoviesDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES_URI:
                return MoviesContract.MoviesTable.CONTENT_TYPE;
            case MOVIE_DETAILS_URI:
                return MoviesContract.MoviesTable.CONTENT_ITEM_TYPE;
            case TRAILERS_URI:
                return MoviesContract.TrailersTable.CONTENT_TYPE;
            case REVIEWS_URI:
                return MoviesContract.ReviewsTable.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    /*
     * Cursor query (Uri uri,
     *           String[] projection,       -> Which columns to return.
     *           String selection,          -> WHERE clause.
     *           String[] selectionArgs,    -> WHERE clause value substitution.
     *           String sortOrder)          ->  // Sort order.
     *
     * query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
     * query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
     *
     * */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        //Given a URI, determine what kind of request it is, and query the database accordingly.
        Cursor retCursor ;
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String tableName;
        final String LIMIT_ONE = "1";


        switch (sUriMatcher.match(uri)) {
            case MOVIES_URI:
            {
                tableName = MoviesContract.MoviesTable.TABLE_NAME;
                retCursor = db.query(tableName,projection,selection,selectionArgs,null,null,sortOrder);
                //retCursor = db.rawQuery("SELECT * FROM " + tableName, null);
                break;
            }
            case MOVIE_DETAILS_URI: {
                tableName = MoviesContract.MoviesTable.TABLE_NAME;
                retCursor = db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder, LIMIT_ONE);
                break;
            }
            case TRAILERS_URI: {
                tableName = MoviesContract.TrailersTable.TABLE_NAME;
                retCursor = db.query(tableName,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            }
            case REVIEWS_URI: {
                tableName = MoviesContract.ReviewsTable.TABLE_NAME;
                retCursor = db.query(tableName,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String tableName;
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case MOVIES_URI: {
                tableName = MoviesContract.MoviesTable.TABLE_NAME;
                Cursor retCursor = db.rawQuery("INSERT into " + tableName + " VALUES (1,12345,'First','Second','Third','fourth','fifth',0,0,0)", null);
                long _id = db.insert(tableName, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.MoviesTable.buildMovieDetailsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MOVIE_DETAILS_URI: {
                tableName = MoviesContract.MoviesTable.TABLE_NAME;
                long _id = db.insert(tableName, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.MoviesTable.buildMovieDetailsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILERS_URI: {
                tableName = MoviesContract.TrailersTable.TABLE_NAME;
                long _id = db.insert(tableName, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.TrailersTable.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS_URI: {
                tableName = MoviesContract.ReviewsTable.TABLE_NAME;
                long _id = db.insert(tableName, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.ReviewsTable.buildReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String tableName;
        int numRows;

        if(selection == null) {
            Log.d(TAG,"selection cannot be null for delete operation!");
            return 0;
        }

        switch (sUriMatcher.match(uri)) {
            case MOVIES_URI: {
                System.out.println("Invalid Delete Request - " + uri);
                numRows = 0;
                break;
            }
            case MOVIE_DETAILS_URI: {
                tableName = MoviesContract.MoviesTable.TABLE_NAME;
                numRows  = db.delete(tableName,selection,selectionArgs);
                break;
            }
            case TRAILERS_URI: {
                tableName = MoviesContract.TrailersTable.TABLE_NAME;
                numRows  = db.delete(tableName,selection,selectionArgs);
                break;
            }
            case REVIEWS_URI: {
                tableName = MoviesContract.ReviewsTable.TABLE_NAME;
                numRows  = db.delete(tableName,selection,selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return numRows;
    }



    /* Helper Methods to query the database */



}
