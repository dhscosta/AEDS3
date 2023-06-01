//Classe para a implementação do algoritmo de casamento de padrões KMP
class KMP {
    //Método para a construção do array de estados que será usado para o casamento de padrões 
    public static int[] arrayEstados(String padrao){
        //Declaração de variáveis
        int i = 1, j = 0;
        int[] estados = new int[padrao.length()];
        estados[0] = 0;

        //Loop para a construção do array de estados
        while(i < padrao.length()){
            if(padrao.charAt(j) == padrao.charAt(i)){
                j = j + 1;
                estados[i] = j;
                i++;
            }else{
                if(j == 0){
                    estados[i] = j;
                    i++;
                }else{
                    j = estados[j-1];
                }
            }
        }

        //retorno do array de estados
        return estados;
    }

    //Método para pesquisa com casamento de padrões KMP (sem sobrescrita)
    public static void search(String padrao, String texto){
        //inicialização de variáveis
        int i = 0, j = 0;

        //construcao do array de estados utilizando o padrao desejado
        int[] estados = arrayEstados(padrao);

        //loop para a pesquisa do padrão em cima do texto
        while(j < texto.length()){
            if(padrao.charAt(i) == texto.charAt(j)){
                j++; i = i + 1;
                if(i == padrao.length()){
                    i = 0; //i = estados[i-1];
                    System.out.println("Achou");
                }
            }else{
                if(i > 0){
                    i = estados[i-1];
                }else{
                    j++;
                }
            }
        }
    }

    public static void main(String[] args){
        String padrao = "sinis";
        String texto = "sinisinis";

        search(padrao, texto);
    }
}