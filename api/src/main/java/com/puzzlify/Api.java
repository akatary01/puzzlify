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

import io.javalin.Javalin;

public class Api {
    public static void main(String[] args) throws IOException {
        // test image
        File file = new File("../api/src/main/resources/sp.png");
        BufferedImage image = ImageIO.read(file);
        
        final int rows = 3;
        final int cols = 3;
        int[][] colors = {
            { 0x80FF0000, 0x8000FF00, 0x800000FF },  // Row 0: Red, Green, Blue
            { 0x80FFFF00, 0x8000FFFF, 0x80FF00FF },  // Row 1: Yellow, Cyan, Magenta
            { 0x80FF8C00, 0x80800080, 0x8000FF80}   // Row 2: Orange, Purple, Lime
        };
        final Cell[][] grid = constructGrid(image, rows, cols);
        final Cell[][] puzzle = constructGrid(image, rows, cols);
        final HashMap<Integer, Pair<Integer, Integer>> merged = new HashMap<>();

        // test that the grid outputs the correct image with the center cell all white
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                final Cell cellA;
                if (puzzle[i][j] == null) {
                    final Pair<Integer, Integer> A = merged.get(i * cols + j);
                    cellA = puzzle[A.first()][A.second()];
                } else {
                    cellA = puzzle[i][j];
                }
                final Pair<Pixel, Pixel> boundA = grid[i][j].boundingBox();
                System.out.println(String.format("processing cellA[%d][%d]: %s, %s", i, j, grid[i][j].size(), boundA));

                final int a = image.getHeight() / rows;
                final int b = image.getWidth() / cols;
                System.out.println(String.format("(a, b): %d, %d", a, b));

                final Pixel topLeft = boundA.first();
                final Pixel bottomRight = boundA.second();
                
                // we do not process the last set of vertical edges
                if (i != rows - 1) { 
                    final Edge edgeH = new Edge(topLeft.translate(0, a - 1), bottomRight);
                    System.out.println(String.format("edgeH: %s", edgeH));
                    // horizontal cut and update cells
                    Puzzle.Pair<Cell, Optional<Cell>> cutH = cut(edgeH, cellA, puzzle[i + 1][j], a, b);
                    puzzle[i][j] = cutH.first();
                    System.out.println(String.format("cutH cellA: %s, %s", grid[i][j].size(), grid[i][j].boundingBox()));
                    puzzle[i + 1][j] = cutH.second().orElse(null);
                    if (cutH.second().isEmpty()) {
                        System.err.println("cutH cellB is merged");
                        merged.put((i + 1) * cols + j, new Pair<>(i, j));
                    } else {
                        System.out.println(String.format("cutH cellB: %s, %s", grid[i + 1][j].size(), grid[i + 1][j].boundingBox()));
                    }
                }
                
                // we do not process the last set of vertical edges
                if (j != cols - 1) {
                    final Edge edgeV = new Edge(topLeft.translate(b - 1, 0), bottomRight);
                    System.out.println(String.format("edgeV: %s", edgeV));
                    //vertical cut and update cells
                    Puzzle.Pair<Cell, Optional<Cell>> cutV = cut(edgeV, cellA, puzzle[i][j + 1], a, b);
                    puzzle[i][j] = cutV.first();
                    System.out.println(String.format("cutV cellA: %s, %s", grid[i][j].size(), grid[i][j].boundingBox()));
                    puzzle[i][j + 1] = cutV.second().orElse(null);
                    if (cutV.second().isEmpty()) {
                        System.err.println("cutV cellB is merged");
                        merged.put(i * cols + (j + 1), new Pair<>(i, j));
                    } else {
                        System.out.println(String.format("cutV cellB: %s, %s", grid[i][j + 1].size(), grid[i][j + 1].boundingBox()));
                    }
                }

                if (puzzle[i][j] == null) { continue; }
                
                for (final Pixel pixel : puzzle[i][j].pixels()) {
                    // only color in non-transparent pixels
                    if (pixel != null) {
                        // System.out.println(image.getRGB(pixel.x(), pixel.y()));
                        image.setRGB(pixel.x(), pixel.y(), colors[i % 3][j % 3]);
                    }
                }
            }
        }
        ImageIO.write(image, "png", new File("../api/src/main/resources/output.png"));
        var api = Javalin.create(/*config*/)
            .get("/puzzlify", ctx -> {
                // the image, rows and cols should come from the request

                throw new UnsupportedOperationException("Not implemented yet");
                // TODO: Eman

                // TODO: store the images and return the urls so the frontend can fetch them 
                // and display and the user can download.
                // ctx.json([urls])
            })
            .start(7070);
    }
}