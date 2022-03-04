package com.github.phantazmnetwork.neuron;

import com.github.phantazmnetwork.commons.vector.ImmutableVec3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.steanky.ethylene.codec.toml.TomlCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class MazeReader {
    public record Data(Collection<Vec3I> solids, Vec3I start, Vec3I end, Vec3I[] correctPath) {}

    public record Vec2I(int getX, int getY) {}

    public static Data readMaze(String mazeName) throws IOException {
        InputStream imageInput = MazeReader.class.getResourceAsStream(mazeName + ".png");

        Collection<Vec3I> solids = new ArrayList<>();
        if(imageInput != null) {
            BufferedImage image = ImageIO.read(imageInput);
            int width = image.getWidth();
            int height = image.getHeight();

            int[][] pixelData = loadGridFromRaster(image.getData());
            Vec2I start = null;
            Vec2I end = null;

            for(int x = 0; x < width; x++) {
                for(int y = 0; y < height; y++) {
                    int pixel = pixelData[x][y];
                    Color color = new Color(pixel);

                    //starting node
                    if(color.equals(Color.RED) && start == null) {
                        start = new Vec2I(x, y);
                    }
                    else if(color.equals(Color.GREEN) && end == null) {
                        end = new Vec2I(x, y);
                    }
                    else if(color.equals(Color.BLACK)) {
                        solids.add(new ImmutableVec3I(x, 0, y));
                    }
                }
            }

            if(start == null) {
                throw new IllegalArgumentException("Start position not found");
            }

            if(end == null) {
                throw new IllegalArgumentException("End position not found");
            }

            InputStream correctPath = MazeReader.class.getResourceAsStream(mazeName + ".toml");

            if(correctPath != null) {
                ConfigCodec configCodec = new TomlCodec();
                ConfigElement root = ConfigBridges.read(correctPath, configCodec);

                Vec3I[] correctPathElements = root.getElement("points").asList().stream().map(element -> {
                    List<ConfigElement> vectors = element.asList();
                    return new ImmutableVec3I(vectors.get(0).asNumber().intValue(), 0, vectors.get(1).asNumber()
                            .intValue());
                }).toArray(Vec3I[]::new);

                return new Data(solids, new ImmutableVec3I(start.getX, 0, start.getY),
                        new ImmutableVec3I(end.getX, 0, end.getY), correctPathElements);
            }

            throw new IllegalArgumentException("Failed to load the correct path for " + mazeName);
        }

        throw new IllegalArgumentException("Could not load maze resource " + mazeName);
    }

    /**
     * Reads a grid of pixels from the provided raster. The grid of pixels represents the image data. You can access
     * the data like this:
     *
     * <code>
     *     //the image file, which must be a PNG
     *     File imageFile = new File("C:\\some\\path\\to\\an\\image.png");
     *
     *     //load into a BufferedImage object so we can edit it
     *     BufferedImage image = ImageIO.read(imageFile);
     *
     *     //get the raster from the image, which is basically its data
     *     Raster raster = image.getData();
     *
     *     //convert the raster into a 2-dimensional int array we can easily parse
     *     int[][] grid = loadGridFromRaster(raster);
     *
     *     //gets the rgb color value for the coordinate 0, 0 (upper left corner)
     *     int rgb = grid[0][0];
     *
     *     //create a Color object from the rbg value, useful for comparing
     *     Color colorAtZeroZero = new Color(rgb);
     *
     *     //check to see if the color is black (RBG value of exactly 0, 0, 0)
     *     if(colorAtZeroZero.equals(Color.BLACK)) {
     *         System.out.println("Upper-left pixel is black!");
     *     }
     * </code>
     *
     * Coordinate [0, 0] represents the upper-left corner of the image. Larger values move down and to the right.
     * @param raster the Raster object to use
     * @return a grid of image pixels
     */
    private static int[][] loadGridFromRaster(Raster raster) {
        int width = raster.getWidth();
        int height = raster.getHeight();

        int[][] grid = new int[width][height];
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                //loads each pixel into the grid
                grid[i][j] = raster.getSample(i, j, 0) << 16 | (raster.getSample(i, j, 1) << 8) |
                        raster.getSample(i, j, 2);
            }
        }

        return grid;
    }
}
