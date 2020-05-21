package com.release.android.tinda;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.release.android.tinda.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.release.android.tinda.Constants.ERROR_DIALOG_REQUEST;
import static com.release.android.tinda.Constants.PERMISSION_REQUEST_ACCESS_FINE_LOCATION;
import static com.release.android.tinda.Constants.PERMISSION_REQUEST_ENABLE_GPS;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAuth firebaseAuth;
    private GoogleApiClient mGoogleApiClient;
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private boolean locationPermissionGranted = false;
    private RecyclerView recyclerView,notificationRecyclerView;
    private ArrayList<String> businessList,genreList,notificationContentList,notificationImageList,notificationTimeList,notificationGenreList,notificationIdList;
    private EditText search;
    private String currentLatitude,currentLongitude,uid,completeNameText;
    private ImageView menu,notification;
    private DrawerLayout drawerLayout;
    private ScrollView scrollView;
    private DatabaseReference rootRef;
    private MainPageAdapter adapter;
    private NavigationView navigationView;
    private ConstraintLayout headerLayout,notificationLayout;
    private TextView headerName,headerLogout,headerCart,headerPrivacy,followed;
    private LocationManager locationManager;
    private Criteria criteria;
    private String bestProvider;
    private Bundle savedInstanceState;
    private int mapState = 0,notificationState = 0;
    private ProgressDialog progressDialog;
    private NotificationPageAdapter notificationAdapter;
    private TextView noNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //load progress dialog//
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading main screen...");
        progressDialog.show();
        //----------------------------------------//

        firebaseAuth = FirebaseAuth.getInstance();
        this.savedInstanceState = savedInstanceState;
        checkIfLoggedIn();

    }

    private void checkIfLoggedIn() {

        //this is for checking if user is already logged in or not//

        if(firebaseAuth.getCurrentUser() != null) {

            //--------setup MapView---------------------------------------------//

            Log.d("NULL_CHECK","NOT NULL!");

            mapState = 1;
            mapView = (MapView) findViewById(R.id.mapViewMain);
            Bundle mapViewBundle = null;
            if (savedInstanceState != null) {
                mapViewBundle = savedInstanceState.getBundle("MapViewBundleKey");
            }

            mapView.onCreate(mapViewBundle);
            mapView.getMapAsync(this);

            //------------------------------------------------------------------//


        }else{

            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();

        }

        ///////////////////////////////////////////////////////////

    }


    private void setupViews() {

        businessList = new ArrayList<>();
        genreList = new ArrayList<>();

        notificationContentList = new ArrayList<>();
        notificationGenreList = new ArrayList<>();
        notificationImageList = new ArrayList<>();
        notificationIdList = new ArrayList<>();
        notificationTimeList = new ArrayList<>();

        noNotification = (TextView) findViewById(R.id.textViewNotificationNoPost);

        notificationLayout = (ConstraintLayout) findViewById(R.id.constraintLayoutMainPopup);
        notification = (ImageView) findViewById(R.id.imageViewMainNotification);

        search = (EditText) findViewById(R.id.editTextMainSearch);
        search.clearFocus();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayoutMain);

        menu = (ImageView) findViewById(R.id.imageViewMainMenu);

        //firebase
        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        uid = firebaseAuth.getCurrentUser().getUid();

        //google
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        scrollView = (ScrollView) findViewById(R.id.scrollViewMain);

        mGoogleApiClient.connect();


        setupHeaderView();

    }


    private void setupHeaderView() {

        navigationView = (NavigationView) findViewById(R.id.navigationViewMain);
        headerLayout = (ConstraintLayout) navigationView.getHeaderView(0);
        headerName = (TextView) headerLayout.findViewById(R.id.textViewMainHeaderName);
        headerLogout = (TextView) headerLayout.findViewById(R.id.textViewMainHeaderLogout);
        headerCart = (TextView) headerLayout.findViewById(R.id.textViewMainHeaderCart);
        headerPrivacy = (TextView) headerLayout.findViewById(R.id.textViewMainHeaderPrivacy);
        followed = (TextView) headerLayout.findViewById(R.id.textViewMainHeaderFollowed);

        loadPersonalInformation();

    }

    private void loadPersonalInformation() {

        DatabaseReference personalRef = rootRef.child("Users").child(uid);

        personalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                completeNameText = dataSnapshot.child("completeName").getValue().toString();
                headerName.setText("Hi, " + completeNameText + "!");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadNotification(){

        final DatabaseReference notificationRef = rootRef.child("Notification").child(uid);
        notificationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    notificationIdList.add(ds.child("Business_id").getValue().toString());
                    notificationContentList.add(ds.child("Business_name").getValue().toString());
                    notificationTimeList.add(ds.child("timestamp").getValue().toString());
                    notificationImageList.add(ds.child("Business_image").getValue().toString());
                    notificationGenreList.add(ds.child("Business_genre").getValue().toString());

                    notificationAdapter.notifyDataSetChanged();

                }

                if(notificationIdList.size() == 0){
                    noNotification.setVisibility(View.VISIBLE);
                }else{
                    noNotification.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setupRecyclerView() {

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewMain);
        adapter = new MainPageAdapter(businessList,MainActivity.this,currentLatitude,currentLongitude);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {

                recyclerView.scrollToPosition(businessList.size()-1);

            }
        });

        notificationRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewNotification);
        notificationAdapter = new NotificationPageAdapter(notificationContentList,notificationTimeList,notificationIdList,notificationImageList,
                notificationGenreList,this,currentLatitude,currentLongitude);
        notificationRecyclerView.setAdapter(notificationAdapter);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private void loadGenreList() {

        //this function is for getting all the genres from the database//

        DatabaseReference genreRef = rootRef.child("Businesses");
        genreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    genreList.add(ds.getKey());

                    if(businessList.size() < 5) {
                        businessList.add(ds.getKey());
                        adapter.notifyDataSetChanged();
                    }
                }

                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void activateListeners() {

        headerLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Signing out...");
                progressDialog.show();

                firebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);

                mGoogleApiClient.disconnect();
                mGoogleApiClient.connect();

                progressDialog.dismiss();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {

                scrollView.post(new Runnable() {
                    @Override
                    public void run() {

                        businessList.clear();
                        adapter.notifyDataSetChanged();

                        loadCloseGenre(editable.toString());
                        scrollView.fullScroll(View.FOCUS_DOWN);
                        search.requestFocus();
                    }
                });

            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                drawerLayout.openDrawer(Gravity.START);

            }
        });

        headerCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this,CartActivity.class);
                intent.putExtra("currentLatitude",currentLatitude);
                intent.putExtra("currentLongitude",currentLongitude);
                startActivity(intent);

            }
        });

        headerPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String privacyUrl = "https://tinda.flycricket.io/privacy.html";

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl));
                startActivity(Intent.createChooser(intent,"Choose browser"));

            }
        });

        followed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this,FollowingActivity.class);
                intent.putExtra("currentLatitude",currentLatitude);
                intent.putExtra("currentLongitude",currentLongitude);
                startActivity(intent);

            }
        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(notificationState == 0) {
                    notificationLayout.setVisibility(View.VISIBLE);
                    notificationState = 1;
                }else{
                    notificationLayout.setVisibility(View.GONE);
                    notificationState = 0;
                }

            }
        });


    }

    private void loadCloseGenre(String searchWord) {

        //this function is for loading the genres that are similar to the searched business//

        for(String genreWord : genreList){

            if(genreWord.toLowerCase().contains(searchWord.toLowerCase())){

                businessList.add(genreWord);
                adapter.notifyDataSetChanged();

            }

        }

    }



    //--------------------The following functions are for permission requests-----------------//


    private void getLastLocation() {

        //this function is for getting long and lat values of current position

        //location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d("LOCATION","Inside");
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                if(task.isSuccessful()){

                    Location location = task.getResult();

                    if(location != null) {

                        LatLng latLngLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        currentLatitude = Double.toString(location.getLatitude());
                        currentLongitude = Double.toString(location.getLongitude());

                        currentLocation = location;

                        //activateLocationChangeManager();

                        setupViews();
                        setupRecyclerView();
                        loadGenreList();
                        activateListeners();
                        editMapView(latLngLocation);
                        loadNotification();



                    }else{

                        locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
                        criteria = new Criteria();
                        bestProvider = String.valueOf(locationManager.getBestProvider(criteria,true));

                        locationManager.requestLocationUpdates(bestProvider, 0, 0, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {

                                currentLatitude = Double.toString(location.getLatitude());
                                currentLongitude = Double.toString(location.getLongitude());

                                currentLocation = location;

                                locationManager.removeUpdates(this);
                                setupViews();
                                setupRecyclerView();
                                loadGenreList();
                                activateListeners();
                                editMapView(new LatLng(location.getLatitude(),location.getLongitude()));
                                loadNotification();

                            }

                            @Override
                            public void onStatusChanged(String s, int i, Bundle bundle) {

                            }

                            @Override
                            public void onProviderEnabled(String s) {

                            }

                            @Override
                            public void onProviderDisabled(String s) {

                            }
                        });
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void activateLocationChangeManager() {

        locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        bestProvider = String.valueOf(locationManager.getBestProvider(criteria,true));

        locationManager.requestLocationUpdates(bestProvider, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                currentLatitude = Double.toString(location.getLatitude());
                currentLongitude = Double.toString(location.getLongitude());

                currentLocation = location;

                locationManager.removeUpdates(this);
                editMapView(new LatLng(location.getLatitude(),location.getLongitude()));

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {



            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });

    }


    private boolean checkMapServices(){

        //this function is for checking permission, first for availability of Google services, then for gps enabled

        if(isServicesOk()){
            if(isMapsEnabled()){
                return true;
            }
        }

        return false;
    }



    public boolean isServicesOk(){

        //function to check if google play services is downloaded for map use

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if(available == ConnectionResult.SUCCESS){
            return true;
        }else{
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this,available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        return false;

    }


    public boolean isMapsEnabled(){

        //function to check if gps is enabled for map use

        String provider = Settings.Secure.getString(getContentResolver(),Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.contains("gps")){
            buildAlertMessageNoGps();
            return false;
        }

        /*
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            buildAlertMessageNoGps();
            return false;
        }
        */

        return true;

    }

    private void buildAlertMessageNoGps(){

        //function is triggered when gps is not enabled. Its for prompting the user to activate GPS

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent,PERMISSION_REQUEST_ENABLE_GPS);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void getLocationPermission(){

        //this function is triggered to explicity ask for another permission for enabling gps

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationPermissionGranted = true;

            getLastLocation();

        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //this function is used to check if triggered explicit permission for enabling gps is accepted
        locationPermissionGranted = false;
        switch (requestCode){
            case PERMISSION_REQUEST_ACCESS_FINE_LOCATION : {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    locationPermissionGranted = true;

                    getLastLocation();

                }
            }
        }

    }


    private void editMapView(LatLng currentLocation) {

        //this function is called to move the camera and add a marker to passed LatLng location

        googleMap.addMarker(new MarkerOptions().position(currentLocation));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,10));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15.5f));
        googleMap.getUiSettings().setAllGesturesEnabled(false);

    }


    //--------------------------------PERMISSION FOR GPS ENDS HERE----------------------------------//

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //this function is to receive the results of dialogs for enabling gps

        switch (requestCode){
            case PERMISSION_REQUEST_ENABLE_GPS : {
                if(!locationPermissionGranted){
                    getLocationPermission();
                }
            }
        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        mapView.onStart();

        //check if user is already logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }

        //connect googleApiClient for google logout

    }

    @Override
    public void onMapReady(GoogleMap mgoogleMap) {

        //this function is for setting up the google map found in mapview

        googleMap = mgoogleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);



        //this is for setting up the map only after the map is loaded//
        if(checkMapServices()){

            if(locationPermissionGranted){

                getLastLocation();

            }else{
                getLocationPermission();
            }
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mapState == 1)
            mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mapState == 1)
            mapView.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();

        if(mapState == 1)
            mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle("MapViewBundleKey");
        if(mapViewBundle == null){
            mapViewBundle = new Bundle();
            outState.putBundle("MapViewBundleKey",mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);

    }


}
