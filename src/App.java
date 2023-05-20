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
        
        RandomAccessFile arq = new RandomAccessFile("gamees.bin", "rw");
        RandomAccessFile arqlog = new RandomAccessFile("arquivo_log.txt", "rw");
        printInterface();

        Crud crud = new Crud();
        int x = 0;
        System.out.print("Insira a opção que deseja executar: ");
        x = ler.nextInt();

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
                    //código para compressão com lzw
                break;

                case 3:
                    //Transforma o arquivo em um arranjo de bytes
                    dados = huffman.read("gamees.bin");

                    //Contagem de tempo da execução da compressão
                    long antes = System.currentTimeMillis();
                    dados = huffman.comprimir(dados);
                    long depois = System.currentTimeMillis();
                    System.out.println("Tempo de execução: " + (depois-antes));

                    //Escrita do arquivo comprimido
                    RandomAccessFile arquivo = new RandomAccessFile("Huffman_compressed.bin", "rw");
                    arquivo.write(dados);

                    //Escrita do arquivo de log com tempo e porcentagem de ganho
                    long ganho = ((arq.length() - arquivo.length()) / arquivo.length());
                    arqlog.writeChars("Tempo de execução Huffman: " + (depois-antes) + "\n");
                    arqlog.writeChars("Porcentagem de ganho Huffman: " + ganho + "\n");
                    arquivo.close();

                break;

                case 4:
                    //código para descompressão com lzw
                break;

                case 5:
                    
                    //Apenas executa se já existe dados para descomprimir
                    if(dados != null){
                        //Contagem de tempo da execução da compressão
                        antes = System.currentTimeMillis();
                        dados = huffman.descomprimir(dados);
                        depois = System.currentTimeMillis();
                        System.out.println("Tempo de execução: " + (depois-antes));

                        //Escrita do arquivo descomprimido
                        RandomAccessFile arquivo2 = new RandomAccessFile("Huffman_descompressed.bin", "rw");
                        arquivo2.write(dados);

                        //Escrita do arquivo de log com tempo e porcentagem de perda
                        long perda = ((arq.length() - arquivo2.length()) / arquivo2.length());
                        arqlog.writeChars("Tempo de execução Huffman: " + (depois-antes) + "\n");
                        arqlog.writeChars("Porcentagem de perda Huffman: " + perda + "\n");
                        arquivo2.close();
                    }    
                
                break;
            }
            printInterface();
            System.out.print("Insira a opção que deseja executar: ");
            x = ler.nextInt();
        }

        csv.close();
        ler.close();
    }
}