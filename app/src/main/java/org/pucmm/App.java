package org.pucmm;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

public class App {

    // Usuarios válidos (usuario -> password)
    private static final Map<String, String> USERS = new HashMap<>();

    public static void main(String[] args) {

        USERS.put("admin", "adminpassword");
        USERS.put("randy", "password123");
        USERS.put("guest", "12345");

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(7000);

        app.before(ctx -> {
            String path = ctx.path();

            boolean isPublic = path.equals("/login.html") ||
                    path.equals("/invalid.html") ||
                    path.equals("/login") ||
                    path.startsWith("/api") ||
                    path.startsWith("/css") ||
                    path.endsWith(".css") ||
                    path.startsWith("/img");

            if (!isPublic && !isAuthenticated(ctx)) {
                ctx.redirect("/login.html");
            }
        });

        app.get("/", ctx -> ctx.redirect("index.html"));

        app.get("/api/me", ctx -> {
            String user = ctx.sessionAttribute("user");

            if (user == null) {
                ctx.status(401);
            } else {
                ctx.json(Map.of("username", user));
            }
        });

        app.get("/auth", ctx -> ctx.redirect("login.html"));

        app.post("/login", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");

            if (USERS.containsKey(username) &&
                    USERS.get(username).equals(password)) {

                ctx.sessionAttribute("user", username);
                ctx.redirect("/");
            } else {
                ctx.status(401);
                ctx.redirect("invalid.html");
            }
        });

        app.get("/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.redirect("/auth");
        });
    }

    private static boolean isAuthenticated(Context ctx) {
        return ctx.sessionAttribute("user") != null;
    }
}
