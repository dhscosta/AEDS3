import java.io.*;
import java.util.*;
import java.util.zip.DataFormatException;
import java.text.*;

public class ArqIndices {

    Diretorio diretorio;
    String arqDir;
    String arqBuc;
    RandomAccessFile fileDir;
    RandomAccessFile fileBuc;
    int bucTamanho;

    class Bucket {
        private int[] ids;
        private Game[] registros;
        private int quantidade;
        private int profundidadeLocal;
    
        public Bucket(int tamanho, int profundidade) throws Exception {
            if(tamanho > 1060){
                throw new Exception("A quantidade maxima de cada bucket e de 1060 elementos");
            }
            if(profundidade > 10){
                throw new Exception("A profundidade máxima é de 10 ");
            }
            this.ids = new int[tamanho];
            this.registros = new Game[tamanho];
            this.quantidade = 0;
            this.profundidadeLocal = profundidade;
            for (int x = 0; x < tamanho; x++)
            {
                registros[x] = new Game();
            }
        }

        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            Game auxGame = new Game();
            dos.writeShort(quantidade);
            dos.writeShort(profundidadeLocal);
            int x=0;
            while(x < quantidade) {
                dos.writeInt(ids[x]);
                dos.write(registros[x].toByte());
                x++;
            }
            while(x < 1060) {
                dos.writeInt(0);
                dos.write(auxGame.toByte());
                x++;
            }
            return baos.toByteArray();            
        }

        public void fromByteArray(byte[] ba) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(ba);
            DataInputStream dis = new DataInputStream(bais);
            quantidade = dis.readShort();
            profundidadeLocal = dis.readShort();

