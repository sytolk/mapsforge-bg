// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;

import java.io.IOException;


/**
 * A simple test verifying the operation of the xml reader and writer tasks.
 *
 */
public class XmlReaderMapsForge extends AbstractDataTest {

    /**
     * A basic test reading and writing an osm file testing both reader and
     * writer tasks.
     *
     * @throws java.io.IOException if any file operations fail.
     */
    @Test
    public void readBulgariaMap() throws IOException {

        // Run the pipeline.
        Osmosis.run(
                new String[]{
                        "-plugin",
                        "org.mapsforge.map.writer.osmosis.MapFileWriterPluginLoader",
                        "--read-xml-0.6",
                        "file=/home/stanimir/Downloads/bulgaria-latest.osm",
                        "--mapfile-writer",
                        "file=/home/stanimir/Downloads/bulgaria.map",
                        "type=hd",
                        "map-start-position=42.5,27.468"
                }
        );
    }

    @Test
    public void exportBulgariaMap() throws IOException {

        // Run the pipeline.
        Osmosis.run(
                new String[]{
                        /*"-plugin",
                        "org.mapsforge.map.writer.osmosis.MapFileWriterPluginLoader",*/
                        "--read-xml",
                        "file=/home/stanimir/Downloads/bulgaria-latest.osm",
                        "--mapfile-writer",
                        "file=/home/stanimir/Downloads/bulgaria.map",
                        "bbox=39.86759,20.97290,45.25169,29.87183",
                        "type=hd",
                        "map-start-zoom=17",
                        "map-start-position=42.5,27.468"
                }
        );
    }
}
