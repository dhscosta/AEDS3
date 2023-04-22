import java.io.*;
import java.text.Format;
import java.util.Scanner;

import com.opencsv.CSVReader;

public class ArqIndices {

    Diretorio diretorio;
    String arqDir;
    String arqBuc;
    RandomAccessFile fileDir;
    RandomAccessFile fileBuc;
    int bucTamanho;
    Crud crud;

    class Bucket {
        private int[] ids; //ids dos games
        private long[] registros; //endereços dos games
        private int quantidade;
        private byte profundidadeLocal;
        
        //construtor
        public Bucket(int tamanho, int profundidade) throws Exception {
            if(tamanho > 1060){
                throw new Exception("A quantidade maxima de cada bucket e de 1060 elementos");
            }
            if(profundidade > 10){
                throw new Exception("A profundidade máxima é de 10 ");
            }
            this.ids = new int[tamanho];
            this.registros = new long[tamanho];
            this.quantidade = 0;
            this.profundidadeLocal = (byte)profundidade;
        }

        /*toByteArray - método para escrever os buckets em bytes 
         *Parâmetros -  nenhum
        */
        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeShort(quantidade);
            dos.writeShort(profundidadeLocal);
            int x=0;
            while(x < quantidade) {
                dos.writeInt(ids[x]);
                dos.writeLong(registros[x]);
                x++;
            }
            while(x < 1060) {
                dos.writeInt(0);
                dos.writeLong(0);
                x++;
            }
            return baos.toByteArray();            
        }
        /*fromByteArray - método para escrever os buckets de bytes para o formato (inteiro e endereço) 
         *Parâmetros -  o array de bytes do bucket
        */
        public void fromByteArray(byte[] ba) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(ba);
            DataInputStream dis = new DataInputStream(bais);

