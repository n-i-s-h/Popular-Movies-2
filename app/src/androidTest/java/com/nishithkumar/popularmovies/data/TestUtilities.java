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

/* NOTICE: Certain functions in this class have been adapted from
 * Udacity's Sunshine project for Android Developer Nanodegree */

package com.nishithkumar.popularmovies.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.nishithkumar.popularmovies.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by Nishith on 22-05-2016.
 */
public class TestUtilities extends AndroidTestCase {

    private static final long MOVIE_ID = 12345;

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }



    /*
     * Create some default values for your database tests.
     */
    static ContentValues createMovieValues(long movieId) {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MoviesContract.MoviesTable.COLUMN_MOVIE_ID, movieId);
        movieValues.put(MoviesContract.MoviesTable.COLUMN_MOVIE_TITLE, "This is a test");
        movieValues.put(MoviesContract.MoviesTable.COLUMN_POSTER_URL, "http://image.tmdb.org/t/p/w185/4qfXT9BtxeFuamR4F49m2mpKQI1.jpg");
        movieValues.put(MoviesContract.MoviesTable.COLUMN_POSTER_LOCAL, "http://image.tmdb.org/t/p/w185/4qfXT9BtxeFuamR4F49m2mpKQI1.jpg" );
        movieValues.put(MoviesContract.MoviesTable.COLUMN_RELEASE_DATE, "2016-05-10" );
        movieValues.put(MoviesContract.MoviesTable.COLUMN_RATING, "4.5" );
        movieValues.put(MoviesContract.MoviesTable.COLUMN_PLOT, "This is a great movie. There is Mocking Bird" );
        movieValues.put(MoviesContract.MoviesTable.COLUMN_FAVOURITE, 1);
        movieValues.put(MoviesContract.MoviesTable.COLUMN_POPULAR, 0);
        movieValues.put(MoviesContract.MoviesTable.COLUMN_TOP_RATED, 0);

        return movieValues;
    }

    /*
     * Create some default values for your database tests.
     */
    static ContentValues createTrailerValues(long movieId) {
        ContentValues trailerValues = new ContentValues();

        trailerValues.put(MoviesContract.TrailersTable.COLUMN_MOVIE_ID, movieId);
        trailerValues.put(MoviesContract.TrailersTable.COLUMN_TRAILER_NAME, "Official Trailer");
        trailerValues.put(MoviesContract.TrailersTable.COLUMN_TRAILER_URL, "Http://youtube/com/asgzcs" );

        return trailerValues;
    }


    /*
     * Create some default values for your database tests.
     */
    static ContentValues createReviewValues(long movieId) {
        ContentValues movieValues = new ContentValues();

        movieValues.put(MoviesContract.ReviewsTable.COLUMN_MOVIE_ID, movieId);
        movieValues.put(MoviesContract.ReviewsTable.COLUMN_REVIEW, "This is a sample review" );
        movieValues.put(MoviesContract.ReviewsTable.COLUMN_AUTHOR, "John Doe");

        return movieValues;
    }


    /*
     * The functions we provide inside of TestProvider use this utility class to test
     * the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
     * CTS tests.
     * Note that this only tests that the onChange function is called; it does not test that the
     * correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }


}