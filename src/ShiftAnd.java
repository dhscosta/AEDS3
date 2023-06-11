class ShiftAnd{
    private int comparacoes;    //contagem de comparações da execução do algoritmo

    // Construtor da classe
    ShiftAnd(){
        comparacoes = 0;
    }

    //Método de acesso de comparações
    public int getComp(){
        return comparacoes;
    }


    /*
     * Método para fazer a pesquisa do padrao
     * text -> titulo do jogo
     * padrao -> string pesquisada no titulo
     * reg -> posiçao do registro que está sendo procurado
     */
    public void search(String text, String padrao, int reg) {
        // |= atribuiçao com operador OR
        // 1L = numero 1 representado em long

        // Pré-processamento do padrão
        int tamPadrao = padrao.length();
        int[] S = new int[70000];

        for (int i = 0; i < tamPadrao; i++) {
            S[padrao.charAt(i)] |= 1 << i;
        }

        //execução
        int shift = 0;
        for (int i = 0; i < text.length(); i++) {
            // Atualiza o vetor de estados
            shift = (shift << 1 | 1) & S[text.charAt(i)];

            // Verifica se ocorreu um casamento completo
            comparacoes++;
            if ((shift & (1L << (tamPadrao - 1))) != 0) {
                int startIndex = i - tamPadrao + 1;
                int endIndex = i;
                System.out.println("Padrão encontrado no registro : " + reg+1 + " - Na posição: " + startIndex + " - " + endIndex + "--   " + text );
            }
        }
    }
}