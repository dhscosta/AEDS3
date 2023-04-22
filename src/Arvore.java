//dependencias
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.zip.DataFormatException;
import java.util.*;

//Classe para estrutura de dados Árvore B+
class Arvore{
    private final int ordem = 8;                                                     //número máximo de filhos
    private final int maxChaves = ordem - 1;                                         //máximo de chaves e de dados que uma página pode ter
    private RandomAccessFile arquivo;                                                //arquivo onde será escrito a árvore
    private int chaveAux;                                                            //chave para auxílio nas movimentações
    private Game jogoAux = new Game();                                               //objeto auxiliar para ajudar nas movimentações
    private long pagAux;                                                             //variável para auxílio com endereços no arquivo de índice
    private boolean split = false;                                                   //variável de auxílio para controle de separação de páginas
    private boolean fusao = false;                                                   //variável de auxílio para controle de fusão de páginas
    private final int TAMANHO_GAME = 4 + 80 + 3 + 30 + 2 + 4 + 8;                    //tamanho do objeto Game
    private final int TAMANHO_REGISTRO = TAMANHO_GAME + 4;                           //tamanho do dado junto com sua chave
    private final int TAMANHO_PAGINA = 4+(TAMANHO_REGISTRO*maxChaves)+(ordem*8) + 16;//tamanho total da página

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
            byte[] ba = new byte[TAMANHO_GAME];

            //lê a quantidade de elementos na pagina
            quantidade = in.readInt();

            //lê todos os elementos da pagina
            int i = 0;
            for(i = 0; i < quantidade; i++){
                filhos[i] = in.readLong();
                chaves[i] = in.readInt();
                in.read(ba);
                jogo.fromByte(ba);
                dados[i] = jogo.clone();
            }

            //lê o último filho da página
            filhos[i] = in.readLong();

