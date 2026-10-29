package com.mycompany.promptzalv3.semantico;

import com.mycompany.promptzalv3.ast.*;
import java.util.ArrayList;
import java.util.List;


public class AnalizadorSemantico {
    private List<Simbolo> tablaSimbolos = new ArrayList<>();
    private List<String> erroresSemanticos = new ArrayList<>();
    private List<String> agentesEjecutados = new ArrayList<>();

    
    public void analizar(Programa prog) {
        for (Agente ag : prog.agentes) {

            // Validar agente duplicado
            if (buscarSimboloGlobal(ag.nombre) != null) {
                erroresSemanticos.add("Error Semántico: El agente '" + ag.nombre + "' ya fue declarado. Fila: " + ag.filaDeclaracion);
            } else {
                tablaSimbolos.add(new Simbolo(ag.nombre, "AGENTE", "Global", ag.filaDeclaracion));
            }

            // Registrar variables iniciales 
            for (Variable var : ag.variables) {
                if (buscarSimbolo(var.nombre, ag.nombre) != null) {
                    erroresSemanticos.add("Error Semántico: La variable '" + var.nombre + "' ya existe en el agente '" + ag.nombre + "'. Fila: " + var.fila);
                } else {
                    tablaSimbolos.add(new Simbolo(var.nombre, "VARIABLE_DECLARADA", ag.nombre, var.fila));
                }
            }

            // Analizar comandos secuencialmente
            for (Comando cmd : ag.comandos) {
                
                // Validar el parámetro principal 
                String param = cmd.parametroPrincipal.trim();
                if (!param.startsWith("\"") && !Character.isDigit(param.charAt(0))) {
                    if (!cmd.operacion.equals("GENERAR") && !param.contains("+")) { 
                        // Si no es GENERAR y es un ID puro, debe existir
                        if (buscarSimbolo(param, ag.nombre) == null) {
                            erroresSemanticos.add("Error Semántico: La variable '" + param + "' usada en el comando " + cmd.operacion + " no ha sido declarada. Fila: " + cmd.fila);
                        }
                    }
                }

                // Validar los conectores 
                for (Conector con : cmd.conectores) {
                    String val = con.valor.trim();
                    // Si no tiene comillas y no empieza con número es una variable
                    if (!val.startsWith("\"") && !Character.isDigit(val.charAt(0)) && !val.contains("+")) {
                        if (buscarSimbolo(val, ag.nombre) == null) {
                            erroresSemanticos.add("Error Semántico: La variable '" + val + "' usada en el conector " + con.tipo + " no ha sido declarada. Fila: " + cmd.fila);
                        }
                    }
                }

                // Registrar la variable destino despues de validar su uso
                if (buscarSimbolo(cmd.variableDestino, ag.nombre) == null) {
                    tablaSimbolos.add(new Simbolo(cmd.variableDestino, "RESULTADO_COMANDO", ag.nombre, cmd.fila));
                } else {
                    erroresSemanticos.add("Error Semántico: La variable de destino '" + cmd.variableDestino + "' ya existe en el agente '" + ag.nombre + "'. Fila: " + cmd.fila);
                }
            }
        }

        // Validar que ejecutar solo pueda referirse a un agente que exista 
        for (String exec : prog.ejecuciones) {
            if (buscarSimboloGlobal(exec) == null) {
                erroresSemanticos.add("Error Semántico: Se intenta EJECUTAR el agente '" + exec + "' pero no existe.");
            } else {
                agentesEjecutados.add(exec);
            }
        }

        // Validar que exportar solo puede referirse a variables que existan en algún agente ejecutado
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