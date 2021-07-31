package com.github.zflxw.loginjam;

import com.github.zflxw.loginjam.utils.Reader;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
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
    private static final HashMap<String, Long> bannedIps = new HashMap<>();
    private static final HashMap<String, Integer> failCount = new HashMap<>();

    private static Properties ips = new Properties();

    public static void main(String[] args) throws IOException {
        WebServer server = WebServer.create();
        Properties data = new Properties();


        data.load(new FileInputStream("./data.properties"));
        ips.load(new FileInputStream("./banned_ips.properties"));

        ips.forEach((key, value) -> {
            bannedIps.put(key.toString(), Long.parseLong(String.valueOf(value)));
        }) ;

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
                map("/").to(request -> (isLoggedIn(request) ? Response.redirect("/dashboard").done() : Response.redirect("/login")).done());

                get("/login").to(request -> Response.ok().body(Reader.readResource("login.html")).done());
                get("/register").to(request -> Response.ok().body(Reader.readResource("register.html")).done());

                get("/dashboard").to(request -> {
                    if (isLoggedIn(request)) {
                        return Response.ok().body(Reader.readResource("dashboard.html")).done();
                    }

                    return Response.ok().body(Reader.readResource("unauthorized.html")).done();
                });

                post("/register").to(request -> {
                    try {
                        String email = request.part("email").value().toLowerCase();
                        String password = request.part("password").value();
                        String passwordRepeat = request.part("password-repeat").value();

                        if (isIPBanned(request)) {
                            return Response.redirect("/register?error-code=3").done();
                        }

                        if (email.isBlank() || password.isBlank() || passwordRepeat.isBlank()) {
                            return Response.redirect("/register?error-code=0").done();
                        }

                        if (!password.equals(passwordRepeat)) {
                            return Response.redirect("/register?error-code=1").done();
                        }

                        if (data.containsKey(email.toLowerCase())) {
                            return Response.redirect("/register?error-code=2").done();
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

                    return Response.redirect("/dashboard").done();
                });

                post("/login").to(request -> {
                    try {
                        String email = request.part("email").value();
                        String password = request.part("password").value();

                        CookieJar cookies = CookieJar.get(request);

                        if (isIPBanned(request)) {
                            return Response.redirect("/login?error-code=3").done();
                        }

                        if (cookies.has("token") && tokens.containsKey(cookies.get("token").value())) {
                            return Response.redirect("/login?error-code=2").done();
                        }

                        if (email.isBlank() || password.isBlank()) {
                            return Response.redirect("/login?error-code=0").done();
                        }

                        if (!String.valueOf(hash(password)).equals(data.getProperty(email.toLowerCase()))) {
                            if (failCount.containsKey(request.remoteIp())) {
                                if (failCount.get(request.remoteIp()) < 2) {
                                    int count = failCount.get(request.remoteIp()) + 1;
                                    failCount.put(request.remoteIp(), count);
                                    System.out.println(count);
                                } else {
                                    banIp(request);
                                    failCount.remove(request.remoteIp());
                                    return Response.redirect("/login?error-code=3").done();
                                }
                            } else {
                                failCount.put(request.remoteIp(), 0);
                                System.out.println(0);
                            }
                            return Response.redirect("/login?error-code=1").done();
                        }

                        UUID token = UUID.randomUUID();
                        tokens.put(token.toString(), email);
                        cookies.add("token", token.toString()).path("/");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }

                    return Response.redirect("/dashboard").done();
                });

                post("/logout").to(request -> {
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

    private static boolean isLoggedIn(Request request) {
        CookieJar cookies = CookieJar.get(request);

        return cookies.has("token") && tokens.containsKey(cookies.get("token").value());
    }

    private static void banIp(Request request) throws Exception {
        bannedIps.put(request.remoteIp(), System.currentTimeMillis());
        ips.put(request.remoteIp(), String.valueOf(System.currentTimeMillis()));
        ips.store(new FileOutputStream("./banned_ips.properties"), "This file does contain all banned user ips");
    }

    private static boolean isIPBanned(Request request) {
        return bannedIps.containsKey(request.remoteIp());
    }
}
