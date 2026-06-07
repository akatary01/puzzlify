package com.puzzlify;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.puzzlify.Puzzle.Cell;
import com.puzzlify.Puzzle.Edge;
import com.puzzlify.Puzzle.Pair;
import com.puzzlify.Puzzle.Pixel;
import static com.puzzlify.Puzzle.constructGrid;
import static com.puzzlify.Puzzle.cut;
import static com.puzzlify.Utils.isBorderPixel;

public class Api {
    public static void main(String[] args) throws IOException {
        // test image
        File file = new File("../api/src/main/resources/billy_nips.png");
        BufferedImage image = ImageIO.read(file);
        BufferedImage cutImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        
        final int rows = 5;
        final int cols = 2;
        int[][] colors = {
            { 0x80FF0000, 0x8000FF00, 0x800000FF },  // Row 0: Red, Green, Blue
            { 0x80FFFF00, 0x8000FFFF, 0x80FF00FF },  // Row 1: Yellow, Cyan, Magenta
            { 0x80FF8C00, 0x80800080, 0x8000FF80}   // Row 2: Orange, Purple, Lime
        };
        final Cell[][] grid = constructGrid(image, rows, cols, false);
        final Cell[][] puzzle = constructGrid(image, rows, cols, true);
        final HashMap<Integer, Pair<Integer, Integer>> merged = new HashMap<>();

        // test that the grid outputs the correct image with the center cell all white
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // skip transparent cells
                if (puzzle[i][j].size() == 0) { continue; }

                final Pair<Pixel, Pixel> boundA = grid[i][j].boundingBox();
                System.out.println(String.format("processing cellA[%d][%d]: %s, %s", i, j, grid[i][j].size(), boundA));

                final int a = image.getHeight() / rows;
                final int b = image.getWidth() / cols;
                System.out.println(String.format("(a, b): %d, %d", a, b));

                final Pixel topLeft = boundA.first();
                final Pixel bottomRight = boundA.second();
                
                // we do not process the last set of horizontal edges
                if (i != rows - 1) { 
                    final Edge edgeH = new Edge(topLeft.translate(0, a - 1), bottomRight);
                    System.out.println(String.format("edgeH: %s", edgeH));
                    // horizontal cut and update cells
                    final double cutSize = 0.125*b;
                    Puzzle.Pair<Cell, Optional<Cell>> cutH = cut(edgeH, puzzle[i][j], puzzle[i + 1][j], cutSize);
                    puzzle[i][j] = cutH.first();
                    System.out.println(String.format("cutH cellA: %s, %s", puzzle[i][j].size(), puzzle[i][j].boundingBox()));
                    puzzle[i + 1][j] = cutH.second().orElse(null);
                    if (cutH.second().isEmpty()) {
                        System.err.println("cutH cellB is merged");
                        merged.put((i + 1) * cols + j, new Pair<>(i, j));
                    } else {
                        System.out.println(String.format("cutH cellB: %s, %s", puzzle[i + 1][j].size(), puzzle[i + 1][j].boundingBox()));
                    }
                }
                
                // we do not process the last set of vertical edges
                if (j != cols - 1) {
                    final Edge edgeV = new Edge(topLeft.translate(b - 1, 0), bottomRight);
                    System.out.println(String.format("edgeV: %s", edgeV));
                    //vertical cut and update cells
                    final double cutSize = 0.125*a;
                    Puzzle.Pair<Cell, Optional<Cell>> cutV = cut(edgeV, puzzle[i][j], puzzle[i][j + 1], cutSize);
                    puzzle[i][j] = cutV.first();
                    System.out.println(String.format("cutV cellA: %s, %s", puzzle[i][j].size(), puzzle[i][j].boundingBox()));
                    puzzle[i][j + 1] = cutV.second().orElse(null);
                    if (cutV.second().isEmpty()) {
                        System.err.println("cutV cellB is merged");
                        merged.put(i * cols + (j + 1), new Pair<>(i, j));
                    } else {
                        System.out.println(String.format("cutV cellB: %s, %s", puzzle[i][j + 1].size(), puzzle[i][j + 1].boundingBox()));
                    }
                }

                if (puzzle[i][j] == null) { continue; }

                for (final Pixel pixel : puzzle[i][j].pixels()) {
                    // only color in non-transparent pixels
                    if (pixel != null) {
                        if (isBorderPixel(puzzle[i][j], pixel)) {
                            // set border pixels to black
                            cutImage.setRGB(pixel.x(), pixel.y(), 0xFF000000);
                        } else {
                            // System.out.println(image.getRGB(pixel.x(), pixel.y()));
                            cutImage.setRGB(pixel.x(), pixel.y(), image.getRGB(pixel.x(), pixel.y()));
                        }
                    }
                }
            }
        }
        ImageIO.write(cutImage, "png", new File("../api/src/main/resources/output.png"));
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (puzzle[i][j] == null || puzzle[i][j].size() == 0) { continue; }
                Pair<Pixel, Pixel> bounds = puzzle[i][j].boundingBox();
                BufferedImage cell = new BufferedImage(puzzle[i][j].width() + 1, puzzle[i][j].height() + 1, BufferedImage.TYPE_INT_ARGB);
                for (final Pixel pixel : puzzle[i][j].pixels()) {
                    // only color in non-transparent pixels
                    if (pixel != null) {
                        if (isBorderPixel(puzzle[i][j], pixel)) {
                            // set border pixels to black
                            cell.setRGB(pixel.x() - bounds.first().x(), pixel.y() - bounds.first().y(), 0xFF000000);
                        } else {
                            // System.out.println(String.format("(x: %d, y: %d) in (w: %d, h: %d, bounds: %s)", pixel.x() - bounds.first().x(), pixel.y() - bounds.first().y(), puzzle[i][j].width(), puzzle[i][j].height(), puzzle[i][j].boundingBox()));
                            cell.setRGB(pixel.x() - bounds.first().x(), pixel.y() - bounds.first().y(), image.getRGB(pixel.x(), pixel.y()));
                        }
                    }
                }
                ImageIO.write(cell, "png", new File(String.format("../api/src/main/resources/puzzle/cell_%s_%s.png", i, j)));
            }
        }
        // var api = Javalin.create(/*config*/)
        //     .get("/puzzlify", ctx -> {
        //         // the image, rows and cols should come from the request

        //         throw new UnsupportedOperationException("Not implemented yet");
        //         // TODO: Eman

        //         // TODO: store the images and return the urls so the frontend can fetch them 
        //         // and display and the user can download.
        //         // ctx.json([urls])
        //     })
        //     .start(7070);
    }
}