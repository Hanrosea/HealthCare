package com.healthcare.healthing;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

public class NapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naps);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        Marker[] markers = new Marker[6];
        InfoWindow[] infoWindows = new InfoWindow[6];

        LatLng[] positions = {
                new LatLng(35.214266, 129.022980),
                new LatLng(35.216577, 129.019604),

                new LatLng(35.228480, 128.672107),
                new LatLng(35.232722, 128.666264),
                new LatLng(35.235468, 128.671382),
                new LatLng(35.229453, 128.680165)
        };

        String[] markerTitles = {
                "어반짐 헬스장",
                "학생회관 헬스장",
                "한국체육관",
                "창원체육관",
                "맨투맨 휘트니스",
                "번피트니"
        };

        for (int i = 0; i < 6; i++) {
            markers[i] = new Marker();
            markers[i].setPosition(positions[i]);
            markers[i].setMap(naverMap);
            markers[i].setIcon(OverlayImage.fromResource(R.drawable.marking));

            infoWindows[i] = new InfoWindow();
            final int index = i; // To access within the anonymous class

            infoWindows[i].setAdapter(new InfoWindow.DefaultTextAdapter(context) {
                @NonNull
                @Override
                public CharSequence getText(@NonNull InfoWindow infoWindow) {
                    return markerTitles[index];
                }
            });

            infoWindows[i].open(markers[i]);
        }
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)){
            if(!locationSource.isActivated()){
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}