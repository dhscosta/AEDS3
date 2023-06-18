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
        
        //cria um arquivo binario
        RandomAccessFile arq = new RandomAccessFile("gamees.bin", "rw");
        printInterface();

        //Inicializa a classe RSA e geração de chaves da criptografia RSA
        RSA rsa = new RSA();
        Chaves chaves = rsa.generateKeys();

        //variáveis de auxílio de RSA
        int tamanho = 0;
        byte[] b;
        char c;
        String saida;
        Game jogo = new Game();

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
                    //espaço reservado para o código de DES
                break;

                case 3:
                    //espaço reservado para o código de DES
                break;

                case 4:
                    //Leitura para forçar parada
                    ler.nextLine();
                
                    //Criação de arquivo para armazenamento da criptografia
                    RandomAccessFile encrypt = new RandomAccessFile("RSA_encrypt.bin", "rw");

                    //posiciona o ponteiro no início do arquivo
                    arq.seek(0);

                    //Efetua a criptografia RSA em cima dos títulos dos jogos
                    while(arq.getFilePointer() != arq.length()){
                        //Leitura do registro
                        c = arq.readChar();
                        tamanho = arq.readInt();
                        
                        if(c == '#'){
                            //se registro for inválido, pula o registro
                            arq.skipBytes(tamanho);
                        }else{
                            //leitura do registro e iniciando jogo a partir dele    
                            b = new byte[tamanho];
                            arq.read(b);
                            jogo.fromByte(b);

                            //criptografia dos títulos, mostra no terminal e salva no arquivo
                            saida = rsa.encrypt(jogo.title, chaves);
                            System.out.println(saida);
                            jogo.setTitle(saida);
                            b = jogo.toByte();
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
                        jogo.fromByte(b);

                        //descriptografia dos títulos e mostra no terminal
                        saida = rsa.decrypt(jogo.title, chaves);
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
