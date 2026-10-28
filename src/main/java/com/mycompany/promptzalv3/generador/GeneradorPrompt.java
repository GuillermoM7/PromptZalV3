package com.mycompany.promptzalv3.generador;

import com.mycompany.promptzalv3.ast.*;

public class GeneradorPrompt {

    public String generar(Programa prog) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Prompt generado con PromptZal\n");
        
        // Encabezado
        for (String dir : prog.directivas) {
            String valorLimpio = dir.substring(dir.indexOf(" ") + 1).replace("\"", "");
            if (dir.startsWith("@modelo")) sb.append("Modelo destino: ").append(valorLimpio).append("\n");
            else if (dir.startsWith("@rol")) sb.append("Rol general: ").append(valorLimpio).append("\n");
            else if (dir.startsWith("@formato")) sb.append("Formato de respuesta: ").append(valorLimpio).append("\n");
        }
        
        
        // Agentes ejecutados
        for (String nombreExec : prog.ejecuciones) {
            Agente ag = buscarAgente(prog, nombreExec);
            if (ag != null) {
                sb.append("\n## Agente: ").append(ag.nombre).append("\n");
                sb.append("Contexto: ").append(ag.contexto.replace("\"", "")).append("\n");
                sb.append("Variables disponibles:\n");
                
                for (Variable v : ag.variables) {
                    if (v.tipoOrigen.equals("CARGAR")) {
                        sb.append(v.nombre).append(": contenido del archivo ").append(v.valor).append(" (adjuntar el archivo)\n");
                    } else {
                        sb.append(v.nombre).append(": ").append(v.valor.replace("\"", "")).append("\n");
                    }
                }
                
                sb.append("Instrucciones:\n");
                int cont = 1;
                for (Comando cmd : ag.comandos) {
                    sb.append(cont).append(". ").append(traducirComando(cmd)).append("\n");
                    cont++;
                }
            }
        }
        
        
        // Resultados
        sb.append("\n## Resultado solicitado\n");
        for (String exp : prog.exportaciones) {
            sb.append(exp).append("\n");
        }
        
        return sb.toString();
    }

    
    private Agente buscarAgente(Programa prog, String nombre) {
        for (Agente ag : prog.agentes) {
            if (ag.nombre.equals(nombre)) return ag;
        }
        return null;
    }

    
    private String traducirComando(Comando cmd) {
        String base = "";
        String parametro = cmd.parametroPrincipal.replace("\"", "");
        String sobre = "", desde = "", en = "", como = "";
        
        for (Conector c : cmd.conectores) {
            if (c.tipo.equals("SOBRE")) sobre = c.valor;
            if (c.tipo.equals("DESDE")) desde = c.valor;
            if (c.tipo.equals("EN")) en = c.valor;
            if (c.tipo.equals("COMO")) como = c.valor.replace("\"", "");
        }

        switch (cmd.operacion) {
            case "PREGUNTAR": base = "Basandote en " + sobre + ", responde: " + parametro + ". Guarda la respuesta como " + cmd.variableDestino + "."; break;
            case "GENERAR": base = "Genera " + parametro + " a partir de: " + desde + ". Guarda el resultado como " + cmd.variableDestino + "."; break;
            case "RESUMIR": base = "Resume " + parametro + " en un maximo de " + en + ". Guarda el resultado como " + cmd.variableDestino + "."; break;
            case "ANALIZAR": base = "Analiza " + parametro + " considerando: " + sobre + ". Guarda el resultado como " + cmd.variableDestino + "."; break;
            case "TRADUCIR": base = "Traduce " + parametro + " al idioma " + en + ". Guarda el resultado como " + cmd.variableDestino + "."; break;
            case "CLASIFICAR": base = "Clasifica " + parametro + " en las categorias: " + en + ". Guarda el resultado como " + cmd.variableDestino + "."; break;
            case "EXTRAER": base = "Extrae " + parametro + " a partir de " + desde + ". Guarda el resultado como " + cmd.variableDestino + "."; break;
        }

        if (!como.isEmpty()) {
            base += " Entrega el resultado en formato " + como + ".";
        }
        return base;
    }
}