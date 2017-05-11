package com.bigbasti.coria.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by Sebastian Gross
 */
public class BaseTest {
    public static String readResource(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        return content;
    }
}
