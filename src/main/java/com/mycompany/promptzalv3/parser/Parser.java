package com.mycompany.promptzalv3.parser;

import com.mycompany.promptzalv3.lexer.LexerAnalizador;
import com.mycompany.promptzalv3.lexer.Token;
import com.mycompany.promptzalv3.lexer.TipoToken;
import com.mycompany.promptzalv3.ast.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private LexerAnalizador lexer;
    private Token tokenActual;
    private List<String> erroresSintacticos;

    //Inicio
    public Parser(LexerAnalizador lexer) {
        this.lexer = lexer;
        this.erroresSintacticos = new ArrayList<>();
        avanzar();
    }

    
    public List<String> getErroresSintacticos() {
        return erroresSintacticos;
    }

    
    // Siguiente Token
    private void avanzar() {
        try {
            tokenActual = lexer.yylex();
        } catch (Exception e) {
            erroresSintacticos.add("Error interno al leer token: " + e.getMessage());
        }
    }

    
    private void match(TipoToken tipoEsperado) {
        if (tokenActual != null && tokenActual.getTipo() == tipoEsperado) {
            avanzar(); 
        } else {
            reportarError(tipoEsperado);
            throw new PanicException();
        }
    }

    
    // Motor del AST
    private String consumir(TipoToken tipoEsperado) {
        String lexema = "";
        if (tokenActual != null) {
            lexema = tokenActual.getLexema();
        }
        match(tipoEsperado);
        return lexema;
    }

    
    //REGLAS!!!

    // <programa> -> <directivas> <agentes> <ejecuciones> <exportar> EOF
    public Programa programa() {
        Programa prog = new Programa(); // Nodo raíz del árbol
        directivas(prog);  
        agentes(prog);
        ejecuciones(prog);
        exportar(prog);
        
        try {
            match(TipoToken.EOF);
        } catch (PanicException e) {}
        
        return prog; // Devuelve el árbol completo
    }

    
    // <directivas> -> DIRECTIVA CADENA <directivas> | ε
    private void directivas(Programa prog) {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.DIRECTIVA) {
            String dir = consumir(TipoToken.DIRECTIVA);
            String val = consumir(TipoToken.CADENA);
            prog.directivas.add(dir + " " + val);
            directivas(prog);
        }
    }
    
    
    // <agentes> -> <agente> <mas_agentes>
    private void agentes(Programa prog) {
        try {
            prog.agentes.add(agente());
        } catch (PanicException e) {
            recuperarModoPanico();
        }
        mas_agentes(prog);
    }

    
    // <mas_agentes> -> <agente> <mas_agentes> | ε
    private void mas_agentes(Programa prog) {
        // Si el siguiente token es AGENTE, hay otro agente; si no, es epsilon (termina).
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.AGENTE) {
            agentes(prog);
        }
    }

    
    // <agente> -> AGENTE ID LLAVE_A <contexto> <variables> <comandos> LLAVE_C
    private Agente agente() {
        match(TipoToken.AGENTE);
        int fila = tokenActual != null ? tokenActual.getFila() : 0;
        String nombre = consumir(TipoToken.ID);
        match(TipoToken.LLAVE_A);       
        // Se instancia el agente y se inyectan los sub-nodos dentro de él
        Agente ag = new Agente(nombre, fila);
        ag.contexto = contexto();
        variables(ag);
        comandos(ag);       
        match(TipoToken.LLAVE_C);
        return ag;
    }

    
    // <contexto> -> CONTEXTO IGUAL CADENA
    private String contexto() {
        match(TipoToken.CONTEXTO);
        match(TipoToken.IGUAL);
        return consumir(TipoToken.CADENA);
    }

    
    // <variables> -> VARIABLE ID IGUAL <expresion> <variables> | ε
    private void variables(Agente ag) {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.VARIABLE) {
            try {
                match(TipoToken.VARIABLE);
                int fila = tokenActual != null ? tokenActual.getFila() : 0;
                String nombre = consumir(TipoToken.ID);
                match(TipoToken.IGUAL);
                String[] exp = expresion();
                ag.variables.add(new Variable(nombre, exp[0], exp[1], fila));
            } catch (PanicException e) {
                recuperarModoPanico();
            }
            variables(ag); 
        }
    }

    
    // <comandos> -> <comando> <mas_comandos>
    private void comandos(Agente ag) {
        try {
            ag.comandos.add(comando());
        } catch (PanicException e) {
            recuperarModoPanico();
        }
        mas_comandos(ag);
    }

    
    // <mas_comandos> -> <comando> <mas_comandos> | ε
    private void mas_comandos(Agente ag) {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.COMANDO_IA) {
            comandos(ag);
        }
    }
    
    
    // <comando> -> COMANDO_IA <expresion> <conectores> FLECHA ID
    private Comando comando() {
        int fila = tokenActual != null ? tokenActual.getFila() : 0;
        String op = consumir(TipoToken.COMANDO_IA);
        Comando cmd = new Comando(op, fila);
        
        String[] exp = expresion();
        cmd.parametroPrincipal = exp[0];
        conectores(cmd);
        
        match(TipoToken.FLECHA);
        cmd.variableDestino = consumir(TipoToken.ID);
        return cmd;
    }
    
    
    // <conectores> -> CONECTOR <expresion> <conectores> | ε
    private void conectores(Comando cmd) {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.CONECTOR) {
            String tipo = consumir(TipoToken.CONECTOR);
            String[] exp = expresion();
            cmd.conectores.add(new Conector(tipo, exp[0]));
            conectores(cmd);
        }
    }

    
    // <expresion> -> <termino> <mas_terminos>
    // Devuelve un arreglo [0]=Valor compuesto, [1]=Tipo base para facilitar la validación semántica
    private String[] expresion() {
        String[] term = termino();
        String mas = mas_terminos();
        if (!mas.isEmpty()) {
            term[0] = term[0] + mas;
            term[1] = "EXPRESION_COMPUESTA";
        }
        return term;
    }

    
    // <mas_terminos> -> MAS <termino> <mas_terminos> | ε
    private String mas_terminos() {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.MAS) {
            match(TipoToken.MAS);
            String[] t = termino();
            String mt = mas_terminos();
            return " + " + t[0] + mt;
        }
        return "";
    }

    
    // <termino> -> CADENA | ID | NUMERO <unidad> | <cargar>
    private String[] termino() {
        String[] res = new String[]{"", ""};
        if (tokenActual == null) return res;
        
        if (tokenActual.getTipo() == TipoToken.CADENA) {
            res[0] = consumir(TipoToken.CADENA);
            res[1] = "CADENA";
        } else if (tokenActual.getTipo() == TipoToken.ID) {
            res[0] = consumir(TipoToken.ID);
            res[1] = "ID";
        } else if (tokenActual.getTipo() == TipoToken.NUMERO) {
            String num = consumir(TipoToken.NUMERO);
            String un = unidad();
            res[0] = num + (un.isEmpty() ? "" : " " + un);
            res[1] = "NUMERO";
        } else if (tokenActual.getTipo() == TipoToken.CARGAR) {
            res = cargar();
        } else {
            reportarError(TipoToken.CADENA); 
            throw new PanicException();
        }
        return res;
    }

    
    // <unidad> -> ID | ε
    private String unidad() {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.ID) {
            return consumir(TipoToken.ID);
        }
        return "";
    }

    
    // <cargar> -> CARGAR PAR_A CADENA PAR_C
    private String[] cargar() {
        match(TipoToken.CARGAR);
        match(TipoToken.PAR_A);
        String arch = consumir(TipoToken.CADENA);
        match(TipoToken.PAR_C);
        return new String[]{arch, "CARGAR"};
    }
    
    
    // <ejecuciones> -> EJECUTAR ID <mas_ejecuciones>
    private void ejecuciones(Programa prog) {
        try {
            match(TipoToken.EJECUTAR);
            prog.ejecuciones.add(consumir(TipoToken.ID));
        } catch (PanicException e) {
            recuperarModoPanico();
        }
        mas_ejecuciones(prog);
    }

    
    // <mas_ejecuciones> -> EJECUTAR ID <mas_ejecuciones> | ε
    private void mas_ejecuciones(Programa prog) {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.EJECUTAR) {
            ejecuciones(prog);
        }
    }

    
    // <exportar> -> EXPORTAR ID <mas_ids>
    private void exportar(Programa prog) {
        try {
            match(TipoToken.EXPORTAR);
            prog.exportaciones.add(consumir(TipoToken.ID));
            mas_ids(prog);
        } catch (PanicException e) {
            recuperarModoPanico();
        }
    }

    
    // <mas_ids> -> COMA ID <mas_ids> | ε
    private void mas_ids(Programa prog) {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.COMA) {
            match(TipoToken.COMA);
            prog.exportaciones.add(consumir(TipoToken.ID));
            mas_ids(prog);
        }
    }

    
    //Manejo de errores
    private void reportarError(TipoToken tipoEsperado) {
        if (tokenActual != null) {
            String msj = "Error Sintáctico: Se esperaba " + tipoEsperado + 
                         " pero se encontró '" + tokenActual.getLexema() + 
                         "' en Fila: " + tokenActual.getFila() + 
                         " Col: " + tokenActual.getColumna();
            erroresSintacticos.add(msj);
        }
    }
    
    
    private void recuperarModoPanico() {
        while (tokenActual != null && tokenActual.getTipo() != TipoToken.EOF) {
            TipoToken t = tokenActual.getTipo();
            if (t == TipoToken.AGENTE || t == TipoToken.LLAVE_C || 
                t == TipoToken.COMANDO_IA || t == TipoToken.VARIABLE || 
                t == TipoToken.EJECUTAR || t == TipoToken.EXPORTAR) {
                break; 
            }
            avanzar(); 
        }
    }  
    
    private class PanicException extends RuntimeException {}
    
}