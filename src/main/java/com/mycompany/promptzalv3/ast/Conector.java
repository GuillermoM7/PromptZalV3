package com.mycompany.promptzalv3.ast;

public class Conector {
    public String tipo; // SOBRE, DESDE, EN, COMO
    public String valor;

    public Conector(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }
}