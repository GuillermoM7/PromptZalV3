package com.mycompany.promptzalv3.reportes;

import com.mycompany.promptzalv3.ast.*;
import com.mycompany.promptzalv3.semantico.Simbolo;
import java.io.FileWriter;
import java.util.List;
import java.io.File;

public class GeneradorReportes {

    
    private void guardarReporte(String nombreArchivo, String contenido) {
        try {
            File directorio = new File("Reportes");
            if (!directorio.exists()) {
                directorio.mkdirs();
            }
            File archivo = new File(directorio, nombreArchivo);
            FileWriter writer = new FileWriter(archivo);
            writer.write(contenido);
            writer.close();
        } catch (Exception e) {
            System.out.println("Error al guardar reporte: " + e.getMessage());
        }
    }

    
    public void generarReporteErrores(List<String> errLex, List<String> errSin, List<String> errSem) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>Reporte de Errores</title>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, sans-serif; background-color: #f1f5f9; padding: 40px; }");
        html.append(".container { max-width: 1000px; margin: auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        html.append("h1 { text-align: center; color: #e11d48; border-bottom: 2px solid #e2e8f0; padding-bottom: 10px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        html.append("th, td { padding: 15px; text-align: left; border-bottom: 1px solid #e2e8f0; }");
        html.append("th { background-color: #f8fafc; font-weight: 600; text-transform: uppercase; font-size: 14px; }");
        html.append(".badge { padding: 5px 10px; border-radius: 20px; font-size: 12px; font-weight: bold; color: white; }");
        html.append(".lexico { background-color: #f59e0b; } .sintactico { background-color: #ef4444; } .semantico { background-color: #8b5cf6; }");
        html.append("</style></head><body><div class='container'>");
        
        html.append("<h1> Reporte de Errores - PromptZal V3</h1>");
        
        if (errLex.isEmpty() && errSin.isEmpty() && errSem.isEmpty()) {
            html.append("<div style='text-align:center; padding: 40px; color: #10b981; font-size: 20px;'><strong>¡Excelente!</strong> No se encontraron errores.</div>");
        } else {
            html.append("<table><thead><tr><th>Fase</th><th>Descripción del Error</th></tr></thead><tbody>");
            for (String e : errLex) html.append("<tr><td><span class='badge lexico'>LÉXICO</span></td><td>").append(e).append("</td></tr>");
            for (String e : errSin) html.append("<tr><td><span class='badge sintactico'>SINTÁCTICO</span></td><td>").append(e).append("</td></tr>");
            for (String e : errSem) html.append("<tr><td><span class='badge semantico'>SEMÁNTICO</span></td><td>").append(e).append("</td></tr>");
            html.append("</tbody></table>");
        }
        
        html.append("</div></body></html>");
        guardarReporte("ReporteErrores.html", html.toString());
    }

    
    public void generarTablaSimbolos(List<Simbolo> simbolos) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>Tabla de Símbolos</title>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, sans-serif; background-color: #f8fafc; padding: 40px; }");
        html.append(".container { max-width: 1000px; margin: auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        html.append("h1 { text-align: center; color: #0284c7; border-bottom: 2px solid #e2e8f0; padding-bottom: 10px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        html.append("th, td { padding: 15px; text-align: left; border-bottom: 1px solid #e2e8f0; }");
        html.append("th { background-color: #f0f9ff; color: #0369a1; }");
        html.append("</style></head><body><div class='container'>");
        
        html.append("<h1> Tabla de Símbolos - PromptZal V3</h1>");
        html.append("<table><thead><tr><th>Nombre del Símbolo</th><th>Tipo</th><th>Ámbito</th><th>Fila</th></tr></thead><tbody>");
        
        for (Simbolo s : simbolos) {
            html.append("<tr><td><strong>").append(s.nombre).append("</strong></td><td>").append(s.tipo)
                .append("</td><td>").append(s.ambito).append("</td><td>").append(s.fila).append("</td></tr>");
        }
        
        html.append("</tbody></table></div></body></html>");
        guardarReporte("TablaSimbolos.html", html.toString());
    }

    
    public void generarReporteAST(Programa prog) {
        try {
            File directorio = new File("Reportes");
            if (!directorio.exists()) directorio.mkdirs();

            StringBuilder dot = new StringBuilder();
            int id = 0;
            dot.append("digraph AST {\n");
            dot.append("  node [shape=box, style=\"rounded,filled\", fontname=\"Arial\", color=\"#3b82f6\", fillcolor=\"#eff6ff\"];\n");
            dot.append("  edge [color=\"#94a3b8\"];\n");
            
            String idRaiz = "n" + (id++);
            dot.append("  ").append(idRaiz).append(" [label=\"Programa\", fillcolor=\"#1e293b\", fontcolor=\"white\"];\n");
            
            for (String dir : prog.directivas) {
                String idDir = "n" + (id++);
                dot.append("  ").append(idDir).append(" [label=\"Directiva\\n").append(dir.replace("\"", "\\\"")).append("\", fillcolor=\"#fef08a\"];\n");
                dot.append("  ").append(idRaiz).append(" -> ").append(idDir).append(";\n");
            }
            
            for (Agente ag : prog.agentes) {
                String idAg = "n" + (id++);
                dot.append("  ").append(idAg).append(" [label=\"Agente: ").append(ag.nombre).append("\", fillcolor=\"#c084fc\"];\n");
                dot.append("  ").append(idRaiz).append(" -> ").append(idAg).append(";\n");
                
                String idCtx = "n" + (id++);
                dot.append("  ").append(idCtx).append(" [label=\"Contexto\\n").append(ag.contexto.replace("\"", "\\\"")).append("\"];\n");
                dot.append("  ").append(idAg).append(" -> ").append(idCtx).append(";\n");
                
                for (Variable v : ag.variables) {
                    String idVar = "n" + (id++);
                    dot.append("  ").append(idVar).append(" [label=\"Variable: ").append(v.nombre).append("\\n(").append(v.tipoOrigen).append(")\"];\n");
                    dot.append("  ").append(idAg).append(" -> ").append(idVar).append(";\n");
                }
                
                for (Comando c : ag.comandos) {
                    String idCmd = "n" + (id++);
                    dot.append("  ").append(idCmd).append(" [label=\"Comando: ").append(c.operacion).append("\\nDestino: ").append(c.variableDestino).append("\", fillcolor=\"#86efac\"];\n");
                    dot.append("  ").append(idAg).append(" -> ").append(idCmd).append(";\n");
                }
            }
            
            for (String exec : prog.ejecuciones) {
                String idExec = "n" + (id++);
                dot.append("  ").append(idExec).append(" [label=\"Ejecutar: ").append(exec).append("\", fillcolor=\"#fca5a5\"];\n");
                dot.append("  ").append(idRaiz).append(" -> ").append(idExec).append(";\n");
            }
            
            for (String exp : prog.exportaciones) {
                String idExp = "n" + (id++);
                dot.append("  ").append(idExp).append(" [label=\"Exportar: ").append(exp).append("\", fillcolor=\"#fca5a5\"];\n");
                dot.append("  ").append(idRaiz).append(" -> ").append(idExp).append(";\n");
            }
            dot.append("}\n");

            File dotFile = new File(directorio, "AST.dot");
            FileWriter writer = new FileWriter(dotFile);
            writer.write(dot.toString());
            writer.close();

            String pngPath = new File(directorio, "AST.png").getAbsolutePath();
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", "-o", pngPath, dotFile.getAbsolutePath());
            Process p = pb.start();
            p.waitFor();

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>Reporte AST</title></head>");
            html.append("<body style='font-family: sans-serif; text-align: center; background-color: #f8fafc;'>");
            html.append("<h1 style='color: #1e293b;'> Árbol de Sintaxis Abstracta</h1>");
            html.append("<img src='AST.png' alt='Grafo AST' style='max-width: 100%; border: 1px solid #cbd5e1; box-shadow: 0 4px 6px rgba(0,0,0,0.1); border-radius: 8px;'>");
            html.append("</body></html>");
            
            guardarReporte("ReporteAST.html", html.toString());

        } catch (Exception e) {
            System.out.println("Error al generar AST con Graphviz: " + e.getMessage());
        }
    }
    
}