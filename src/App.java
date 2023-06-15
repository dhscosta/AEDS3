import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import com.opencsv.*;

public class App
{

    public static void printInterface()
    {
        System.out.println("-----------INTERFACE-----------");
        System.out.println("----ATENÇÃO----SELECIONAR A OPÇÃO 1 DESFAZ QUALQUER OPERAÇÃO DO CRUD FEITA ANTES");
        System.out.println("0 - Parar a execução");
        System.out.println("1 - Carregar o arquivo com o os dados do csv (create)");
        System.out.println("2 - Executar criptografia DES");
        System.out.println("3 - Executar criptografia RSA");
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
                    //espaço reservado para o código de RSA
                break;
            }
            printInterface();
            x = ler.nextInt();
        }

        csv.close();
        ler.close();
        arq.close();
    }
}
