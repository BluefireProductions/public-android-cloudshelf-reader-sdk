package com.bluefire.sampleapplication;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.support.v7.app.ActionBarDrawerToggle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bluefire.bclreader.privateclasses.Cfi;
import com.bluefire.bclreader.publicclasses.BCLHighlight;
import com.bluefire.bclreader.publicclasses.BCLLocation;
import com.bluefire.bclreader.publicclasses.BCLPackage;
import com.bluefire.bclreader.publicclasses.BCLPageList;
import com.bluefire.bclreader.publicclasses.BCLPageListItem;
import com.bluefire.bclreader.publicclasses.BCLReaderSettings;
import com.bluefire.bclreader.publicclasses.BCLReaderView;

import org.json.JSONException;
import org.json.JSONObject;
import org.readium.sdk.android.SpineItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;


public class ReaderControllerActivity extends FragmentActivity implements
        ViewerSettingsDialog.OnViewerSettingsChange, TableOfContentsDialog.OnTableOfContentsChange {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.reader_controller_activity);

        mReaderView = (BCLReaderView) findViewById(R.id.bcl_reader_view);

        // BCLReader Intialization
        mReaderView.initializeWithApiKey(this, "325c8acc1b8a7da7811f27e6d0a3a3db1a31f055");

        String path = getIntent().getStringExtra("path");
        mPath = path;


        // Settings
        BCLReaderSettings.BCLReaderColumnMode columnMode = BCLReaderSettings.BCLReaderColumnMode.BCL_READER_COLUMN_MODE_DEFAULT;
        boolean isMediaOverlayTapToPlayEnabled = true;
        boolean isTextAlignmentJustified = false;
        int marginAmount = 0;
        int textScale = 100;
        BCLReaderSettings.BCLReaderTheme theme = BCLReaderSettings.BCLReaderTheme.BCL_READER_THEME_DEFAULT;
        BCLReaderSettings settings = new BCLReaderSettings(columnMode, isMediaOverlayTapToPlayEnabled, isTextAlignmentJustified, marginAmount, textScale, theme);

        // Highlights
        List<BCLHighlight> highlights = new ArrayList<>();


        // Initialize with book path
        BCLLocation locationToOpen = new BCLLocation("cover", "/4/2");

