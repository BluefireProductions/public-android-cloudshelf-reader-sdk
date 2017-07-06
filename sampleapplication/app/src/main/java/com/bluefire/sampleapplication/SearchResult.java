package com.bluefire.sampleapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchResult implements BaseColumns {
	private static final String TAG = "SearchResult";
	
	public SearchResult(String jsonData, long bookId, long dateCreated) {
		try {
			Log.e(TAG, "[bookmark] jsonData: "+jsonData);
			
			JSONObject object = new JSONObject(jsonData);
			mBookId = bookId;
			
			//Columns: JSON KEYS: [text, prefix, suffix, cfi, idref] 
			
			if(object.has(COLUMN_SEARCH_TEXT))
				mSearchText = object.getString(COLUMN_SEARCH_TEXT);
			if(object.has(COLUMN_SEARCH_PREFIX))
				mPrefix = object.getString(COLUMN_SEARCH_PREFIX);
			if(object.has(COLUMN_SEARCH_SUFFIX)) 
				mSuffix = object.getString(COLUMN_SEARCH_SUFFIX);
			if(object.has(COLUMN_SEARCH_CFI))
				mCfi = object.getString(COLUMN_SEARCH_CFI);
			if(object.has(COLUMN_SEARCH_IDREF))
				mIdref = object.getString(COLUMN_SEARCH_IDREF);
			
			mCreatedDate = dateCreated;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public SearchResult(JSONObject jsonData, long bookId, long dateCreated) {
		try {
//			Log.e(TAG, "[bookmark] jsonData: "+jsonData);
			
			JSONObject object = jsonData;
			mBookId = bookId;
			
			//Columns: JSON KEYS: [text, prefix, suffix, cfi, idref] 
			
			if(object.has(COLUMN_SEARCH_TEXT))
				mSearchText = object.getString(COLUMN_SEARCH_TEXT);
			if(object.has(COLUMN_SEARCH_PREFIX))
				mPrefix = object.getString(COLUMN_SEARCH_PREFIX);
			if(object.has(COLUMN_SEARCH_SUFFIX)) 
				mSuffix = object.getString(COLUMN_SEARCH_SUFFIX);
			if(object.has(COLUMN_SEARCH_CFI))
				mCfi = object.getString(COLUMN_SEARCH_CFI);
			if(object.has(COLUMN_SEARCH_IDREF))
				mIdref = object.getString(COLUMN_SEARCH_IDREF);
			
			mCreatedDate = dateCreated;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Cursor based constructor
	 * @param cursor
	 */
	public SearchResult(Cursor cursor) {
		
		int tableIdIndex = cursor.getColumnIndex(_ID);
		int bookIdIndex = cursor.getColumnIndex(COLUMN_BOOK_ID);
		int searchTextIndex = cursor.getColumnIndex(COLUMN_SEARCH_TEXT);
		int searchPrefixIndex = cursor.getColumnIndex(COLUMN_SEARCH_PREFIX);
		int searchSuffixIndex = cursor.getColumnIndex(COLUMN_SEARCH_SUFFIX);
		int searchCfiIndex = cursor.getColumnIndex(COLUMN_SEARCH_CFI);
		int searchIdrefIndex = cursor.getColumnIndex(COLUMN_SEARCH_IDREF);
		int searchCreatedIndex = cursor.getColumnIndex(COLUMN_SEARCH_CREATED);
		int searchSectinIndex = cursor.getColumnIndex(COLUMN_SEARCH_SECTION);
		
        mSearchTableId = cursor.getLong(tableIdIndex);  
        mBookId = cursor.getLong(bookIdIndex);
        mSearchText = cursor.getString(searchTextIndex);
        mPrefix = cursor.getString(searchPrefixIndex);
    	mSuffix = cursor.getString(searchSuffixIndex);
        mCfi = cursor.getString(searchCfiIndex);
        mIdref = cursor.getString(searchIdrefIndex);
        mCreatedDate = cursor.getLong(searchCreatedIndex); 
        mSectionCfi = cursor.getString(searchSectinIndex); 
	}
	
    /**
     * Get the values to be written to the database.
     * @return
     */
    public ContentValues getUpdateOrInsertValues() {
    	ContentValues values = new ContentValues();
    	
    	values.put(COLUMN_BOOK_ID, mBookId);
    	values.put(COLUMN_SEARCH_TEXT, mSearchText);
    	values.put(COLUMN_SEARCH_PREFIX, mPrefix);
    	values.put(COLUMN_SEARCH_SUFFIX, mSuffix);
        values.put(COLUMN_SEARCH_CFI, mCfi);
        values.put(COLUMN_SEARCH_IDREF, mIdref);
        values.put(COLUMN_SEARCH_CREATED, mCreatedDate);
        values.put(COLUMN_SEARCH_SECTION, mSectionCfi);
    	
    	return values;
    }
	
	
	//TODO: Implement here
    public long getSearchTableId() {
		return mSearchTableId;
	}
	public void setSearchTableId(long mSearchTableId) {
		this.mSearchTableId = mSearchTableId;
	}
	public long getBookId() {
		return mBookId;
	}
	public void setBookId(long mBookId) {
		this.mBookId = mBookId;
	}
	public String getSearchText() {
		return mSearchText;
	}
	public void setSearchText(String mSearchText) {
		this.mSearchText = mSearchText;
	}
	public String getPrefix() {
		return mPrefix;
	}
	public void setPrefix(String mPrefix) {
		this.mPrefix = mPrefix;
	}
	public String getSuffix() {
		return mSuffix;
	}
	public void setSuffix(String mSuffix) {
		this.mSuffix = mSuffix;
	}
	public String getCfi() {
		return mCfi;
	}
	public void setCfi(String mCfi) {
		this.mCfi = mCfi;
	}
	public String getIdref() {
		return mIdref;
	}
	public void setIdref(String mIdref) {
		this.mIdref = mIdref;
	}
	public long getCreatedDate() {
		return mCreatedDate;
	}
	public void setCreatedDate(long mCreatedDate) {
		this.mCreatedDate = mCreatedDate;
	}
	public String getSectionCfi() {
		return mSectionCfi;
	}
	public void setSectionCfi(String mSectionCfi) {
		this.mSectionCfi = mSectionCfi;
	}
	
	/**
	 * Convert a JSON array to a list of search result objects
	 */
	public static List<SearchResult> jsonToSearchResultsList (String jsonData, long bookId, long dateCreated) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		
		try {
			JSONArray array = new JSONArray(jsonData);
			for(int x=0; x<array.length(); x++) {
				JSONObject object = (JSONObject) array.get(x);
				results.add(new SearchResult(object, bookId, dateCreated));
			}
		} catch (JSONException e) {
			Log.e(TAG, "[jsonToSearchResultsList] failed to parse json data. ", e);
		}
		
		return results;
	}
	
    private long mSearchTableId;   //Auto ID Field
    private long mBookId;          //Book ID that contains this search result
    private String mSearchText;  //Selected text of the search
    private String mPrefix;      //Prefix for the search term
	private String mSuffix;      //Suffix for the search term
    private String mCfi;	     //CFI of the result
    private String mIdref;	     //Spine ID Reference found in
    private long mCreatedDate;   //Date this search item was created
    private String mSectionCfi;  //TOC lookup value (TODO)
	
	//Columns: JSON KEYS: [text, prefix, suffix, cfi, idref] 
	
    //Database/JSON Tags
    public static final String TABLE_NAME = "searchresults";
    public static final String COLUMN_BOOK_ID = "bookid";         //Book ID that contains this esarch result
    public static final String COLUMN_SEARCH_TEXT = "text";       //text of the search
    public static final String COLUMN_SEARCH_PREFIX = "prefix";   //prefix of the search
    public static final String COLUMN_SEARCH_SUFFIX = "suffix";   //suffix of the search
    public static final String COLUMN_SEARCH_CFI = "cfi";         //cfi of the search result
    public static final String COLUMN_SEARCH_IDREF = "idref";     //Spine ID Reference
    public static final String COLUMN_SEARCH_CREATED = "date";    //Date Created
    public static final String COLUMN_SEARCH_SECTION = "section"; //Book Section CFI
}
