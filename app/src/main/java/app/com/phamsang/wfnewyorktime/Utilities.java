package app.com.phamsang.wfnewyorktime;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Quang Quang on 3/17/2016.
 */
public class Utilities {
    public static final String API_KEY = "api-key=5b7beab1724c1bee4a33f80ad11b7ffe:6:74726332";
    private static final String LOG_TAG = Utilities.class.getSimpleName() ;

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getNewsYourTimeQueryString(String keyWord, int page){
        String result ="http://api.nytimes.com/svc/search/v2/articlesearch.json?q=";
        result = result + keyWord +"&page="+page+ "&"+API_KEY;
        Log.i(LOG_TAG,"build simple query string: "+result);
        return result;
    }

    public static String getNewsYourTimeQueryString(String keyWord, int page,
                                                    int year, int month, int date,
                                                    String order, boolean isArt,
                                                    boolean isFashion, boolean isSport){
        String result ="http://api.nytimes.com/svc/search/v2/articlesearch.json?q=";
        result = result + keyWord +"&page="+page+ "&"+API_KEY;
        result = result + "&begin_date="+year+(month<10?"0"+month:month)+(date<10?"0"+date:date);
        result += "&sort="+order;
        if(isArt || isFashion || isSport){
            result+="&fd=news_desk:(";
            if(isArt){
                result+="\"arts\"";
            }
            if(isFashion){
                result+="\"Fashion%20%26%20Style\"";
            }
            if(isSport){
                result+="\"sports\"";
            }
            result+=")";
        }
        Log.i(LOG_TAG,"build complex query string: "+result);
        return result;
    }

    public static String updatePage(String url, int page){
        return url.replaceAll("page=.&","page="+page+"&");

    }
    public static String updateQueryString(String url, String queryString){
        return url.replaceAll("q=*&","q="+queryString+"&");
    }
}
