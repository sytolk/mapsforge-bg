/*
 * Copyright 2010, 2011, 2012 mapsforge.org
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
package org.mapsforge.applications.android.filefilter;

import android.util.Log;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.reader.header.MapFileException;

import java.io.File;
import java.io.FileFilter;

/**
 * Accepts all valid map files.
 */
public final class ValidMapFile implements FileFilter {
    //private FileOpenResult fileOpenResult;

    @Override
    public boolean accept(File mapFile) {
        try {
            MapFile mapDataStore = new MapFile(mapFile);
            return true;
        } catch (MapFileException e) {
            Log.e("ValidMapFile", "MapFileException:", e);
        }
        return false;
        /*MapDatabase mapDatabase = new MapDatabase();
        this.fileOpenResult = mapDatabase.openFile(mapFile);
		mapDatabase.closeFile();
		return this.fileOpenResult.isSuccess();*/
    }

    /*@Override
    public FileOpenResult getFileOpenResult() {
        return this.fileOpenResult;
    }*/
}
