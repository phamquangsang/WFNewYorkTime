package app.com.phamsang.wfnewyorktime;

import android.content.Context;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Quang Quang on 3/17/2016.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>{
    private static final int NO_IMAGE_TYPE = 0;
    private static final int IMAGE_TYPE = 1;
    private static final int LOADING_INDICATOR_TYPE = 2;

    private Context mContext;
    private List<SearchItemObject> mDataSet = new ArrayList<SearchItemObject>();
    public SearchAdapter(Context c) {
        super();
        mContext = c;
    }

    public List<SearchItemObject> getDataSet() {
        return mDataSet;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size()+1;//for loading indicator at the end
    }

    @Override
    public int getItemViewType(int position) {
        if(position==mDataSet.size()){
            return LOADING_INDICATOR_TYPE;
        }
        SearchItemObject object = mDataSet.get(position);
        if(object.getThumbnail()==null){
            return NO_IMAGE_TYPE;
        }else{
            return IMAGE_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(position==mDataSet.size()-1){
            Toast.makeText(mContext,"loading more ...",Toast.LENGTH_SHORT);
            Log.d("SearchAdapter: ","end of list at position: "+position);
        }
        if(position==mDataSet.size()){
            return;
        }
        final SearchItemObject object = mDataSet.get(position);
        holder.mHeadLine.setText(object.getHeadline());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity searchActivity = (SearchActivity)mContext;
                searchActivity.launchUrl(object.getUrl());
            }
        });


        if(holder.mType == NO_IMAGE_TYPE){
            holder.mSnipet.setText(object.getSnipet());
        }else if(holder.mType == IMAGE_TYPE){
            Glide.with(mContext).load(object.getImageUrl())
                    .placeholder(R.drawable.placeholder).dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.mImageView);
        }


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(viewType == IMAGE_TYPE)
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item_layout,parent,false);
        else if(viewType==NO_IMAGE_TYPE){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item_no_image_layout,parent,false);
        }else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_layout_item,parent,false);
        }
        return new ViewHolder(v, viewType);
    }

    public void swapData(List<SearchItemObject> dataSet) {
        mDataSet = dataSet;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private View mView;
        private ImageView mImageView;
        private TextView mHeadLine;
        private TextView mSnipet;
        private int mType;
        public ViewHolder(View itemView, int type) {
            super(itemView);
            mView = itemView;
            mType = type;
            mImageView = (ImageView)itemView.findViewById(R.id.imageView);
            mHeadLine =  (TextView)itemView.findViewById(R.id.textView_headline);
            mSnipet = (TextView)itemView.findViewById(R.id.textView_snippet);
        }


    }
}
