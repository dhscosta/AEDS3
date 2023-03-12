//Dependências
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.DataFormatException;

class Crud{
    //atributos da classe
    private int identificador;          
    
    //construtor
    public Crud() throws FileNotFoundException
    {
        this.identificador = 0;
    }

    public int getIdentificador(){return this.identificador;}

    /*mostrarTodos - Método para mostrar todos os registros do arquivo
     *Parâmetros - nenhum
     *Retorno - void
    */
    public void mostrarTodos() throws Exception
    {
        //declaração de variáveis
        RandomAccessFile arq = new RandomAccessFile("gamees.bin", "r");
        arq.seek(0);
        int tam;
        byte[] c;
        
        for(int i = 0; i < identificador; i++){
            //percorre todos os registros até encontrar o registro com o id
            Game gamer = new Game();
            char z = arq.readChar();
            tam = arq.readInt();
            if(z == '#')                     //se registro tem lápide
            {
                arq.skipBytes(tam);         //pula o equivalente ao número de bytes do registro
            }
            else{                           //lê registro 
                c = new byte[tam];
                arq.read(c);
                gamer.fromByte(c);
                System.out.print(i + " ");
                gamer.mostrar();        //atribui o registro desejado a variável de retorno
            }
        }
        arq.close();
    }


    /*format - Método para formatar cadeia de Strings, na forma que será usada nos registros do arquivo
     *Parâmetros - String[] line - Arranjo de Strings, obtido a partir da leitura do arquivo csv
     *Retorno - Game game - objeto Game
    */
    public Game format(String[] line) throws DataFormatException, ParseException{

        //Declaração de variáveis
        SimpleDateFormat fDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Game game = new Game();                             //variável de retorno

        //Atribuindo os valores de 'line' no objeto
        game.setId(Integer.parseInt(line[0]));
        game.setTitle(line[1]);
        game.setData(fDateFormat.parse(line[2]));
        game.setWin(Boolean.parseBoolean(line[3]));
        game.setMac(Boolean.parseBoolean(line[4]));
        game.setLinux(Boolean.parseBoolean(line[5]));
        game.boolToArray();                                 //transforma os 3 booleanos em um arranjo de String
        game.toSigla(line[6]);                              //transforma a string da avaliaçao em uma sigla de 2 digitos 
        game.setPrice(Float.parseFloat(line[9]));


        return game;
    }
    
    /*create - método para criar registro no arquivo binário
     *Parâmetros -  String[] line - Arranjo de Strings, obtido a partir da leitura do arquivo csv
    */
    public void create(String[] line) throws FileNotFoundException, IOException, DataFormatException, ParseException{
        
        //declaração de variáveis
        RandomAccessFile arq = new RandomAccessFile("gamees.bin", "rw");
        byte[] b;
        Game game = format(line);                               //inicia um objeto game usando "line" já formatado
        long tamArq = arq.length();

        //transforma o objeto em um arranjo de bytes e escreve no arquivo (create)
        b = game.toByte();
        arq.seek(tamArq);
        arq.writeChar(' ');
        arq.writeInt(b.length);
        arq.write(b);
        
        identificador++; //incrementa o número de registros
        arq.close();
    }

    /*read - método para procurar registro no arquivo binário
     *Parâmetros -  int id - chave de procura
     *Retorno - void - o jogo vai ser exibido de dentro da funçao
    */
    public boolean read(int id)throws FileNotFoundException, IOException{
        
        //declaração de variáveis
        RandomAccessFile arq = new RandomAccessFile("gamees.bin", "r");
        arq.seek(0);
        int tam;
        byte[] c;
        
        for(int i = 0; i < identificador; i++){
            //percorre todos os registros até encontrar o registro com o id
            Game gamer = new Game();
            char z = arq.readChar();
            tam = arq.readInt();
            if(z == '#')                     //se registro tem lápide
            {
                arq.skipBytes(tam);         //pula o equivalente ao número de bytes do registro
            }
            else{                           //lê registro e o compara com id procurado
                c = new byte[tam];
                arq.read(c);
                gamer.fromByte(c);
                if(gamer.getId() == id)
                {
                    gamer.mostrar();        //atribui o registro desejado a variável de retorno
                    return true;                  //interrompe a iteração
                }
            }
        }
        System.out.println("Registro não encontrado");
        arq.close();
        return false;
    }

    /*update - método para atualizar registro já existente no arquivo binário
     *Parâmetros -  todos os atributos da classe Game
    */
    public void update( int id, String title, boolean win, boolean mac, boolean linux, String rating, float price, Date data ) throws IOException, FileNotFoundException{
        
        //Declaração de variáveis
        RandomAccessFile arq = new RandomAccessFile("gamees.bin", "rw");
        int tamanho;
        byte[] c;
        arq.seek(0);
        
        for(int i = 0; i < identificador; i++){
            //percorre todos os registros ate encontrar o registro com o id
            long posP = arq.getFilePointer();
            char l = arq.readChar();
            tamanho = arq.readInt();
            if(l == '#')
            {
                arq.skipBytes(tamanho);
            }
            else{
                c = new byte[tamanho];
                arq.read(c);
                Game jogoArq = new Game();
                jogoArq.fromByte(c);
            
                if(jogoArq.getId() == id)
                {
                    //quando encontrar, atribui os valores que serão atualizadas (dentro de um novo objeto)
                    Game newGame = new Game();
                    newGame.setId(id); 
                    newGame.setTitle(title); 
                    newGame.setWin(win);
                    newGame.setMac(mac);
                    newGame.setLinux(linux);
                    newGame.boolToArray();
                    newGame.setRating(rating);
                    newGame.toSigla(rating);
                    newGame.setPrice(price);
                    newGame.setData(data);
                    byte[] d = newGame.toByte();
                    newGame.mostrar();

                    if(c.length >= d.length){
                        //se couber no mesmo registro faz um clone do registro atualizado, senao...                        
                        arq.seek(posP);
                        arq.writeChar(' ');
                        arq.writeInt(c.length);
                        arq.write(d);
                        
                        return;
                    }else{
                        arq.seek(posP);        //colocar o ponteiro na lapide
                        arq.writeChar('#');       //insere lápide no registro

                        //insere o novo game (atualizado) no final do arquivo                        
                        arq.seek(arq.length());
                        arq.writeChar(' ');
                        arq.writeInt(d.length);
                        arq.write(d);
                        identificador++;
                        return;
                    }
                }
            }
        }
        arq.close();
    }

    /*delete - método para remover, logicamente, registros do arquivo binário
     *Parâmetros -  todos os atributos da classe Game
    */
    public void delete(int id) throws IOException, FileNotFoundException {
        
        //Declaração de variáveis
        RandomAccessFile arq = new RandomAccessFile("gamees.bin", "rw");
        arq.seek(0);
        int tam;
        byte[] c;
        char z;
        
        for(int i = 0; i < identificador; i++){
            //percorre todos os registros até encontrar o registro com o id
            long posL= arq.getFilePointer();
            z = arq.readChar();
            tam = arq.readInt();
            if(z == '#')
            {
                arq.skipBytes(tam);
            }
            else
            {
                c = new byte[tam];
                arq.read(c);
                Game gamer = new Game();
                gamer.fromByte(c);
                if(gamer.getId() == id)
                {
                    arq.seek(posL);
                    arq.writeChar('#');     //faz remoção lógica inserindo lápide no registro
                    return;      //acaba com o laço de iteração
                }
            }
        }
        arq.close();
    }
}
