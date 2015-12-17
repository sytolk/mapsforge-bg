/*
 * Copyright © 2014 Ludwig M Brinckmann
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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import org.mapsforge.applications.android.filefilter.FilterByFileExtension;
import org.mapsforge.applications.android.filefilter.ValidMapFile;
import org.mapsforge.applications.android.filepicker.FilePicker;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.graphics.AndroidSvgBitmapStore;
import org.mapsforge.map.android.input.MapZoomControls;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.util.MapViewerTemplate;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.MapWorkerPool;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.scalebar.ImperialUnitAdapter;
import org.mapsforge.map.scalebar.MetricUnitAdapter;
import org.mapsforge.map.scalebar.NauticalUnitAdapter;

import java.io.File;
import java.io.FileFilter;

/**
 * Code common to most activities in the Samples app.
 */
public abstract class BaseMapActivity extends MapViewerTemplate implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String SETTING_DEBUG_TIMING = "debug_timing";
    public static final String SETTING_SCALE = "scale";
    public static final String SETTING_TEXTWIDTH = "textwidth";
    public static final String SETTING_WAYFILTERING = "wayfiltering";
    public static final String SETTING_WAYFILTERING_DISTANCE = "wayfiltering_distance";
    public static final String SETTING_TILECACHE_PERSISTENCE = "tilecache_persistence";
    public static final String SETTING_RENDERING_THREADS = "rendering_threads";
    public static final String SETTING_PREFERRED_LANGUAGE = "language_selection";
    //public static final String SETTING_TILECACHE_THREADING = "tilecache_threading";
    //public static final String SETTING_TILECACHE_QUEUESIZE = "tilecache_queuesize";

    public static final String SETTING_SCALEBAR = "scalebar";
    public static final String SETTING_SCALEBAR_METRIC = "metric";
    public static final String SETTING_SCALEBAR_IMPERIAL = "imperial";
    public static final String SETTING_SCALEBAR_NAUTICAL = "nautical";
    public static final String SETTING_SCALEBAR_BOTH = "both";
    public static final String SETTING_SCALEBAR_NONE = "none";

    protected static final int DIALOG_ENTER_COORDINATES = 2923878;
    protected SharedPreferences sharedPreferences;

    private String mapFileName;
    protected static final String PREFERENCE_MAP_PATH = "PREFERENCE_MAP_PATH";
    private static final FileFilter FILE_FILTER_EXTENSION_MAP = new FilterByFileExtension(".map");
    protected static final int SELECT_MAP_FILE = 0;
    protected static final String TAG = "BaseMapActivity";

    @Override
    protected int getLayoutId() {
        return R.layout.mapviewer;
    }

    @Override
    protected int getMapViewId() {
        return R.id.mapView;
    }

    @Override
    protected MapPosition getDefaultInitialPosition() {

        MapDataStore mapFile = getMapFile();
        if (mapFile != null) {

            return new MapPosition(mapFile.startPosition(), mapFile.startZoomLevel());
            //MapDatabase mapDatabase = new MapDatabase(); //new MapFile
            /*final FileOpenResult result = mapDatabase.openFile(mapFile);
            if (result.isSuccess()) {
                final MapFileInfo mapFileInfo = mapDatabase.getMapFileInfo();
                if (mapFileInfo != null && mapFileInfo.startPosition != null) {
                    return new MapPosition(mapFileInfo.startPosition, mapFileInfo.startZoomLevel);
                }
            }*/
        }

        return new MapPosition(new LatLong(42.5, 27.468), (byte) 17);
    }

    @Override
    protected void createLayers() {
        TileRendererLayer tileRendererLayer = AndroidUtil.createTileRendererLayer(this.tileCaches.get(0),
                mapView.getModel().mapViewPosition, getMapFile(), getRenderTheme(), false, true);
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);

        // needed only for samples to hook into Settings.
        setMaxTextWidthFactor();
    }

	@Override
	protected void createControls()	{
		super.createControls();
		setMapScaleBar();
	}

    @Override
    protected void createMapViews() {
        super.createMapViews();

        mapView.getMapZoomControls().setZoomControlsOrientation(MapZoomControls.Orientation.VERTICAL_IN_OUT);
        mapView.getMapZoomControls().setZoomInResource(R.drawable.zoom_control_in);
        mapView.getMapZoomControls().setZoomOutResource(R.drawable.zoom_control_out);
        mapView.getMapZoomControls().setMarginHorizontal(getResources().getDimensionPixelOffset(R.dimen.controls_margin));
        mapView.getMapZoomControls().setMarginVertical(getResources().getDimensionPixelOffset(R.dimen.controls_margin));
    }

    protected void createTileCaches() {

        boolean persistent = sharedPreferences.getBoolean(SETTING_TILECACHE_PERSISTENCE, true);

        this.tileCaches.add(AndroidUtil.createTileCache(this, getPersistableId(),
                this.mapView.getModel().displayModel.getTileSize(), this.getScreenRatio(),
                this.mapView.getModel().frameBufferModel.getOverdrawFactor(), persistent
        ));

        /*boolean threaded = sharedPreferences.getBoolean(SETTING_TILECACHE_THREADING, true);
        int queueSize = Integer.parseInt(sharedPreferences.getString(SETTING_TILECACHE_QUEUESIZE, "4"));

        this.tileCaches.add(AndroidUtil.createTileCache(this, getPersistableId(),
                this.mapView.getModel().displayModel.getTileSize(), this.getScreenRatio(),
                this.mapView.getModel().frameBufferModel.getOverdrawFactor(),
                threaded, queueSize
        ));*/
    }

    /**
     * @return the map file name to be used
     */
    protected String getMapFileName() {
        return this.mapFileName; //"germany.map";
    }

	@Override
	protected void onDestroy() {
		this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	/*
     * Settings related methods.
	 */

    @Override
    protected void createSharedPreferences() {
        super.createSharedPreferences();

        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // problem that the first call to getAll() returns nothing, apparently the
        // following two calls have to be made to read all the values correctly
        // http://stackoverflow.com/questions/9310479/how-to-iterate-through-all-keys-of-shared-preferences
        this.sharedPreferences.edit().clear();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressLint("InflateParams")
    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        switch (id) {
            case DIALOG_ENTER_COORDINATES:
                builder.setIcon(android.R.drawable.ic_menu_mylocation);
                builder.setTitle(R.string.dialog_location_title);
                final View view = factory.inflate(R.layout.dialog_enter_coordinates, null);
                builder.setView(view);
                builder.setPositiveButton(R.string.okbutton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double lat = Double.parseDouble(((EditText) view.findViewById(R.id.latitude)).getText()
                                .toString());
                        double lon = Double.parseDouble(((EditText) view.findViewById(R.id.longitude)).getText()
                                .toString());
                        byte zoomLevel = (byte) ((((SeekBar) view.findViewById(R.id.zoomlevel)).getProgress()) +
                                BaseMapActivity.this.mapView.getModel().mapViewPosition.getZoomLevelMin());

                        BaseMapActivity.this.mapView.getModel().mapViewPosition.setMapPosition(
                                new MapPosition(new LatLong(lat, lon), zoomLevel));
                    }
                });
                builder.setNegativeButton(R.string.cancelbutton, null);
                return builder.create();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        if (item.getItemId() == R.id.menu_preferences) {
            intent = new Intent(this, Settings.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            if (renderThemeStyleMenu != null) {
                intent.putExtra(Settings.RENDERTHEME_MENU, renderThemeStyleMenu);
            }
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_position_enter_coordinates) {
            showDialog(DIALOG_ENTER_COORDINATES);
        } else if (item.getItemId() == R.id.menu_svgclear) {
            AndroidSvgBitmapStore.clear();
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPrepareDialog(int id, final Dialog dialog) {
        if (id == this.DIALOG_ENTER_COORDINATES) {
            MapViewPosition currentPosition = BaseMapActivity.this.mapView.getModel().mapViewPosition;
            LatLong currentCenter = currentPosition.getCenter();
            EditText editText = (EditText) dialog.findViewById(R.id.latitude);
            editText.setText(Double.toString(currentCenter.latitude));
            editText = (EditText) dialog.findViewById(R.id.longitude);
            editText.setText(Double.toString(currentCenter.longitude));
            SeekBar zoomlevel = (SeekBar) dialog.findViewById(R.id.zoomlevel);
            zoomlevel.setMax(currentPosition.getZoomLevelMax() - currentPosition.getZoomLevelMin());
            zoomlevel.setProgress(BaseMapActivity.this.mapView.getModel().mapViewPosition.getZoomLevel()
                    - currentPosition.getZoomLevelMin());
            final TextView textView = (TextView) dialog.findViewById(R.id.zoomlevelValue);
            textView.setText(String.valueOf(zoomlevel.getProgress()));
            zoomlevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    textView.setText(String.valueOf(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                    // nothing
                }

                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                    // nothing
                }
            });
        } else {
            super.onPrepareDialog(id, dialog);
        }
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
		if (SETTING_SCALE.equals(key)) {
			this.mapView.getModel().displayModel.setUserScaleFactor(DisplayModel.getDefaultUserScaleFactor());
			Log.d(TAG, "Tilesize now " + this.mapView.getModel().displayModel.getTileSize());
			AndroidUtil.restartActivity(this);
		}
		if (SETTING_PREFERRED_LANGUAGE.equals(key)) {
			String language = preferences.getString(SETTING_PREFERRED_LANGUAGE, null);
			Log.d(TAG, "Preferred language now " + language);
			AndroidUtil.restartActivity(this);
		}
		if (SETTING_TILECACHE_PERSISTENCE.equals(key)) {
			if (!preferences.getBoolean(SETTING_TILECACHE_PERSISTENCE, false)) {
				Log.d(TAG, "Purging tile caches");
				for (TileCache tileCache : this.tileCaches) {
					tileCache.purge();
				}
			}
			AndroidUtil.restartActivity(this);
		}
		if (SETTING_TEXTWIDTH.equals(key)) {
			AndroidUtil.restartActivity(this);
		}
		if (SETTING_SCALEBAR.equals(key)) {
			setMapScaleBar();
		}
		if (SETTING_DEBUG_TIMING.equals(key)) {
			MapWorkerPool.DEBUG_TIMING = preferences.getBoolean(SETTING_DEBUG_TIMING, false);
		}
		if (SETTING_RENDERING_THREADS.equals(key)) {
			MapWorkerPool.NUMBER_OF_THREADS = Integer.parseInt(preferences.getString(SETTING_RENDERING_THREADS, Integer.toString(MapWorkerPool.DEFAULT_NUMBER_OF_THREADS)));
			AndroidUtil.restartActivity(this);
		}
		if (SETTING_WAYFILTERING_DISTANCE.equals(key) ||
				SETTING_WAYFILTERING.equals(key)) {
			MapFile.wayFilterEnabled = preferences.getBoolean(SETTING_WAYFILTERING, true);
			if (MapFile.wayFilterEnabled) {
				MapFile.wayFilterDistance = Integer.parseInt(preferences.getString(SETTING_WAYFILTERING_DISTANCE, "20"));
			}
		}
	}

    /**
     * Sets the scale bar from preferences.
     */
    protected void setMapScaleBar() {
        String value = this.sharedPreferences.getString(SETTING_SCALEBAR, SETTING_SCALEBAR_BOTH);

        if (SETTING_SCALEBAR_NONE.equals(value)) {
            AndroidUtil.setMapScaleBar(this.mapView, null, null);
        } else {
            if (SETTING_SCALEBAR_BOTH.equals(value)) {
                AndroidUtil.setMapScaleBar(this.mapView, MetricUnitAdapter.INSTANCE, ImperialUnitAdapter.INSTANCE);
            } else if (SETTING_SCALEBAR_METRIC.equals(value)) {
                AndroidUtil.setMapScaleBar(this.mapView, MetricUnitAdapter.INSTANCE, null);
            } else if (SETTING_SCALEBAR_IMPERIAL.equals(value)) {
                AndroidUtil.setMapScaleBar(this.mapView, ImperialUnitAdapter.INSTANCE, null);
            } else if (SETTING_SCALEBAR_NAUTICAL.equals(value)) {
                AndroidUtil.setMapScaleBar(this.mapView, NauticalUnitAdapter.INSTANCE, null);
            }
        }
    }

    /**
     * sets the value for breaking line text in labels.
     */
    protected void setMaxTextWidthFactor() {
        mapView.getModel().displayModel.setMaxTextWidthFactor(Float.valueOf(sharedPreferences.getString(SETTING_TEXTWIDTH, "0.7")));
    }

    /**
     * STANIMIR
     *
     * @param mapFile
     */
    protected void setMapFile(String mapFile) {
        if (mapFile != null) { //this.mapFileName == null
            File file = new File(mapFile);
            if (file.exists()) {
                this.mapFileName = mapFile;
                if (preferencesFacade != null) {
                    preferencesFacade.putString(PREFERENCE_MAP_PATH, getMapFileName());
                    preferencesFacade.save();
                }
                Log.i("setMapFile", "setMapFile map file to:" + mapFile);
            } else Log.e("setMapFile", "Map file not exist:" + mapFile);
        } else Log.e("setMapFile", "Map file=null");
    }

    /**
     * @return a map file
     */
    protected MapDataStore getMapFile() {
        if (mapFileName == null && preferencesFacade != null)
            mapFileName = preferencesFacade.getString(PREFERENCE_MAP_PATH, null);
        if (mapFileName != null) {
            File file = new File(mapFileName); //Environment.getExternalStorageDirectory(), mapFileName);
            //Log.i("TAG", "Map file is " + file.getAbsolutePath());
            if (file.exists()) {
                return new MapFile(file);
            } else {
                Log.e("TAG", "Map file not exist " + file.getAbsolutePath());
                startMapFilePicker();
            }
        } else {
            Log.i("TAG", "Map file is NULL");
            startMapFilePicker();
        }
        return null;
    }

    /**
     * Sets all file filters and starts the FilePicker to select a map file.
     */
    protected synchronized void startMapFilePicker() {
        FilePicker.setFileDisplayFilter(FILE_FILTER_EXTENSION_MAP);
        FilePicker.setFileSelectFilter(new ValidMapFile());
        Intent fileIntent = new Intent(this, FilePicker.class);
        fileIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(fileIntent, SELECT_MAP_FILE);
    }
}
