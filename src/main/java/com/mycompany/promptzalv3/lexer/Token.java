package com.mycompany.promptzalv3.lexer;

public class Token {
    private TipoToken tipo;
    private String lexema;
    private int fila;
    private int columna;

    
    public Token(TipoToken tipo, String lexema, int fila, int columna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.fila = fila;
        this.columna = columna;
    }

    
    public TipoToken getTipo() {
        return tipo;
    }

    public void setTipo(TipoToken tipo) {
        this.tipo = tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public int getColumna() {
        return columna;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }
    

    @Override
    public String toString() {
        return "Token{" + "tipo=" + tipo + ", lexema='" + lexema + '\'' + 
               ", fila=" + fila + ", col=" + columna + '}';
    }
}