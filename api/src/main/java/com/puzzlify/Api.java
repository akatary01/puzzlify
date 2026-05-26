package com.puzzlify;

import io.javalin.Javalin;

public class Api {
    public static void main(String[] args) {
        var api = Javalin.create(/*config*/)
            .get("/", ctx -> ctx.result("Hello World"))
            .start(7070);
    }
}