package com.puzzlify;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import static com.puzzlify.Puzzle.puzzlify;
import com.puzzlify.records.Cell;
import com.puzzlify.records.Utils.Pair;
import com.puzzlify.records.Utils.Pixel;

public class Api {
    public static void main(String[] args) throws IOException {
        // test image
        File file = new File("../api/src/main/resources/epic_drawer.png");
        BufferedImage image = ImageIO.read(file);
        BufferedImage cutImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        final int rows = 10;
        final int cols = 5;
        final Cell[][] puzzle = puzzlify(image, rows, cols);
        // final Set<Pixel> borderPixels = new HashSet<>();

        // for (int i = 0; i < rows; i++) {
        //     for (int j = 0; j < cols; j++) {
        //         if (puzzle[i][j] == null) { continue; }
        //         borderPixels.addAll(puzzle[i][j].border(5));
        //     }
        // }

        // test that the grid outputs the correct image with the center cell all white
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (puzzle[i][j] == null || puzzle[i][j].size() == 0) { continue; }
                System.out.println(String.format("Cell %s,%s has %s pixels", i, j, puzzle[i][j].size()));
                Pair<Pixel, Pixel> bounds = puzzle[i][j].boundingBox();
                BufferedImage cell = new BufferedImage(puzzle[i][j].width() + 1, puzzle[i][j].height() + 1, BufferedImage.TYPE_INT_ARGB);
                for (final Pixel pixel : puzzle[i][j].pixels()) {
                    // only color in non-transparent pixels
                    if (pixel != null) {
                        if (puzzle[i][j].isBorderPixel(pixel)) {
                            // set border pixels to black
                            cutImage.setRGB(pixel.x(), pixel.y(), 0xFF000000);
                            cell.setRGB(pixel.x() - bounds.first().x(), pixel.y() - bounds.first().y(), 0xFF000000);
                        } else {
                            // System.out.println(image.getRGB(pixel.x(), pixel.y()));
                            cutImage.setRGB(pixel.x(), pixel.y(), image.getRGB(pixel.x(), pixel.y()));
                            cell.setRGB(pixel.x() - bounds.first().x(), pixel.y() - bounds.first().y(), image.getRGB(pixel.x(), pixel.y()));
                        }
                    }
                }
                ImageIO.write(cell, "png", new File(String.format("../api/src/main/resources/puzzle/cell_%s_%s.png", i, j)));
            }
        }
        ImageIO.write(cutImage, "png", new File("../api/src/main/resources/output.png"));
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
