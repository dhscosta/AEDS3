import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class Des {

    public static void main(String[] args) {
        String plaintext = "Hello, world!";
        String key = "0123456789abcdef"; // Chave de 16 caracteres hexadecimal

        try {
            byte[] encryptedBytes = encrypt(plaintext, key);
            String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
            System.out.println("Texto criptografado: " + encryptedText);

            byte[] decryptedBytes = decrypt(encryptedBytes, key);
            String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);
            System.out.println("Texto descriptografado: " + decryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] encrypt(String plaintext, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("DES");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "DES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decrypt(byte[] ciphertext, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("DES");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "DES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(ciphertext);
    }
}