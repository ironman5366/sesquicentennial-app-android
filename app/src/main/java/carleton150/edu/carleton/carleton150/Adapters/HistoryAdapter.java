package carleton150.edu.carleton.carleton150.Adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import carleton150.edu.carleton.carleton150.Interfaces.RecyclerViewClickListener;
import carleton150.edu.carleton.carleton150.Interfaces.RecyclerViewScrolledListener;
import carleton150.edu.carleton.carleton150.Models.BitmapWorkerTask;
import carleton150.edu.carleton.carleton150.POJO.GeofenceInfoObject.GeofenceInfoContent;
import carleton150.edu.carleton.carleton150.R;

/**
 * Adapter for the RecyclerView in the HistoryInfoPopup
 */
public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private GeofenceInfoContent[] historyList = null;
    public static RecyclerViewClickListener clickListener;
    public static RecyclerViewScrolledListener scrolledListener;
    public int screenWidth;
    public int screenHeight;

    public HistoryAdapter(GeofenceInfoContent[] historyList,
                          RecyclerViewClickListener clickListener, RecyclerView recyclerView,
                          RecyclerViewScrolledListener scrolledListener, int screenWidth, int screenHeight) {
        this.historyList = historyList;
        this.clickListener = clickListener;
        this.scrolledListener = scrolledListener;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == recyclerView.SCROLL_STATE_IDLE){
                    clearDate();
                }else {
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    displayDate(lm);
                }

            }
        });
    }

    private void displayDate(LinearLayoutManager lm){
        int lastVisible = lm.findLastVisibleItemPosition();
        scrolledListener.recyclerViewScrolled(historyList[lastVisible].getYear());
    }

    private void clearDate(){
        scrolledListener.recyclerViewStoppedScrolling();
    }

    /**
     * Returns 0 if the object contains an image, 1 if it contains text
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if(historyList[position].getType().equals(historyList[position].TYPE_IMAGE)){
            return 0;
        } if(historyList[position].getType().equals(historyList[position].TYPE_TEXT)){
            return 1;
        } else {
            return -1;
        }
    }




    /**
     * Depending on the type, creates a ViewHolder from either the
     * history_info_card_image or the history_info_card_text
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.history_info_card_image, parent, false);
                return new HistoryViewHolderImage(itemView);
            case 1:
                View view = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.history_info_card_text, parent, false);
                return new HistoryViewHolderText(view);
        }
        return null;
    }

    /**
     * When the holder is bound, sets the necessary fields depending on the type
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        GeofenceInfoContent geofenceInfoContent = historyList[position];
        if(holder instanceof HistoryViewHolderText){
            ((HistoryViewHolderText) holder).setTxtMedia(geofenceInfoContent.getData());
            ((HistoryViewHolderText) holder).setTxtDate(geofenceInfoContent.getYear());
        }else if(holder instanceof HistoryViewHolderImage){
            ((HistoryViewHolderImage) holder).setImage(position, geofenceInfoContent.getData(), screenWidth, screenHeight);
            ((HistoryViewHolderImage) holder).setTxtDate(geofenceInfoContent.getYear());
            ((HistoryViewHolderImage) holder).setTxtCaption(geofenceInfoContent.getCaption());
        }
    }

    /**
     * returns the number of items in the historyList
     * @return
     */
    @Override
    public int getItemCount() {
        if(historyList != null) {
            return historyList.length;
        }else{
            return 0;
        }
    }

    public GeofenceInfoContent[] getHistoryList(){
        return this.historyList;
    }


    /**
     * A ViewHolder for views that contain only an image and date
     */
    public static class HistoryViewHolderImage extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txtDate;
        private ImageView imgMedia;
        private TextView txtCaption;

        public HistoryViewHolderImage(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            txtDate = (TextView) itemView.findViewById(R.id.txt_date);
            imgMedia = (ImageView) itemView.findViewById(R.id.img_history_info_image);
            txtCaption = (TextView) itemView.findViewById(R.id.txt_caption);


        }

        public String getTxtDate() {
            return txtDate.getText().toString();
        }

        public void setTxtDate(String txtDate) {
            this.txtDate.setText(txtDate);
        }

        public void setTxtCaption(String caption){
            this.txtCaption.setText(caption);
        }


        /**
         */
        public void setImage(int resId, String encodedImage, int screenWidth, int screenHeight) {

            int w = 10, h = 10;

            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            Bitmap mPlaceHolderBitmap = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap


            if (cancelPotentialWork(resId, imgMedia)) {

                //TODO: find better formula than dividing by 2
                final BitmapWorkerTask task = new BitmapWorkerTask(imgMedia,  encodedImage
                        , screenWidth/2, screenHeight/2);
                final BitmapWorkerTask.AsyncDrawable asyncDrawable =
                        new BitmapWorkerTask.AsyncDrawable(mPlaceHolderBitmap, task);
                imgMedia.setImageDrawable(asyncDrawable);
                task.execute(resId);
            }

        }

        public static boolean cancelPotentialWork(int data, ImageView imageView) {
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (bitmapWorkerTask != null) {
                final int bitmapData = bitmapWorkerTask.data;
                // If bitmapData is not yet set or it differs from the new data
                if (bitmapData == 0 || bitmapData != data) {
                    // Cancel previous task
                    bitmapWorkerTask.cancel(true);
                } else {
                    // The same work is already in progress
                    return false;
                }
            }
            // No task associated with the ImageView, or an existing task was cancelled
            return true;
        }


        private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
            if (imageView != null) {
                final Drawable drawable = imageView.getDrawable();
                if (drawable instanceof BitmapWorkerTask.AsyncDrawable) {
                    final BitmapWorkerTask.AsyncDrawable asyncDrawable = (BitmapWorkerTask.AsyncDrawable) drawable;
                    return asyncDrawable.getBitmapWorkerTask();
                }
            }
            return null;
        }

        @Override
        public void onClick(View v) {
            clickListener.recyclerViewListClicked(v, getLayoutPosition());
        }
    }


    /**
     * A ViewHolder for views that contain only a text description and date
     */

    public static class HistoryViewHolderText extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txtMedia;
        private TextView txtDate;

        public HistoryViewHolderText(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            txtMedia = (TextView) itemView.findViewById(R.id.txt_txt_media);
            txtDate = (TextView) itemView.findViewById(R.id.txt_date);


        }

        public TextView getTxtMedia() {
            return txtMedia;
        }

        public void setTxtMedia(String txtMedia) {
            this.txtMedia.setText(txtMedia);
        }

        public String getTxtDate() {
            return txtDate.getText().toString();
        }

        public void setTxtDate(String txtDate) {
            this.txtDate.setText(txtDate);
        }

        @Override
        public void onClick(View v) {
            clickListener.recyclerViewListClicked(v, getLayoutPosition());
        }
    }


}