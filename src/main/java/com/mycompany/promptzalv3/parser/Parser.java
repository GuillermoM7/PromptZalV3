package com.mycompany.promptzalv3.parser;

import com.mycompany.promptzalv3.lexer.LexerAnalizador;
import com.mycompany.promptzalv3.lexer.Token;
import com.mycompany.promptzalv3.lexer.TipoToken;
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

    

    private boolean match(TipoToken tipoEsperado) {
        if (tokenActual != null && tokenActual.getTipo() == tipoEsperado) {
            avanzar(); 
            return true;
        } else {
            reportarError(tipoEsperado);
            recuperarModoPanico(); 
            return false;
        }
    }

    //REGLAS!!!

    // <programa> -> <directivas> <agentes> <ejecuciones> <exportar> EOF
    public void programa() {
        directivas();  
        agentes();
        ejecuciones();
        exportar();
        match(TipoToken.EOF);
    }

    // <directivas> -> DIRECTIVA CADENA <directivas> | ε
    private void directivas() {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.DIRECTIVA) {
            match(TipoToken.DIRECTIVA);
            match(TipoToken.CADENA);
            directivas();
        }
        else {
            return;
        }
    }
    
    // <agentes> -> <agente> <mas_agentes>
    private void agentes() {
        agente();
        mas_agentes();
    }

    // <mas_agentes> -> <agente> <mas_agentes> | ε
    private void mas_agentes() {
        // Si el siguiente token es AGENTE, hay otro agente; si no, es epsilon (termina).
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.AGENTE) {
            agente();
            mas_agentes();
        }
    }

    // <agente> -> AGENTE ID LLAVE_A <contexto> <variables> <comandos> LLAVE_C
    private void agente() {
        match(TipoToken.AGENTE);
        match(TipoToken.ID);
        match(TipoToken.LLAVE_A);
        
        contexto();
        variables();
        comandos();
        
        match(TipoToken.LLAVE_C);
    }

    // <contexto> -> CONTEXTO IGUAL CADENA
    private void contexto() {
        match(TipoToken.CONTEXTO);
        match(TipoToken.IGUAL);
        match(TipoToken.CADENA);
    }

    // <variables> -> VARIABLE ID IGUAL <expresion> <variables> | ε
    private void variables() {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.VARIABLE) {
            match(TipoToken.VARIABLE);
            match(TipoToken.ID);
            match(TipoToken.IGUAL);
            expresion();
            variables(); 
        }
    }

    
    // <comandos> -> <comando> <mas_comandos>
    private void comandos() {
        comando();
        mas_comandos();
    }

    
    // <mas_comandos> -> <comando> <mas_comandos> | ε
    private void mas_comandos() {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.COMANDO_IA) {
            comando();
            mas_comandos();
        }
    }

    
    // <comando> -> COMANDO_IA <expresion> <conectores> FLECHA ID
    private void comando() {
        match(TipoToken.COMANDO_IA);
        expresion();
        conectores();
        match(TipoToken.FLECHA);
        match(TipoToken.ID);
    }

    
    // <conectores> -> CONECTOR <expresion> <conectores> | ε
    private void conectores() {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.CONECTOR) {
            match(TipoToken.CONECTOR);
            expresion();
            conectores();
        }
    }

    
    // <expresion> -> <termino> <mas_terminos>
    private void expresion() {
        termino();
        mas_terminos();
    }

    
    // <mas_terminos> -> MAS <termino> <mas_terminos> | ε
    private void mas_terminos() {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.MAS) {
            match(TipoToken.MAS);
            termino();
            mas_terminos();
        }
    }

    
    // <termino> -> CADENA | ID | NUMERO <unidad> | <cargar>
    private void termino() {
        if (tokenActual == null) return;
        
        if (tokenActual.getTipo() == TipoToken.CADENA) {
            match(TipoToken.CADENA);
        } else if (tokenActual.getTipo() == TipoToken.ID) {
            match(TipoToken.ID);
        } else if (tokenActual.getTipo() == TipoToken.NUMERO) {
            match(TipoToken.NUMERO);
            unidad();
        } else if (tokenActual.getTipo() == TipoToken.CARGAR) {
            cargar();
        } else {
            reportarError(TipoToken.CADENA); 
            recuperarModoPanico();
        }
    }

    
    // <unidad> -> ID | ε
    private void unidad() {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.ID) {
            match(TipoToken.ID);
        }
    }

    
    // <cargar> -> CARGAR PAR_A CADENA PAR_C
    private void cargar() {
        match(TipoToken.CARGAR);
        match(TipoToken.PAR_A);
        match(TipoToken.CADENA);
        match(TipoToken.PAR_C);
    }

    
    // <ejecuciones> -> EJECUTAR ID <mas_ejecuciones>
    private void ejecuciones() {
        match(TipoToken.EJECUTAR);
        match(TipoToken.ID);
        mas_ejecuciones();
    }

    
    // <mas_ejecuciones> -> EJECUTAR ID <mas_ejecuciones> | ε
    private void mas_ejecuciones() {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.EJECUTAR) {
            match(TipoToken.EJECUTAR);
            match(TipoToken.ID);
            mas_ejecuciones();
        }
    }

    
    // <exportar> -> EXPORTAR ID <mas_ids>
    private void exportar() {
        match(TipoToken.EXPORTAR);
        match(TipoToken.ID);
        mas_ids();
    }

    
    // <mas_ids> -> COMA ID <mas_ids> | ε
    private void mas_ids() {
        if (tokenActual != null && tokenActual.getTipo() == TipoToken.COMA) {
            match(TipoToken.COMA);
            match(TipoToken.ID);
            mas_ids();
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
        avanzar();
        
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
    
}