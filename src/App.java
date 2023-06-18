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
        System.out.println("5 - Executar descriptografia RSA");
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
                    
                    //inicializa classe RSA
                    RSA rsa = new RSA();

                    //Gerar chaves
                    Chaves chaves = rsa.generateKeys();

                    //posiciona o ponteiro no início do arquivo
                    arq.seek(0);

                    //variáveis de auxílio
                    int tamanho = 0;
                    byte[] b;
                    long pos;
                    String texto, saida;

                    //Efetua o casamento de padrões com o padrão desejado procurando por todos os registros
                    while(arq.getFilePointer() != arq.length()){
                        //Leitura da lápide, do tamanho, da posição do ponteiro de arquivo e do id do jogo, respectimente
                        arq.readChar();
                        tamanho = arq.readInt();
                        pos = arq.getFilePointer();
                        arq.readInt();

                        //leitura do título
                        texto = arq.readUTF();
                        
                        //volta arquivo para a posicao do início do registro
                        arq.seek(pos);
                        
                        //leitura para pular registro
                        b = new byte[tamanho];
                        arq.read(b);

                        //criptografia dos títulos e mostra no terminal
                        saida = rsa.encrypt(texto, chaves);
                        System.out.println(saida);
                    }

                break;

                case 5:
                    //espaço reservado para o código de RSA
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
