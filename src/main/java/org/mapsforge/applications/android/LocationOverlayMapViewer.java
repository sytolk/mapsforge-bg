/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright 2013-2014 Ludwig M Brinckmann
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.applications.android;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;
import org.mapsforge.applications.android.filepicker.FilePicker;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

/**
 * MapViewer that shows current position. In the data directory of the Samples
 * project is the file berlin.gpx that can be loaded in the Android Monitor to
 * simulate location data in the center of Berlin.
 */
public class LocationOverlayMapViewer extends RenderTheme4 {
    private MyLocationOverlay myLocationOverlay;

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void createLayers() {
        super.createLayers();

        // a marker to show at the position
        Drawable drawable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? getDrawable(R.drawable.ic_maps_indicator_current_position_anim1) : getResources().getDrawable(R.drawable.ic_maps_indicator_current_position_anim1);
        Bitmap my_position = AndroidGraphicFactory.convertToBitmap(drawable);
        // since we want to keep the bitmap around, we have to increment
        // its ref count, otherwise it gets recycled automatically when it is replaced with the other colour.
        my_position.incrementRefCount();
        // create the overlay and tell it to follow the location
        this.myLocationOverlay = new MyLocationOverlay(this, this.mapView.getModel().mapViewPosition, my_position);
        this.myLocationOverlay.setSnapToLocationEnabled(true);
        mapView.getLayerManager().getLayers().add(this.myLocationOverlay);

        //fast location fix
        this.myLocationOverlay.enableMyLocation(true);

        ToggleButton snapToLocationView = (ToggleButton) findViewById(R.id.snapToLocationView);
        snapToLocationView.setVisibility(View.VISIBLE);
        snapToLocationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invertSnapToLocation();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        myLocationOverlay.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (this.myLocationOverlay != null) this.myLocationOverlay.enableMyLocation(true);
    }

    @Override
    protected void onStop() {
        if (this.myLocationOverlay != null) myLocationOverlay.disableMyLocation();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //Log.i("L onActivityResult", "requestCode:" + requestCode + " resultCode:" + resultCode + " mapFileName:" + intent.getStringExtra(FilePicker.SELECTED_FILE));
        if (requestCode == SELECT_MAP_FILE) {
            if (resultCode == RESULT_OK) {
                if (this.myLocationOverlay != null) this.myLocationOverlay.setSnapToLocationEnabled(false);
                if (intent != null && intent.getStringExtra(FilePicker.SELECTED_FILE) != null) {
                    setMapFile(intent.getStringExtra(FilePicker.SELECTED_FILE));
                    Log.i("L onActivityResult", "mapFileName:" + getMapFileName());

                    if (getMapFile() != null) {
                        try {
                            super.onCreate(null);
                        } catch (IllegalArgumentException e) { //invalid map file
                            e.printStackTrace();
                            startMapFilePicker();
                        }
                    }
                    //redrawLayers();
                } else Log.e("L onActivityResult", "intent:" + intent);
            } else if (resultCode == RESULT_CANCELED) { //&& mapFileName == null) {
                Log.e("L onActivityResult", "resultCode:" + resultCode);
                finish();
            } else Log.e("L onActivityResult", "resultCode:" + resultCode);
        } else Log.e(TAG, "requestCode:" + requestCode);
    }

    protected void invertSnapToLocation() {
        if (this.myLocationOverlay != null) {
            if (this.myLocationOverlay.isSnapToLocationEnabled()) {
                this.myLocationOverlay.setSnapToLocationEnabled(false);
            } else this.myLocationOverlay.setSnapToLocationEnabled(true);
        }
    }
}
