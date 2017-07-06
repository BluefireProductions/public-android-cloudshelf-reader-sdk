package com.bluefire.sampleapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class SampleApplicationDatabase extends SQLiteOpenHelper {
	private static final String TAG = "CloudShelfDatabase";
	
	public static SampleApplicationDatabase getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new SampleApplicationDatabase(context.getApplicationContext());
		}
		return mInstance;
	}
	
    protected SampleApplicationDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        synchronized(WRITE_LOCK) {
            db.execSQL(SQL_CREATE_SEARCH_RESULT);
        }
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	if(oldVersion < DATABASE_VERSION && newVersion == DATABASE_VERSION) {
            synchronized(WRITE_LOCK) {
                db.execSQL(SQL_DELETE_SEARCH_RESULT);
                onCreate(db);
            }
    	}
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    // Search

    /**
     * Internal method for the shared code used in every insert search result case
     * @param db
     * @param searchResult
     */
    private static SearchResult handleInsert(SQLiteDatabase db, SearchResult searchResult) {
		long recordId = -1l;

    	try {
    		recordId = db.insert(SearchResult.TABLE_NAME, null, searchResult.getUpdateOrInsertValues());

    	} catch (SQLException e){
    		Log.e(TAG, "Failed to insert for some other reason... ",e);
    	}
    	if(recordId > -1l)
    		searchResult.setSearchTableId(recordId);

    	return searchResult;
    }

    /**
     * Insert/Replace a list of search results in our database. In general, we will only be adding search
     * results in bulk, and we won't be updating them, so this should be the only insert method needed.
     * @param context
     * @param searchResults
     * @return
     */
    public static List<SearchResult> insertSearchResults(Context context, List<SearchResult> searchResults) {
        synchronized (WRITE_LOCK) {
            SampleApplicationDatabase dbHelper = SampleApplicationDatabase.getInstance(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            db.beginTransaction();

            for (SearchResult searchResult : searchResults) {
                SearchResult tempResult = handleInsert(db, searchResult);
                searchResult.setSearchTableId(tempResult.getSearchTableId());

            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    	return searchResults;
    }

    /**
     * Flush all search results for the current book. There is no reason to keep these around.
     * @param context
     */
    public static void clearSearchResultsForBook(Context context) {
    	if(context == null) {
    		return;
    	}
    	synchronized (WRITE_LOCK) {
            SampleApplicationDatabase dbHelper = SampleApplicationDatabase.getInstance(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            //TODO: Clip this down to the columns we actually need
//            StringBuilder selection = new StringBuilder(SearchResult.COLUMN_BOOK_ID + " = " + book.getBookId());
            StringBuilder selection = new StringBuilder(SearchResult.COLUMN_BOOK_ID + " = 12345");

            //TODO: Sort mode or other filters needed here?
            db.delete(SearchResult.TABLE_NAME, selection.toString(), null);
        }

    }

    /**
     * Obtain a search result cursor for the given book. For now we only store one set of search results per
     * book. We may need a way to differentiate search tasks in case we have a canceled search running
     * mid-query. Don't want to store results from the wrong search request.
     * @param context
     * @return
     */
    public static Cursor getSearchResultCursorForBook(Context context) {
    	if(context == null) {
    		return null;
    	}

        SampleApplicationDatabase dbHelper = SampleApplicationDatabase.getInstance(context);
        ;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //TODO: Clip this down to the columns we actually need
        String columns[] = null;
//        StringBuilder selection = new StringBuilder(SearchResult.COLUMN_BOOK_ID + " = " + book.getBookId());
        StringBuilder selection = new StringBuilder(SearchResult.COLUMN_BOOK_ID + " = 12345");



        Cursor cursor = db.query(SearchResult.TABLE_NAME, columns, selection.toString(), null, null, null, null);

        return cursor;

    }


    //Database reference
	private static SampleApplicationDatabase mInstance;
    private static final Object WRITE_LOCK = new Object();

    // Database Constants
    public static final int DATABASE_VERSION = 23; //Update the version if we change the schema
    public static final String DATABASE_NAME = "CloudshelfDatabase.db";

    private static final String SQL_CREATE_SEARCH_RESULT =
            "CREATE TABLE " + SearchResult.TABLE_NAME + " (" +
            SearchResult._ID + " INTEGER PRIMARY KEY," +
            SearchResult.COLUMN_BOOK_ID + " INTEGER, " +
            SearchResult.COLUMN_SEARCH_TEXT + " STRING, " +
            SearchResult.COLUMN_SEARCH_PREFIX + " STRING, " +
            SearchResult.COLUMN_SEARCH_SUFFIX + " STRING, " +
            SearchResult.COLUMN_SEARCH_CFI + " STRING, " +
            SearchResult.COLUMN_SEARCH_IDREF + " STRING, " +
            SearchResult.COLUMN_SEARCH_CREATED + " LONG, " +
            SearchResult.COLUMN_SEARCH_SECTION + " STRING "
            +")";

    private static final String SQL_DELETE_SEARCH_RESULT =
            "DROP TABLE IF EXISTS " + SearchResult.TABLE_NAME;

    
}
