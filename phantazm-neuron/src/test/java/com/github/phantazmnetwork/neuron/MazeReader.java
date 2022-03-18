package com.github.phantazmnetwork.neuron;

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

/**
 * Static utility class for reading data from mazes, used for testing pathfinding.
 */
public final class MazeReader {
    /**
     * Represents maze data. Contains a collection of vectors where solid (walls) are located, the starting vector, the
     * ending (destination) vector, and an ordered array of vectors representing the correct path.
     */
    public record Data(Collection<Vec3I> solids, Vec3I start, Vec3I end, Vec3I[] correctPath) {}

    /**
     * Represents a 2D integer vector. Likely temporary, will implement in phantazm-commons eventually.
     */
    public record Vec2I(int getX, int getY) {}

    /**
     * Reads some maze data from the given name. Will load maze data from a file {@code mazeName}.png in the same
     * package as this class, and optimal path data from a file named {@code mazeName}.toml in the same package as this
     * class.
     * @param mazeName the name of the maze
     * @return the data representing the maze (walls, start, and end position)
     * @throws IOException if the appropriate resources cannot be found
     * @throws IllegalArgumentException if some necessary data is missing
     */
    public static Data readMaze(String mazeName) throws IOException {
        InputStream imageInput = MazeReader.class.getResourceAsStream(mazeName + ".png");

        Collection<Vec3I> solids = new ArrayList<>(); //list of wall nodes
        if(imageInput != null) {
            try(imageInput) {
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

                        if(color.equals(Color.RED) && start == null) {
                            start = new Vec2I(x, y); //starting node is red
                        }
                        else if(color.equals(Color.GREEN) && end == null) {
                            end = new Vec2I(x, y); //starting node is green
                        }
                        else if(color.equals(Color.BLACK)) {
                            solids.add(Vec3I.of(x, 0, y)); //wall nodes are black
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
                    try(correctPath) {
                        ConfigCodec configCodec = new TomlCodec();
                        ConfigElement root = ConfigBridges.read(correctPath, configCodec);

                        //converts list of points to Vec3I array
                        Vec3I[] correctPathElements = root.getElement("points").asList().stream().map(element -> {
                            List<ConfigElement> vectors = element.asList();
                            return Vec3I.of(vectors.get(0).asNumber().intValue(), 0, vectors.get(1).asNumber()
                                    .intValue());
                        }).toArray(Vec3I[]::new);

                        return new Data(solids, Vec3I.of(start.getX, 0, start.getY),
                                Vec3I.of(end.getX, 0, end.getY), correctPathElements);
                    }
                }

                throw new IllegalArgumentException("Failed to load the correct path for " + mazeName);
            }
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
