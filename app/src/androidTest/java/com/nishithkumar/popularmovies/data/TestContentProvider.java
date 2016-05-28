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

package com.nishithkumar.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by Nishith on 22-05-2016.
 */
public class TestContentProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestContentProvider.class.getSimpleName();
    private  MoviesDBHelper dbHelper;
    private ContentValues testMovieValues;
    private ContentValues testTrailerValues;
    private ContentValues testReviewValues;

    /*
     * This helper function deletes all records from both database tables using the database
     * functions only.
     **/
    public void deleteAllRecordsFromDB() {
        dbHelper = new MoviesDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(MoviesContract.MoviesTable.TABLE_NAME, null, null);
        db.delete(MoviesContract.TrailersTable.TABLE_NAME, null, null);
        db.delete(MoviesContract.ReviewsTable.TABLE_NAME, null, null);

    }

    /*
     * Helper function to wipe all data from teh Database
     */
    public void cleanDB() {
        deleteAllRecordsFromDB();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cleanDB();
    }


    @Override
    protected void tearDown() throws Exception {
       // SQLiteDatabase db = dbHelper.getWritableDatabase();
       // db.close();
        super.tearDown();
    }

    /************************* Start Content Provider Test *************************/

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieDataProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MovieDataProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + MoviesContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MoviesContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MovieDataProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }



    /*
     * This test doesn't touch the database.  It verifies that the ContentProvider returns
     * the correct type for each type of URI that it can handle.
     */
    public void testGetType() {
        // content://com.nishithkumar.popularmovies
        String type = mContext.getContentResolver().getType(MoviesContract.MoviesTable.CONTENT_URI);
        // content://com.nishithkumar.popularmovies/movies
        assertEquals("Error: (1) MoviesTable CONTENT_URI should return MoviesTable.CONTENT_TYPE",
                MoviesContract.MoviesTable.CONTENT_TYPE, type);

        long movieID = 12345;
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                MoviesContract.MoviesTable.buildMovieDetailsUri(movieID));
        // content://com.nishithkumar.popularmovies/movies/12345
        assertEquals("Error: (2) MoviesTable CONTENT_URI with MovieID must return MoviesTable.CONTENT_ITEM_TYPE",
                MoviesContract.MoviesTable.CONTENT_ITEM_TYPE, type);

        // content://com.nishithkumar.popularmovies/trailers
        type = mContext.getContentResolver().getType(MoviesContract.TrailersTable.CONTENT_URI);
        assertEquals("Error: (3) TrailersTable CONTENT_URI should return TrailersTable.CONTENT_URI",
                MoviesContract.TrailersTable.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(MoviesContract.ReviewsTable.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals("Error: (4) ReviewsTable CONTENT_URI should return ReviewsTable.CONTENT_TYPE",
                MoviesContract.ReviewsTable.CONTENT_TYPE, type);
    }


    /****** Test the CRUD functions for the content providers ******/

    /*
     * Test insert values and query function
     */
    public void testInsertRecords() {
        testMovieValues = TestUtilities.createMovieValues(12345);

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MoviesContract.MoviesTable.CONTENT_URI, true, tco);
        Uri movieUri = mContext.getContentResolver().insert(MoviesContract.MoviesTable.CONTENT_URI, testMovieValues);

        //Did our content observer get called?  If this fails, your insert function
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowId = ContentUris.parseId(movieUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);


    }


    // Make sure we can still delete after adding/updating stuff



    /*
     * Test query function
     */
    public void testQueryRecords() {

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.
        // A cursor is your primary interface to the query results.
      Cursor cursor = mContext.getContentResolver().query(
                MoviesContract.MoviesTable.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        /* Validate if query returned the correct values */
      TestUtilities.validateCursor("testReadProvider. Error validating MovieEntry.",
          cursor, testMovieValues);

    }


    /*
     * Test update function
     */
    public void testUpdateRecords() {

    }


    /*
     * Test delete function
     */
    public void testDeleteRecords() {

    }


    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the delete functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
//    public void testDeleteRecords() {
//        testInsertReadProvider();
//
//        // Register a content observer for our location delete.
//        TestUtilities.TestContentObserver locationObserver = TestUtilities.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(LocationEntry.CONTENT_URI, true, locationObserver);
//
//        // Register a content observer for our weather delete.
//        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(WeatherEntry.CONTENT_URI, true, weatherObserver);
//
//        deleteAllRecordsFromProvider();
//
//        // Students: If either of these fail, you most-likely are not calling the
//        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
//        // delete.  (only if the insertReadProvider is succeeding)
//        locationObserver.waitForNotificationOrFail();
//        weatherObserver.waitForNotificationOrFail();
//
//        mContext.getContentResolver().unregisterContentObserver(locationObserver);
//        mContext.getContentResolver().unregisterContentObserver(weatherObserver);
//    }

}