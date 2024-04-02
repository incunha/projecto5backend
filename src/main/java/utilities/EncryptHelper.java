package utilities;

import jakarta.ejb.Singleton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Singleton
public class EncryptHelper {

    public EncryptHelper() {
        try {
            this.m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    MessageDigest m;


    public String encryptPassword(String plainPassword) {
        m.update(plainPassword.getBytes());

        /* Convert the hash value into bytes */
        byte[] bytes = m.digest();

        /* The bytes array has bytes in decimal form. Converting it into hexadecimal format. */
        StringBuilder s = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            s.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return s.toString();
    }

}