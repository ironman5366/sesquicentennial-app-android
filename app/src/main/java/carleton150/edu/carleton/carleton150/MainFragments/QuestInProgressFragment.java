package carleton150.edu.carleton.carleton150.MainFragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import carleton150.edu.carleton.carleton150.FlipAnimation;
import carleton150.edu.carleton.carleton150.Interfaces.FragmentChangeListener;
import carleton150.edu.carleton.carleton150.MainActivity;
import carleton150.edu.carleton.carleton150.Models.BitmapWorkerTask;
import carleton150.edu.carleton.carleton150.POJO.Quests.Quest;
import carleton150.edu.carleton.carleton150.POJO.Quests.Waypoint;
import carleton150.edu.carleton.carleton150.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuestInProgressFragment extends MapMainFragment {


    private Quest quest = null;
    private int numClue = 0;
    private TextView txtClue;
    private Button btnFoundIt;
    private TextView txtHint;
    private TextView txtClueNumber;
    private ImageButton btnReturnToMyLocation;
    private Button btnFlipCardToHint;
    private Button btnFlipCardToClue;
    private RelativeLayout relLayoutQuestCompleted;
    private TextView txtQuestCompleted;
    private ImageView imgQuestCompleted;
    private Button btnDoneWithQuest;
    private SlidingDrawer slidingDrawerClue;
    private SlidingDrawer slidingDrawerHint;
    private ImageView imgClue;
    private ImageView imgHint;
    private int screenWidth;
    private int screenHeight;
    private static final String QUEST_STARTED = "You already started this quest. " +
            "Would you like to Resume it or Start Over?";

    View rootLayout;
    View cardFace;
    View cardBack;


    private SupportMapFragment mapFragment;


    public QuestInProgressFragment() {
        // Required empty public constructor
    }

    /**
     * This must be called after creating the QuestInProgressFragment in order to pass
     * it the current quest
     * @param quest quest to be completed by user
     */
    public void initialize(Quest quest){
        this.quest = quest;
    }


    /**
     * Sets OnClickListeners to register when hint button or found it button is clicked
     * and to show the hint or check if the user is within a valid radius of the waypoint
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_quest_in_progress, container, false);
        txtClue = (TextView) v.findViewById(R.id.txt_clue);
        btnFoundIt = (Button) v.findViewById(R.id.btn_found_location);
        txtHint = (TextView) v.findViewById(R.id.txt_hint);
        txtClueNumber = (TextView) v.findViewById(R.id.txt_clue_number);
        btnReturnToMyLocation = (ImageButton) v.findViewById(R.id.btn_return_to_my_location);
        rootLayout = v.findViewById(R.id.lin_layout_card_root);
        cardFace = v.findViewById(R.id.clue_view_front);
        cardBack = v.findViewById(R.id.clue_view_back);
        btnFlipCardToClue = (Button) v.findViewById(R.id.btn_show_clue);
        btnFlipCardToHint = (Button) v.findViewById(R.id.btn_show_hint);
        relLayoutQuestCompleted = (RelativeLayout) v.findViewById(R.id.rel_layout_quest_completed);
        txtQuestCompleted = (TextView) v.findViewById(R.id.txt_completion_message);
        imgQuestCompleted = (ImageView) v.findViewById(R.id.img_animation_quest_completed);
        btnDoneWithQuest = (Button) v.findViewById(R.id.btn_done_with_quest);
        slidingDrawerClue = (SlidingDrawer) v.findViewById(R.id.front_drawer);
        slidingDrawerHint = (SlidingDrawer) v.findViewById(R.id.drawer_hint);
        imgHint = (ImageView) v.findViewById(R.id.img_hint_img_back);
        imgClue = (ImageView) v.findViewById(R.id.img_clue_image_front);


       checkIfQuestStarted();


        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        btnFlipCardToHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipCard();
            }
        });

        btnFlipCardToClue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipCard();
            }
        });

        Waypoint[] waypoints = quest.getWaypoints();
        String hint = waypoints[numClue].getHint().getText();

        String image = null;
        String hintImage = null;
        if(waypoints[numClue].getHint().getImage() != null) {
            hintImage = waypoints[numClue].getHint().getImage().getImage();
        }if(waypoints[numClue].getClue().getImage() != null){
            image = waypoints[numClue].getClue().getImage().getImage();
        }


        if(hint == null || hint.equals("")) {
            txtHint.setText(getResources().getString(R.string.no_hint_available));
        } else {
            txtHint.setText(waypoints[numClue].getHint().getText());
        }


        if (image != null){
            slidingDrawerClue.setVisibility(View.VISIBLE);
            setImage(image, screenWidth, screenHeight, imgClue);
        }else{
            slidingDrawerClue.setVisibility(View.GONE);
        }

        if (hintImage != null){
            slidingDrawerHint.setVisibility(View.VISIBLE);
            setImage(hintImage, screenWidth, screenHeight, imgHint);
        }else{
            slidingDrawerHint.setVisibility(View.GONE);
        }

        btnReturnToMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnZoomToUserLocation();
            }
        });

        btnFoundIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfClueFound();

            }
        });
        updateCurrentWaypoint();
        return v;
    }

    /**
     * zooms to the user's current location
     */
    private void returnZoomToUserLocation(){
        zoomCamera = true;
        setCamera();
    }

    /**
     * replaces the RelativeLayout named my_map with a SupportMapFragment
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
         mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.my_map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.my_map, mapFragment).commit();
        }

    }

    /**
     * Lifecycle method overridden to set up the map if it
     * is currently null
     */
    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if(mainActivity.mLastLocation != null){
            drawLocationMarker(mainActivity.mLastLocation);
        }
        drawTiles();
    }

    /**
     * Checks if the user's current location is within the radius of the waypoint
     * (both the radius and waypoint are specified in the quest object)
     */
    private void checkIfClueFound(){
        Location curLocation = mainActivity.mLastLocation;
        if(curLocation != null) {
            Waypoint curWaypoint = quest.getWaypoints()[numClue];
            double lat = curWaypoint.getLat();
            double lon = curWaypoint.getLng();
            double rad = curWaypoint.getRad();
            float[] results = new float[1];

            Location.distanceBetween(curLocation.getLatitude(), curLocation.getLongitude(),
                    lat, lon,
                    results);
            if (results[0] <= rad) {
                clueCompleted();
            } else {
                //String to display if hint is not already showing
                String alertString = getActivity().getResources().getString(R.string.location_not_found_hint);
                if (txtHint.getVisibility() == View.VISIBLE) {
                    //String to display if hint is already showing
                    alertString = getActivity().getResources().getString(R.string.location_not_found);
                }
                mainActivity.showAlertDialog(alertString,
                        new AlertDialog.Builder(mainActivity).create());
            }
        }else{
            Log.i(logMessages.LOCATION, "QuestInProgressFragment: checkIfClueFound: location is null");
            //: this shouln't happen. Handle it better...
        }
    }


    /**
     * Sets up the map
     * Monitors the zoom and target of the camera and changes them
     * if the user zooms out too much or scrolls map too far off campus.
     */
    @Override
    protected void setUpMap() {

        super.setUpMap();
        // to get rid of blue dot showing user's location
        mMap.setMyLocationEnabled(false);
        }


    /**
     * Updates map view to reflect user's new location
     *
     * @param newLocation
     */
    @Override
    public void handleLocationChange(Location newLocation) {
        super.handleLocationChange(newLocation);
        setCamera();
        drawLocationMarker(newLocation);

        if(mainActivity.getGeofenceMonitor().currentLocation != null) {
            setUpMapIfNeeded();
        }
    }

    /**
     * draws a custom location marker for the user's current location
     * @param newLocation
     */
    private void drawLocationMarker(Location newLocation) {
        if(mMap != null) {
            Log.i(logMessages.LOCATION, "QuestInProgressFragment : drawLocationMarker : mMap is not null");
            mMap.clear();
            Bitmap knightIcon = BitmapFactory.decodeResource(getResources(), R.drawable.knight_horse_icon);
            LatLng position = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(knightIcon);
            Marker curLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("Current Location")
                    .icon(icon));
        }else{
            Log.i(logMessages.LOCATION, "QuestInProgressFragment : drawLocationMarker : mMap is null");
        }
    }



    /**
     * Checks if the quest is finished. If not, sets the text to show the next clue
     *
     * @return boolean, true if quest is finished, false otherwise
     */
    public boolean updateCurrentWaypoint(){
        boolean finished = false;
        Waypoint[] waypoints = quest.getWaypoints();
        try {
            if (waypoints[numClue] == null &&
                    waypoints[numClue - 1] != null) {
                finished = true;
                return finished;
            }
            txtClue.setText(waypoints[numClue].getClue().getText());
            txtClueNumber.setText((numClue + 1) + "/" + quest.getWaypoints().length);
            if(txtHint != null || !txtHint.equals("")){
                txtHint.setText(waypoints[numClue].getHint().getText());
            }else{
                txtHint.setText(getResources().getString(R.string.no_hint_available));
            }


            String image = null;
            String hintImage = null;
            if(waypoints[numClue].getHint().getImage() != null) {
                hintImage = waypoints[numClue].getHint().getImage().getImage();
            }if(waypoints[numClue].getClue().getImage() != null){
                image = waypoints[numClue].getClue().getImage().getImage();
            }
             if (image != null){
                  slidingDrawerClue.setVisibility(View.VISIBLE);
                  setImage(image, screenWidth, screenHeight, imgClue);
             }else{
                  slidingDrawerClue.setVisibility(View.GONE);
              }

            if (hintImage != null){
                slidingDrawerHint.setVisibility(View.VISIBLE);
                setImage(hintImage, screenWidth, screenHeight, imgHint);
            }else{
                slidingDrawerHint.setVisibility(View.GONE);
            }


            return finished;
        } catch (NullPointerException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Handles when a clue has been completed by incrementing the clue
     * number, updating the current waypoint, and checking if the quest is completed
     */
    public void clueCompleted() {
        Log.i(logMessages.GEOFENCE_MONITORING, "QuestInProgressFragment: clueCompleted");
        showClueCompletedMessage();
        numClue += 1;

        //saves the quest progress into SharedPreferences
        SharedPreferences.Editor sharedPrefsEditor = mainActivity.getPersistentQuestStorage().edit();
        sharedPrefsEditor.putInt(quest.getName(), numClue);
        sharedPrefsEditor.commit();


        boolean completedQuest = updateCurrentWaypoint();
        if (completedQuest){
            showCompletedQuestMessage();
        }

    }

    /**
     * Shows the message stored with the quest when the quest has been
     * completed
     */
    private void showCompletedQuestMessage(){

        imgQuestCompleted.setImageDrawable(ContextCompat.getDrawable(mainActivity, R.drawable.anim_quest_completed));
        txtQuestCompleted.setText("Message is : " + quest.getCompMsg());
        txtQuestCompleted.setMovementMethod(new ScrollingMovementMethod());
        imgQuestCompleted.setVisibility(View.VISIBLE);
        relLayoutQuestCompleted.setVisibility(View.VISIBLE);
        ((AnimationDrawable) imgQuestCompleted.getBackground()).start();
        btnDoneWithQuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToQuestSelectionScreen();
            }
        });
    }

    /**
     * Shows the message stored with the quest when the quest has been
     * completed
     */
    private void showClueCompletedMessage(){
        ;
        txtQuestCompleted.setText("Message is : " + quest.getWaypoints()[numClue].getCompletion().getText());
        txtQuestCompleted.setMovementMethod(new ScrollingMovementMethod());

        if(quest.getWaypoints()[numClue].getCompletion().getImage() != null){
            setImage(quest.getWaypoints()[numClue].getCompletion().getImage(),
                    screenWidth, screenHeight, imgQuestCompleted);
        }else{
            imgQuestCompleted.setVisibility(View.GONE);
        }

        relLayoutQuestCompleted.setVisibility(View.VISIBLE);

        btnDoneWithQuest.setText("Continue to Next Hint");
        btnDoneWithQuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relLayoutQuestCompleted.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Called when the fragment comes into view (different than onResume() because
     * the viewPager keeps several fragments in resumed state. This method is called
     * when the fragment actually comes into view on the screen
     *
     * updates the waypoints,
     * and sets the map camera if necessary
     */
    @Override
    public void fragmentInView() {
        Log.i(logMessages.LOCATION, "QuestInProgressFragment : fragmentInView : called");
        updateCurrentWaypoint();
        if(mainActivity.mLastLocation != null){
            Log.i(logMessages.LOCATION, "QuestInProgressFragment : fragmentInView : last location not null, drawing marker");
            drawLocationMarker(mainActivity.mLastLocation);
        }
        if(this.isResumed()) {
            drawTiles();
        }
        setCamera();
    }



    /**
     * Map should be set to null in onDestroyView(), but then there is an error
     * because the FragmentManager has already called onSaveInstanceState, so variables
     * can no longer be changed. Therefore, it is necessary to make mMap = null before
     * saving the instance state.
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        mMap = null;
        super.onSaveInstanceState(outState);
    }

    private void goBackToQuestSelectionScreen(){
        QuestFragment fr=new QuestFragment();
        FragmentChangeListener fc=(FragmentChangeListener)getActivity();
        fc.replaceFragment(fr);
    }

    private void flipCard()
    {

        FlipAnimation flipAnimation = new FlipAnimation(cardFace, cardBack);

        if (cardFace.getVisibility() == View.GONE)
        {
            flipAnimation.reverse();
        }
        rootLayout.startAnimation(flipAnimation);
    }

    /**
     */
    public void setImage(String encodedImage, int screenWidth, int screenHeight, ImageView imageView) {

        int w = 10, h = 10;

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap mPlaceHolderBitmap = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap



            final BitmapWorkerTask task = new BitmapWorkerTask(imageView,  encodedImage
                    , screenWidth/2, screenHeight/2);
            final BitmapWorkerTask.AsyncDrawable asyncDrawable =
                    new BitmapWorkerTask.AsyncDrawable(mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute();

    }

    private void checkIfQuestStarted(){
        SharedPreferences sharedPreferences = mainActivity.getPersistentQuestStorage();
        int curClue = sharedPreferences.getInt(quest.getName(), 0);
        if(curClue != 0){
            showOptionToResumeQuest();
        }
    }

    private void showOptionToResumeQuest(){
        mainActivity.showAlertDialogNoNeutralButton(new AlertDialog.Builder(mainActivity)
                .setMessage(QUEST_STARTED)
                .setPositiveButton("Resume", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resumeQuest();
                    }
                })
                .setNegativeButton("Start Over", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create());
    }

    private void resumeQuest(){
        int curClue = mainActivity.getPersistentQuestStorage().getInt(quest.getName(), 0);
        if(curClue != 0){
            numClue = curClue;
        }

        boolean completedQuest = updateCurrentWaypoint();
        if (completedQuest){
            showCompletedQuestMessage();
        }
    }
}