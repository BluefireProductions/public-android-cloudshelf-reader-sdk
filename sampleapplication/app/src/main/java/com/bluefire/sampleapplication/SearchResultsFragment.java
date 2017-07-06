package com.bluefire.sampleapplication;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;



import org.json.JSONException;

/**
 * This fragment implements a display for the book's table of contents which
 * can optionally be a full screen or split screen fragment. (Not floating)
 * 
 * @author Bluefire
 *
 */
public class SearchResultsFragment extends ListFragment {
	private static final String TAG = "SearchResultsFragment";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		Log.e(TAG, "onCreateView");
		View v = inflater.inflate(R.layout.search_list_fragment, container, false);
		mBackground = (LinearLayout)v.findViewById(R.id.list_background);
		mTopIcon = (ImageView)v.findViewById(R.id.menu_title_thumb);
		mTopText = (TextView)v.findViewById(R.id.item_name);
		mClickableTitle = (LinearLayout)v.findViewById(R.id.menu_title_region);
		
		//For now we will hard code the return button to only go to the locations base menu
		mClickableTitle.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {

				Activity activity = getActivity();
				if(activity!=null && activity instanceof ReaderControllerActivity) {
					ReaderControllerActivity fragmentHost = (ReaderControllerActivity)activity;
					fragmentHost.cancelSearch();
					//Navigate back to the location fragment here...
					fragmentHost.hideSearchPanel();
					
				}
			}
		});
		
		

		Log.e(TAG, "[onCreateView] pulling book data");
		Context context = inflater.getContext();
		Cursor searchCursor = SampleApplicationDatabase.getSearchResultCursorForBook(context);
		mAdapter = new SearchResultAdapter(context, searchCursor, false);
		setListAdapter(mAdapter);

		return v;
	}	
	
	/**
	 * Requeries the base dataset for this item.
	 */
	public void refereshDataSet() {
		Context context = getActivity().getApplicationContext();
		Cursor searchCursor = SampleApplicationDatabase.getSearchResultCursorForBook(context);
		mAdapter.swapCursor(searchCursor); // = new SearchResultAdapter(context, searchCursor, false);
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		SearchResult result = mAdapter.getItem(position);
		OpenPageRequest openPageRequest = OpenPageRequest.fromIdrefAndCfi(result.getIdref(), result.getCfi());
    	Activity activity = getActivity();
    	if(activity instanceof ReaderControllerActivity) {
    		ReaderControllerActivity bookHost = (ReaderControllerActivity) activity;
    		
    		try {
    			//Close the search control
    			bookHost.closeSearchControl();
				Log.e(TAG, "[onListItemClick] navigating to: "+openPageRequest.toJSON().toString());
    			bookHost.updateBookLocation(openPageRequest.toJSON().toString(), true);
			} catch (JSONException e) {
				Log.e(TAG, "[onListItemClick] Failed to generate open page request.");
			}
    		
    	}

    }
	
	private SearchResultAdapter mAdapter;
	private LinearLayout mBackground;
	private ImageView mTopIcon = null;
	private TextView mTopText = null;
	private LinearLayout mClickableTitle = null;
}
