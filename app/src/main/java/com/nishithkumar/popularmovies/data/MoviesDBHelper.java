package com.nishithkumar.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



/**
 * Created by Nishith on 10-04-2016.
 */
public class MoviesDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "movies.db";

    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public MoviesDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /* Called to set-up the DB */
    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesContract.MoviesTable
                .TABLE_NAME + " (" +
                MoviesContract.MoviesTable._ID + " INTEGER PRIMARY KEY," +
                MoviesContract.MoviesTable.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                MoviesContract.MoviesTable.COLUMN_MOVIE_TITLE + " VARCHAR NOT NULL, " +
                MoviesContract.MoviesTable.COLUMN_POSTER_URL + " VARCHAR NOT NULL, " +
                MoviesContract.MoviesTable.COLUMN_POSTER_LOCAL + " VARCHAR NOT NULL, " +
                MoviesContract.MoviesTable.COLUMN_RELEASE_DATE + " DATE NOT NULL, " +
                MoviesContract.MoviesTable.COLUMN_RATING + " LONG NOT NULL, " +
                MoviesContract.MoviesTable.COLUMN_PLOT + " TEXT NOT NULL, " +
                MoviesContract.MoviesTable.COLUMN_FAVOURITE + " BOOLEAN NOT NULL, " +
                MoviesContract.MoviesTable.COLUMN_POPULAR + "  BOOLEAN NOT NULL, " +
                MoviesContract.MoviesTable.COLUMN_TOP_RATED + "  BOOLEAN NOT NULL " +
                " );";

        final String SQL_CREATE_TRAILERS_TABLE = "CREATE TABLE " + MoviesContract.TrailersTable
                .TABLE_NAME + " (" +
                MoviesContract.TrailersTable. _ID + " INTEGER PRIMARY KEY," +
                MoviesContract.TrailersTable.COLUMN_MOVIE_ID+ " INTEGER NOT NULL," +
                MoviesContract.TrailersTable.COLUMN_TRAILER_NAME + " VARCHAR NOT NULL, " +
                MoviesContract.TrailersTable.COLUMN_TRAILER_URL + " VARCHAR NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + MoviesContract.TrailersTable.COLUMN_MOVIE_ID + ") REFERENCES " +
                MoviesContract.MoviesTable.TABLE_NAME + " (" + MoviesContract.MoviesTable.COLUMN_MOVIE_ID + ") " +

                " );";


        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + MoviesContract.ReviewsTable
                .TABLE_NAME + " (" +
                MoviesContract.ReviewsTable._ID + " INTEGER PRIMARY KEY," +
                MoviesContract.ReviewsTable.COLUMN_MOVIE_ID+ " INTEGER NOT NULL," +
                MoviesContract.ReviewsTable.COLUMN_REVIEW + " TEXT NOT NULL, " +
                MoviesContract.ReviewsTable.COLUMN_AUTHOR + " VARCHAR NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + MoviesContract.TrailersTable.COLUMN_MOVIE_ID + ") REFERENCES " +
                MoviesContract.MoviesTable.TABLE_NAME + " (" + MoviesContract.MoviesTable.COLUMN_MOVIE_ID + ") " +

                " );";

        /* Execute the SQL statements */
        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_TRAILERS_TABLE);
        db.execSQL(SQL_CREATE_REVIEWS_TABLE);

    }

    /* Used to upgrade/modify the database.
     * Make sur eto chnage teh database version number to ensure this call succeeds
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 3 lines
        // should be your top priority before modifying this method.
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MoviesTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.TrailersTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.ReviewsTable.TABLE_NAME);
        onCreate(db);
    }

}
