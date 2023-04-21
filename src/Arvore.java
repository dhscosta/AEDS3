import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.zip.DataFormatException;
import java.util.*;

class Arvore{
    private final int ordem = 8;
    private final int maxChaves = ordem - 1;
    private RandomAccessFile arquivo;
    private int chaveAux;
    private Game jogoAux = new Game();
    private long pagAux;
    private boolean split = false;
    private boolean fusao = false;
    private final int TAMANHO_REGISTRO = 4 + 10 + 3 + 2 + 1 + 4 + 8 + 4;
    private final int TAMANHO_PAGINA = 4+(TAMANHO_REGISTRO*maxChaves)+(ordem*8);

    /*
     * Classe para operar as paginas da arvore internamente. 
    */
    private class Pagina{
        private int    quantidade;      //quantidade de elementos presente na pagina
        private int[]  chaves;          //arranjo das chaves na pagina
        private Game[] dados;           //arranjo dos dados da arvore
        private long[] filhos;          //arranjo de ponteiros para os filhos da pagina
        private long   proxima;         //ponteiro para a proxima pagina

        /*
         * Construtor da classe 
        */
        public Pagina(){
            
            //estabelecendo valores padrões e populando toda a página, com valores também padrões
            quantidade = 0;
            chaves = new int[maxChaves];
            dados = new Game[maxChaves];
            filhos = new long[ordem];
            proxima = -1;

            for(int i = 0; i < maxChaves; i++){
                chaves[i] = -1;
                dados[i] = new Game();
                filhos[i] = -1;
            }

            filhos[maxChaves] = -1;
        }

        /*
         * Metodo para transformar pagina em um vetor de bytes 
        */
        public byte[] getByte()throws IOException{
            
            //Variaveis utilizadas para a contrucao do arranjo de bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            //Escreve a quantidade de elementos presentes na pagina
            out.writeInt(quantidade);

            //Escreve todos os filhos, chaves e dados da pagina
            int i;
            for(i = 0; i < quantidade; i++){
                out.writeLong(filhos[i]);
                out.writeInt(chaves[i]);
                out.write(dados[i].toByte());
            }

            //escreve ultimo filho
            out.writeLong(filhos[i]);

            //escreve o ponteiro para a proxima pagina
            out.writeLong(proxima);

            //retorno do arranjo de bytes
            return baos.toByteArray();
        }

        /*
         *Metodo que executa o processo inverso da função getByte() 
        */
        public void setByte(byte[] buffer)throws IOException{
            
            //Variaveis utilizadas para a leitura do arranjo de bytes
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            DataInputStream in = new DataInputStream(bais);

            //variaveis auxiliares para a leitura
            Game jogo = new Game();
            byte[] ba = new byte[TAMANHO_PAGINA];

            //lê a quantidade de elementos na pagina
            quantidade = in.readInt();

            //lê todos os elementos da pagina
            int i = 0;
            for(i = 0; i < quantidade; i++){
                filhos[i] = in.readLong();
                chaves[i] = in.readInt();
                in.read(ba);
                jogo.fromByte(ba);
                dados[i] = jogo;
            }

            //lê o último filho da página
            filhos[i] = in.readLong();

            //lê o ponteiro para a próxima página
            proxima = in.readLong();
        } 
    }

