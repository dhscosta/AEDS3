// Dependências
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

//Classe de criptografia
class RSA {
    //Método para encontrar a variável E, necessária para as fórmulas
    //e == menor primo referente ao x
    private Integer foundE(int x){

        //Array de inteiros para encontrar os divisores
        ArrayList<Integer> dx = new ArrayList<>();
        ArrayList<Integer> dy = new ArrayList<>();
        ArrayList<Integer> resposta = new ArrayList<>();
        
        //variável de retorno
        int retorno = 0;

        //Encontrar divisores do inteiro passado como parâmetro
        for(int i = 1; i <= x; i++){
            if(x % i == 0)
                dx.add(i);
        }

        //Variáveis de auxílio para encontrar o menor número primo referente ao parâmetro
        int y = 2;
        int max;

        //Enquanto o número for menor que o parâmetro
        while(y < x){
            //Encontra os divisores da variável Y
            for(int i = 1; i <= y; i++){
                if(y % i == 0)
                    dy.add(i);
            }

            //Seleciona o maior tamanho entre os dois arrays
            max = (dx.size() > dy.size()) ? dx.size() : dy.size();

            //Procura os divisores comuns entre os dois números e adiciona em um novo array
            for(int i = 0; i < max;i++){
                //Se o índice for maior que um dos tamanho termina a repetição
                if(i >= dx.size() || i >= dy.size())
                    break;
                
                //Se encontrar valores comuns entre os arrays, adiciona no array de respostas
                if(dx.contains(dy.get(i)))
                    resposta.add(dy.get(i));
            }

            //Se o array de respostas obtiver apenas um número e esse número for 1
            //acaba a repetição e atribui o valor a variável de retorno
            if((resposta.size() == 1) && (resposta.get(0) == 1)){
                retorno = y;
                break;
            }

            //limpa o array de resposta e o de divisores da variável y
            resposta.clear();
            dy.clear();

            //incrementa o Y
            y = y + 1;
        }

        //retorno da função
        return retorno;
    }

    //Método para gerar as chaves da criptografia
    public Chaves generateKeys(){
        //Número aleatório para geração das chaves
        SecureRandom random = new SecureRandom();

        //Números primos gerados aleatoriamente
        BigInteger p = BigInteger.probablePrime(4, random);
        BigInteger q = BigInteger.probablePrime(4, random);
        
        //Segundo primo necessariamente tem que ser diferente do primeiro
        while(q.equals(p))
            q = BigInteger.probablePrime(4, random);

        //Variável para atribuir os dois primos, decrementados
        BigInteger p_1 = p.subtract(BigInteger.ONE);
        BigInteger q_1 = q.subtract(BigInteger.ONE);
        
        //n = p * q
        //z = (p-1)*(q-1)
        BigInteger n = p.multiply(q);
        BigInteger z = p_1.multiply(q_1);
        
        //Encontro do menor primo referente ao z
        int aux = foundE(z.intValue());
        BigInteger e = new BigInteger(Integer.toString(aux));
        
        //Encontro do 'd' a partir do e
        //(e * d) % z == 1
        BigInteger d = e.modInverse(z);

        //Atribui as chaves a variável de retorno
        Chaves chaves = new Chaves(e, d, n);

        //retorno das chaves
        return chaves;
    }
    
    //Método para criptografar a mensagem
    public String encrypt(String entrada, Chaves chaves){
        //Variáveis para  criptografia
        BigInteger P, C;
        int aux;
        String encrypted = new String();

        //Laço para criptografar caracter por caracter
        for(int i = 0; i < entrada.length(); i++){
            aux = (int)entrada.charAt(i);
            P = new BigInteger(Integer.toString(aux));
            C = P.modPow(chaves.e, chaves.n);
            encrypted = encrypted + (char)C.intValue();
        }

        //retorno da mensagem criptografada
        return encrypted;
    }

    //Método para descriptografar mensagens
    public String decrypt(String entrada, Chaves chaves){
        //Variáveis para descriptografia
        BigInteger P, C;
        int aux;
        String decrypted = new String();

        //Laço para descriptografar, caracter por caracter
        for(int i = 0; i < entrada.length(); i++){
            aux = (int)entrada.charAt(i);
            C = new BigInteger(Integer.toString(aux));
            P = C.modPow(chaves.d, chaves.n);
            decrypted = decrypted + (char)P.intValue();
        }

        //retorno do mensagem descriptografada
        return decrypted;
    }

    //main para testes
    /*public void main(String[] args){
        Chaves chaves = generateKeys();
        
        String entrada = "Matheus Sinis";

        String crypted = encrypt(entrada, chaves);
        
        String decrypted = decrypt(crypted, chaves);

        System.out.printf("Texto puro: %s;\nTexto criptografado: %s;\nTexto descriptografado: %s", entrada, crypted, decrypted);
    }*/
}
