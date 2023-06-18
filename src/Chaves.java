//Dependências
import java.math.BigInteger;

//Classe para armazenar chaves
public class Chaves{
    //Chaves de criptografia
    public BigInteger e, d, n;

    //Construtor da classe passando valores internos
    Chaves(BigInteger e_e, BigInteger e_d, BigInteger e_n){
        e = e_e;
        d = e_d;
        n = e_n;
    }
}