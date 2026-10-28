package com.mycompany.promptzalv3.semantico;

import com.mycompany.promptzalv3.ast.*;
import java.util.ArrayList;
import java.util.List;

public class AnalizadorSemantico {
    private List<Simbolo> tablaSimbolos = new ArrayList<>();
    private List<String> erroresSemanticos = new ArrayList<>();
    private List<String> agentesEjecutados = new ArrayList<>();

    public void analizar(Programa prog) {
        // Metodo para validar agendes duplicados y registrar variables
        for (Agente ag : prog.agentes) {

            if (buscarSimboloGlobal(ag.nombre) != null) {
                erroresSemanticos.add("Error Semántico: El agente '" + ag.nombre + "' ya fue declarado. Fila: " + ag.filaDeclaracion);
            } else {
                tablaSimbolos.add(new Simbolo(ag.nombre, "AGENTE", "Global", ag.filaDeclaracion));
            }

            for (Variable var : ag.variables) {
                if (buscarSimbolo(var.nombre, ag.nombre) != null) {
                    erroresSemanticos.add("Error Semántico: La variable '" + var.nombre + "' ya existe en el agente '" + ag.nombre + "'. Fila: " + var.fila);
                } else {
                    tablaSimbolos.add(new Simbolo(var.nombre, "VARIABLE_DECLARADA", ag.nombre, var.fila));
                }
            }

            // Registrar variables creadas por el destino de comandos (-> variable)
            for (Comando cmd : ag.comandos) {
                if (buscarSimbolo(cmd.variableDestino, ag.nombre) == null) {
                    tablaSimbolos.add(new Simbolo(cmd.variableDestino, "RESULTADO_COMANDO", ag.nombre, cmd.fila));
                }
            }
        }

        
        // Metodo para validar que EJECUTAR solo pueda referisrse a un agente que exista 
        for (String exec : prog.ejecuciones) {
            if (buscarSimboloGlobal(exec) == null) {
                erroresSemanticos.add("Error Semántico: Se intenta EJECUTAR el agente '" + exec + "' pero no existe.");
            } else {
                agentesEjecutados.add(exec);
            }
        }

        // Metodo para validar que EXPORTAR solo puede referirse a variables que existan en algún agente ejecutado
        for (String exp : prog.exportaciones) {
            boolean encontrada = false;
            for (String agEjecutado : agentesEjecutados) {
                if (buscarSimbolo(exp, agEjecutado) != null) {
                    encontrada = true;
                    break;
                }
            }
            if (!encontrada) {
                erroresSemanticos.add("Error Semántico: Se intenta EXPORTAR la variable '" + exp + "' pero no existe en ningún agente EJECUTADO.");
            }
        }
    }

    
    // Metodos para busqueda
    
    private Simbolo buscarSimboloGlobal(String nombre) {
        for (Simbolo s : tablaSimbolos) {
            if (s.nombre.equals(nombre) && s.tipo.equals("AGENTE")) return s;
        }
        return null;
    }

    private Simbolo buscarSimbolo(String nombre, String ambito) {
        for (Simbolo s : tablaSimbolos) {
            if (s.nombre.equals(nombre) && s.ambito.equals(ambito)) return s;
        }
        return null;
    }

    
    public List<Simbolo> getTablaSimbolos() {
        return tablaSimbolos; 
    }
      
    public List<String> getErroresSemanticos() {
        return erroresSemanticos; 
    }
    
}