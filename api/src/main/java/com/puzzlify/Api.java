package com.puzzlify;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.puzzlify.Puzzle.Cell;
import com.puzzlify.Puzzle.Pixel;
import static com.puzzlify.Puzzle.constructGrid;

import io.javalin.Javalin;

public class Api {
    public static void main(String[] args) throws IOException {
        // test image
        File file = new File("/home/akatary/puzzlify/api/src/main/resources/billy_nips.png");
        BufferedImage image = ImageIO.read(file);
        
        // test that the grid outputs the correct image with the center cell all white
        final Cell[][] grid = constructGrid(image, 3, 3);
        for (final Pixel pixel : grid[2][2].pixels()) {
            // only color in non-transparent pixels
            if (pixel != null) {
                image.setRGB(pixel.x(), pixel.y(), 255);
            }
        }
        ImageIO.write(image, "png", new File("/home/akatary/puzzlify/api/src/main/resources/output.png"));

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