            quantidade = dis.readShort();
            profundidadeLocal = dis.readByte();
            int i=0;
            while(i<1060) {
                ids[i] = dis.readInt();
                registros[i] = dis.readLong();
                i++;
            }
        }
        /*inserir - método para inserir um id e o endereço
         *Parâmetros -  o id e o endereço
        */
        public boolean inserir(int id, long end) throws Exception{
            //não inserir se o bucket estiver cheio(o tratamento de divisao de bucket é feito no diretorio)
            if(quantidade == 1060)
            {
                return false;
            }

            int x = quantidade - 1;
            //posicionar os arrays para liberar espaço (eles são guardados de forma ordenada)
            while(x >= 0 && id < ids[x]) {
                ids[x+1] = ids[x];
                registros[x+1] = registros[x];
                x--;
            }
            x++;
            // escreve o id e o endereço
            ids[x] = id;
            registros[x] = end;
            quantidade++;
            return true;
        }
        /*buscar - método para buscar um endereço através do id
         *Parâmetros -  o id
        */
        public long buscar(int id) {
            //se não houver elementos no bucket retorna -1
            if(quantidade == 0){
                return -1;
            }

            int x = 0;
            // enquanto houver elementos comparar o id pesquisado com os ids presentes
            while (x < quantidade){
                if(id == ids[x]){
                    return registros[x];
                }
                x++;
            }
            return -1;
        }
        /*update - método para dar update no endereço de um ID
         *Parâmetros -  o id e o endereço
        */
        public boolean update (int id, long end){
            //se não houver nenhum retorna falso
            if(quantidade == 0){
                return false;
            }

            int y = 0;
            while (y < quantidade && id > ids[y]){
                y++;
            }
            //se tiver parado pq o id é igual e a quantidade ainda for menor então atualiza o endereço
            if(y < quantidade && id == ids[y]){
                registros[y] = end;
                return true;
            }
            return false;
        }
        /*delete - método para dar delete no endereço de um ID
         *Parâmetros -  o id e o endereço
        */
        public boolean delete (int id){
            //se for vazio não há o que deletar 
            if (quantidade ==0){
                return false;
            }

            int y = 0; 
            while (y < quantidade && id > ids[y]){
                y++;
            } 
            //se tiver parado pq o id é igual deletar o id e o endereço
            if(id == ids[y]){
                while (y < quantidade -1 ){
                    ids[y] = ids[y + 1];
                    registros[y] = registros[y + 1];
                    y++;
                }
                quantidade--;
                return true;
            }
            return false;
        }
    }
    
    class Diretorio {
        private long[] ends; //endereço dos buckets
        private byte profundidadeGlobal;
        
        public Diretorio() {
            this.ends = new long[1];
            ends[0] = 0;
            this.profundidadeGlobal = 0;
        }
        /*toByteArray - método para escrever os diretorios em bytes 
         *Parâmetros -  nenhum
        */
        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            int quantidade = (int)Math.pow(2,profundidadeGlobal);
            int x = 0;

            dos.writeByte(profundidadeGlobal);
            while(x < quantidade) {
                dos.writeLong(ends[x]);
                x++;
            }
            return baos.toByteArray();            
        }
        /*toByteArray - método para escrever os o diretorio de bytes em formato de long(manipulação em memoria primaria) 
         *Parâmetros -  diretorio em bytes
        */
        public void fromByteArray(byte[] ba) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(ba);
            DataInputStream dis = new DataInputStream(bais);
            profundidadeGlobal = dis.readByte();
            int quantidade = (int)Math.pow(2,profundidadeGlobal);
            ends = new long[quantidade];
            int x = 0;

            while(x < quantidade) {
                ends[x] = dis.readLong();
                x++;
            }
        }
        /*addProfundidade - método para aumentar + 1 na profundidade global 
         *Parâmetros -  nenhum
        */
        private void addProfundidade(){
            this.profundidadeGlobal = profundidadeGlobal++;
        }
        /*addProfundidade - método para settar os endereços dos buckets 
         *Parâmetros -  os endereços
        */
        private void setEnds(long[] newEnds){
            this.ends = newEnds;
        }
        /*hash - método para fazer o hash do id
         *Parâmetros -  id
        */
        private int hash(int id) {
            return id % (int)Math.pow(2, profundidadeGlobal);
        }
        /*hash - método para fazer o hash do id cm a profundidade local
         *Parâmetros -  id e profundidade
        */
        private int hashLocal(int id, int local){
            return (id % (int)Math.pow(2, local));
        }

        /*getEnd - método para pegar os endereços em uma posição
         *Parâmetros - posiçao
        */
        private long getEnd(int p) {
            if( p > Math.pow(2,profundidadeGlobal)){
                return -1;
            }
            return ends[p];
        }
        /*newDirEnd - método para trocar um determinado endereço
         *Parâmetros -  endereço e a posição
        */
        private boolean newDirEnd(int x, long end) {

            if(x > Math.pow(2,profundidadeGlobal)){
                return false;
            }
            ends[x] = end;
            return true;
        }
    }

    //construtor
    public ArqIndices(int tamanhoBuc) throws Exception{
        //inicializa o arquivo do diretorio e do bucket e um CRUD
        fileDir = new RandomAccessFile("diretorio","rw");
        fileBuc = new RandomAccessFile("bucket","rw");
        crud = new Crud();

        //se o diretorio ou o bucket tiver vazios criar novos buckets e diretorio
        if(fileDir.length() == 0 || fileBuc.length() == 0){
            this.diretorio = new Diretorio();
            fileDir.write(diretorio.toByteArray());

            Bucket buc = new Bucket(tamanhoBuc, 0);
            fileBuc.seek(0);
            fileBuc.write(buc.toByteArray());
        }
    }
    /*create - método para ler pegar ler o game, pegar o endereço, ler o id e inserir na tabela hash
         *Parâmetros -  linha do csv
    */
    public void create(String[] line) throws Exception{
        Game game = crud.format(line);             //inicializa o game
        long end = crud.create(game);              //coloca no arquivo binário e retorna o endereço de onde foi inserido
        RandomAccessFile arqCrud = new RandomAccessFile("gamees.bin", "rw");
        arqCrud.seek(end);                         //coloca o ponteiro do arquivo onde foi inserido
        char lapide = arqCrud.readChar(); 
        int id = arqCrud.readInt();                //lê o id do game inserido
        arqCrud.close();
        inserirHash(end, id);                      //insere o endereço e o id do game lido
    } 
    /*inserirHash - método para inserir o id e o endereço do game lido dentro do bucket
         *Parâmetros -  endereço e id
    */
    public boolean inserirHash(long end, int id) throws Exception{
        //inicializa o diretorio
        int tam = (int)fileDir.length();     //lê o tamanho do arquivo do diretorio para passar pra bytes
        byte[] b = new byte[tam];            // passa pra bytes
        fileDir.seek(0);            
        fileDir.read(b);                     //lê
        diretorio = new Diretorio();
        diretorio.fromByteArray(b);          //transforma em objeto
        int hash = diretorio.hash(id);       // faz o hash do id recebido
        long endBuc = diretorio.getEnd(hash);  // acha o endereço referente à esse id
        //inicializa o bucket
        Bucket buc = new Bucket(1060, 0);     
        byte[] bucByte = new byte[12723];       //inicializa um array de bytes com o tamanho maximo do bucket(para 1067 registros)
        fileBuc.seek(endBuc);                   //posiciona o ponteiro no endereço com o hash
        fileBuc.read(bucByte);                  //lê esse bucket 
        buc.fromByteArray(bucByte);             //transforma em objeto

        //testa se o bucket não está cheio
        if(buc.quantidade < 1060){
            // se não estiver é so inserir
            buc.inserir(id, end);
            fileBuc.seek(endBuc);
            fileBuc.write(buc.toByteArray());
            return true;
        }
        // se estiver
        else{
            byte profLoc = buc.profundidadeLocal;
            //testa se ainda pode aumentar a profundidade do bucket
            if(buc.profundidadeLocal >= diretorio.profundidadeGlobal || diretorio.profundidadeGlobal < 10){
                //se puder aumenta a profundidade e muda os endereços
                diretorio.addProfundidade();
                long[] newEnds = new long [(int)Math.pow(2,diretorio.profundidadeGlobal)];
                int x = 0;
                while ( x < (int)Math.pow(2, diretorio.profundidadeGlobal-1)){
                    newEnds[x] = diretorio.ends[x];
                    x++;
                }
                while (x < (int)Math.pow(2, diretorio.profundidadeGlobal)){
                    newEnds[x] = diretorio.ends[x - (int)Math.pow(2, diretorio.profundidadeGlobal-1) ];
                    x++;
                }
                diretorio.setEnds(newEnds);
            }
            //se não for possivel ja esta cheio
            else{
                throw new Exception("Não é possivel inserir mais, pois está cheio");
            }
            //depois de aumentar a profundidade e criar novos endereços, inserir nos buckets novos
            byte profGlob = diretorio.profundidadeGlobal;

            Bucket buc2 = new Bucket(1060, buc.profundidadeLocal+1);
            fileBuc.seek(endBuc);
            fileBuc.write(buc2.toByteArray());

            Bucket buc3 = new Bucket(1060, buc.profundidadeLocal + 1);
            long newEndBuc = fileBuc.length();
            fileBuc.seek(newEndBuc);
            fileBuc.write(buc3.toByteArray());
            //faz o hash para a profundidade local
            int i = diretorio.hashLocal(id, buc.profundidadeLocal);
            int controle = 1;
            //atualiza os endereços do diretório para os novos buckets

            for(int x = i; x < (int)Math.pow(2, profLoc) ; x += (int)Math.pow(2, profGlob)){
                if(controle % 2 == 0){
                    diretorio.newDirEnd(x, newEndBuc);
                }
                controle++;
            }
            //lê o diretorio novo e escreve no arquivo do diretorio
            b = diretorio.toByteArray();
            fileDir.seek(0);
            fileDir.write(b);
            //faz a inserção dos registros antigos 
            for(int x = 0; x < buc.quantidade; x++){
                inserirHash(buc.registros[x], buc.ids[x]);
            }
            //chama o hash novamente, agora que tem novos buckets para inserir
            inserirHash(end, id);
            return false;

        }
    }
    /*buscarHash - método para buscar um id dentro da tabela hash
    *Parâmetros -  id 
    */
    public void buscar (int id) throws Exception{
        long pos = buscarHash(id);    // pega a posição com a função abaixo

        if(pos == -1){
         throw new Exception("Chave inexistente");
        }
        
        //inicializa o arquivo em binario para retornar o objeto pesquisado
        RandomAccessFile arqCrud = new RandomAccessFile("gamees.bin", "rw");
        arqCrud.seek(pos);              //ponteiro no endereço guardado com este id
        char lapide = arqCrud.readChar();
        int tam = arqCrud.readInt();
        byte[] b = new byte[tam];
        arqCrud.read(b);
        Game game = new Game();
        game.fromByte(b);
        game.mostrar();
    }

    /*buscarHash - método para buscar um id dentro da tabela hash
    *Parâmetros -  id 
    */
    public long buscarHash(int id) throws Exception{
        //inicializa o diretorio e lê do arquivo 
        byte[] b = new byte[(int)fileDir.length()];
        fileDir.seek(0);
        fileDir.read(b);
        this.diretorio = new Diretorio();
        diretorio.fromByteArray(b);        //transforma em objeto

        int hash = diretorio.hash(id);     //pega o hash do id pesquisado

        //inicializa o bucket no endereço do hash
        long endBuc = diretorio.getEnd(hash);
        Bucket buc = new Bucket(1060, 0);
        byte[] bucByte = new byte[12723];
        fileBuc.seek(endBuc);
        fileBuc.read(bucByte);
        buc.fromByteArray(bucByte);        //trasforma em objeto

        return buc.buscar(id);           // busca dentro do bucket encontrado e retorna a posiçao dele
    }

    public void updateHash(int id, long pos, String[] line) throws Exception{
        /*nao consegui fazer */
    }
    /*delete - método para deletar um id dentro da tabela hash
    *Parâmetros -  id 
    */
    public boolean delete(int id) throws Exception{
        //inicializa o diretorio
        int tamArq = (int)arqDir.length();
        byte[] b = new byte[tamArq];
        fileDir.seek(0);
        fileDir.read(b);
        diretorio = new Diretorio();
        diretorio.fromByteArray(b); //transforma em objeto

        int hash = diretorio.hash(id);
        //inicializa o bucket do diretorio
        long endBuc = diretorio.getEnd(hash);
        Bucket buc = new Bucket(1060, 0);
        byte[] b2 = new byte[12723];
        fileBuc.seek(endBuc);
        fileBuc.read(b2);
        buc.fromByteArray(b2); //transforma em objeto
        
        // chama o delete do bucket para esse objeto
        if(!buc.delete(id)){
            return false;
        }

        fileBuc.seek(endBuc);
        fileBuc.write(buc.toByteArray()); //escreve o bucket depois de deletar
        return true;
    }

    /*lerHash - método para chamar o create com o csv reader
    *Parâmetros -  id 
    */
    public void lerHash() throws Exception{
        CSVReader csv = new CSVReader(new FileReader("games.csv"));

        for(int x = 0; x < 41; x++){
            create(csv.readNext());
        }
        csv.close();
        
    }
    
}