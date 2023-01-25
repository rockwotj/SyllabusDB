package com.rockwotj.syllabusdb.server;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.ServiceUnavailableResponse;

/** The server! */
public final class Server implements AutoCloseable {

  private final Javalin app;

  private Server() {
    this.app =
        Javalin.create()
            .post("/lookup", this::lookup)
            .post("/query", this::query)
            .post("/write", this::write);
  }

  public static Server create() {
    return new Server();
  }

  public static void main(String[] args) {
    var server = Server.create();
    server.start(3000);
  }

  public void start(int port) {
    this.app.start(port);
  }

  private void lookup(Context context) {
    throw new ServiceUnavailableResponse();
  }

  private void query(Context context) {
    throw new ServiceUnavailableResponse();
  }

  private void write(Context context) {
    throw new ServiceUnavailableResponse();
  }

  @Override
  public void close() {
    app.close();
  }
}
