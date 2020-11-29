package com.The032solutions.MouTCV.Activity.Main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.The032solutions.MouTCV.Activity.FetchURL;
import com.The032solutions.MouTCV.Activity.TaskLoadedCallback;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mahc.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayout;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayoutBehavior;
import com.The032solutions.MouTCV.Activity.CompletedTracks.CompletedTracksActivity;
import com.The032solutions.MouTCV.Activity.ItemPagerAdapter;
import com.The032solutions.MouTCV.CurrentTrack.CurrentTrackView;
import com.The032solutions.MouTCV.CurrentUser.CurrentUserData;
import com.The032solutions.MouTCV.R;
import com.The032solutions.MouTCV.Services.GoogleMapUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.The032solutions.MouTCV.R.drawable.ic_go_forward;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, OnMapLoadedCallback, LocationListener, TaskLoadedCallback, OnChartValueSelectedListener {

    private GoogleMap map;
    private ProgressDialog loadMapProgressDialog;

    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    public static File filesDir;


    public enum AppState {
        MAIN_BOTTOM_STOP,
        MAIN_BOTTOM_RUNNING,
        DIRECTIONS_STOP,
        DIRECTIONS_RUNNING,
    }

    AppState appState = AppState.MAIN_BOTTOM_STOP;

    int[] mDrawables = {
            R.drawable.ic_play_track,
            R.drawable.ic_play_track,
            R.drawable.ic_play_track,
            R.drawable.ic_play_track,
            R.drawable.ic_play_track,
            R.drawable.ic_play_track
    };

    TextView bottomSheetTextView;

    PlacesClient placesClient;

    ItemPagerAdapter adapter;
    Polyline currentPolyline;
    LatLng myLatLng;
    List<LatLng> route;

    BottomSheetBehaviorGoogleMapsLike behaviorMain;
    BottomSheetBehaviorGoogleMapsLike behaviorDirections;
    FloatingActionButton fabDirs;
    FloatingActionButton fabBike;

    int deletedCards = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateWindow();
        setContentView(R.layout.activity_main);

        showMapLoadProgress();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setViewTrackPanelNotClickable();

        View trackPanel = findViewById(R.id.track_panel);
        trackPanel.setVisibility(View.GONE);

        filesDir = getFilesDir();
        CurrentUserData.initializeUserData();
        CurrentTrackView.initializeTrack(this,
                (TextView) findViewById(R.id.timeVal),
                (TextView) findViewById(R.id.distanceVal),
                (TextView) findViewById(R.id.speedVal),
                (ImageButton) findViewById(R.id.buttonStop),
                (ImageButton) findViewById(R.id.buttonPause),
                (ImageButton) findViewById(R.id.buttonPlay));

        /**
         * If we want to listen for states callback
         */
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        behaviorMain = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        behaviorMain.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        fabBike.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        fabBike.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        fabBike.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                        fabBike.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d("bottomsheet-", "STATE_HIDDEN");
                        break;
                    default:
                        Log.d("bottomsheet-", "STATE_SETTLING");
                        fabBike.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        MergedAppBarLayout mergedAppBarLayout = findViewById(R.id.mergedappbarlayout);
        MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        mergedAppBarLayoutBehavior.setToolbarTitle("MouT");
        mergedAppBarLayoutBehavior.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //behaviorMain.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
            }
        });

        bottomSheetTextView = (TextView) bottomSheet.findViewById(R.id.bottom_sheet_title);
        adapter = new ItemPagerAdapter(this,mDrawables);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);
        viewPager.setVisibility(View.GONE);

        behaviorMain.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
        behaviorMain.setPeekHeight(400);
        //behavior.setCollapsible(false);

        // Directions bottom sheet
        View bottomSheet2 = coordinatorLayout.findViewById(R.id.bottom_sheet_directions);
        behaviorDirections = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet2);
        behaviorDirections.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        fabDirs.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        fabDirs.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        fabDirs.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                        fabDirs.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d("bottomsheet-", "STATE_HIDDEN");
                        break;
                    default:
                        Log.d("bottomsheet-", "STATE_SETTLING");
                        fabDirs.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        behaviorDirections.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
        behaviorDirections.setPeekHeight(400);

        // PLACES

        // Initialize the SDK
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_static_key));

        // Create a new PlacesClient instance
        placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i("PLACE", "Place: " + place.getName() + ", " + place.getId());

                String url = "";

                if(myLatLng != null) {
                    url = "https://maps.googleapis.com/maps/api/directions/json?\n" +
                            "origin=" + myLatLng.latitude + "," + myLatLng.longitude +
                            "&destination=place_id:" + place.getId() + "\n" +
                            "&mode=bicycling&language=es&key=" + getString(R.string.google_maps_key);
                    url = "https://maps.googleapis.com/maps/api/directions/json?\n" +
                            "origin=39.47035954480136,-0.3348893907690009" +
                            "&destination=39.4798101405679, -0.34581230690110903&waypoints=via:39.47641279586022%2C-0.3339372156612928" +
                            "&mode=bicycling&language=es&key=" + getString(R.string.google_maps_key);

                    FetchURL fetchURL = new FetchURL(MainActivity.this, map);
                    fetchURL.execute(url, "bicycling");

                    // Instantiate the RequestQueue.
                    RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

                    // Request a string response from the provided URL.
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // Display the first 500 characters of the response string.

                                    try {
                                        JSONObject jObject = new JSONObject(response);
                                        ShowDirections(jObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps").toString());

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
                    // Add the request to the RequestQueue.
                    queue.add(stringRequest);
                }
                //RequestPhoto(place);
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i("PLACE", "An error occurred: " + status);
            }
        });

        fabBike = findViewById(R.id.start_bike);
        fabBike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (appState == AppState.MAIN_BOTTOM_STOP) {
                    clickStartTrackButton(view);
                    fabBike.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_stop_24));
                    appState = AppState.MAIN_BOTTOM_RUNNING;
                } else {
                    clickStopTrackButton(view);
                    fabBike.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_directions_bike_24));

                    appState = AppState.MAIN_BOTTOM_STOP;
                    behaviorMain.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                    behaviorDirections.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
                }
            }
        });

        fabDirs = findViewById(R.id.start_directions);
        fabDirs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (appState == AppState.DIRECTIONS_STOP) {
                    clickStartTrackButton(view);
                    fabDirs.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_stop_24));
                    appState = AppState.DIRECTIONS_RUNNING;
                } else {
                    clickStopTrackButton(view);
                    fabDirs.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_directions_bike_24));

                    appState = AppState.MAIN_BOTTOM_STOP;
                    behaviorMain.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                    behaviorDirections.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
                }
            }
        });

        SetupData();
    }

    private void SetupData() {
        // Data
        float[] kms = {1.23f, 3.54f, 2.38f, 0.0f, 1.53f, 3.12f, 4.34f};
        float[] days = {1f, 2f, 3f, 4f, 5f, 6f, 7f};

        // Info
        float meanKmsF = 0.0f;
        for (float km : kms) {
            meanKmsF += (km / 7.0f);
        }
        TextView meanKms = (TextView) findViewById(R.id.meanKms);
        meanKms.setText(String.format("%.2f", meanKmsF) + " kms");

        TextView emisionesTxt = (TextView) findViewById(R.id.emisionesCO2);
        emisionesTxt.setText(String.format("%.2f",(meanKmsF * 7 * 23)) + "g");

        // Charts
/*        BarChart barChart = (BarChart) findViewById(R.id.chartKms);
        barChart.setDescription(null);
        barChart.setDrawGridBackground(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getLegend().setEnabled(false);
        barChart.setScaleEnabled(false);

        List<BarEntry> entries = new ArrayList<BarEntry>();
        for (int i = 0; i < kms.length; i++) {
            // turn your data into Entry objects
            entries.add(new BarEntry(days[i], kms[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Label"); // add entries to dataset
        //dataSet.setColors(new int[] {Color.BLUE, Color.BLUE, R.color.quantum_black_text, R.color.quantum_googgreen, R.color.colorAccent, R.color.colorAccent, R.color.colorAccent});
        dataSet.setColor(Color.parseColor("#91d290"));
        dataSet.setValueTextColor(Color.parseColor("#91d290"));

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate(); // refresh*/

        // in this example, a LineChart is initialized from xml
        /*LineChart chart = (LineChart) findViewById(R.id.chart);

        List<Entry> entries2 = new ArrayList<Entry>();
        for (int i = 0; i < kms.length; i++) {
            // turn your data into Entry objects
            entries2.add(new Entry(days[i], kms[i]));
        }

        LineDataSet dataSet2 = new LineDataSet(entries2, "Label"); // add entries to dataset
        dataSet2.setColor(R.color.colorAccent);
        dataSet2.setValueTextColor(R.color.colorAccent);

        LineData lineData = new LineData(dataSet2);
        chart.setData(lineData);
        chart.invalidate(); // refresh

        chart.setOnChartValueSelectedListener(this);*/
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void ShowDirections(String dirNoFilter) throws JSONException {
        String dirs = dirNoFilter.replace("\\/", "/");
        //JSONObject jObject = new JSONObject(dirs);
        int lastSearchInstr = dirs.indexOf("html_instructions", 0);
        int lastSearchDist = dirs.indexOf("distance", 0);
        lastSearchDist = dirs.indexOf("distance", lastSearchDist + 1);
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.directions_sheet_content);

        appState = AppState.DIRECTIONS_STOP;
        behaviorMain.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
        behaviorDirections.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
        fabDirs.setVisibility(View.VISIBLE);
        int counter = 0;
        deletedCards = 0;
        route = new ArrayList<>();

        while (lastSearchInstr != -1) {
            String workingStr = dirs.substring(lastSearchInstr -100, lastSearchInstr + 200);
            // Create main layout
            final LinearLayout newCard = new LinearLayout(this);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT );
            params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 115, getResources().getDisplayMetrics());
            params1.setMargins(4, 4, 4, 4);
            newCard.setLayoutParams(params1);
            newCard.setBackgroundColor(getResources().getColor(R.color.colorWhite, getTheme()));
            newCard.setElevation(4);


            // Create image
            ImageView dirImage = new ImageView(this);
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params2.weight = 0.5f;
            params2.setMargins(15, 15, 15, 15);
            dirImage.setLayoutParams(params2);
            if (workingStr.contains("turn-right")) {
                dirImage.setImageResource(R.drawable.ic_turn_right);
            } else if (workingStr.contains("turn-left")) {
                dirImage.setImageResource(R.drawable.ic_turn_left);
            } else if (workingStr.contains("turn-slight-right")) {
                dirImage.setImageResource(R.drawable.ic_turn_slight_right);
            } else if (workingStr.contains("turn-slight-left")) {
                dirImage.setImageResource(R.drawable.ic_turn_slight_left);
            } else {
                dirImage.setImageResource(ic_go_forward);
            }
            dirImage.setBackgroundColor(getColor(R.color.transparent));
            newCard.addView(dirImage);

            // Right Layout
            final LinearLayout rightLayout = new LinearLayout(this);
            LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params3.weight = 0.5f;
            params3.setMargins(5, 5, 5, 5);
            rightLayout.setLayoutParams(params3);
            rightLayout.setOrientation(LinearLayout.VERTICAL);
            newCard.addView(rightLayout);

            // Dir text
            TextView dirText = new TextView(this);
            LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params4.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 85, getResources().getDisplayMetrics());
            dirText.setLayoutParams(params4);
            dirText.setText(Html.fromHtml(dirs.substring(20 + lastSearchInstr, dirs.indexOf("\",\"", 20 + lastSearchInstr))));
            dirText.setTextColor(getColor(R.color.quantum_grey800));
            dirText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            rightLayout.addView(dirText);

            // Meters text
            TextView metersText = new TextView(this);
            LinearLayout.LayoutParams params5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            metersText.setLayoutParams(params5);
            metersText.setText(dirs.substring(dirs.indexOf("ce\":{\"text\":\"", lastSearchDist) + 13, dirs.indexOf("m\",\"value", lastSearchDist) + 1));
            metersText.setTextColor(getColor(R.color.quantum_grey600));
            metersText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            rightLayout.addView(metersText);

            mainLayout.addView(newCard);

            lastSearchInstr = dirs.indexOf("html_instructions", lastSearchInstr + 1);
            lastSearchDist = dirs.indexOf("distance", lastSearchDist + 1);
        }
        // Fill
        LinearLayout fill = new LinearLayout(this);
        LinearLayout.LayoutParams params6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params6.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 700, getResources().getDisplayMetrics());
        fill.setBackgroundColor(Color.WHITE);
        mainLayout.addView(fill);
    }

    // PHOTO REQUEST
    private void RequestPhoto(Place myPlace) {
        // Define a Place ID.
        String placeId = myPlace.getId();

        // Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
        final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

        // Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);

        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            final Place place = response.getPlace();

            // Get the photo metadata.
            final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                Log.w("PHOTO", "No photo metadata.");
                return;
            }
            final PhotoMetadata photoMetadata = metadata.get(0);

            // Get the attribution text.
            final String attributions = photoMetadata.getAttributions();

            // Create a FetchPhotoRequest.
            final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500) // Optional.
                    .setMaxHeight(300) // Optional.
                    .build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                adapter.replaceImage(bitmap);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    final ApiException apiException = (ApiException) exception;
                    Log.e("PHOTO", "Place not found: " + exception.getMessage());
                    final int statusCode = apiException.getStatusCode();
                    // TODO: Handle error with given status code.
                }
            });
        });
    }

    private void setViewTrackPanelNotClickable() {
        View view = findViewById(R.id.viewCurrentTrackPanel);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent motionEvent) {
                return true;
            }
        });
    }

    private void updateWindow() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    private void setWindowFlag(final Activity activity, final int bits, final boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void showMapLoadProgress() {
        loadMapProgressDialog = new ProgressDialog(this);
        loadMapProgressDialog.setTitle("Map Loading ...");
        loadMapProgressDialog.setMessage("Please wait...");
        loadMapProgressDialog.setCancelable(true);
        loadMapProgressDialog.show();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);
        GoogleMapUtils.settingMap(map, this);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        map.setMyLocationEnabled(true);
    }

    @Override
    public void onMapLoaded() {
        loadMapProgressDialog.dismiss();

        int accessCoarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int accessFinePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[] { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };
            ActivityCompat.requestPermissions(this, permissions, REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);
            return;
        }
        showMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String permissions[], final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ID_ACCESS_COURSE_FINE_LOCATION) {
            if (grantResults.length > 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                Log.i("MapInfo", "Permission denied!");
                this.showMyLocation();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                Log.i("MapInfo", "Permission denied!");
            }
        }
    }

    private void showMyLocation() {
        final double DISTANCE_TO_CENTER = 0.00;

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String locationProvider = GoogleMapUtils.getEnabledLocationProvider(locationManager, this);
        if (locationProvider == null) {
            return;
        }

        final long MIN_TIME_BW_UPDATES = 1000;
        final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
        Location myLocation;
        try {
            locationManager.requestLocationUpdates(
                    locationProvider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            myLocation = locationManager.getLastKnownLocation(locationProvider);
        } catch (SecurityException e) {
            Toast.makeText(this, "Show My Location Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            Log.i("MapInfo", "Show My Location Error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (myLocation != null) {
            myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude() - DISTANCE_TO_CENTER);
            myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude() - DISTANCE_TO_CENTER);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(myLatLng)
                    .zoom(15)
                    .bearing(90)
                    .tilt(40)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            Toast.makeText(this, "Location not found", Toast.LENGTH_LONG).show();
            Log.i("MapInfo", "Location not found");
        }
    }


    @Override
    public void onLocationChanged(final Location location) {
        if (CurrentTrackView.newPosition(location, map)) {
            /*route.remove(0);
            int id = getResources().getIdentifier("dir" + deletedCards, "id", getPackageName());
            View cardToDelete = findViewById(STARTING_ID_FOR_CARDS + deletedCards);
            cardToDelete.setVisibility(View.GONE);
            deletedCards++;

            cardToDelete = findViewById(STARTING_ID_FOR_CARDS + deletedCards);*/
        }
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) { }

    @Override
    public void onProviderEnabled(final String provider) { }

    @Override
    public void onProviderDisabled(final String provider) { }


    public void clickStartTrackButton(final View view) {
        final Animation animScale = AnimationUtils.loadAnimation(this, R.anim.scale_track_button);
        view.startAnimation(animScale);
        CurrentTrackView.startTrack();
    }

    public void clickPauseTrackButton(final View view) {
        final Animation animScale = AnimationUtils.loadAnimation(this, R.anim.scale_track_button);
        view.startAnimation(animScale);
        CurrentTrackView.pauseTrack();
    }

    public void clickStopTrackButton(final View view) {
        final Animation animScale = AnimationUtils.loadAnimation(this, R.anim.scale_track_button);
        view.startAnimation(animScale);
        CurrentTrackView.stopTrack(this);
    }

    public void clickCompletedTracksButton(final View view) {
        final int BUTTON_ANIMATION_DELAY = 200;
        final Animation animScale = AnimationUtils.loadAnimation(this, R.anim.scale_menu_button);
        view.startAnimation(animScale);
        view.setClickable(false);

        TimerTask changeButtonsTask = new TimerTask() {
            @Override
            public void run() {
                Intent completedTracksIntent = new Intent(MainActivity.this, CompletedTracksActivity.class);
                startActivity(completedTracksIntent);
                overridePendingTransition(R.anim.activity_slide_left_in, R.anim.activity_slide_left_out);
                view.setClickable(true);
            }
        };
        Timer changeButtonsTimer = new Timer();
        changeButtonsTimer.schedule(changeButtonsTask, BUTTON_ANIMATION_DELAY);
    }

    public void clickCurrentPositionButton(final View view) {
        final Animation animScale = AnimationUtils.loadAnimation(this, R.anim.scale_menu_button);
        view.startAnimation(animScale);
        showMyLocation();
    }


    public void setText(final TextView text, final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    public void enableButton(final ImageButton button, final boolean enable){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = map.addPolyline((PolylineOptions) values[0]);
    }
}
