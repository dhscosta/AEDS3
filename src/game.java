import java.io.*;
import java.text.*;
import java.util.*;

class Game {
    static SimpleDateFormat default_dateFormat = new SimpleDateFormat("yyy-mm-dd", Locale.ENGLISH);    
    //atributos
    protected int g_id;
    protected String title;
    protected boolean win, mac, linux;
    protected ArrayList<String> plataformas  ; //os valores booleanos serão armazenados em forma de string dentro de um array
    protected String rating;
    protected float price;
    protected Date data;
    
    //getters
    public int getId()               {return this.g_id;}
    public String getTitle()         {return this.title;}
    public boolean getWin()          {return this.win;}
    public boolean getMac()          {return this.mac;}
    public boolean getLinux()        {return this.linux;}
    public ArrayList<String> getPlataformas() {return this.plataformas;}
    public String getRating()        {return this.rating;}
    public float getPrice()          {return this.price;}
    public Date getData()            {return this.data;}
    //setters
    public void setId(int id)                        {this.g_id = id;}
    public void setTitle(String title)               {this.title = title;}
    public void setWin(boolean win)                  {this.win = win;}
    public void setMac(boolean mac)                  {this.mac = mac;}
    public void setLinux(boolean linux)              {this.linux = linux;}
    public void setPlataformas(ArrayList<String> plataformas) {this.plataformas = plataformas;}
    public void setRating (String review)        {this.rating = review;}
    public void setPrice(float price)                {this.price = price;}
    public void setData(Date data)                   {this.data = data;}

    //construtores
    public Game(){
        this.g_id = -1;
        this.title = null; 
        this.win = this.mac = this.linux = false;
        this.plataformas = new ArrayList<String>(3);
        this.rating = null;
        this.price = -1;
        this.data = null;
    }

    public Game(int id, String title, boolean win, boolean mac, boolean linux, ArrayList<String> plat, String review, float price, Date data)
    {
        this.g_id = id;
        this.title = title; 
        this.win = win;
        this.mac = mac;
        this.linux = linux;
        this.plataformas = plat;
        this.rating = review;
        this.price = price;
        this.data = data;
    }
    

    //metodo para transformar os valores booleanos (win,mac,linus) em um arranjo de string
    public void boolToArray() {

        if(this.win == false)    {plataformas.add("false");} 
        else                    {plataformas.add("true");}

        if (this.mac == false)   {plataformas.add("false");}
        else                    {plataformas.add("true");}

        if (this.linux == false) {plataformas.add("false");}
        else                    {plataformas.add("true");}
    
    }

    public void mostrar()
    {

        System.out.println(g_id + " , " + title + " , " + data + " , " + plataformas + " , " + rating + " , " + price);

    }
    //transforma as avaliacoes de usuarios em siglas de tamanho fixo
    public void toSigla(String s) 
    {
        if      (s.compareTo("Very Positive") == 0)   {setRating("VP");}
        else if (s.compareTo("Positive") == 0)        {setRating("PO");}
        else if (s.compareTo("Mixed") == 0)           {setRating("MI");}
        else if (s.compareTo("Mostly Positive") == 0) {setRating("MP");}
        else    {setRating("ER");}
    }

    public Game clone ()
    {
        Game game = new Game();
        game.setId(this.getId());
        game.setTitle(this.getTitle());
        game.setData(this.getData());
        game.setWin(this.getWin());
        game.setMac(this.getMac());
        game.setLinux(this.getLinux());
        game.setPlataformas(this.getPlataformas());
        game.setRating(this.getRating());
        game.setPrice(this.getPrice());

        return game;
    }

    //retorna um arranjo de bytes com os valores do game (registro)
    public byte[] toByte() throws IOException{

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(g_id);
        dos.writeUTF(title); 
        dos.writeLong(data.getTime());

        //transforma cada posicao do arranjo de String
        for(int x = 0; x < plataformas.size(); x++)
        {
            dos.writeUTF(plataformas.get(x));
        }
        dos.writeUTF(rating);
        dos.writeFloat(price);

        return baos.toByteArray();
    }

    //lê um registro e transforma em um objeto (game)
    public void fromByte(byte ba[]) throws IOException{

        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        g_id=dis.readInt();
        title=dis.readUTF();
        data = new Date(dis.readLong());
        
        for(int x = 0; x < 3; x++)
        {
            plataformas.add(dis.readUTF());
        }

        rating = dis.readUTF();

        price = dis.readFloat();

    }
    
}
