import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import com.opencsv.*;

public class App
{
    //funcao para mostrar a interface do CRUD
    public static void printInterface()
    {
        System.out.println("-----------INTERFACE-----------");
        System.out.println("0 - Parar a execução");
        System.out.println("1 - Hash");
        System.out.println("2 - Arvore");
    }

    public static void TelaArquiHash() throws Exception {
        int y =0;
        Scanner sc = new Scanner(System.in);
        CSVReader csv = new CSVReader(new FileReader("games.csv"));
        ArqIndices arqIndices = new ArqIndices(1060);
        arqIndices.lerHash();

        do{
        
            System.out.println("\nOpcoes ");
            System.out.println(" 0 - parar");
            System.out.println(" 1 - Criar a base");
            System.out.println(" 2 - Buscar");
            System.out.println(" 3 - Deletar");
            System.out.println(" 4 - Update(erro)");
            y = sc.nextInt();

            switch(y){
                case 1:
                    arqIndices.lerHash();
                    break;
            }
        }while(y!=0);
    }

    public static void main(String[] args) throws Exception {
        Scanner ler = new Scanner(System.in);
        printInterface();
        int x = 0;
        System.out.print("Insira a opção que deseja executar: ");
        x = ler.nextInt();
        boolean teste = false;

        while(x != 0){
            switch(x)
            {
                case 1:
                    TelaArquiHash();
                break;
                /*
                 * case 2:
                 * TelaArv();
                 * break;
                 */

            }
            printInterface();
            x = ler.nextInt();
        }

        ler.close();
    }
}
