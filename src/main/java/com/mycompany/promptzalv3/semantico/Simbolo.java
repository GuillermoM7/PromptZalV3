package com.mycompany.promptzalv3.semantico;

public class Simbolo {
    public String nombre;
    public String tipo;    // Puede ser: "AGENTE", "VARIABLE_DECLARADA", "RESULTADO_COMANDO"
    public String ambito; // Nombre del agente al que pertenece
    public int fila;

    public Simbolo(String nombre, String tipo, String ambito, int fila) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.ambito = ambito;
        this.fila = fila;
    }

    @Override
    public String toString() {
        return "Simbolo{" + "nombre='" + nombre + "', tipo='" + tipo + "', ambito='" + ambito + "', fila=" + fila + '}';
    }
}