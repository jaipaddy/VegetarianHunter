package com.jpadmanabhan.vegetarianhunter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.*;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    // Progress Dialog Object
    private ProgressDialog prgDialog;
    private EditText addressText;
    private ListView listView;
    private final ArrayList<Restaurant> restaurantArrayList = new ArrayList<>();
    private CustomAdapter customAdapter;
    private static AsyncHttpClient client;
    // location params
    private static String latitude;
    private static String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Obtain GPS location
        obtainLocation();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Action bar Toolbar
        Toolbar actionBarToolbar = (Toolbar)findViewById(R.id.my_action_bar_toolbar) ;
        setSupportActionBar(actionBarToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        //Get AdMob to show in the app
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //Set up view
        addressText = (EditText)findViewById(R.id.addressText);
        //Ensure empty text
        addressText.setText("");
        // Keyboard hide and Search
        addressText.setImeActionLabel("Search", EditorInfo.IME_ACTION_GO);
        addressText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                hideKeyboard();
                findVegRestaurants();
                return true;
            }
        });
        listView = (ListView)findViewById(R.id.listView);
        // Clear button
        ImageButton clearButton = (ImageButton) findViewById(R.id.delete_Text);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressText.setText("");
            }
        });
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        //Adapter to display list
        customAdapter = new CustomAdapter(restaurantArrayList, this);

        //Ensure portrait mode only
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //Fetch list based on current location - default behavior
        findVegRestaurants();
        //findVegRestaurants();
        //Hide keyboard when not activated by text field
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * On selecting action bar icons
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // search action
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        findVegRestaurants();
                        return true;
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Locate current location
    private void obtainLocation() {
        final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
//        final List<String> lProviders = locationManager.getProviders(false);
//        for(int i=0; i<lProviders.size(); i++){
//            Log.d("jai", lProviders.get(i));
//        }
        final String locationManagerBestProvider = locationManager.getBestProvider(criteria, true); // null
        final LocationListener locationListener = new MyLocationListener();

        // If there are no location providers available
        if (locationManagerBestProvider == null){
            // Closes the activity
            Toast.makeText(MainActivity.this, "Is Location access permission on?", Toast.LENGTH_LONG).show();
//            finish();
        }
        // Location services is enabled
        else{
            locationManager.requestLocationUpdates(locationManagerBestProvider, 5000, 5, locationListener);
        }
        if(latitude.isEmpty() || longitude.isEmpty()){
            final Location lastKnownLocation = locationManager.getLastKnownLocation(locationManagerBestProvider);
            if(lastKnownLocation != null) {
                longitude = String.valueOf(lastKnownLocation.getLongitude());
                latitude = String.valueOf(lastKnownLocation.getLatitude());
            }
        }
    }


    //Hide the keyboard after typing address
    private void hideKeyboard() {
        try  {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            //noinspection ConstantConditions
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    //On Find button click
    void findVegRestaurants(){
        String fullUrl;
        String address =  addressText.getText().toString();
        if(address.isEmpty() || address.equals("")){
            String lat_long =  latitude.concat(","+longitude);
            //Read URL from strings.xml
            fullUrl = getResources().getString(R.string.POSITION_URL) + lat_long;
        }else{
            fullUrl = getResources().getString(R.string.ADDRESS_URL) + address;
        }
        //Clear arrayList and notify adapter for each button click to delete history
        restaurantArrayList.clear();
        customAdapter.notifyDataSetChanged();
        invokeWS(fullUrl);
    }

    //Singleton client
    private AsyncHttpClient getHttpClient(){
        if(client == null){
            client = new AsyncHttpClient();
        }
        return client;
    }

    /**
     * Method that performs RESTful webservice invocations
     */
    void invokeWS(String url){
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        //Log.d("jai", "Full URL = "+url);
         getHttpClient().addHeader("User-Agent", "Veg Finder Android App/v4.0");
         getHttpClient().get(url, new AsyncHttpResponseHandler() {
             @Override
             public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                 
             }

             @Override
             public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {

             }

             // When the response returned by REST has Http response code '200'
             @Override
             public void onSuccess(int i, Header[] headers, byte[] bytes) {
                 // Dismiss Progress Dialog
                 try {
                     if (prgDialog != null && prgDialog.isShowing()) {
                         prgDialog.dismiss();
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 JSONArray jsonArray;
                 try {
                     // JSON Object
                     final String string = new String(bytes);
                     JSONObject response = new JSONObject(string);
                     if (response.getString("entry_count").equals(String.valueOf(0))) {
                         Toast.makeText(getApplicationContext(), "Sorry, no results to display", Toast.LENGTH_LONG).show();
                     } else {
                         jsonArray = parseJsonArray(response);
                         if (jsonArray.length() >= 1) {
                             // Display list
                             displayListAndMapOnSelect();
                         }
                         // Else display error message
                         else {
                             // errorMsg.setText(obj.getString("error_msg"));
                             Toast.makeText(getApplicationContext(), "Sorry, no results to display", Toast.LENGTH_LONG).show();
                         }
                     }

                 } catch (JSONException e) {
                     Toast.makeText(getApplicationContext(), "Either the address is invalid or Server error occurred", Toast.LENGTH_LONG).show();
                     e.printStackTrace();

                 }
             }

             private JSONArray parseJsonArray(JSONObject response) throws JSONException {
                 final JSONArray jsonArray = response.getJSONArray("entries");
                 for (int j = 0; j < jsonArray.length(); j++) {
                     final JSONObject data = jsonArray.getJSONObject(j);
                     final Restaurant restaurant = new Restaurant();
                     if (data.has("sortable_name")) restaurant.setName(data.getString("sortable_name"));
                     if (data.has("short_description")) restaurant.setDescription(data.getString("short_description"));
                     if (data.has("distance")) restaurant.setDistance(data.getString("distance").substring(0, 5));
                     if (data.has("city")) restaurant.setCity(data.getString("city"));
                     if (data.has("phone"))  restaurant.setPhone(data.getString("phone"));
                     if (data.has("weighted_rating")) restaurant.setRating(data.getString("weighted_rating"));
                     if (data.has("address1")) restaurant.setAddress(data.getString("address1"));

                     //Add to the ArrayList of Restaurant
                     restaurantArrayList.add(restaurant);
                 }
                 return jsonArray;
             }

             private void displayListAndMapOnSelect() {
                 listView.setAdapter(customAdapter);
                 listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                     @Override
                                                     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                         final Restaurant selected = (Restaurant) listView.getAdapter().getItem(listView.getCheckedItemPosition());
                                                         if (selected.getAddress().equals(""))
                                                             showMap(selected.getCity());
                                                         else
                                                             showMap(selected.getAddress() + " " + selected.getCity());
                                                     }
                                                 }
                 );
             }


             // When the response returned by REST has Http response code other than '200'
             @Override
             public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable error) {
                 // Hide Progress Dialog
                 prgDialog.hide();
                 if (addressText.getText().toString().isEmpty() && (latitude.isEmpty() || longitude.isEmpty())) {
                     Toast.makeText(getApplicationContext(), "Is Location access permission on?", Toast.LENGTH_SHORT).show();
                 }
                 // When Http response code is '404'
                 else if (statusCode == 404) {
                     Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_SHORT).show();
                 }
                 // When Http response code is '500'
                 else if (statusCode == 500) {
                     Toast.makeText(getApplicationContext(), "Oops! something went wrong at server end", Toast.LENGTH_SHORT).show();
                 }
                 // When Http response code other than 404, 500
                 else {
                    // Log.d("jai", "statusCode = "+String.valueOf(statusCode));
                     Toast.makeText(getApplicationContext(), "Mobile data is turned off or remote server is not responding", Toast.LENGTH_LONG).show();
                 }
             }

         });
    }

    //Open map
    void showMap(String dest_address) {
        String encodedQuery = Uri.encode(dest_address);
//        Log.d("jai", encodedQuery);
        Uri geoLocation =  Uri.parse("geo:0,0?q="+encodedQuery);
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }else{
            //Open Maps in browser
            Toast.makeText(getApplicationContext(), "Could not open in Google maps",
                    Toast.LENGTH_LONG).show();
            geoLocation =  Uri.parse("http:/a/maps.google.com/maps?"
                + "saddr="+ latitude+","+longitude + "&daddr="+dest_address);
            intent.setData(geoLocation);
            startActivity(intent);
        }
    }


     public final class MyLocationListener implements LocationListener {

         public MyLocationListener(){
             latitude = "";
             longitude = "";
         }

        @Override
        public void onLocationChanged(Location location) {
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
//            Log.d("jai", "lat = " + latitude + ", long =" + latitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

}

