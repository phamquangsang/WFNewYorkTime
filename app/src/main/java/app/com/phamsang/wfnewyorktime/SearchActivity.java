package app.com.phamsang.wfnewyorktime;

import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity implements AdvancedSearchDialogFragment.NoticeDialogListener {

    //member variable for chrome custom tab
    public static final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";
    private static final String LOG_TAG = SearchActivity.class.getSimpleName();
    private static final String QUERY_STRING = "query_string";
    private static final String QUERY_URL = "query_url";
    private static final String DATASET = "saved_state_dataset";
    private static final String ADVANCED_SEARCH_TAG = "advanced_search_dialog_fragment";
    private static final String SEARCH_SETTING = "search_setting";
    private static final String BEGIN_YEAR = "begin_year";
    private static final String BEGIN_MONTH = "begin_month";
    private static final String BEGIN_DATE = "begin_date";
    private static final String ORDER = "order";
    private static final String IS_ARTS = "is_arts";
    private static final String IS_FASHION = "is_fashion";
    private static final String IS_SPORT = "is_sport";
    CustomTabsClient mClient;
    CustomTabsSession mCustomTabsSession;
    CustomTabsServiceConnection mCustomTabsServiceConnection;
    CustomTabsIntent customTabsIntent;
    //end of setup chrome custom tab

    private List<SearchItemObject> mSearchResult = new ArrayList<SearchItemObject>();
    private List<SearchItemObject> mMostViewArticles = new ArrayList<SearchItemObject>();

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private SearchAdapter mAdapter = new SearchAdapter(this);
    private RecyclerView.LayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
    private RecyclerView.LayoutManager mStaggredLayoutManager;

    private String mQueryString = "";
    private String mQueryUrl = "";
    private int mBeginYear;
    private int mBeginMonth;
    private int mBeginDate;
    private boolean isArts;
    private boolean isFashion;
    private boolean isSports;
    private String mOrder;
    private TextView mPopularTitleTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences setting = getSharedPreferences(SEARCH_SETTING, MODE_PRIVATE);
        mBeginYear = setting.getInt(BEGIN_YEAR, 1851);
        mBeginMonth = setting.getInt(BEGIN_MONTH, 9);
        mBeginDate = setting.getInt(BEGIN_DATE, 18);
        mOrder = setting.getString(ORDER, "newest");
        isArts = setting.getBoolean(IS_ARTS, false);
        isFashion = setting.getBoolean(IS_FASHION, false);
        isSports = setting.getBoolean(IS_SPORT, false);

        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);

        //make recycler view look better in difference device screen
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenWidth = (int) (displaymetrics.widthPixels / displaymetrics.density);
        int screenHeight = (int) (displaymetrics.heightPixels / displaymetrics.density);
        int gridCollumn;
        if (screenWidth >= 480) {
            gridCollumn = 3;
        } else
            gridCollumn = 2;

        mPopularTitleTextView = (TextView)findViewById(R.id.popularTextViewTitle);
        //set up Recycler View
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mStaggredLayoutManager = new StaggeredGridLayoutManager(gridCollumn,StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mStaggredLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener
                ((StaggeredGridLayoutManager) mRecyclerView.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.i(LOG_TAG, "loading page: " + page);
                if (page != 0)
                    performQuery(Utilities.updatePage(mQueryUrl, page), mAdapter.getDataSet(), mProgressBar);
            }
        });
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        if (savedInstanceState != null) {
            mQueryUrl = savedInstanceState.getString(QUERY_URL, "");
            List<SearchItemObject> dataSet = savedInstanceState.getParcelableArrayList(DATASET);
            mAdapter.swapData(dataSet);
        }
        if (mAdapter.getDataSet().size() == 0) {
            //todo the most view article to fill empty space
            loadMostViewArticle();
        }


        //Setup Chrome Custom Tabs
        mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                //Pre-warming
                mClient = customTabsClient;
                mClient.warmup(0L);
                //Initialize a session as soon as possible.
                mCustomTabsSession = mClient.newSession(null);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mClient = null;
            }
        };

        CustomTabsClient.bindCustomTabsService(this, CUSTOM_TAB_PACKAGE_NAME, mCustomTabsServiceConnection);
        //End custom tabs setup
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(QUERY_STRING, mQueryString);
        outState.putString(QUERY_URL, mQueryUrl);
        outState.putParcelableArrayList(DATASET, (ArrayList<SearchItemObject>) mAdapter.getDataSet());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);


        if (!mQueryString.isEmpty()) {
            searchView.setQuery(mQueryString, true);
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //todo perform query operation
                Toast.makeText(SearchActivity.this, "submit query!!! : " + query, Toast.LENGTH_SHORT).show();
                mQueryString = query;
                mQueryUrl = Utilities.getNewsYourTimeQueryString(query, 0, mBeginYear, mBeginMonth, mBeginDate, mOrder, isArts, isFashion, isSports);
                performQuery(mQueryUrl, new ArrayList<SearchItemObject>(), mProgressBar);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        // Expand the search view and request focus


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_advanced_search) {
            AdvancedSearchDialogFragment advanced =
                    AdvancedSearchDialogFragment
                            .newInstance(mBeginYear, mBeginMonth, mBeginDate, mOrder, isArts, isFashion, isSports);
            advanced.show(getFragmentManager(), ADVANCED_SEARCH_TAG);

            return true;
        } else if (id == R.id.action_search) {
            //todo expand search view
            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.requestFocus();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void performQuery(String url, final List<SearchItemObject> currentData, final ProgressBar progressBar) {
        if (!Utilities.isNetworkAvailable(this)) {
            Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
            mRecyclerView.setVisibility(View.GONE);
            findViewById(R.id.no_internet_image_view).setVisibility(View.VISIBLE);
            return;
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.no_internet_image_view).setVisibility(View.GONE);
        }
        mRecyclerView.setLayoutManager(mStaggredLayoutManager);
        mQueryUrl = url;//udpate url
        Log.d(LOG_TAG, "Loading: " + mQueryUrl);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                int progress = (int) ((bytesWritten * 100) / totalSize);
                progressBar.setProgress(progress);
                Log.i(LOG_TAG, "onProgress :" + progress + " - bytesWritten/totalSize: " + bytesWritten + "/" + totalSize);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                List<SearchItemObject> dataset = new ArrayList<SearchItemObject>();
                try {
                    String baseUrl = "http://www.nytimes.com/";

                    JSONArray rootList = response.getJSONObject("response").getJSONArray("docs");
                    for (int i = 0; i < rootList.length(); ++i) {
                        JSONObject object = rootList.getJSONObject(i);
                        SearchItemObject item = new SearchItemObject();
                        item.setUrl(object.getString("web_url"));
                        item.setSnipet(object.getString("snippet"));
                        item.setLeadParagraph(object.getString("lead_paragraph"));

                        JSONArray imageArray = object.getJSONArray("multimedia");
                        for (int j = 0; j < imageArray.length(); j++) {
                            JSONObject image = imageArray.getJSONObject(j);
                            if (image.getString("subtype").equalsIgnoreCase("wide")) {
                                item.setThumbnail(baseUrl + image.getString("url"));
                            } else if (image.getString("subtype").equalsIgnoreCase("xlarge")) {
                                item.setImageUrl(baseUrl + image.getString("url"));
                            } else if (image.getString("subtype").equalsIgnoreCase("thumbnail")) {

                            }
                        }
                        JSONObject headLine = object.getJSONObject("headline");
                        item.setHeadline(headLine.getString("main"));
                        item.setDate(object.getString("pub_date"));
                        item.setNewsDesk(object.getString("news_desk"));
                        item.setId(object.getString("_id"));
                        dataset.add(item);
                    }
                    currentData.addAll(dataset);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "parsing json failed - dataCount: " + dataset.size());
                }
                mAdapter.swapData(currentData);
                mRecyclerView.setVisibility(View.VISIBLE);
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    mRecyclerView.setLayoutManager(mStaggredLayoutManager);
                progressBar.setVisibility(View.GONE);
                mPopularTitleTextView.setVisibility(View.GONE);
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }

    void launchUrl(String url) {
        // Launch Chrome Custom Tabs on click
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        PendingIntent share = PendingIntent.getActivity(this, 0, shareIntent, 0);
        customTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setShowTitle(true).setActionButton(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_share_white_24dp), "Action Share", share, true)
                .build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        AdvancedSearchDialogFragment advancedDialog = (AdvancedSearchDialogFragment) dialog;

        String[] dateString = advancedDialog.getBeginDate().getText().toString().split("/");
        mBeginYear = Integer.parseInt(dateString[0]);
        mBeginMonth = Integer.parseInt(dateString[1]);
        mBeginDate = Integer.parseInt(dateString[2]);
        Log.d(LOG_TAG, advancedDialog.getBeginDate().getText().toString());
        mOrder = advancedDialog.getOrder().getSelectedItem().toString();
        if (advancedDialog.getIsArts().isChecked()) {
            isArts = true;
        } else {
            isArts = false;
        }
        if (advancedDialog.getIsFashion().isChecked()) {
            isFashion = true;
        } else isFashion = false;

        if (advancedDialog.getIsSport().isChecked()) {
            isSports = true;
        } else isSports = false;
        SharedPreferences setting = getSharedPreferences(SEARCH_SETTING, MODE_PRIVATE);
        setting.edit().putInt(BEGIN_YEAR, mBeginYear).putInt(BEGIN_MONTH, mBeginMonth).putInt(BEGIN_DATE, mBeginDate).commit();
        setting.edit().putString(ORDER, mOrder).putBoolean(IS_ARTS, isArts).putBoolean(IS_FASHION, isFashion)
                .putBoolean(IS_SPORT, isSports).commit();
        String queryUrl = Utilities.getNewsYourTimeQueryString
                (mQueryString, 0, mBeginYear, mBeginMonth, mBeginDate, mOrder, isArts, isFashion, isSports);
        performQuery(queryUrl, new ArrayList<SearchItemObject>(), mProgressBar);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    public List<SearchItemObject> loadMostViewArticle() {
        String url = "http://api.nytimes.com/svc/mostpopular/v2/mostviewed/all-sections/7.json?offset=0&api-key=4cf8fae1cd774a712d244dbbbafa5c70:12:74726332";
        AsyncHttpClient client = new AsyncHttpClient();
        final ArrayList<SearchItemObject> list = new ArrayList<SearchItemObject>();
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    JSONArray rootList = response.getJSONArray("results");
                    for (int i = 0; i < rootList.length(); ++i) {
                        SearchItemObject item = new SearchItemObject();
                        JSONObject jsonObject = rootList.getJSONObject(i);
                        item.setUrl(jsonObject.getString("url"));
                        item.setDate(jsonObject.getString("published_date"));
                        item.setHeadline(jsonObject.getString("title"));
                        item.setId(jsonObject.getString("id"));
                        item.setSnipet(jsonObject.getString("abstract"));

                        JSONArray mediaArray = jsonObject.getJSONArray("media");
                        for (int j = 0; j < mediaArray.length(); ++j) {
                            JSONObject mediaObject = mediaArray.getJSONObject(j);
                            if (mediaObject.getString("type").equalsIgnoreCase("image")) {
                                JSONArray imageArray = mediaObject.getJSONArray("media-metadata");
                                for (int k = 0; k < imageArray.length(); ++k) {
                                    JSONObject imageObject = imageArray.getJSONObject(k);
                                    if (imageObject.getString("format").equalsIgnoreCase("Normal")) {
                                        item.setThumbnail(imageObject.getString("url"));
                                    } else if (imageObject.getString("format").equalsIgnoreCase("Large")) {
                                        item.setImageUrl(imageObject.getString("url"));
                                    }
                                }
                            }
                        }
                        list.add(item);
                    }
                    Log.d(LOG_TAG,"loading most view articles: "+ list.size()+" loaded");
                    mAdapter.swapData(list);
                    mRecyclerView.setLayoutManager(mLinearLayoutManager);
                    mPopularTitleTextView.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onSuccess(statusCode, headers, response);
            }
        });
        return list;
    }
}
