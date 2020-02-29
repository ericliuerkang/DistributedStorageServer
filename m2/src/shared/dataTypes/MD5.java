package shared.dataTypes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
    public static String getMD5EncryptedValue(String input) throws NoSuchAlgorithmException {
        final byte[] defaultBytes = input.getBytes();
        final MessageDigest md5MsgDigest = MessageDigest.getInstance("MD5");
        md5MsgDigest.reset();
        md5MsgDigest.update(defaultBytes);
        final byte messageDigest[] = md5MsgDigest.digest();
        final StringBuffer hexString = new StringBuffer();
        for (final byte element : messageDigest) {
            final String hex = Integer.toHexString(0xFF & element);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        input = hexString + "";
        return  input;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.println(getMD5EncryptedValue("Server_1"));
    }
}
