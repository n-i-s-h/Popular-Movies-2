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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;

/**
 * Created by Nishith on 22-05-2016.
 */
public class TestDB extends AndroidTestCase {

    public static final String LOG_TAG = TestDB.class.getSimpleName();



    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MoviesDBHelper.DATABASE_NAME);
    }

    /*
     * This function gets called before each test is executed to delete the database.
     * This makes sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();

        /* Redirect debug messages to test console */
        OutputStream os = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(os);
        System.setOut(printStream);

        //System.out.println("********** START ********");
    }


    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MoviesContract.MoviesTable.TABLE_NAME);
        tableNameHashSet.add(MoviesContract.TrailersTable.TABLE_NAME);
        tableNameHashSet.add(MoviesContract.ReviewsTable.TABLE_NAME);


        // Delete the DB if it already exists and create a new DB
        mContext.deleteDatabase(MoviesDBHelper.DATABASE_NAME);

        SQLiteDatabase db = new MoviesDBHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        //If table name is null
        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain all the required tables
        assertTrue("Error: Your database was created without all the required  tables",
                tableNameHashSet.isEmpty());


        /***************************************************************************************/
        /* if you reached here all the tables are present. Lets check for columns in the table */
        // now, do our tables contain the correct columns?

        /******* 1. Test Movies Table *******/
        c = db.rawQuery("PRAGMA table_info(" + MoviesContract.MoviesTable.TABLE_NAME + ")",
                null);

        assertTrue("Error: Unable to query the database for Movies table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> MoviesColumnHashSet = new HashSet<String>();
        MoviesColumnHashSet.add(MoviesContract.MoviesTable._ID);
        MoviesColumnHashSet.add(MoviesContract.MoviesTable.COLUMN_MOVIE_ID);
        MoviesColumnHashSet.add(MoviesContract.MoviesTable.COLUMN_POSTER_URL);
        MoviesColumnHashSet.add(MoviesContract.MoviesTable.COLUMN_POSTER_LOCAL);
        MoviesColumnHashSet.add(MoviesContract.MoviesTable.COLUMN_RELEASE_DATE);
        MoviesColumnHashSet.add(MoviesContract.MoviesTable.COLUMN_RATING);
        MoviesColumnHashSet.add(MoviesContract.MoviesTable.COLUMN_PLOT);
        MoviesColumnHashSet.add(MoviesContract.MoviesTable.COLUMN_FAVOURITE);
        MoviesColumnHashSet.add(MoviesContract.MoviesTable.COLUMN_POPULAR);
        MoviesColumnHashSet.add(MoviesContract.MoviesTable.COLUMN_TOP_RATED);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            MoviesColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all the required columns in Movies Table!",
                MoviesColumnHashSet.isEmpty());


        /******* 2. Test Trailers Table *******/
        c = db.rawQuery("PRAGMA table_info(" + MoviesContract.TrailersTable.TABLE_NAME + ")",
                null);

        assertTrue("Error: Unable to query the database for Trailers table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> TrailersColumnHashSet = new HashSet<String>();
        TrailersColumnHashSet.add(MoviesContract.TrailersTable._ID);
        TrailersColumnHashSet.add(MoviesContract.TrailersTable.COLUMN_MOVIE_ID);
        TrailersColumnHashSet.add(MoviesContract.TrailersTable.COLUMN_TRAILER_NAME);
        TrailersColumnHashSet.add(MoviesContract.TrailersTable.COLUMN_TRAILER_URL);

        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            TrailersColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all the required columns in Trailers Table!",
                TrailersColumnHashSet.isEmpty());


        /******* 3. Test Reviews Table *******/
        c = db.rawQuery("PRAGMA table_info(" + MoviesContract.ReviewsTable.TABLE_NAME + ")",
                null);

        assertTrue("Error: Unable to query the database for Reviews table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> ReviewsColumnHashSet = new HashSet<String>();
        ReviewsColumnHashSet.add(MoviesContract.ReviewsTable._ID);
        ReviewsColumnHashSet.add(MoviesContract.ReviewsTable.COLUMN_MOVIE_ID);
        ReviewsColumnHashSet.add(MoviesContract.ReviewsTable.COLUMN_REVIEW);
        ReviewsColumnHashSet.add(MoviesContract.ReviewsTable.COLUMN_AUTHOR);

        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            ReviewsColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all the required columns in Reviews Table!",
                ReviewsColumnHashSet.isEmpty());

        db.close();


    }


    /***** Test CRUD operations - Insert, Query, Update, Delete *****/

    /*
     *  To test that we can insert and query the Movies Table in the database.
     */
    public void testMoviesTable() {

        // First step: Get reference to writable database
        MoviesDBHelper dbHelper = new MoviesDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = TestUtilities.createMovieValues(12345);

        /********* Test Insert *********/
        // Third Step: Insert ContentValues into database and get a row ID back
        long rowId;
        rowId = db.insert(MoviesContract.MoviesTable.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(rowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        /********* Test Query *********/
        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MoviesContract.MoviesTable.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from Movies Table", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Movies Table Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from query",
                cursor.moveToNext() );

        /********* Test Delete *********/
        int numRows = db.delete(
                MoviesContract.MoviesTable.TABLE_NAME,  // Table to Query
                null, // all columns
                null // Columns for the "where" clause
        );

        assertEquals("Delete operation Failed!", 1, numRows);

        // Release resource: Close Cursor and Database
        cursor.close();
        db.close();
    }


    /*
     *  To test that we can insert and query the Trailers Table in the database.
     */
    public void testTrailersTable() {

        // First step: Get reference to writable database
        MoviesDBHelper dbHelper = new MoviesDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = TestUtilities.createTrailerValues(12345);

        /********* Test Insert *********/
        // Third Step: Insert ContentValues into database and get a row ID back
        long rowId;
        rowId = db.insert(MoviesContract.TrailersTable.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(rowId != -1);

        /********* Test Query *********/
        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MoviesContract.TrailersTable.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from Trailers Table", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Trailers Table Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from Trailers query",
                cursor.moveToNext() );

        /********* Test Delete *********/
        int numRows = db.delete(
                MoviesContract.TrailersTable.TABLE_NAME,  // Table to Query
                null, // all columns
                null // Columns for the "where" clause
        );

        assertEquals("Delete operation Failed!", 1, numRows);

        // Release resource: Close Cursor and Database
        cursor.close();
        db.close();
    }


    /*
 *  To test that we can insert and query the Reviews Table in the database.
 */
    public void testReviewsTable() {
        // First step: Get reference to writable database
        MoviesDBHelper dbHelper = new MoviesDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = TestUtilities.createReviewValues(12345);

        /********* Test Insert *********/
        // Third Step: Insert ContentValues into database and get a row ID back
        long rowId;
        rowId = db.insert(MoviesContract.ReviewsTable.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(rowId != -1);

        /********* Test Query *********/
        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MoviesContract.ReviewsTable.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from Reviews Table", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Reviews Table Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from Reviews query",
                cursor.moveToNext() );

        /********* Test Delete *********/
        int numRows = db.delete(
                MoviesContract.ReviewsTable.TABLE_NAME,  // Table to Query
                "movie_id=12345", // all columns
                null // Columns for the "where" clause
        );

        assertEquals("Delete operation Failed!", 1, numRows);

        // Release resource: Close Cursor and Database
        cursor.close();
        db.close();
    }


    @Override
    protected void tearDown() throws Exception {

        //System.setOut(stdout);
        super.tearDown();
    }
}
