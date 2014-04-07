package com.kc.utils;

import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created with IntelliJ IDEA.
 * User: GLEB
 * Date: 07.04.14
 * Time: 7:04
 * To change this template use File | Settings | File Templates.
 */
public class Deserializer {
    private static FileInputStream fin;
    private static ObjectInputStream ois;
    private static final Path TMP_FILE = Paths.get("D:\\key.ser");

    public static SecretKey deserializeObject() throws IOException, ClassNotFoundException {
        SecretKey key;
        try {
            createTmpFile();
            fin = new FileInputStream(TMP_FILE.toFile());
            ois = new ObjectInputStream(fin);
            key = (SecretKey) ois.readObject();
            return key;
        } finally {
            if (ois != null) {
                ois.close();
            }
            if (fin != null) {
                fin.close();
            }
        }
    }

    private static void createTmpFile() throws IOException {
        Files.deleteIfExists(TMP_FILE);
        Deserializer des = new Deserializer();
        InputStream stream = des.getClass().getResourceAsStream("/text/key.ser");
        //Files.createFile(TMP_FILE);
        //OutputStream ostream = new FileOutputStream(TMP_FILE.toFile());
        Files.copy(stream, TMP_FILE);
    }
}