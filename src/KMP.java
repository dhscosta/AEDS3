//Classe para a implementação do algoritmo de casamento de padrões KMP
class KMP {
    private int comparacoes;    //contagem de comparações da execução do algoritmo

    // Construtor da classe
    KMP(){
        comparacoes = 0;
    }

    //Método de acesso de comparações
    public int getComp(){
        return comparacoes;
    }

    //Método para a construção do array de estados que será usado para o casamento de padrões 
    private int[] arrayEstados(String padrao){
        //Declaração de variáveis
        int i = 1, j = 0;
        int[] estados = new int[padrao.length()];
        estados[0] = 0;

        //Loop para a construção do array de estados
        while(i < padrao.length()){
            comparacoes++;
            if(padrao.charAt(j) == padrao.charAt(i)){
                j = j + 1;
                estados[i] = j;
                i++;
                comparacoes++;
            }else{
                if(j == 0){
                    estados[i] = j;
                    i++;
                    comparacoes++;
                }else{
                    j = estados[j-1];
                }
            }
        }

        //retorno do array de estados
        return estados;
    }

    //Método para pesquisa com casamento de padrões KMP (sem sobrescrita)
    public void search(String padrao, String texto){
        //inicialização de variáveis
        int i = 0, j = 0;

        //construcao do array de estados utilizando o padrao desejado
        int[] estados = arrayEstados(padrao);

        /*
         * procura usando array de bytes
         * 
         * byte[] text = texto.getBytes();
         * byte[] pad = padrao.getBytes();
         * 
         * Comparação de byte[] com byte[], funciona, e  byte[] com String também funciona
        */

        //loop para a pesquisa do padrão em cima do texto
        while(j < texto.length()){
            comparacoes++;
            if(padrao.charAt(i) == texto.charAt(j)){
                comparacoes++;
                j++; i = i + 1;
                if(i == padrao.length()){
                    comparacoes++;
                    i = 0; //i = estados[i-1];
                    System.out.println("Achou: " + texto);
                    return;
                }
            }else{
                if(i > 0){
                    comparacoes++;
                    i = estados[i-1];
                }else{
                    j++;
                }
            }
        }
    }
}