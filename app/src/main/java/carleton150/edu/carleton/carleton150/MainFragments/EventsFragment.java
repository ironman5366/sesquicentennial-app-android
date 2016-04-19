package carleton150.edu.carleton.carleton150.MainFragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import carleton150.edu.carleton.carleton150.Adapters.EventDateCardAdapter;
import carleton150.edu.carleton.carleton150.Adapters.EventsListAdapter;
import carleton150.edu.carleton.carleton150.Interfaces.RecyclerViewDatesClickListener;
import carleton150.edu.carleton.carleton150.MainActivity;
import carleton150.edu.carleton.carleton150.POJO.EventObject.EventContent;
import carleton150.edu.carleton.carleton150.POJO.EventObject.Events;
import carleton150.edu.carleton.carleton150.R;

/**
 * The main fragment for the Events portion of the app. Displays
 * and events calendar
 *
 */
public class EventsFragment extends MainFragment implements RecyclerViewDatesClickListener {

    private Button btnTryAgain;
    private TextView txtTryAgain;
    private ArrayList<EventContent> eventsList = new ArrayList<>();
    ArrayList<String> dateInfo = new ArrayList<>();

    private EventsListAdapter eventsListAdapter;
    ExpandableListView eventsListView;

    // RecyclerView Pager
    private static View v;
    private RecyclerView dates;
    private LinkedHashMap<String, ArrayList<EventContent>> eventsMapByDate = new LinkedHashMap<String, ArrayList<EventContent>>();
    private ArrayList<EventContent> tempEventContentLst = new ArrayList<EventContent>();
    private int screenWidth;
    private LinearLayoutManager dateLayoutManager;
    private EventDateCardAdapter eventDateCardAdapter;

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_events, container, false);
        btnTryAgain = (Button) v.findViewById(R.id.btn_try_getting_events);
        txtTryAgain = (TextView) v.findViewById(R.id.txt_request_events);

        // Before buildRecyclerViews is called, we need to grab all events
        MainActivity mainActivity = (MainActivity) getActivity();
        eventsMapByDate = mainActivity.getEventsMapByDate();


        eventsListView = (ExpandableListView) v.findViewById(R.id.lst_events);
        eventsListAdapter = new EventsListAdapter(getActivity(), eventsList);
        eventsListView.setAdapter(eventsListAdapter);

        /*If no events were retrieved, displays this button so the user can click
        to try again once the network is connected
         */
        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtTryAgain.setText(getString(R.string.requesting_events));
                btnTryAgain.setVisibility(View.GONE);
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.requestEvents();
            }
        });

        // Build RecyclerViews to display date tabs
        buildRecyclerViews();

        if(eventsMapByDate == null){
            mainActivity.requestEvents();
        }else if(eventsMapByDate.size() == 0){
            mainActivity.requestEvents();
        }else{
            handleNewEvents(eventsMapByDate);
        }

        return v;
    }

    /**
     * Builds the views for the quests
     */
    private void buildRecyclerViews(){
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        dates = (RecyclerView) v.findViewById(R.id.lst_event_dates);
        dateLayoutManager = new LinearLayoutManager(getActivity());

        dateLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        dates.setLayoutManager(dateLayoutManager);

        eventDateCardAdapter = new EventDateCardAdapter(dateInfo, this, screenWidth);
        dates.setAdapter(eventDateCardAdapter);
        eventDateCardAdapter.notifyDataSetChanged();
    }


    /**
     * Called from VolleyRequester. Handles new events from server
     * @param eventsMapByDate
     */
    @Override
    public void handleNewEvents(LinkedHashMap<String, ArrayList<EventContent>> eventsMapByDate) {

        this.eventsMapByDate = eventsMapByDate;
        if(eventsMapByDate == null) {
            if (eventsList.size() == 0) {
                txtTryAgain.setText(getString(R.string.no_events_retrieved));
                txtTryAgain.setVisibility(View.VISIBLE);
                btnTryAgain.setVisibility(View.VISIBLE);
                eventsListView.setVisibility(View.GONE);
            }
        }
        dateInfo.clear();
        for (Map.Entry<String, ArrayList<EventContent>> entry : eventsMapByDate.entrySet()) {
            dateInfo.add(entry.getKey());
        }

        eventDateCardAdapter.notifyDataSetChanged();

        String key = eventsMapByDate.keySet().iterator().next();
        ArrayList<EventContent> newEvents = eventsMapByDate.get(key);
        eventsList.clear();
        for(int i = 0; i<newEvents.size(); i++){
            eventsList.add(newEvents.get(i));
        }

        txtTryAgain.setVisibility(View.GONE);
        btnTryAgain.setVisibility(View.GONE);
        eventsListView.setVisibility(View.VISIBLE);
        eventsListAdapter.notifyDataSetChanged();

    }


    /**
     * When a recycler view date selector is clicked, shows events for that day
     *
     * @param dateInfo
     */
    @Override
    public void recyclerViewListClicked(String dateInfo) {
        ArrayList<EventContent> newEvents = eventsMapByDate.get(dateInfo);
        eventsList.clear();
        for(int i = 0; i<newEvents.size(); i++){
            eventsList.add(newEvents.get(i));
        }

        eventsListAdapter.notifyDataSetChanged();
    }
}