//        mReaderView.initWithBookPath(path, settings, highlights, locationToOpen);
        mReaderView.initWithBookPath(path);

        mReaderView.useCFILocations(false);
        mLocationClickoffView = (FrameLayout) findViewById(R.id.location_panel_clickoff);


        // Settings
        mViewerSettings = settings;

        // Configure ActionBar
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle("Library");
        getActionBar().hide();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_action_logo, R.drawable.ic_action_logo) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        RelativeLayout settingsFragmentWrapper = (RelativeLayout) findViewById(R.id.settings_fragment_wrapper);
        settingsFragmentWrapper.setVisibility(View.GONE);

        // Inflate correct settings into settings fragment wrapper (either reflowable or FXL)
        View v;
        boolean isFixedLayout = mReaderView.isFixedLayout();
        if (isFixedLayout) {
            v = getLayoutInflater().inflate(R.layout.viewer_settings_fixed_layout, null);
        } else {
            v = getLayoutInflater().inflate(R.layout.viewer_settings, null);
        }
        settingsFragmentWrapper.addView(v);

        // Wire up location panel clickoff (to hide settings, etc)
        FrameLayout locationPanelClickoff = (FrameLayout) findViewById(R.id.location_panel_clickoff);
        locationPanelClickoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout settingsFragmentWrapper = (RelativeLayout) findViewById(R.id.settings_fragment_wrapper);
                settingsFragmentWrapper.setVisibility(View.GONE);
                FrameLayout locationPanelClickoff = (FrameLayout) findViewById(R.id.location_panel_clickoff);
                locationPanelClickoff.setVisibility(View.GONE);
            }
        });

        // Wire up next page / previous page buttons
        ImageButton pageBack = (ImageButton) findViewById(R.id.page_back);
        pageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReaderView.turnPage(false);
            }
        });
        ImageButton pageRight = ((ImageButton) findViewById(R.id.page_next));
        pageRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReaderView.turnPage(true);
            }
        });

        // Set event listeners within view

        // Show / hide chrome when page tapped
        mPageControl = (LinearLayout)findViewById(R.id.control_bottom);
        mPageControl.setVisibility(LinearLayout.GONE);
        mReaderView.setOnPageTappedListener(new BCLReaderView.OnPageTappedListener() {
            @Override
            public void onPageTapped() {
                Log.i("ReaderControllerActivi…", "Page tapped.");
                if (mControlsVisible) {
                    Log.i("ReaderControllerActivi…", "Hiding chrome.");
                    mPageControl.setVisibility(LinearLayout.GONE);
                    getActionBar().hide();

                } else {
                    Log.i("ReaderControllerActivi…", "Showing chrome.");
                    mPageControl.setVisibility(LinearLayout.VISIBLE);
                    getActionBar().show();
                }
                mControlsVisible = !mControlsVisible;
            }
        });

        // Page Slider
        mSliderPageInfo = (TextView)findViewById(R.id.page_indicator);
        mPageDisplay = (TextView)findViewById(R.id.page_number_display);

        mPageControl = (LinearLayout)findViewById(R.id.control_bottom);
        mSeekBar = (SeekBar)findViewById(R.id.slider);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser && mSelecting && mOrderList != null && mOrderList.size() > progress) {
                    mProgress = progress;
                    BCLPageListItem bclPageListItem = mOrderList.get(progress); // Just update the page number without re-rendering
                    Log.e("ReaderControllerActiv…", "[onSeekBarChangeListener] progress= "+progress+" page label="+bclPageListItem.getLabel());
                    mSliderPageInfo.setVisibility(TextView.VISIBLE);
                    mSliderPageInfo.setText(getString(R.string.page_x_of_y,
                            bclPageListItem.getLabel(),
                            mOrderList.get(mOrderList.size()-1).getLabel()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSelecting = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSliderPageInfo.setVisibility(TextView.GONE);
                mSelecting = false;
                if(mProgress >- 1){
                    updatePagePositionByProgress(mProgress);
                }
                mProgress = -1;
            }

        });

        // Page list completed
        mReaderView.setOnPageListDidCompleteListener(new BCLReaderView.OnPageListDidCompleteListener() {
            @Override
            public void onPageListDidComplete() {
                mOrderList = mReaderView.getPageListItems();
                mSeekBar.setMax(mOrderList.size() - 1);
            }
        });

        // Search results completed
        mReaderView.setOnReportSearchResultsListener(new BCLReaderView.OnReportSearchResultsListener() {
            @Override
            public void onReportSearchResults(String searchResults) {

                Context context = getApplicationContext();
                final String lSearchResults = searchResults;
                List<SearchResult> results = SearchResult.jsonToSearchResultsList(lSearchResults, 12345, System.currentTimeMillis());
                synchronized(mSearchSpineItems) {
                    if(results.size() > 0) {
                        if(mSearchQuery.equalsIgnoreCase(results.get(0).getSearchText())) {
                            SampleApplicationDatabase.insertSearchResults(context, results);
                        }
                    }
                }

                Runnable showSearchDialog = new Runnable() {
                    @Override
                    public void run() {
                        //Display Search View
                        if(!mIsSearchResultsShowing) {
                            mIsSearchResultsShowing = true;
                            showSearchPanel();
                        } else {
                            updateSearchResults();
                        }
                        continueSearch();
                    }
                };

                runOnUiThread(showSearchDialog);

                Log.d(TAG, "reportSearchResults: "+searchResults);
            }
        });

        // PageList initialization did make progress listener
        mReaderView.setOnPageListInitializationDidMakeProgressListener(new BCLReaderView.OnPageListInitializationDidMakeProgressListener() {
            @Override
            public void onPageListInitializationDidMakeProgress(double progress) {
                // Progress will be a value between 0.0 and 1.0 (inclusive) representing how far the
                // page list initialization has progressed.
                Log.i("ReaderControllerActivi…", "Progress: " + progress);

            }
        });

        // Reader View location change listener
        mReaderView.setOnReaderViewLocationDidChangeListener(new BCLReaderView.OnReaderViewLocationDidChangeListener() {
            @Override
            public void onReaderViewLocationDidChange(BCLLocation location) {
                Log.i("ReaderControllerActivi…", "mReaderView.getLocation().getCfi(): " + mReaderView.getLocation().getCfi());
                Log.i("ReaderControllerActivi…", "mReaderView.getLastVisibleLocation().getCfi(): " + mReaderView.getLastVisibleLocation().getCfi());
                Log.i("ReaderControllerActivi…", "idref: " + location.getIdref());
                Log.i("ReaderControllerActivi…", "cfi: " + location.getCfi());
                Log.i("ReaderControllerActivi…", "estimatedChapterTitle: " + mReaderView.getEstimatedChapterTitle(location));
                Log.i("ReaderControllerActivi…", "screenContainsLocation: " + mReaderView.screenContainsLocation(location));

                mScreenCfi = mReaderView.getScreenCfi();
                mScreenIdref = mReaderView.getScreenIdref();

                if (location.getPageListItem() != null) {
                    mSeekBar.setProgress(location.getPageListItem().getOrderListIndex());
                }


                BCLPageList pageList = new BCLPageList(mReaderView);
                BCLPageListItem pageListItem = pageList.getItemForLocation(location);
                if (pageListItem != null) {
                    Log.i("ReaderControllerActivi…", "PageListItem order list index: " + pageListItem.getOrderListIndex());
                    Log.i("ReaderControllerActivi…", "PageListItem getPageListItem order list index: " + location.getPageListItem().getOrderListIndex());
                }



                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isFixedLayout = mReaderView.isFixedLayout();
                        FrameLayout bclReaderViewWrapper = (FrameLayout) findViewById(R.id.bcl_reader_view_wrapper);
                        if (isFixedLayout) {
                            mReaderView.setBodyBackgroundColor("#333");
                            bclReaderViewWrapper.setBackgroundColor(0xFF333333);
                        } else {
                            if (mViewerSettings.getTheme() == BCLReaderSettings.BCLReaderTheme.BCL_READER_THEME_DEFAULT) {
                                mReaderView.setBodyBackgroundColor("#FFF");
                                bclReaderViewWrapper.setBackgroundColor(0xFFFFFFFF);
                            } else if (mViewerSettings.getTheme() == BCLReaderSettings.BCLReaderTheme.BCL_READER_THEME_NIGHT) {
                                mReaderView.setBodyBackgroundColor("#000");
                                bclReaderViewWrapper.setBackgroundColor(0xFF000000);
                            } else if (mViewerSettings.getTheme() == BCLReaderSettings.BCLReaderTheme.BCL_READER_THEME_SEPIA) {
                                mReaderView.setBodyBackgroundColor("#e7dec7");
                                bclReaderViewWrapper.setBackgroundColor(0xFFE7DEC7);
                            }
                        }
                    }
                });

                updatePageDisplay(false);

                JSONObject json = new JSONObject();
                try {
                    json.put("idref", location.getIdref());
                    json.put("cfi", location.getCfi());
                    json.put("percentageThroughSpineItem", location.getPercentageThroughSpineItem());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String locationString = json.toString();
            }
        });

        // New highlight created listener
        mReaderView.setOnHighlightDidCompleteListener(new BCLReaderView.OnHighlightDidCompleteListener() {
            @Override
            public void onHighlightDidComplete(BCLHighlight highlight, String text) {
                Log.i("ReaderControllerActivi…", "id: " + highlight.getHighlightID());
                BCLLocation location = highlight.getLocation();
                Log.i("ReaderControllerActivi…", "idref: " + location.getIdref());
                Log.i("ReaderControllerActivi…", "cfi: " + location.getCfi());
                Log.i("ReaderControllerActivi…", "text: " + text);
            }
        });

        // Swipe up gesture listener
        mReaderView.setOnSwipeUpGestureListener(new BCLReaderView.OnSwipeUpGestureListener() {
            @Override
            public void onSwipeUpGesture(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Stub
                Log.i("ReaderControllerActivi…", "Swiped up.");
            }
        });

        // Swipe down gesture listener
        mReaderView.setOnSwipeDownGestureListener(new BCLReaderView.OnSwipeDownGestureListener() {
            @Override
            public void onSwipeDownGesture(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Stub
                Log.i("ReaderControllerActivi…", "Swiped down.");
            }
        });
    }


    // Action Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        if(menu.findItem(R.id.menu_book_search) != null) {
            MenuItem item = menu.findItem(R.id.menu_book_search);
            mSearchView = (SearchView) item.getActionView();
            mSearchMenuItem = item;
            mSearchView.setIconifiedByDefault(false); // Do not iconify the widget
            mSearchView.setOnQueryTextListener(onQueryTextListener);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case R.id.menu_book_settings:
                showSettings();
                break;
            case R.id.menu_table_of_contents:
                showTableOfContents();
                break;
            default:
                break;
        }

        return true;
    }


    // Fixed layout
    public boolean isFixedLayout() {
        return mReaderView.isFixedLayout();
    }

    // Page Slider
    /**
     * Update the page position from user interaction.
     * @param progress
     */
    public void updatePagePositionByProgress(int progress) {
        Log.e("ReaderControllerActiv…", "[updatePagePositionByProgress] progress = " + progress);

        // Get the ordered number by using our progress "percent"
        if(mOrderList != null && mOrderList.size() > progress) {
            BCLPageListItem bclPageListItem = mOrderList.get(progress);
            synchronized(mProgressFromUser) {
                mProgressFromUser = true;
                Log.e("ReaderControllerActiv…", "[updatePagePositionByProgress] updating gotopage |Progress: = "+progress
                        +" |idref: "+bclPageListItem.getIdref()+" |cfi: "+ bclPageListItem.getCfi());
                Log.e("ReaderControllerActiv…", "[updatePagePositionByProgress] gotoPage("+bclPageListItem.getIdref()+", "+ bclPageListItem.getCfi()+");");
                BCLLocation location = new BCLLocation(bclPageListItem.getIdref(), bclPageListItem.getCfi());
                mReaderView.goToLocation(location, false);
                synchronized(mUserPaged){
                    mUserPaged = true;
                }
            }
        }
    }



    /**
     * Update the page display to show the current page.
     */
    private void updatePageDisplay(boolean fromScrubber) {
//        Log.e(TAG, "[updatePageDisplay] page id info: "+mScreenIdref+mScreenCfi);
//        Log.e(TAG, "[updatePageDisplay] mLookupMap is null? "+(mLookupMap==null?"true":"false"));
//        int idrefIndex = -1;
//        mOrderedIdrefs = mReaderView.getOrderedIdrefs();
//        for (int i = 0; i < mOrderedIdrefs.size(); i++) {
//            String idref = mOrderedIdrefs.get(i);
//            if (idref.matches(mScreenIdref)) {
//                idrefIndex = i;
//                break;
//            }
//        }
//        Log.e(TAG, "[updatePageDisplay] idrefIndex: " + idrefIndex);
//        String lookupString = "/" + idrefIndex + "/4/2";
//        Cfi lookupCfi = new Cfi(lookupString);
//        Log.e(TAG, "[updatePageDisplay] lookupString: " + lookupString);
//
//        BCLPageListItem matchingPage = mLookupMap.get(lookupCfi);
//        int matchingPageOrderListIndex = matchingPage.getOrderListIndex();

        final BCLPageListItem matchingPage = mReaderView.getCurrentPageListItem();
        if (matchingPage != null) {
            runOnUiThread(new Runnable() {
                public void run(){
                    mPageDisplay.setText("Page " + matchingPage.getLabel() + " of " + mReaderView.getTotalPageListItemCount());
                }
            });
        }

//        String nextSpineItemString = "/" + (idrefIndex + 1) + "/4/2";
//        Cfi nextSpineItemCfi = new Cfi(nextSpineItemString);
//        BCLPageListItem nextSpineItemFirstPage = mLookupMap.get(nextSpineItemCfi);
//        int displayPageIndexForScrubber;
//        if (nextSpineItemFirstPage == null) { // We're on the last spine item
//            Log.e(TAG, "[updatePageDisplay] On the last spine item.");
//            displayPageIndexForScrubber = mOrderList.size() - 1;
//        } else {
//            int nextSpineItemFirstPagePageOrderListIndex = nextSpineItemFirstPage.getOrderListIndex();
//            int totalNumberOfPagesThisSpineItem = nextSpineItemFirstPagePageOrderListIndex - matchingPageOrderListIndex;
//            float totalNumberOfScreensThisSpineItem = mSpineItemPageCount;
//            float currentScreen = mSpineItemPageIndex;
//            float ratioOfCurrentToTotalScreens = currentScreen / totalNumberOfScreensThisSpineItem; // e.g. if 10 screens, and currently on 1, then 0.1
//            int currentPageIndexInSpineItem = (int) (totalNumberOfPagesThisSpineItem * ratioOfCurrentToTotalScreens);
//            displayPageIndexForScrubber = matchingPageOrderListIndex + currentPageIndexInSpineItem;
//        }
//        Log.e(TAG, "[updatePageDisplay] displayPageIndexForScrubber: " + displayPageIndexForScrubber);
//
//        if(matchingPage != null && displayPageIndexForScrubber > -1 && displayPageIndexForScrubber < mOrderList.size()) {
//            Log.e(TAG, "[updatePageDisplay] displayPageIndexForScrubber: " + displayPageIndexForScrubber);
//            mPageDisplay.setText(getString(R.string.page_x_of_y,
//                    mOrderList.get(displayPageIndexForScrubber).getLabel(),
//                    mOrderList.get(mOrderList.size()-1).getLabel()));
//            mPageDisplay.setVisibility(TextView.VISIBLE);
//            mScrubber.setMax(mOrderList.size() - 1);
//            if(!fromScrubber)
//                updateScrubberPosition(displayPageIndexForScrubber);
//        } else {
//            Log.e(TAG, "[updatePageDisplay] Error, page (" + lookupString + ") not found.");
//            if(mOrderList!=null&&mOrderList.size()>0) {
//                mPageDisplay.setText(getString(R.string.page_x_of_y,
//                        "",
//                        mOrderList.get(mOrderList.size()-1).getLabel()));
//                mPageDisplay.setVisibility(TextView.VISIBLE);
//                mScrubber.setMax(mOrderList.size()-1);
//                if(!fromScrubber)
//                    updateScrubberPosition(0);
//            }
//        }
    }

    // Search
    /**
     * Show a search fragment.
     */
    public void showSearchPanel() {
        android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        Fragment fragment = new SearchResultsFragment();
        if(fragment != null) {
            mLocationClickoffView.setVisibility(FrameLayout.VISIBLE);
            mLocationClickoffView.setClickable(true);
            mLocationClickoffView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    hideSearchPanel();
                }
            });
            fragment.setArguments(bundle);
            ft.replace(R.id.location_panel_host, fragment);
            ft.commit();
        }
    }

    /**
     * Hide the search panel
     */
    public void hideSearchPanel() {
        android.app.FragmentManager manager = getFragmentManager();
        Fragment locationFragment = manager.findFragmentById(R.id.location_panel_host);
        if(locationFragment != null) {
            android.app.FragmentTransaction ft = manager.beginTransaction();
            ft.remove(locationFragment);
            mLocationClickoffView.setVisibility(FrameLayout.INVISIBLE);
            ft.commit();
        }
    }

    /**
     * Refresh the dataset for the search results panel
     */
    public void updateSearchPanel() {
        android.app.FragmentManager manager = getFragmentManager();
        Fragment locationFragment = manager.findFragmentById(R.id.location_panel_host);
        if(locationFragment != null && locationFragment instanceof SearchResultsFragment) {
            ((SearchResultsFragment) locationFragment).refereshDataSet();
        }
    }

    /**
     * Close the open search control (and hide the keyboard). Used after clicking on
     * search results to keep the screen free of excess controls
     */
    public void closeSearchControl() {
        mSearchView.onActionViewCollapsed();
        hideSearchPanel();
        mSearchMenuItem.collapseActionView();
        // Hide soft keyboard
        InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    /**
     * Update the page location based on the parameter page request.
     * @param pageRequest
     */
    public void updateBookLocation(String pageRequest) {
        mReaderView.updateLocation(pageRequest);
    }

    public void updateBookLocation(String pageRequest, boolean isSearchResult) {
        mReaderView.updateLocation(pageRequest, isSearchResult);
    }


    // Search query listener
    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String newText) {
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            Log.i("ReaderControllerActivi…", "Begin Search with: " + query);
            beginSearch(query);
            return true;
        }

    };
    /**
     * Execute a search query using the string passed in from the calling activity.
     * @param query
     */
    public void beginSearch(String query) {
        Context context = getApplicationContext();
        mSearchCanceled = false;
        synchronized(mSearchSpineItems) {

            SampleApplicationDatabase.clearSearchResultsForBook(context);

            mSearchSpineItems.clear();
            // Hide the search panel, if it's showing
            hideSearchPanel();
            mIsSearchResultsShowing = false;
            mSearchSpineItems.addAll(mReaderView.getSpineItems());
            mSearchQuery = query;
        }
        continueSearch();
    }

    public void cancelSearch() {
        Log.e(TAG, "Canceled search.");
        mSearchCanceled = true;
    }

    /**
     * Continue executing the ongoing search
     */
    private void continueSearch() {
        synchronized(mSearchSpineItems) {
            int spineCount = 0;
            spineCount = mSearchSpineItems.size();
            Log.e(TAG, "[Continue Search] "+spineCount+" spine items remain.");
            if(mSearchSpineItems.size() > 0) {
                SpineItem currentSpineItem = mSearchSpineItems.remove(0);
                String href = currentSpineItem.getHref();
                String idref = currentSpineItem.getIdRef();
                String query = mSearchQuery.toString();
                Log.e(TAG, "[Continue Search] Firing FindIn.");
                mReaderView.executeSearch(query, href, idref);
            } else {
                return; //Done with the search loop
            }
        }
    }

    /**
     * Force the search results window to update its dataset
     */
    public void updateSearchResults() {
        android.app.FragmentManager manager = getFragmentManager();
        Fragment locationFragment = manager.findFragmentById(R.id.location_panel_host);
        if(locationFragment != null && locationFragment instanceof SearchResultsFragment) {
            ((SearchResultsFragment) locationFragment).refereshDataSet();
        }
    }


    // Settings

    private void showSettings() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        DialogFragment dialog = new ViewerSettingsDialog(this, mViewerSettings);
        dialog.show(fm, "dialog");
        fragmentTransaction.commit();
    }


    // Table of Contents

    private void showTableOfContents() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        BCLPackage pckg = new BCLPackage(mPath);
        DialogFragment dialog = new TableOfContentsDialog(this, pckg);
        dialog.show(fm, "dialog");
        fragmentTransaction.commit();
    }


    @Override
    public void onViewerSettingsChange(BCLReaderSettings viewerSettings) {
        updateSettings(viewerSettings);
    }

    @Override
    public void onTableOfContentsChange(String href) {
        mReaderView.goToHref(href);
    }

    private void updateSettings(BCLReaderSettings viewerSettings) {
        mViewerSettings = viewerSettings;
        mReaderView.updateSettings(viewerSettings);
        FrameLayout bclReaderViewWrapper = (FrameLayout) findViewById(R.id.bcl_reader_view_wrapper);
        switch (viewerSettings.getTheme()) {
            case BCL_READER_THEME_DEFAULT:
                bclReaderViewWrapper.setBackgroundColor(Color.parseColor("#FFFFFF"));
                break;
            case BCL_READER_THEME_NIGHT:
                bclReaderViewWrapper.setBackgroundColor(Color.parseColor("#141414"));
                break;
            case BCL_READER_THEME_SEPIA:
                bclReaderViewWrapper.setBackgroundColor(Color.parseColor("#E7DEC7"));
                break;
        }
    }


    // Settings
    private BCLReaderSettings mViewerSettings;
    private static final String TAG = "ReaderControllerActiv…";

    // Reader View
    private BCLReaderView mReaderView;

    // Components
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private LinearLayout mPageControl;
    private boolean mControlsVisible = false;
    private FrameLayout mLocationClickoffView = null;

    // Path to book
    private String mPath;

    // Search Variables
    private MenuItem mSearchMenuItem;
    private SearchView mSearchView;
    private List<SpineItem> mSearchSpineItems = new ArrayList<SpineItem>();
    private String mSearchQuery = "";
    private boolean mIsSearchResultsShowing = false;
    private boolean mSearchCanceled = false;

    // Page Slider Variables
    private List<SpineItem> mPageNoSpineItems = new ArrayList<SpineItem>();
    private List<Map.Entry<String, String>> mNativePageLabels = new ArrayList<Map.Entry<String, String>>();
    private List<String> mOrderedIdrefs = new ArrayList<String>();
    private List<BCLPageListItem> mOrderList = new ArrayList<BCLPageListItem>();
    private TreeMap<Cfi, BCLPageListItem> mLookupMap = new TreeMap<Cfi, BCLPageListItem>();
    private String mCurIdref = "";
    private Boolean mProgressFromUser = false;
    private Boolean mSelecting = false;
    private Boolean mUserPaged = false;
    private int mProgress = -1;
    private SeekBar mSeekBar;
    private TextView mSliderPageInfo;
    private TextView mPageDisplay;
    private String mScreenCfi;
    private String mScreenIdref;
}
