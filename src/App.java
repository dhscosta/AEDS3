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

    public static void telaArvore() throws Exception {
        Scanner ler = new Scanner(System.in);
        CSVReader csv = new CSVReader(new FileReader("games.csv"));
        String[] line;
        Arvore arv = new Arvore("games.bin");
        int x = 0;
        System.out.print("Insira a opção que deseja executar: ");
        x = ler.nextInt();
        boolean teste = false;

        while(x != 0){
            switch(x)
            {
                case 1:
                    
                    //carga inicial dos dados do csv
                    while((line = csv.readNext()) != null)
                    {
                        arv.create(line);
                        
                    }
                    System.out.println("Aperte ENTER para continuar");
                    ler.nextLine();
                    ler.nextLine();
                    
                break;

                case 2:
                    Game jogo = new Game();
                    
                    //pesquisa por um id
                    System.out.println("Insira o id do jogo que será pesquisado: ");
                    int pesqId = ler.nextInt();
                    jogo = arv.read(pesqId).clone();

                    System.out.println("O objeto encontrado foi:");
                    jogo.mostrar();
                break;

                case 3:
                    //update do registro passando os novos parametros e o id do registro que será trocado
                    System.out.println("Insira todos os campos do registro, mesmo os que não forem alterados");
                    int iden;
                    
                    System.out.print("Insira o ID do jogo que sofrerá update: ");
                    iden = ler.nextInt();
                    

                    System.out.print("Insira o título do jogo: ");
                    ler.nextLine();
                    String title = ler.nextLine();

                    System.out.print("O jogo funciona em sistemas windows?(true/false): ");
                    boolean win = ler.nextBoolean();
                    System.out.print("O jogo funciona em sistemas mac?(true/false): ");
                    boolean mac = ler.nextBoolean();
                    System.out.print("O jogo funciona em sistemas linux?(true/false): ");
                    boolean linux = ler.nextBoolean();

                    System.out.print("Insira a avaliação do jogo: ");
                    ler.nextLine();
                    String rating = ler.nextLine();

                    System.out.print("Insira o preço do jogo(use virgula para decimal): ");
                    float price = ler.nextFloat();

                    System.out.print("Insira a data de lançamento do jogo(yyyy-mm-dd): ");
                    SimpleDateFormat fDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    ler.nextLine();
                    String stringData = ler.nextLine();
                    Date data = fDateFormat.parse(stringData);

                    teste = arv.update(iden, title, win, mac, linux, rating, price, data);

                    if(teste){
                        System.out.println("Update feito com sucesso");
                    }else{
                        System.out.println("Nao foi possível executar o update");
                    }

                break;

                case 4:
                    //deleta um registro
                    int identificador;
                    identificador = ler.nextInt();
                    teste = arv.delete(identificador);

                    if(teste){
                        System.out.println("Objeto removido com sucesso");
                    }else{
                        System.out.println("Nao foi possivel remover o objeto");
                    }

                break;
            }
        }
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
                case 2:
                    telaArvore();
                break;

            }
            printInterface();
            x = ler.nextInt();
        }

        ler.close();
    }
}
