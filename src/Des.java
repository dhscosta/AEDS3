import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class Des {
    /*
     * função pra criptografar uma string
     * params 
     * ori -> string original
     * key -> chave de criptografia
     */
    public static String encrypt(String ori, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");                                 //inicializa o modo de operaçao do cipher
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "DES");   //cria uma chave secreta a partir da sequencia de bytes da chave lida
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);                                                //inicializa o cipher no modo de criptografia (enviando a chave)
        byte[] cripBytes = cipher.doFinal(ori.getBytes(StandardCharsets.UTF_8));                    //criptografa o texto original
        return Base64.getEncoder().encodeToString(cripBytes);                                       //transforma em string
    }

    public static String decrypt(String cripText, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");                                 //inicializa o modo de operaçao do cipher
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "DES");   //cria uma chave secreta a partir da sequencia de bytes da chave lida
        cipher.init(Cipher.DECRYPT_MODE, secretKey);                                                //inicializa o cipher no modo de descriptografia (enviando a chave)
        byte[] decrypBytes = cipher.doFinal(Base64.getDecoder().decode(cripText));                  //descriptografa 
        return new String(decrypBytes, StandardCharsets.UTF_8);                                     //cria a string descriptografada
    }
}