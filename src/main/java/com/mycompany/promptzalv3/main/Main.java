package com.mycompany.promptzalv3.main;

import com.mycompany.promptzalv3.lexer.LexerAnalizador;
import com.mycompany.promptzalv3.lexer.Token;
import com.mycompany.promptzalv3.lexer.TipoToken;
import java.io.StringReader;

public class Main {
    public static void main(String[] args) {
        // Código de prueba basado en tu PDF
        String codigoPrueba = "@modelo \"claude-sonnet\"\n" +
                              "variable ventas = CARGAR(\"datos.csv\")\n" +
                              "PREGUNTAR \"tendencias?\" SOBRE ventas -> resumen\n" +
                              "$$$"; // Este último símbolo generará un error léxico intencional

        try {
            // Inicializamos el Lexer generado por JFlex
            LexerAnalizador lexer = new LexerAnalizador(new StringReader(codigoPrueba));
            Token token = lexer.yylex();

            System.out.println("--- TOKENS RECONOCIDOS ---");
            // Iteramos hasta que JFlex devuelva el token de fin de archivo (EOF)
            while (token != null && token.getTipo() != TipoToken.EOF) {
                System.out.println(token);
                token = lexer.yylex();
            }

            System.out.println("\n--- ERRORES LÉXICOS ---");
            for (String error : lexer.getErroresLexicos()) {
                System.out.println(error);
            }

        } catch (Exception e) {
            System.out.println("Error en la ejecución: " + e.getMessage());
        }
    }
}