            int i=0;
            while(i<1060) {
                System.out.println(i);
                ids[i] = dis.read();
                dis.read(ba);
                registros[i].fromByte(ba); 
                i++;
            }
        }

        public boolean inserir(int newId, Game reg) throws Exception{
            if(quantidade == 1060)
            {
                return false;
            }

            int x = quantidade - 1;

            while(x >= 0 && newId < ids[x]) {
                ids[x+1] = ids[x];
                registros[x+1] = registros[x];
                x--;
            }
            x++;
            ids[x] = newId;
            registros[x] = reg;
            quantidade++;
            return true;
        }
    
        public Game buscar(int id) {
            if(quantidade == 0){
                return null;
            }

            int x = 0;
            while (x < quantidade){
                if(id == ids[x]){
                    return registros[x];
                }
                x++;
            }
            return null;
        }
        

    }
    
    class Diretorio {
        private long[] ends;
        private int profundidadeGlobal;
    
        public Diretorio() {
            this.profundidadeGlobal = 10;
            int qnt = (int)Math.pow(2,profundidadeGlobal);
            this.ends = new long[qnt];
            for(int x = 0; x < (int)Math.pow(2,profundidadeGlobal); x++){
                ends[x] = x;
            }
        }

        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
        
            dos.writeByte(profundidadeGlobal);
            int quantidade = (int)Math.pow(2,profundidadeGlobal);
            int x = 0;
            while(x < quantidade) {
                dos.writeLong(ends[x]);
                x++;
            }
            return baos.toByteArray();            
        }

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

        private void addProfundidade(){
            this.profundidadeGlobal = profundidadeGlobal++;
        }

        private void setEnds(long[] newEnds){
            this.ends = newEnds;
        }

        private int hash(int id) {
            System.out.println(profundidadeGlobal);
            System.out.println(id);
            return (id % (profundidadeGlobal*2));

        }

        private int hashLocal(int id, int local){
            return (id % (local * 2));
        }

        private long getEnd(int p) {
            if( p > Math.pow(2,profundidadeGlobal)){
                return -1;
            }
            return ends[p];
        }

        private boolean newDirEnd(int x, long end) {

            if(x > Math.pow(2,profundidadeGlobal)){
                return false;
            }
            ends[x] = end;
            return true;
        }
    }

    public ArqIndices(int tamanhoBuc) throws Exception{
        fileDir = new RandomAccessFile("diretorio","rw");
        fileBuc = new RandomAccessFile("bucket","rw");

        if(fileDir.length() == 0 || fileBuc.length() == 0){
            this.diretorio = new Diretorio();
            fileDir.write(diretorio.toByteArray());

            Bucket buc = new Bucket(tamanhoBuc, 10);
            fileBuc.seek(0);
            fileBuc.write(buc.toByteArray());
        }
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

    public void inserir(String[] line) throws Exception{
        Game jogo = format(line);
        int id = jogo.getId();
        inserir(jogo, id);
    }

    public boolean inserir(Game reg, int id) throws Exception{

        int tam = (int)fileDir.length();
        byte[] b = new byte[tam];
        fileDir.seek(0);
        fileDir.read(b);
        diretorio = new Diretorio();
        //diretorio.fromByteArray(b);
        int hash = diretorio.hash(id);

        long endBuc = diretorio.getEnd(hash);
        Bucket buc = new Bucket(1060, 10);
        byte[] bucByte = new byte[12723];
        fileBuc.seek(endBuc);
        fileBuc.read(bucByte);
        buc.fromByteArray(bucByte); 
        if(buc.buscar(id) != null){
            throw new Exception("Id repetido");
        }

        if(buc.quantidade < 1060){
            buc.inserir(id, reg);
            fileBuc.seek(endBuc);
            fileBuc.write(buc.toByteArray());
            return true;
        }
        else{
            int profLoc = buc.profundidadeLocal;
            int profGlob = diretorio.profundidadeGlobal;
            if(buc.profundidadeLocal >= diretorio.profundidadeGlobal || diretorio.profundidadeGlobal < 10){
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
            else{
                throw new Exception("Não é possivel inserir mais, pois está cheio");
            }

            Bucket buc2 = new Bucket(1060, buc.profundidadeLocal+1);
            fileBuc.seek(endBuc);
            fileBuc.write(buc2.toByteArray());

            Bucket buc3 = new Bucket(1060, buc.profundidadeLocal + 1);
            long newEndBuc = fileBuc.length();
            fileBuc.seek(newEndBuc);
            fileBuc.write(buc3.toByteArray());

            int i = diretorio.hashLocal(id, buc.profundidadeLocal);
            int controle = 1;
            for(int x = i; x < (int)Math.pow(2, profLoc) ; x += (int)Math.pow(2, profGlob)){
                if(controle % 2 == 0){
                    diretorio.newDirEnd(x, newEndBuc);
                }
                controle++;
            }

            b = diretorio.toByteArray();
            fileDir.seek(0);
            fileDir.write(b);

            for(int x = 0; x < buc.quantidade; x++){
                inserir(buc.registros[x], buc.ids[x]);
            }
            inserir(reg,id);
            return false;

        }
    }

    public Game buscar(int id) throws Exception{
        byte[] b = new byte[(int)fileDir.length()];
        fileDir.seek(0);
        fileDir.read(b);

        this.diretorio = new Diretorio();
        diretorio.fromByteArray(b);

        int hash = diretorio.hash(id);

        long endBuc = diretorio.getEnd(hash);
        Bucket buc = new Bucket(1060, 0);
        byte[] bucByte = new byte[12723];
        fileBuc.seek(endBuc);
        fileBuc.read(bucByte);
        buc.fromByteArray(bucByte);

        return buc.buscar(id);
    }

    public void print() {
        try {
            byte[] bd = new byte[(int)fileDir.length()];
            fileDir.seek(0);
            fileDir.read(bd);
            diretorio = new Diretorio();
            diretorio.fromByteArray(bd);   
            System.out.println("\nDIRETÓRIO ------------------");
            System.out.println(diretorio);

            System.out.println("\nCESTOS ---------------------");
            fileBuc.seek(0);
            while(fileBuc.getFilePointer() != fileBuc.length()) {
                Bucket c = new Bucket(1060, 0);
                byte[] ba = new byte[68];
                fileBuc.read(ba);
                c.fromByteArray(ba);
                System.out.println(c);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
   
}
    /* 
    public int buscar(int id) {
        ArqIndices indice = diretorio.buscar(id);
        return indice.getPosicao();
    }

    public static void main(String[] args) throws Exception {
    
    }
    */