            //lê o ponteiro para a próxima página
            proxima = in.readLong();
        }
    }

    /*
     * Método privado para completar o tamanho das strings para não ocorrer problemas de tamanho diferentes
    */
    private String completaBrancos(String str, int tamanho) {
        char[] resposta = new char[tamanho];

        int i = 0;
        while(i < str.length()){
            resposta[i] = str.charAt(i);
            i++;
        }

        while(i < tamanho){
            resposta[i] = ' ';
            i++;
        }

        String aux = new String(resposta);

        return aux;
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
        String aux;

        //Atribuindo os valores de 'line' no objeto
        game.setId(Integer.parseInt(line[0]));
        aux = completaBrancos(line[1], 80);
        game.setTitle(aux);
        game.setData(fDateFormat.parse(line[2]));
        game.setWin(Boolean.parseBoolean(line[3]));
        game.setMac(Boolean.parseBoolean(line[4]));
        game.setLinux(Boolean.parseBoolean(line[5]));
        game.boolToArray();                                 //transforma os 3 booleanos em um arranjo de String
        game.toSigla(line[6]);                              //transforma a string da avaliaçao em uma sigla de 2 digitos 
        game.setPrice(Float.parseFloat(line[9]));


        return game;
    }

    /*
     * Método para criar um objeto pagina e escrevelo no arquivo em forma de bytes 
    */
    public boolean create(String[] line)throws IOException, DataFormatException, ParseException{
        //transforma a linha em um objeto game
        Game jogo = format(line).clone();

        //cópia da chave
        int chave = jogo.getId();

        //teste para prevenir a utilização de chave inválida
        if(chave < 0){
            System.out.println("Chave não pode ser negativa");
            return false;
        }

        //utilização de variáveis auxiliares
        chaveAux = chave;
        jogoAux = jogo.clone();
        pagAux = -1;
        split = false;

        //posiciona o ponteiro do arquivo para o início e guarda o endereço da raiz em uma variável
        arquivo.seek(0);
        long pagina = arquivo.readLong();

        //chama a função auxiliar para a criação do objeto na árvore
        boolean inserido = create1(pagina);

        //se ocorreu o split e retornou a este ponto, cria uma nova raiz
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

    //Método para criar o objeto página
    public boolean create1(long pagina)throws IOException{
        
        //se a página for filha de uma página folha, utiliza as variáveis globais de auxílio 
        if(pagina == -1){
            split = true;
            pagAux = -1;
            return true;
        }

        //inicializa um objeto pagina lendo-o do arquivo
        byte[] ba = new byte[TAMANHO_PAGINA];
        arquivo.seek(pagina);
        arquivo.read(ba);
        Pagina pa = new Pagina();
        pa.setByte(ba);

        //encontro o ponto certo, na teoria, de inserção
        int i = 0;
        while(i < pa.quantidade && chaveAux > pa.chaves[i]){
            i++;
        }

        //acaba com a chamada de função, caso a chave já existir
        if(chaveAux == pa.chaves[i] && pa.filhos[0] == -1){
            System.out.println("Chave já existente");
            return false;
        }

        boolean resposta = false;

        //chamada recursiva, para acessar os filhos das páginas
        if(chaveAux < pa.chaves[i]){
            resposta = create1(pa.filhos[i]);
        }else if(i == pa.quantidade){
            resposta = create1(pa.filhos[i+1]);
        }
        
        //se a folha comportar mais uma inclusao, apenas inclui na pagina
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

        //se split == false, acaba a chamada aqui, pois a partir daqui são
        //tratamentos de caso para caso se precise de fragmentar a pagina
        if(!split){
            return resposta;
        }

        //cria uma nova página e copia metade dos elementos para essa página
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

        //se o local de inserção for menor que a metade, novo registro deve ficar na pagina da esquerda
        if(i <= metade){
            //passa todos os elementos uma casa a frente
            for(int j = metade; j > 0 && j > i; j--){
                pa.chaves[j] = pa.chaves[j-1];
                pa.dados[j] = pa.dados[j-1].clone();
                pa.filhos[j+1] = pa.filhos[j];
            }

            //insere o elemento
            pa.chaves[i] = chaveAux;
            pa.dados[i] = jogoAux.clone();
            pa.filhos[i] = pagAux;
            pa.quantidade++;

            //se a página for folha retorna o primeiro elemento da página da direita
            if(pa.filhos[0] == -1){
                chaveAux = novaPagina.chaves[0];
                jogoAux = novaPagina.dados[0].clone();
            }else{ //se não for folha, retorna o último elemento da página da esquerda e o remove da página
                chaveAux = pa.chaves[pa.quantidade-1];
                jogoAux = pa.dados[pa.quantidade-1].clone();
                pa.chaves[pa.quantidade-1] = -1;
                pa.dados[pa.quantidade-1] = new Game();
                pa.filhos[pa.quantidade] = -1;
                pa.quantidade--;
            }

        }else{ // se o local de inserção for maior que a metade

            //copia todos os elementos uma casa a frente
            for(int j = maxChaves-metade; j > 0 && j > i; j--){
                novaPagina.chaves[j] = novaPagina.chaves[j-1];
                novaPagina.dados[j] = novaPagina.dados[j-1].clone();
                novaPagina.filhos[j+1] = novaPagina.filhos[j];
            }

            //insere o elemento
            novaPagina.chaves[i] = chaveAux;
            novaPagina.dados[i] = jogoAux.clone();
            novaPagina.filhos[i+1] = pagAux;
            novaPagina.quantidade++;

            //utiliza as variáveis auxiliares para promover o elemento correto
            chaveAux = novaPagina.chaves[0];
            jogoAux = novaPagina.dados[0].clone();
            
            if(pa.filhos[0] != -1){ // se a página não for folha, remove o elemento promovido
                for(int j = 0; j < novaPagina.quantidade-1; j++){
                    novaPagina.chaves[j] = novaPagina.chaves[j+1];
                    novaPagina.dados[j] = novaPagina.dados[j+1].clone();
                    novaPagina.filhos[j] = novaPagina.filhos[j+1];
                }
                novaPagina.filhos[novaPagina.quantidade-1] = novaPagina.filhos[novaPagina.quantidade]; 

                //apaga o ultimo elemento
                novaPagina.chaves[novaPagina.quantidade-1] = -1;
                novaPagina.dados[novaPagina.quantidade-1] = new Game();
                novaPagina.filhos[novaPagina.quantidade-1] = -1;
                novaPagina.quantidade--;   
            }
        }
        
        //se a página for folha, atualiza os ponteiros para as proximas folhas
        if(pa.filhos[0] == -1){
            novaPagina.proxima = pa.proxima;
            pa.proxima = arquivo.length();
        }

        //escreve as alterações no arquivo de índices
        pagAux = arquivo.length();
        arquivo.seek(pagAux);
        arquivo.write(novaPagina.getByte());

        arquivo.seek(pagina);
        arquivo.write(pa.getByte());

        return resposta;
    }

    //Método público de chamada da leitura
    public Game read(int ID)throws IOException{
        //copia a ID a ser procurado
        chaveAux = ID;

        //posiciona o arquivo e lê o endereço da raiz
        arquivo.seek(0);
        pagAux = arquivo.readLong();

        //se a chave for inválida acaba com a função
        if(ID < 0){
            System.out.println("Chave inválida");
            return new Game();
        }

        //chama o método que de fato faz a leitura
        Game jogo = read1(pagAux).clone();

        return jogo;
    }

    //Método de busca recursiva na árvore
    private Game read1(long pagina)throws IOException{
        Game jogo = new Game();
        
        //teste de validade da página
        if(pagina == -1){
            System.out.println("Arvore vazia ou objeto nao encontrado");
            return jogo;
        }

        //lê a página do arquivo
        Pagina pa = new Pagina();
        arquivo.seek(pagina);
        byte[] ba = new byte[TAMANHO_PAGINA];
        arquivo.read(ba);
        pa.setByte(ba);

        //acha a posição correta do objeto
        int i = 0;
        while(i < pa.quantidade && chaveAux > pa.chaves[i]){
            i++;
        }

        //se a página for folha, a chave corresponder e o i não tiver ultrapassado a quantidade de elementos
        //retorna o elemento
        if(pa.filhos[0] == -1 && chaveAux == pa.chaves[i] && i < pa.quantidade){
            return pa.dados[i];
        }

        //chama a recursão para as próximas páginas
        if(pa.chaves[i] > chaveAux || i == pa.quantidade){
            jogo = read1(pa.filhos[i]);
        }else{
            jogo = read1(pa.filhos[i+1]);
        }

        return jogo;
    }

    //método para fazer update de um objeto
    public boolean update(int ID, String title, boolean win, boolean mac, boolean linux, String rating, float price, Date data)throws IOException{
        boolean teste;
        Game jogo = new Game();

        //armazena todos os dados em um objeto game
        jogo.setId(ID);
        jogo.setTitle(title);
        jogo.setWin(win);
        jogo.setMac(mac);
        jogo.setLinux(linux);
        jogo.boolToArray();
        jogo.setRating(rating);
        jogo.setPrice(price);
        jogo.setData(data);

        //passa os valores para as variáveis auxiliares
        chaveAux = ID;
        jogoAux = jogo.clone();

        //lê o endereço da raiz
        arquivo.seek(0);
        long pagina = arquivo.readLong();

        //chama o método recursivo de update
        teste = update1(pagina);

        return teste;
    }

    //Método privado recursivo para fazer update de um objeto
    private boolean update1(long pagina)throws IOException{
        boolean teste;

        //acaba com a execução de um código a página não estiver populada
        if(pagina == -1){
            System.out.println("Arvore vazia ou objeto nao encontrado");
            return false;
        }

        //lê a página do arquivo
        Pagina pa = new Pagina();
        byte[] ba = new byte[TAMANHO_PAGINA];
        arquivo.seek(pagina);
        arquivo.read(ba);
        pa.setByte(ba);

        //acha a posição correta do elemento
        int i = 0;
        while(i < pa.quantidade && chaveAux > pa.chaves[i]){
            i++;
        }
        
        //se encontrar o elemento, faz o seu update e retorna a função
        if(pa.filhos[0] == -1 && pa.chaves[i] == chaveAux && i < pa.quantidade){
            pa.dados[i] = jogoAux.clone();
            arquivo.seek(pagina);
            arquivo.write(pa.getByte());
            return true;
        }

        //chama a recursão
        if(i == pa.quantidade || pa.chaves[i] > chaveAux){
            teste = update1(pa.filhos[i]);
        }else{
            teste = update1(pa.filhos[i+1]);
        }

        return teste;
    }

    //Método público para chamada da função de exclusão
    public boolean delete(int ID)throws IOException{
        boolean teste;

        //copia o endereço para a variável global
        chaveAux = ID;

        //lê a posição da raiz da árvore
        arquivo.seek(0);
        long pagina = arquivo.readLong();

        //chama função recursiva de exclusão
        teste = delete1(pagina);

        return teste;
    }

    //Método recursivo para a exclusão de dados da árvore
    private boolean delete1(long pagina)throws IOException{
        boolean teste, esq;
        int metade = maxChaves/2;
        int filho = 0;

        //acaba com a função caso não encontre o objeto a ser excluído
        if(pagina == -1){
            System.out.println("Arvore vazia ou objeto nao encontrado");
            return false;
        }

        //lê a página do arquivo
        Pagina pa = new Pagina();
        byte[] ba = new byte[TAMANHO_PAGINA];
        arquivo.seek(pagina);
        arquivo.read(ba);
        pa.setByte(ba);

        //acha a posição correta do objeto
        int i = 0;
        while(i < pa.quantidade && chaveAux > pa.chaves[i]){
            i++;
        }
        
        //se o objeto é encontrado em uma folha o exclui
        if(pa.filhos[0] == -1 && pa.chaves[i] == chaveAux && i < pa.quantidade){
            //move todos os elementos uma casa antes, sobrescrevendo o objeto a ser excluido
            int j = 0;
            for(j = i; j < pa.quantidade-1; j++){
                pa.chaves[j] = pa.chaves[j+1];
                pa.dados[j] = pa.dados[j+1].clone();
            }

            //apaga o último objeto, para não ficar duplicado
            pa.chaves[pa.quantidade] = -1;
            pa.dados[pa.quantidade] = new Game();
            pa.quantidade--;

            //escreve a página no arquivo
            arquivo.seek(pagina);
            arquivo.write(pa.getByte());

            //faz o teste para saber se tem a necessidade de tratamento de excessão
            fusao = pa.quantidade < metade;
            return true;
        }

        //chamada de recursão
        if(i == pa.quantidade || pa.chaves[i] > chaveAux){
            teste = update1(pa.filhos[i]);
            filho = i;
        }else{
            teste = update1(pa.filhos[i+1]);
            filho = i+1;
        }

        //inicializa variaveis para auxilio
        Pagina pagFilho;
        long paginaFilho = pa.filhos[filho];
        Pagina pagIrmao;
        long paginaIrmao;

        //se houver a necessidade de fusão
        if(fusao){

            //inicializa a página filho
            ba = new byte[TAMANHO_PAGINA];
            arquivo.seek(paginaFilho);
            arquivo.read(ba);
            pagFilho = new Pagina();
            pagFilho.setByte(ba);

            //inicializa a página irmão
            pagIrmao = new Pagina();
            paginaIrmao = pa.filhos[filho-1];

            //se existir o filho, faz a primeira tentativa com o irmão da esquerda
            if(filho > 0){

                //inicializa a pagina irmao
                ba = new byte[TAMANHO_PAGINA];
                arquivo.seek(paginaIrmao);
                arquivo.read(ba);
                pagIrmao = new Pagina();
                pagIrmao.setByte(ba);

                //testa a possibilidade do irmao ceder um elemento
                if(pagIrmao.quantidade > metade){
                    
                    //move todos os elementos para a direita, para ceder espaço
                    esq = true;
                    int j;
                    for(j = pagFilho.quantidade; j > 0; j--){
                        pagFilho.chaves[j] = pagFilho.chaves[j-1];
                        pagFilho.dados[j] = pagFilho.dados[j-1].clone();
                        pagFilho.filhos[j+1] = pagFilho.filhos[j];
                    }

                    pagFilho.filhos[j+1] = pagFilho.filhos[j];
                    pagFilho.quantidade++;

                    //se for folha copia o elemento do irmão
                    if(pagFilho.filhos[0] == -1){
                        pagFilho.chaves[0] = pagIrmao.chaves[pagIrmao.quantidade-1];
                        pagFilho.dados[0] = pagIrmao.dados[pagIrmao.quantidade-1].clone();
                    }else{ // se não rotaciona os elementos
                        pagFilho.chaves[0] = pa.chaves[filho-1];
                        pagFilho.dados[0] = pa.dados[filho-1];
                    }

                    //copia o elemento do irmão
                    pa.chaves[filho-1] = pagIrmao.chaves[pagIrmao.quantidade-1];
                    pa.dados[filho-1] = pagIrmao.dados[pagIrmao.quantidade-1].clone();

                    //reduz os elementos do irmão
                    pagFilho.filhos[0] = pagIrmao.filhos[pagIrmao.quantidade];
                    pagIrmao.quantidade--;

                    fusao = false;

                }else{ // se não puder ceder faz o processo de fusão
                    //se a página não for folha, copia o elemento do pai para o irmão
                    esq = true;
                    if(pagIrmao.filhos[0] != -1){
                        pagIrmao.chaves[pagIrmao.quantidade] = pa.chaves[filho-1];
                        pagIrmao.dados[pagIrmao.quantidade] = pa.dados[filho-1].clone();
                        pagIrmao.filhos[pagIrmao.quantidade+1] = pagFilho.filhos[0];
                        pagIrmao.quantidade++;
                    }

                    //copia os registros para o irmão da esquerda
                    for(int j = 0; j < pagFilho.quantidade; j++){
                        pagIrmao.chaves[pagIrmao.quantidade] = pagFilho.chaves[j];
                        pagIrmao.dados[pagIrmao.quantidade] = pagFilho.dados[j].clone();
                        pagIrmao.filhos[pagIrmao.quantidade+1] = pagFilho.filhos[j+1];
                        pagIrmao.quantidade++;
                    }

                    pagFilho.quantidade = 0; //"acaba" com o filho

                    //se a página for folha, copia o ponteiro para a proxima folha
                    if(pagIrmao.filhos[0] == -1){
                        pagIrmao.proxima = pagFilho.proxima;
                    }

                    //copia os registros no pai, passando-os uma casa para esquerda
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

                    fusao = pa.quantidade < metade; // testa a quantidade de elementos do pai
                }

                //faz fusão com o irmão da direita
                if(esq == false){
                    //inicializa a página irmã
                    paginaIrmao = pa.filhos[filho+1];
                    ba = new byte[TAMANHO_PAGINA];
                    pagIrmao = new Pagina();
                    arquivo.seek(paginaIrmao);
                    arquivo.read(ba);
                    pagIrmao.setByte(ba);

                    //testa se o irmão pode ceder algum elemento
                    if(pagIrmao.quantidade > metade){
                        
                        //se for folha
                        if(pagIrmao.filhos[0] == -1){
                            //copia o elemento do irmão
                            pagFilho.chaves[pagFilho.quantidade] = pagIrmao.chaves[0];
                            pagFilho.dados[pagFilho.quantidade] = pagIrmao.dados[0].clone();
                            pagFilho.quantidade++;
                            
                            //promove o elemento do irmão
                            pa.chaves[filho] = pagIrmao.chaves[1];
                            pa.dados[filho] = pagIrmao.dados[1].clone();
                        }else{ // se não for folha rotaciona os elementos

                            //copia o elemento do pai
                            pagFilho.chaves[pagFilho.quantidade] = pa.chaves[filho];
                            pagFilho.dados[pagFilho.quantidade] = pa.dados[filho].clone();
                            pagFilho.filhos[pagFilho.quantidade+1] = pagIrmao.filhos[0];
                            pagFilho.quantidade++;
    
                            //promove o elemento do irmão
                            pa.chaves[filho] = pagIrmao.chaves[0];
                            pa.dados[filho] = pagIrmao.dados[0].clone();
                        }
    
                        //move todos os elementos do irmão para a esquerda
                        int j;
                        for(j = 0; j < pagIrmao.quantidade-1; j++){
                            pagIrmao.chaves[j] = pagIrmao.chaves[j+1];
                            pagIrmao.dados[j] = pagIrmao.dados[j+1];
                            pagIrmao.filhos[j] = pagIrmao.filhos[j+1];
                        }

                        //diminui a quantidade do irmão
                        pagIrmao.filhos[j] = pagIrmao.filhos[j+1];
                        pagIrmao.quantidade--;
    
                        fusao = false;
                    }else{ //se não puder ceder

                        //se a página não for folha, copia o elemento do pai para o filho
                        if(pagFilho.filhos[0] != -1){
                            pagFilho.chaves[pagFilho.quantidade] = pa.chaves[filho-1];
                            pagFilho.dados[pagFilho.quantidade] = pa.dados[filho-1].clone();
                            pagFilho.filhos[pagFilho.quantidade+1] = pagIrmao.filhos[0];
                            pagFilho.quantidade++;
                        }
    
                        //copia todos os elementos do irmão para o filho
                        for(int j = 0; j < pagFilho.quantidade; j++){
                            pagFilho.chaves[pagFilho.quantidade] = pagIrmao.chaves[j];
                            pagFilho.dados[pagFilho.quantidade] =pagIrmao.dados[j].clone();
                            pagFilho.filhos[pagFilho.quantidade+1] = pagIrmao.filhos[j+1];
                            pagFilho.quantidade++;
                        }
    
                        pagIrmao.quantidade = 0; //"apaga" a página irmão
    
                        //se a página for folha, copia os endereços para as próximas folhas
                        if(pagFilho.filhos[0] == -1){
                            pagFilho.proxima = pagIrmao.proxima;
                        }
    
                        //move os elementos do pai para a esquerda
                        int j = 0;
                        for(j = filho; j < pa.quantidade-1; j++){
                            pa.chaves[j] = pa.chaves[j+1];
                            pa.dados[j] = pa.dados[j+1].clone();
                            pa.filhos[j+1] = pa.filhos[j+2];
                        }

                        //diminui a quantidade do pai
                        pa.quantidade--;

                        fusao = pa.quantidade < metade; // testa a necessidade de fusao
                    }
                }
            }

            //atualiza os registros no arquivo
            arquivo.seek(pagina);
            arquivo.write(pa.getByte());
            arquivo.seek(paginaFilho);
            arquivo.write(pagFilho.getByte());
            arquivo.seek(paginaIrmao);
            arquivo.write(pagIrmao.getByte());

        }

        return teste;
    }

    //Método para apagar o arquivo de índices
    public void apagar(String nome)throws IOException, FileNotFoundException{
        File f = new File(nome);
        f.delete();

        arquivo = new RandomAccessFile(nome, "rw");
        arquivo.writeLong(-1);
    }
    
}