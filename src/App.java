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
        System.out.println("----ATENÇÃO----SELECIONAR A OPÇÃO 1 DESFAZ QUALQUER OPERAÇÃO DO CRUD FEITA ANTES");
        System.out.println("0 - Parar a execução");
        System.out.println("1 - Carregar o arquivo com o os dados do csv (create)");
        System.out.println("2 - Ler um registro do arquivo usando o id (read)");
        System.out.println("3 - Atualizar um registro de um jogo (update)");
        System.out.println("4 - Apagar um registro de jogo (delete)");
        System.out.println("5 - Mostrar todos os registros do arquivo");
        System.out.println("6 - ");
    }
    public static void main(String[] args) throws Exception {

        //abre o csv 
        CSVReader csv = new CSVReader(new FileReader("games.csv"));
        Scanner ler = new Scanner(System.in);

        //lê a primeira linha que informa o nome dos valores
        csv.readNext();
        String[] line;
        
        printInterface();

        //cria um crud
        Crud crud = new Crud();
        int x = 0;
        System.out.print("Insira a opção que deseja executar: ");
        x = ler.nextInt();

        while(x != 0){
            switch(x)
            {
                case 1:
                    //carga inicial dos dados do csv
                    while((line = csv.readNext()) != null)
                    {
                        crud.create(line);
                    }
                    System.out.println("Aperte ENTER para continuar");
                    ler.nextLine();
                    ler.nextLine();
                    
                    
                break;

                case 2:
                    //pesquisa por um id
                    System.out.println("Insira o id do jogo que será pesquisado: ");
                    int pesqId = ler.nextInt();
                    crud.read(pesqId);
                break;

                case 3:
                    //update do registro passando os novos parametros e o id do registro que será trocado
                    System.out.println("Insira todos os campos do registro, mesmo os que não forem alterados");
                    int iden;
                    do{
                        System.out.print("Insira o ID do jogo que sofrerá update: ");
                        iden = ler.nextInt();
                    }while(crud.read(iden) == false);

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

                    crud.update(iden, title, win, mac, linux, rating, price, data);
                break;

                case 4:
                    //deleta um registro
                    int identificador;
                    do{
                        System.out.println("Insira o id do jogo que será deletado: ");
                        identificador = ler.nextInt();
                    }while(crud.read(identificador) == false);
                    crud.delete(identificador);
                break;

                case 5:
                    //mostra todos os registros no momento
                    crud.mostrarTodos();
                    System.out.println("Aperte ENTER para continuar");
                    ler.nextLine();
                    ler.nextLine();
                break;
            }
            printInterface();
            x = ler.nextInt();
        }

        csv.close();
        ler.close();
    }
}
