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
        System.out.println("2 - Compressão LZW");
        System.out.println("3 - Compressão Huffman");
        System.out.println("4 - Descomprimir LZW");
        System.out.println("5 - Descomprimir Huffman(funciona apenas se a opção 3 já foi executada)");
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

        LZW lzw = new LZW();
        Huffman huffman = new Huffman();
        byte[] dados = null;

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
                    lzw.comprimir("gamees.bin", "compressed.bin");
                break;

                case 3:
                    dados = huffman.read("gamees.bin");

                    huffman.comprimir(dados);
                break;

                case 4:
                    lzw.descomprimir("compressed.bin", "descompressed.bin");
                break;

                case 5:
                    
                    if(dados != null){
                        huffman.descomprimir(dados);
                    }    
                
                break;
            }
            printInterface();
            System.out.print("Insira a opção que deseja executar: ");
            x = ler.nextInt();
        }

        csv.close();
        ler.close();
        arq.close();
    }
}