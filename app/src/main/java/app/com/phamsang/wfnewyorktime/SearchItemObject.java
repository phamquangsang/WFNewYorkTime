package app.com.phamsang.wfnewyorktime;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Quang Quang on 3/17/2016.
 */
public class SearchItemObject implements Parcelable {
    private String mId;
    private String mHeadline;
    private String mUrl;
    private String mNewsDesk;
    private String mDate;
    private String mImageUrl;
    private String mThumbnail;
    private String mSnipet;
    private String mLeadParagraph;

    public SearchItemObject(String id, String headline, String url, String newsDesk, String date, String imageUrl, String thumbnail, String snipet, String leadParagraph) {
        mId = id;
        mHeadline = headline;
        mUrl = url;
        mNewsDesk = newsDesk;
        mDate = date;
        mImageUrl = imageUrl;
        mThumbnail = thumbnail;
        mSnipet = snipet;
        mLeadParagraph = leadParagraph;
    }

    public SearchItemObject() {

    }


    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getHeadline() {
        return mHeadline;
    }

    public void setHeadline(String headline) {
        mHeadline = headline;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getNewsDesk() {
        return mNewsDesk;
    }

    public void setNewsDesk(String newsDesk) {
        mNewsDesk = newsDesk;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(String thumbnail) {
        mThumbnail = thumbnail;
    }

    public String getSnipet() {
        return mSnipet;
    }

    public void setSnipet(String snipet) {
        mSnipet = snipet;
    }

    public String getLeadParagraph() {
        return mLeadParagraph;
    }

    public void setLeadParagraph(String leadParagraph) {
        mLeadParagraph = leadParagraph;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
        dest.writeString(this.mHeadline);
        dest.writeString(this.mUrl);
        dest.writeString(this.mNewsDesk);
        dest.writeString(this.mDate);
        dest.writeString(this.mImageUrl);
        dest.writeString(this.mThumbnail);
        dest.writeString(this.mSnipet);
        dest.writeString(this.mLeadParagraph);
    }

    protected SearchItemObject(Parcel in) {
        this.mId = in.readString();
        this.mHeadline = in.readString();
        this.mUrl = in.readString();
        this.mNewsDesk = in.readString();
        this.mDate = in.readString();
        this.mImageUrl = in.readString();
        this.mThumbnail = in.readString();
        this.mSnipet = in.readString();
        this.mLeadParagraph = in.readString();
    }

    public static final Parcelable.Creator<SearchItemObject> CREATOR = new Parcelable.Creator<SearchItemObject>() {
        @Override
        public SearchItemObject createFromParcel(Parcel source) {
            return new SearchItemObject(source);
        }

        @Override
        public SearchItemObject[] newArray(int size) {
            return new SearchItemObject[size];
        }
    };
}
