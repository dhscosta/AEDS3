import java.io.*;
import java.util.*;
import com.opencsv.*;

public class App
{

    //Método de print da interface
    public static void printInterface()
    {
        System.out.println("-----------INTERFACE-----------");
        System.out.println("----ATENÇÃO----SELECIONAR A OPÇÃO 1 DESFAZ QUALQUER OPERAÇÃO DO CRUD FEITA ANTES");
        System.out.println("0 - Parar a execução");
        System.out.println("1 - Carregar o arquivo com o os dados do csv (create)");
        System.out.println("2 - Executar criptografia DES");
        System.out.println("3 - Executar descriptografia DES");
        System.out.println("4 - Executar criptografia RSA");
        System.out.println("5 - Executar descriptografia RSA(funciona apenas após opção 4)");
    }
    public static void main(String[] args) throws Exception {
        //abre o csv 
        CSVReader csv = new CSVReader(new FileReader("games.csv"));
        Scanner ler = new Scanner(System.in);

        //lê a primeira linha que informa o nome dos valores
        csv.readNext();
        String[] line;
        
        //cria um arquivo binario para a base e para a criptografia
        RandomAccessFile arq = new RandomAccessFile("gamees.bin", "rw");
        printInterface();

        //Inicializa a classe RSA e geração de chaves da criptografia RSA
        RSA rsa = new RSA();
        Chaves chaves = rsa.generateKeys();

        //variáveis de auxílio de RSA
        int tamanho = 0;
        byte[] b;
        String saida;
        Game jogos = new Game();

        Crud crud = new Crud();
        int x = 0;
        System.out.print("Insira a opção que deseja executar: ");
        x = ler.nextInt();

        while(x != 0){
            switch(x)
            {
                case 1:
                    
                    while((line = csv.readNext()) != null)
                    {
                        crud.create(line);
                    }
                    System.out.println("Aperte ENTER para continuar");
                    ler.nextLine();
                    ler.nextLine();
                    
                    
                break;
                
                case 2:
                    //Leitura para forçar parada
                    ler.nextLine();

                    RandomAccessFile arqCrip = new RandomAccessFile("arqCrip.bin", "rw");

                    arq.seek(0);
        
                    //lê a chave 
                    String key;
                    System.out.print("Digite a chave para criptografar: ");
                    key = ler.nextLine();

                    //variáveis de auxílio
                    int tam = 0;
                    byte[] by;
                    char c;
                    byte[] crypt;
                    String cryptStr;
                    Game jogo = new Game();

                    //Efetua o casamento de padrões com o padrão desejado procurando por todos os registros
                    while(arq.getFilePointer() != arq.length()){
                        c = arq.readChar();         //le lapide
                        tam = arq.readInt();     //le tamanho registro
                        //pula se tiver lapide
                        if(c == '#'){
                            arq.skipBytes(tam);
                        }
                        else{
                            //lê o registro e transforma em um objeto game
                            by = new byte[tam];
                            arq.read(by);
                            jogo.fromByte(by);
                            jogo.mostrar();


                            //criptografia dos titulos, mostra no terminal, e salva no arquivo
                            crypt = Des.encrypt(jogo.title, key);
                            cryptStr = crypt.toString();     
                            System.out.println(cryptStr);
                            jogo.setTitle(cryptStr);           //seta titulo criptografado
                            by = jogo.toByte();                //transforma o novo game em bytes
                            jogo.mostrar();
                            arqCrip.write(by);                 //escreve no arquivo criptografado
                        }
                    }

                    arqCrip.close();
                break;

                case 3:
                    RandomAccessFile arqDes = new RandomAccessFile("arqCrip.bin", "rw");
                    Game jg = new Game();

                    //lê a chave 
                    System.out.print("Digite a chave para criptografar: ");
                    key = ler.nextLine();

                    arqDes.seek(0);

                    while(arqDes.getFilePointer() != arqDes.length()){
                        c = arqDes.readChar();
                        tam = arqDes.readInt();

                        by = new byte[tam];
                        arqDes.read(by);
                        jg.fromByte(by);

                        byte[] aux = jg.title.getBytes();
                        crypt = Des.decrypt(aux, key);
                        cryptStr = crypt.toString();
                        System.out.println(cryptStr);
                    }
                    ler.nextLine();
               
                
                break;

                case 4:
                    //Leitura para forçar parada
                    ler.nextLine();
                
                    //Criação de arquivo para armazenamento da criptografia
                    RandomAccessFile encrypt = new RandomAccessFile("RSA_encrypt.bin", "rw");

                    //posiciona o ponteiro no início do arquivo
                    arq.seek(0);

                    //variável de auxílio
                    char caracter;

                    //Efetua a criptografia RSA em cima dos títulos dos jogos
                    while(arq.getFilePointer() != arq.length()){
                        //Leitura do registro
                        caracter = arq.readChar();
                        tamanho = arq.readInt();
                        
                        if(caracter == '#'){
                            //se registro for inválido, pula o registro
                            arq.skipBytes(tamanho);
                        }else{
                            //leitura do registro e iniciando jogo a partir dele    
                            b = new byte[tamanho];
                            arq.read(b);
                            jogos.fromByte(b);

                            //criptografia dos títulos, mostra no terminal e salva no arquivo
                            saida = rsa.encrypt(jogos.title, chaves);
                            System.out.println(saida);
                            jogos.setTitle(saida);
                            b = jogos.toByte();
                            encrypt.write(b.length);
                            encrypt.write(b);
                        }
                    }

                break;

                case 5:
                    //Abertura de arquivo criptografado para leitura
                    RandomAccessFile decrypt = new RandomAccessFile("RSA_encrypt.bin", "r");

                    //posiciona o ponteiro no início do arquivo
                    decrypt.seek(0);

                    //Efetua a descriptografia RSA em cima dos títulos dos jogos
                    while(decrypt.getFilePointer() != decrypt.length()){
                        //Leitura do tamanho do registro
                        tamanho = decrypt.readInt();
                        
                        //leitura do registro e iniciando jogo a partir dele    
                        b = new byte[tamanho];
                        decrypt.read(b);
                        jogos.fromByte(b);

                        //descriptografia dos títulos e mostra no terminal
                        saida = rsa.decrypt(jogos.title, chaves);
                        System.out.println(saida);
                    }
                break;
            }

            //Printa a interface no terminal e lê próxima opção
            printInterface();
            System.out.print("Insira a opção deseja executar: ");
            x = ler.nextInt();
        }

        //fechamento de classes
        csv.close();
        ler.close();
        arq.close();
    }
}
