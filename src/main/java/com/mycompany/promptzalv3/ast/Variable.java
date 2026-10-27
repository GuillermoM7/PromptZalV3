package com.mycompany.promptzalv3.ast;

public class Variable {
    public String nombre;
    public String valor; 
    public String tipoOrigen; // "LITERAL_CADENA", "CARGAR", "RESULTADO_COMANDO"
    public int fila;

    public Variable(String nombre, String valor, String tipoOrigen, int fila) {
        this.nombre = nombre;
        this.valor = valor;
        this.tipoOrigen = tipoOrigen;
        this.fila = fila;
    }
}