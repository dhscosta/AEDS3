//Dependências
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.security.auth.callback.TextOutputCallback;
import javax.swing.text.html.parser.ParserDelegator;

import com.opencsv.*;

//Classe principal para teste dos métodos implementados
public class App
{
    //Método para imprimir na tela opções disponíveis
    public static void printInterface()
    {
        System.out.println("-----------INTERFACE-----------");
        System.out.println("0 - Parar a execução");
        System.out.println("1 - Carregar o arquivo com os dados do csv");
        System.out.println("2 - Executar casamento de padroes KMP");
        System.out.println("3 - Executar casamento de padroes Shift-And");
    }
    
    //Função principal
    public static void main(String[] args) throws Exception {

        //Abre o csv 
        CSVReader csv = new CSVReader(new FileReader("games.csv"));
        Scanner ler = new Scanner(System.in);

        //Lê a primeira linha que informa o nome dos valores
        csv.readNext();
        String[] line;
        
        //Cria um arquivo binario
        RandomAccessFile arq = new RandomAccessFile("gamees.bin", "rw");

        //Inicializa o Crud(necessário para carregamento do arquivo binário)
        Crud crud = new Crud();
        int x = 0;

        //Imprime interface com as opções e lê opção desejada
        printInterface();
        System.out.print("Insira a opção que deseja executar: ");
        x = ler.nextInt();

        //Repete enquanto opção for diferente de 0 (0 == terminar execução do código)
        while(x != 0){
            switch(x)
            {
                //Cria arquivo binário a partir do csv
                case 1:
                    
                    while((line = csv.readNext()) != null)
                    {
                        crud.create(line);
                    }
                    System.out.println("Aperte ENTER para continuar");
                    ler.nextLine();
                    ler.nextLine();
                    
                    
                break;
                
                //Executa algoritmo KMP
                case 2:
                    //Leitura para forçar parada
                    ler.nextLine();
                    
                    //inicializa classe KMP
                    KMP kmp = new KMP();
                    arq.seek(0);
                    
                    //lê o padrão do terminal
                    String nome;
                    System.out.print("Digite o nome que deseja procurar: ");
                    nome = ler.nextLine();

                    //variáveis de auxílio
                    int tamanho = 0;
                    byte[] b;
                    long pos;
                    String texto;

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

                        //pesquisa com kmp no registro
                        kmp.search(nome, texto);
                    }

                break;

                //Executa algoritmo Shift-And
                case 3:
                    
                    //espaço reservado para Shift-And

                break;

                default:
                    System.out.println("Opção inválida!");
                break;
            }

            //Imprime interface com as opções e lê próxima opção desejada
            printInterface();
            System.out.print("Insira a opção que deseja executar: ");
            x = ler.nextInt();
        }

        //fechamento de classes
        csv.close();
        ler.close();
        arq.close();
    }
}