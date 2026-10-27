package com.mycompany.promptzalv3.ast;

import java.util.ArrayList;
import java.util.List;

public class Agente {
    public String nombre;
    public String contexto;
    public List<Variable> variables = new ArrayList<>();
    public List<Comando> comandos = new ArrayList<>();
    public int filaDeclaracion;

    public Agente(String nombre, int filaDeclaracion) {
        this.nombre = nombre;
        this.filaDeclaracion = filaDeclaracion;
    }
}