package com.github.zflxw.loginjam.utils;

import com.github.zflxw.loginjam.LoginScreen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

public class Reader {
    public static String read(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        String content = new String(inputStream.readAllBytes());

        inputStream.close();
        return content;
    }

    public static String readResource(String name) throws URISyntaxException, IOException {
        return read(new File(LoginScreen.class.getClassLoader().getResource(name).toURI()));
    }
}
