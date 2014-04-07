package com.kc.utils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created with IntelliJ IDEA.
 * User: GLEB
 * Date: 07.04.14
 * Time: 6:59
 * To change this template use File | Settings | File Templates.
 */
public class CryptoUtils {

    public static void decrypt(InputStream is, OutputStream os) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, ClassNotFoundException, InvalidAlgorithmParameterException, InvalidKeyException {
        SecretKey key = Deserializer.deserializeObject();

        //get Cipher instance and initiate in decrypt mode
        Cipher decryptCipher = Cipher.getInstance("AES");
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
        //create CipherOutputStream to decrypt the data using decryptCipher
        is = new CipherInputStream(is, decryptCipher);
        writeData(is, os);
    }

    private static void writeData(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[1024];
        int numRead;

        //read and write operation
        while ((numRead = is.read(buf)) >= 0) {
            os.write(buf, 0, numRead);
        }
        os.close();
        is.close();
    }
}
