package com.mycompany.promptzalv3.ast;

import java.util.ArrayList;
import java.util.List;

public class Comando {
    public String operacion; // PREGUNTAR, GENERAR, etc.
    public String parametroPrincipal;
    public List<Conector> conectores = new ArrayList<>();
    public String variableDestino; // Lo que va después de ->
    public int fila;

    public Comando(String operacion, int fila) {
        this.operacion = operacion;
        this.fila = fila;
    }
}