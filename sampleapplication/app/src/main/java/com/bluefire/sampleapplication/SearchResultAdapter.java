package com.bluefire.sampleapplication;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

//import android.support.v4.widget.CursorAdapter;

/**
 * Cursor for handling the display of books
 * @author Bluefire
 *
 */
public class SearchResultAdapter extends CursorAdapter {
	private static final String TAG = "SearchResultAdapter";
	
	
	public SearchResultAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		// TODO Auto-generated constructor stub
	}

	public SearchResultAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		populateView(view, cursor);
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.search_result_item, parent, false);
		
        return populateView(v, cursor);
	}
	
	
	
	@Override
	public SearchResult getItem(int position) {

//		return super.getItem(position);
		
		Cursor cursor = getCursor();
		cursor.moveToPosition(position);
		
		return new SearchResult(cursor);
		
	}

	/**
	 * Populate the the given view with our current cursor position
	 * @param view the view to populate
	 * @param cursor the cursor to populate with
	 * @return the populated view
	 */
	private View populateView(View view, Cursor cursor) { 

		int bookIdIndex = cursor.getColumnIndex(SearchResult.COLUMN_BOOK_ID);
		int prefixIndex = cursor.getColumnIndex(SearchResult.COLUMN_SEARCH_PREFIX);
		int textIndex = cursor.getColumnIndex(SearchResult.COLUMN_SEARCH_TEXT);
		int suffixIndex = cursor.getColumnIndex(SearchResult.COLUMN_SEARCH_SUFFIX);
		int cfiIndex = cursor.getColumnIndex(SearchResult.COLUMN_SEARCH_CFI);
		
		Long bookId = cursor.getLong(bookIdIndex);
		String prefix = cursor.getString(prefixIndex);
		if(prefix.length()>MAX_STRING_LENGTH) {
			prefix = prefix.substring(prefix.length()-MAX_STRING_LENGTH, prefix.length());
		}
		String suffix = cursor.getString(suffixIndex);
		if(suffix.length()>MAX_STRING_LENGTH) {
			suffix = suffix.substring(0, MAX_STRING_LENGTH);
		}
		String foundText = cursor.getString(textIndex);
		String searchCfi = cursor.getString(cfiIndex);
		
		TextView searchItem = (TextView)view.findViewById(R.id.search_item);
		TextView pageNumber = (TextView)view.findViewById(R.id.page_number);
		
		//TODO:Assemble String -- use html so we can bold the selected text?
		//     ... should we tuck away the CFI for this search result in the TAG?
		StringBuilder formattedText = new StringBuilder();
		formattedText.append(prefix);
		formattedText.append("<b>");
		formattedText.append(foundText);
		formattedText.append("</b>");
		formattedText.append(suffix);
		searchItem.setText(Html.fromHtml(formattedText.toString()));
		searchItem.setTag(searchCfi);

//		pageNumber.setText("00");
		pageNumber.setVisibility(TextView.GONE);
        
        return view;
	}
	
	private static final int MAX_STRING_LENGTH = 30;
}
