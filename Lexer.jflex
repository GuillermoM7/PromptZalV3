package com.mycompany.promptzalv3.lexer;

import java.util.ArrayList;
import java.util.List;

%%
%public
%class LexerAnalizador
%type Token
%line
%column

%{
    // Lista para acumular errores léxicos sin detener el análisis
    private List<String> erroresLexicos = new ArrayList<>();

    public List<String> getErroresLexicos() {
        return erroresLexicos;
    }

    // Método de ayuda para crear tokens sumando 1 a fila y columna (JFlex empieza en 0)
    private Token token(TipoToken tipo, String lexema) {
        return new Token(tipo, lexema, yyline + 1, yycolumn + 1);
    }
%}

// --- MACROS Y EXPRESIONES REGULARES ---
ESPACIO = [ \t\r\n]+
COMENTARIO_LINEA = "//" [^\n]*
COMENTARIO_BLOQUE = "/*" ~"*/"

LETRA = [a-zA-Z]
DIGITO = [0-9]
ID = {LETRA} ({LETRA} | {DIGITO} | "_")*
CADENA = \" [^\"]* \"
NUMERO = {DIGITO}+ ("." {DIGITO}+)?

%%
// --- REGLAS LÉXICAS ---

// Ignorar espacios y comentarios
{ESPACIO} { /* Ignorar */ }
{COMENTARIO_LINEA} { /* Ignorar */ }
{COMENTARIO_BLOQUE} { /* Ignorar */ }

// Directivas
"@modelo" | "@rol" | "@formato" { return token(TipoToken.DIRECTIVA, yytext()); }

// Palabras Reservadas
"AGENTE" { return token(TipoToken.AGENTE, yytext()); }
"contexto" { return token(TipoToken.CONTEXTO, yytext()); }
"variable" { return token(TipoToken.VARIABLE, yytext()); }
"EJECUTAR" { return token(TipoToken.EJECUTAR, yytext()); }
"EXPORTAR" { return token(TipoToken.EXPORTAR, yytext()); }

// Comandos IA
"PREGUNTAR" | "GENERAR" | "RESUMIR" | "ANALIZAR" | "TRADUCIR" | "CLASIFICAR" | "EXTRAER" { return token(TipoToken.COMANDO_IA, yytext()); }

// Función Cargar
"CARGAR" { return token(TipoToken.CARGAR, yytext()); }

// Conectores
"SOBRE" | "DESDE" | "EN" | "COMO" { return token(TipoToken.CONECTOR, yytext()); }

// Símbolos y Delimitadores
"->" { return token(TipoToken.FLECHA, yytext()); }
"=" { return token(TipoToken.IGUAL, yytext()); }
"+" { return token(TipoToken.MAS, yytext()); }
"{" { return token(TipoToken.LLAVE_A, yytext()); }
"}" { return token(TipoToken.LLAVE_C, yytext()); }
"(" { return token(TipoToken.PAR_A, yytext()); }
")" { return token(TipoToken.PAR_C, yytext()); }
"," { return token(TipoToken.COMA, yytext()); }

// Patrones dinámicos
{CADENA} { return token(TipoToken.CADENA, yytext()); }
{NUMERO} { return token(TipoToken.NUMERO, yytext()); }
{ID} { return token(TipoToken.ID, yytext()); }

// Recuperación de Errores Léxicos
[^] { 
    String msj = "Error Léxico: Carácter no reconocido '" + yytext() + "' en Fila: " + (yyline+1) + " Col: " + (yycolumn+1);
    erroresLexicos.add(msj);
}

<<EOF>> { return token(TipoToken.EOF, "EOF"); }