    public Arvore(String nome)throws IOException{
        //inicialização do arquivo e escreve uma raiz vazia
        arquivo = new RandomAccessFile(nome, "rw");
        arquivo.writeLong(-1);
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

    public boolean create(String[] line)throws IOException, DataFormatException, ParseException{
        Game jogo = format(line).clone();

        int chave = jogo.getId();

        if(chave < 0){
            System.out.println("Chave não pode ser negativa");
            return false;
        }

        chaveAux = chave;
        jogoAux = jogo.clone();
        pagAux = -1;
        split = false;

        arquivo.seek(0);
        long pagina = arquivo.readLong();

        boolean inserido = create1(pagina);

        if(split){
            Pagina pa = new Pagina();
            pa.quantidade = 1;
            pa.chaves[0] = chaveAux;
            pa.dados[0] = jogoAux.clone();
            pa.filhos[0] = pagina;
            pa.filhos[1] = pagAux;

            arquivo.seek(arquivo.length());
            long raiz = arquivo.getFilePointer();
            arquivo.write(pa.getByte());
            arquivo.seek(0);
            arquivo.writeLong(raiz);

        }
        
        return inserido;
    }

    public boolean create1(long pagina)throws IOException{
        
        if(pagina == -1){
            split = true;
            pagAux = -1;
            return true;
        }

        byte[] ba = new byte[TAMANHO_PAGINA];
        arquivo.seek(pagina);
        arquivo.read(ba);
        Pagina pa = new Pagina();
        pa.setByte(ba);

        int i = 0;
        while(i < pa.quantidade && chaveAux > pa.chaves[i]){
            i++;
        }

        if(chaveAux == pa.chaves[i] && pa.filhos[0] == -1){
            System.out.println("Chave já existente");
            return false;
        }

        boolean resposta = false;
        if(chaveAux < pa.chaves[i]){
            resposta = create1(pa.filhos[i]);
        }else if(i == pa.quantidade){
            resposta = create1(pa.filhos[i+1]);
        }
        
        if(!split){
            return resposta;
        }

        if(pa.quantidade < maxChaves){

            for(int j = pa.quantidade; j > i; j--){
                pa.chaves[j] = pa.chaves[j-1];
                pa.dados[j] = pa.dados[j-1].clone();
                pa.filhos[j+1] = pa.filhos[j];
            }
         
            pa.quantidade++;
            pa.chaves[i] = chaveAux;
            pa.dados[i] = jogoAux.clone();
            pa.filhos[i+1] = -1;

            arquivo.seek(pagina);
            arquivo.write(pa.getByte());

            return true;
        }

        Pagina novaPagina = new Pagina();

        int metade = maxChaves/2;
        for(int j = 0; j < (maxChaves-metade); j++){
            novaPagina.chaves[j] = pa.chaves[j+metade];
            novaPagina.dados[j] = pa.dados[j+metade].clone();
            novaPagina.filhos[j+1] = pa.filhos[j+metade+1];

            pa.chaves[j+metade] = -1;
            pa.dados[j+metade] = new Game();
            pa.filhos[j+metade+1] = -1;
        }

        novaPagina.filhos[0] = pa.filhos[metade];
        novaPagina.quantidade = maxChaves-metade;
        pa.quantidade = metade;

        if(i <= metade){
            for(int j = metade; j > 0 && j > i; j--){
                pa.chaves[j] = pa.chaves[j-1];
                pa.dados[j] = pa.dados[j-1].clone();
                pa.filhos[j+1] = pa.filhos[j];
            }

            pa.chaves[i] = chaveAux;
            pa.dados[i] = jogoAux.clone();
            pa.filhos[i] = pagAux;
            pa.quantidade++;

            if(pa.filhos[0] == -1){
                chaveAux = novaPagina.chaves[0];
                //jogoAux = novaPagina.dados[0].clone();
            }else{
                chaveAux = pa.chaves[pa.quantidade-1];
                //jogoAux = pa.dados[pa.quantidade-1].clone();
                pa.chaves[pa.quantidade-1] = -1;
                pa.dados[pa.quantidade-1] = new Game();
                pa.filhos[pa.quantidade] = -1;
                pa.quantidade--;
            }

        }else{

            for(int j = maxChaves-metade; j > 0 && j > i; j--){
                novaPagina.chaves[j] = novaPagina.chaves[j-1];
                novaPagina.dados[j] = novaPagina.dados[j-1].clone();
                novaPagina.filhos[j+1] = novaPagina.filhos[j];
            }

            novaPagina.chaves[i] = chaveAux;
            novaPagina.dados[i] = jogoAux.clone();
            novaPagina.filhos[i+1] = pagAux;
            novaPagina.quantidade++;

            chaveAux = novaPagina.chaves[0];
            //jogoAux = novaPagina.dados[0].clone();
            
            if(pa.filhos[0] != -1){
                for(int j = 0; j < novaPagina.quantidade-1; j++){
                    novaPagina.chaves[j] = novaPagina.chaves[j+1];
                    novaPagina.dados[j] = novaPagina.dados[j+1].clone();
                    novaPagina.filhos[j] = novaPagina.filhos[j+1];
                }
                novaPagina.filhos[novaPagina.quantidade-1] = novaPagina.filhos[novaPagina.quantidade]; 

                novaPagina.chaves[novaPagina.quantidade-1] = -1;
                novaPagina.dados[novaPagina.quantidade-1] = new Game();
                novaPagina.filhos[novaPagina.quantidade-1] = -1;
                novaPagina.quantidade--;   
            }
        }
        
        if(pa.filhos[0] == -1){
            novaPagina.proxima = pa.proxima;
            pa.proxima = arquivo.length();
        }

        pagAux = arquivo.length();
        arquivo.seek(pagAux);
        arquivo.write(novaPagina.getByte());

        arquivo.seek(pagina);
        arquivo.write(pa.getByte());

        return resposta;
    }

    public Game read(int ID)throws IOException{
        chaveAux = ID;

        arquivo.seek(0);
        pagAux = arquivo.readLong();

        if(ID < 0){
            System.out.println("Chave inválida");
            return new Game();
        }

        Game jogo = read1(pagAux).clone();

        return jogo;
    }

    private Game read1(long pagina)throws IOException{
        Game jogo = new Game();
        
        if(pagina == -1){
            System.out.println("Arvore vazia ou objeto nao encontrado");
            return jogo;
        }

        Pagina pa = new Pagina();
        arquivo.seek(pagina);
        byte[] ba = new byte[TAMANHO_PAGINA];
        arquivo.read(ba);
        pa.setByte(ba);

        int i = 0;
        while(i < pa.quantidade && chaveAux > pa.chaves[i]){
            i++;
        }

        if(pa.filhos[0] == -1 && chaveAux == pa.chaves[i] && i < pa.quantidade){
            return pa.dados[i];
        }

        if(pa.chaves[i] > chaveAux || i == pa.quantidade){
            jogo = read1(pa.filhos[i]);
        }else{
            jogo = read1(pa.filhos[i+1]);
        }

        return jogo;
    }

    public boolean update(int ID, String title, boolean win, boolean mac, boolean linux, String rating, float price, Date data)throws IOException{
        boolean teste;
        Game jogo = new Game();

        jogo.setId(ID);
        jogo.setTitle(title);
        jogo.setWin(win);
        jogo.setMac(mac);
        jogo.setLinux(linux);
        jogo.boolToArray();
        jogo.setRating(rating);
        jogo.setPrice(price);
        jogo.setData(data);

        chaveAux = ID;
        jogoAux = jogo.clone();

        arquivo.seek(0);
        long pagina = arquivo.readLong();

        teste = update1(pagina);

        return teste;
    }

    private boolean update1(long pagina)throws IOException{
        boolean teste;

        if(pagina == -1){
            System.out.println("Arvore vazia ou objeto nao encontrado");
            return false;
        }

        Pagina pa = new Pagina();
        byte[] ba = new byte[TAMANHO_PAGINA];
        arquivo.seek(pagina);
        arquivo.read(ba);
        pa.setByte(ba);

        int i = 0;
        while(i < pa.quantidade && chaveAux > pa.chaves[i]){
            i++;
        }
        
        if(pa.filhos[0] == -1 && pa.chaves[i] == chaveAux && i < pa.quantidade){
            pa.dados[i] = jogoAux.clone();
            arquivo.seek(pagina);
            arquivo.write(pa.getByte());
            return true;
        }

        if(i == pa.quantidade || pa.chaves[i] > chaveAux){
            teste = update1(pa.filhos[i]);
        }else{
            teste = update1(pa.filhos[i+1]);
        }

        return teste;
    }

    public boolean delete(int ID)throws IOException{
        boolean teste;

        chaveAux = ID;

        arquivo.seek(0);
        long pagina = arquivo.readLong();

        teste = delete1(pagina);

        if(teste && fusao){

        }

        return teste;
    }

    private boolean delete1(long pagina)throws IOException{
        boolean teste, esq;
        int metade = maxChaves/2;
        int filho = 0;

        if(pagina == -1){
            System.out.println("Arvore vazia ou objeto nao encontrado");
            return false;
        }

        Pagina pa = new Pagina();
        byte[] ba = new byte[TAMANHO_PAGINA];
        arquivo.seek(pagina);
        arquivo.read(ba);
        pa.setByte(ba);

        int i = 0;
        while(i < pa.quantidade && chaveAux > pa.chaves[i]){
            i++;
        }
        
        if(pa.filhos[0] == -1 && pa.chaves[i] == chaveAux && i < pa.quantidade){
            int j = 0;
            for(j = i; j < pa.quantidade-1; j++){
                pa.chaves[j] = pa.chaves[j+1];
                pa.dados[j] = pa.dados[j+1].clone();
            }

            pa.chaves[pa.quantidade] = -1;
            pa.dados[pa.quantidade] = new Game();
            pa.quantidade--;

            arquivo.seek(pagina);
            arquivo.write(pa.getByte());

            fusao = pa.quantidade < metade;
            return true;
        }

        if(i == pa.quantidade || pa.chaves[i] > chaveAux){
            teste = update1(pa.filhos[i]);
            filho = i;
        }else{
            teste = update1(pa.filhos[i+1]);
            filho = i+1;
        }

        Pagina pagFilho;
        long paginaFilho = pa.filhos[filho];
        Pagina pagIrmao;
        long paginaIrmao;

        if(fusao){
            ba = new byte[TAMANHO_PAGINA];
            arquivo.seek(paginaFilho);
            arquivo.read(ba);
            pagFilho = new Pagina();
            pagFilho.setByte(ba);

            pagIrmao = new Pagina();
            paginaIrmao = pa.filhos[filho-1];

            if(filho > 0){
                ba = new byte[TAMANHO_PAGINA];
                arquivo.seek(paginaIrmao);
                arquivo.read(ba);
                pagIrmao = new Pagina();
                pagIrmao.setByte(ba);

                if(pagIrmao.quantidade > metade){
                    esq = true;
                    int j;
                    for(j = pagFilho.quantidade; j > 0; j--){
                        pagFilho.chaves[j] = pagFilho.chaves[j-1];
                        pagFilho.dados[j] = pagFilho.dados[j-1].clone();
                        pagFilho.filhos[j+1] = pagFilho.filhos[j];
                    }

                    pagFilho.filhos[j+1] = pagFilho.filhos[j];
                    pagFilho.quantidade++;

                    if(pagFilho.filhos[0] == -1){
                        pagFilho.chaves[0] = pagIrmao.chaves[pagIrmao.quantidade-1];
                        pagFilho.dados[0] = pagIrmao.dados[pagIrmao.quantidade-1].clone();
                    }else{
                        pagFilho.chaves[0] = pa.chaves[filho-1];
                        pagFilho.dados[0] = pa.dados[filho-1];
                    }

                    pa.chaves[filho-1] = pagIrmao.chaves[pagIrmao.quantidade-1];
                    pa.dados[filho-1] = pagIrmao.dados[pagIrmao.quantidade-1].clone();

                    pagFilho.filhos[0] = pagIrmao.filhos[pagIrmao.quantidade];
                    pagIrmao.quantidade--;

                    fusao = false;

                }else{
                    esq = true;
                    if(pagIrmao.filhos[0] != -1){
                        pagIrmao.chaves[pagIrmao.quantidade] = pa.chaves[filho-1];
                        pagIrmao.dados[pagIrmao.quantidade] = pa.dados[filho-1].clone();
                        pagIrmao.filhos[pagIrmao.quantidade+1] = pagFilho.filhos[0];
                        pagIrmao.quantidade++;
                    }

                    for(int j = 0; j < pagFilho.quantidade; j++){
                        pagIrmao.chaves[pagIrmao.quantidade] = pagFilho.chaves[j];
                        pagIrmao.dados[pagIrmao.quantidade] = pagFilho.dados[j].clone();
                        pagIrmao.filhos[pagIrmao.quantidade+1] = pagFilho.filhos[j+1];
                        pagIrmao.quantidade++;
                    }

                    pagFilho.quantidade = 0;

                    if(pagIrmao.filhos[0] == -1){
                        pagIrmao.proxima = pagFilho.proxima;
                    }

                    int j = 0;
                    for(j = filho-1; j < pa.quantidade-1; j++){
                        pa.chaves[j] = pa.chaves[j+1];
                        pa.dados[j] = pa.dados[j+1].clone();
                        pa.filhos[j+1] = pa.filhos[j+2];
                    }

                    pa.chaves[j] = -1;
                    pa.dados[j] = new Game();
                    pa.filhos[j+1] = -1;
                    pa.quantidade--;

                    fusao = pa.quantidade < metade;
                }

                if(esq == false){
                    paginaIrmao = pa.filhos[filho+1];
                    ba = new byte[TAMANHO_PAGINA];
                    pagIrmao = new Pagina();
                    arquivo.seek(paginaIrmao);
                    arquivo.read(ba);
                    pagIrmao.setByte(ba);

                    if(pagIrmao.quantidade > metade){
                        if(pagIrmao.filhos[0] == -1){
                            pagFilho.chaves[pagFilho.quantidade] = pagIrmao.chaves[0];
                            pagFilho.dados[pagFilho.quantidade] = pagIrmao.dados[0].clone();
                            pagFilho.quantidade++;
                            
                            pa.chaves[filho] = pagIrmao.chaves[1];
                            pa.dados[filho] = pagIrmao.dados[1].clone();
                        }else{
                            pagFilho.chaves[pagFilho.quantidade] = pa.chaves[filho];
                            pagFilho.dados[pagFilho.quantidade] = pa.dados[filho].clone();
                            pagFilho.filhos[pagFilho.quantidade+1] = pagIrmao.filhos[0];
                            pagFilho.quantidade++;
    
                            pa.chaves[filho] = pagIrmao.chaves[0];
                            pa.dados[filho] = pagIrmao.dados[0].clone();
                        }
    
                        int j;
                        for(j = 0; j < pagIrmao.quantidade-1; j++){
                            pagIrmao.chaves[j] = pagIrmao.chaves[j+1];
                            pagIrmao.dados[j] = pagIrmao.dados[j+1];
                            pagIrmao.filhos[j] = pagIrmao.filhos[j+1];
                        }
                        pagIrmao.filhos[j] = pagIrmao.filhos[j+1];
                        pagIrmao.quantidade--;
    
                        fusao = false;
                    }else{
                        if(pagFilho.filhos[0] != -1){
                            pagFilho.chaves[pagFilho.quantidade] = pa.chaves[filho-1];
                            pagFilho.dados[pagFilho.quantidade] = pa.dados[filho-1].clone();
                            pagFilho.filhos[pagFilho.quantidade+1] = pagIrmao.filhos[0];
                            pagFilho.quantidade++;
                        }
    
                        for(int j = 0; j < pagFilho.quantidade; j++){
                            pagFilho.chaves[pagFilho.quantidade] = pagIrmao.chaves[j];
                            pagFilho.dados[pagFilho.quantidade] =pagIrmao.dados[j].clone();
                            pagFilho.filhos[pagFilho.quantidade+1] = pagIrmao.filhos[j+1];
                            pagFilho.quantidade++;
                        }
    
                        pagIrmao.quantidade = 0;
    
                        if(pagFilho.filhos[0] == -1){
                            pagFilho.proxima = pagIrmao.proxima;
                        }
    
                        int j = 0;
                        for(j = filho; j < pa.quantidade-1; j++){
                            pa.chaves[j] = pa.chaves[j+1];
                            pa.dados[j] = pa.dados[j+1].clone();
                            pa.filhos[j+1] = pa.filhos[j+2];
                        }

                        pa.quantidade--;
                        fusao = pa.quantidade < metade;
                    }
                }
            }

            arquivo.seek(pagina);
            arquivo.write(pa.getByte());
            arquivo.seek(paginaFilho);
            arquivo.write(pagFilho.getByte());
            arquivo.seek(paginaIrmao);
            arquivo.write(pagIrmao.getByte());

        }

        return teste;
    }

    public void mostrarTodos(){
        
    }

    public void apagar(String nome)throws IOException, FileNotFoundException{
        File f = new File(nome);
        f.delete();

        arquivo = new RandomAccessFile(nome, "rw");
        arquivo.writeLong(-1);
    }
    
}