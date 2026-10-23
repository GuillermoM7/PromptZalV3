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

    
    // Verificar si coincide con el token esperado
    private boolean match(TipoToken tipoEsperado) {
        if (tokenActual != null && tokenActual.getTipo() == tipoEsperado) {
            avanzar(); 
            return true;
        } else {
            reportarError(tipoEsperado);
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

    private void variables() {}
    private void comandos() {}
    private void ejecuciones() {}
    private void exportar() {}

    
    //Manejo de errores, pendiente el modo panico
    private void reportarError(TipoToken tipoEsperado) {
        if (tokenActual != null) {
            String msj = "Error Sintáctico: Se esperaba " + tipoEsperado + 
                         " pero se encontró '" + tokenActual.getLexema() + 
                         "' en Fila: " + tokenActual.getFila() + 
                         " Col: " + tokenActual.getColumna();
            erroresSintacticos.add(msj);
        }
    }
}