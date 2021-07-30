package com.github.zflxw.loginjam;

import com.github.zflxw.loginjam.utils.Reader;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.lib.CookieJar;
import com.vtence.molecule.middlewares.Cookies;
import com.vtence.molecule.routing.Routes;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

public class LoginScreen {
    // Token, Username (E-Mail)
    private static final HashMap<String, String> tokens = new HashMap<>();

    public static void main(String[] args) throws IOException {
        WebServer server = WebServer.create();
        Properties data = new Properties();

        data.load(new FileInputStream("./data.properties"));

        server.add(next -> request -> next.handle(request).whenComplete((response, throwable) -> {
            if (response.statusCode() == 404) {
                try {
                    response.contentType("text/html").done(Reader.readResource("404.html"));
                } catch (URISyntaxException | IOException e) {
                    e.printStackTrace();
                }
            }
        }))
        .add(new Cookies())
            .route(new Routes() {{
                map("/").to(request -> Response.ok().body(Reader.readResource("index.html")).done());

                get("/login").to(request -> Response.ok().body(Reader.readResource("login.html")).done());
                get("/register").to(request -> Response.ok().body(Reader.readResource("register.html")).done());
                get("/success").to(request -> Response.ok().body(Reader.readResource("success.html")).done());
                get("/unauthorized").to(request -> Response.ok().body(Reader.readResource("unauthorized.html")).done());

                get("/dashboard").to(request -> {
                    CookieJar cookies = CookieJar.get(request);

                    if (cookies.has("token")) {
                        Cookie cookie = cookies.get("token");
                        String token = cookie.value();

                        if (tokens.containsKey(token)) {
                            return Response.ok().body(Reader.readResource("dashboard.html")).done();
                        }
                    }

                    return Response.redirect("/unauthorized").done();
                });

                post("/register").to(request -> {
                    try {
                        String email = request.part("email").value().toLowerCase();
                        String password = request.part("password").value();
                        String passwordRepeat = request.part("password_repeat").value();

                        if (email.isBlank() || password.isBlank() || passwordRepeat.isBlank()) {
                            return Response.redirect("/register?error_code=0").done();
                        }

                        if (!password.equals(passwordRepeat)) {
                            return Response.redirect("/register?error_code=1").done();
                        }

                        if (data.containsKey(email.toLowerCase())) {
                            return Response.redirect("/register?error_code=2").done();
                        }

                        data.put(email, String.valueOf(hash(password)));
                        data.store(new FileOutputStream("./data.properties"), "This file does contain all user data (email and hashed password)");

                        CookieJar cookies = CookieJar.get(request);
                        UUID token = UUID.randomUUID();

                        System.out.println("Token: " + token);
                        tokens.put(token.toString(), email);
                        cookies.add("token", token.toString()).path("/");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }

                    return Response.redirect("/success").done();
                });

                post("/login").to(request -> {
                    try {
                        String email = request.part("email").value();
                        String password = request.part("password").value();

                        CookieJar cookies = CookieJar.get(request);

                        if (cookies.has("token") && tokens.containsKey(cookies.get("token").value())) {
                            return Response.redirect("/login?error_code=2").done();
                        }

                        if (email.isBlank() || password.isBlank()) {
                            return Response.redirect("/login?error_code=0").done();
                        }

                        if (!String.valueOf(hash(password)).equals(data.getProperty(email.toLowerCase()))) {
                            return Response.redirect("/login?error_code=1").done();
                        }

                        UUID token = UUID.randomUUID();
                        tokens.put(token.toString(), email);
                        cookies.add("token", token.toString()).path("/");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }

                    return Response.redirect("/").done();
                });

                post("/logout").to(request -> {
                    System.out.println("Called logout");
                   CookieJar cookieJar = CookieJar.get(request);

                   if (cookieJar.has("token") && tokens.containsKey(cookieJar.get("token").value())) {
                       tokens.remove(cookieJar.get("token").value());
                       cookieJar.discard("token");
                   }

                   return Response.redirect("/").done();
                });
            }});
    }

    /**
     * this hashing method is totally insecure and should NOT be used in production use!
     * I just wanted to use some funny stuff, since the hashing does not really matter in this code jam
     * @param password the password to hash
     * @return the hashed password
     */
    private static int hash(String password) {
        return password.hashCode() + 10 + password.charAt(0);
    }
}
