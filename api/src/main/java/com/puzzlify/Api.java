package com.puzzlify;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.puzzlify.Puzzle.Cell;
import com.puzzlify.Puzzle.Pixel;
import static com.puzzlify.Puzzle.constructGrid;
import static com.puzzlify.Puzzle.cut;

import io.javalin.Javalin;

public class Api {
    public static void main(String[] args) throws IOException {
        // test image
        File file = new File("../api/src/main/resources/billy_nips_bg.jpeg");
        BufferedImage image = ImageIO.read(file);
        
        // test that the grid outputs the correct image with the center cell all white
        final Cell[][] grid = constructGrid(image, 3, 3);
        for(int i=1;i< 2;i++){
            for(int j=0;j<2;j++){
                Pixel start = new Pixel(i*image.getWidth()/3,j*image.getHeight()/3);
                Puzzle.Edge edgeH = new Puzzle.Edge(start, start.translate(image.getWidth()/3, 0));
                Puzzle.Edge edgeV = new Puzzle.Edge(start, start.translate(0, image.getHeight()/3));

                //horizontal cut and update cells
                Puzzle.Pair<Cell, Optional<Cell>> cutH =  cut(edgeH,grid[i][j],grid[i+1][j]);
                grid[i][j] = cutH.first();
                grid[i+1][j] = cutH.second().isPresent()? cutH.second().get():null;
                //vertical cut and update cells
                Puzzle.Pair<Cell, Optional<Cell>> cutV = cut(edgeV, grid[i][j],grid[i][j+1]);
                grid[i][j] = cutV.first();
                grid[i][j+1]= cutV.second().isPresent()? cutV.second().get():null;

            }

        }
        int count=0;
        for (final Pixel pixel : grid[0][0].pixels()) {
            // only color in non-transparent pixels

            if (pixel != null) {
                image.setRGB(pixel.x(), pixel.y(), 255);
                count++;
            }
        }
        ImageIO.write(image, "png", new File("../api/src/main/resources/output.png"));
        System.out.println("Count is: "+ count);


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