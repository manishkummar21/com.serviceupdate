package locationtracking.com.rapidobike;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import locationtracking.com.rapidobike.databinding.ActivityMainBinding;
import locationtracking.com.rapidobike.presenter.LiveUpdatePresenter;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private LiveUpdatePresenter presenter;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private Marker latestpos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        presenter = new LiveUpdatePresenter();
        presenter.startupdate(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter("com.locationupdate"));
    }


    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapFragment.onLowMemory();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);

    }

    private void AddMarker(LatLng initialpos) {
        MarkerOptions markerOptions = new MarkerOptions().position(initialpos);
        latestpos = mMap.addMarker(markerOptions);
    }

    private void UpdateMarker(LatLng newlatlng) {

        if (latestpos != null)
            latestpos.setPosition(newlatlng);
        else
            AddMarker(newlatlng);


        mMap.animateCamera(CameraUpdateFactory.newCameraPosition
                (new CameraPosition.Builder().target(newlatlng)
                        .zoom(15.5f).build()));


    }


    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getExtras() != null)
                UpdateMarker(new LatLng(intent.getDoubleExtra("lat", 0), intent.getDoubleExtra("lng", 0)));

        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.out.println("on new intent");
    }
}

