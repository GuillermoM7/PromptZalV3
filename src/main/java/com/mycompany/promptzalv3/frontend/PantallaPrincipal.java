package com.mycompany.promptzalv3.frontend;

import com.mycompany.promptzalv3.ast.Programa;
import com.mycompany.promptzalv3.lexer.LexerAnalizador;
import com.mycompany.promptzalv3.lexer.Token;
import com.mycompany.promptzalv3.lexer.TipoToken;
import com.mycompany.promptzalv3.parser.Parser;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PantallaPrincipal extends JFrame {

    // Componentes
    private JTextPane editorCodigo; 
    private JTextArea vistaPrompt;
    private JTextArea consolaErrores;
   
    private final Color COLOR_FONDO = new Color(30, 30, 30);
    private final Color COLOR_TEXTO = new Color(212, 212, 212);
    private final Color COLOR_CONSOLA = new Color(45, 45, 45);
    private final Font FUENTE_CODIGO = new Font("Consolas", Font.PLAIN, 15);
    
    // Datos de la última compilación para los reportes
    private List<String> ultimosErrLex = new java.util.ArrayList<>();
    private List<String> ultimosErrSin = new java.util.ArrayList<>();
    private List<String> ultimosErrSem = new java.util.ArrayList<>();
    private Programa ultimoAST = null;
    private List<com.mycompany.promptzalv3.semantico.Simbolo> ultimaTablaSimbolos = new java.util.ArrayList<>();

    
    public PantallaPrincipal() {
        configurarVentana();
        inicializarComponentes();
    }

    
    private void configurarVentana() {
        setTitle("PromptZal V3 - IDE");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    
    private void inicializarComponentes() {
        //Barra de Herramientas (Botones)
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelSuperior.setBackground(new Color(43, 45, 48)); 
        
        JButton btnAbrir = crearBoton("Abrir .pz", new Color(66, 133, 244));
        JButton btnGuardar = crearBoton("Guardar .pz", new Color(66, 133, 244));
        JButton btnCompilar = crearBoton("Compilar", new Color(76, 175, 80));
        JButton btnReportes = crearBoton("Reportes HTML", new Color(230, 124, 115));
        JButton btnAST = crearBoton("Ver AST", new Color(156, 39, 176)); 
        
        btnAbrir.addActionListener(e -> abrirArchivo());
        btnGuardar.addActionListener(e -> guardarArchivo());
        btnCompilar.addActionListener(e -> ejecutarCompilador());
        btnReportes.addActionListener(e -> generarReportesTablas());
        btnAST.addActionListener(e -> generarReporteAST());
        
        panelSuperior.add(btnAbrir);
        panelSuperior.add(btnGuardar);
        panelSuperior.add(btnCompilar);
        panelSuperior.add(btnReportes);
        panelSuperior.add(btnAST);
        
        add(panelSuperior, BorderLayout.NORTH);

        
        editorCodigo = new JTextPane();
        editorCodigo.setBackground(COLOR_FONDO);
        editorCodigo.setForeground(COLOR_TEXTO);
        editorCodigo.setCaretColor(Color.WHITE);
        editorCodigo.setFont(FUENTE_CODIGO);
        editorCodigo.setMargin(new Insets(10, 10, 10, 10));
        editorCodigo.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aplicarResaltadoSintaxis(); }
            @Override public void removeUpdate(DocumentEvent e) { aplicarResaltadoSintaxis(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });
        
        vistaPrompt = new JTextArea("# Prompt final generado...\n\n(Se mostrará al compilar)");
        vistaPrompt.setEditable(false);
        vistaPrompt.setBackground(new Color(40, 44, 52));
        vistaPrompt.setForeground(new Color(152, 195, 121));
        vistaPrompt.setFont(FUENTE_CODIGO);

        consolaErrores = new JTextArea("Consola lista...");
        consolaErrores.setBackground(COLOR_CONSOLA);
        consolaErrores.setForeground(new Color(255, 100, 100));
        consolaErrores.setEditable(false);
        consolaErrores.setFont(FUENTE_CODIGO);

        JSplitPane splitSuperior = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(editorCodigo), new JScrollPane(vistaPrompt));
        splitSuperior.setResizeWeight(0.6);
        splitSuperior.setDividerSize(5);

        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitSuperior, new JScrollPane(consolaErrores));
        splitPrincipal.setResizeWeight(0.75);
        splitPrincipal.setDividerSize(5);

        add(splitPrincipal, BorderLayout.CENTER);
    }

    
    private JButton crearBoton(String texto, Color colorFondo) {
        JButton btn = new JButton(texto);
        btn.setBackground(colorFondo);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(true);
        btn.setBorderPainted(false); 
        btn.setMargin(new Insets(5, 15, 5, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }


    private void abrirArchivo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos PromptZal (*.pz)", "pz"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String contenido = new String(Files.readAllBytes(chooser.getSelectedFile().toPath()));
                editorCodigo.setText(contenido);
                aplicarResaltadoSintaxis();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + e.getMessage());
            }
        }
    }

    
    private void guardarArchivo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos PromptZal (*.pz)", "pz"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File archivo = chooser.getSelectedFile();
                if (!archivo.getName().toLowerCase().endsWith(".pz")) {
                    archivo = new File(archivo.getAbsolutePath() + ".pz");
                }
                Files.write(archivo.toPath(), editorCodigo.getText().getBytes());
                JOptionPane.showMessageDialog(this, "Archivo guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + e.getMessage());
            }
        }
    }

    
    // Pintor
    private void aplicarResaltadoSintaxis() {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = editorCodigo.getStyledDocument();
                String texto = doc.getText(0, doc.getLength());              
                Style estiloBase = editorCodigo.addStyle("Base", null);
                StyleConstants.setForeground(estiloBase, COLOR_TEXTO);
                doc.setCharacterAttributes(0, texto.length(), estiloBase, true);

                pintarPatron(texto, doc, "//.*|/\\*(.|\\n)*?\\*/", new Color(92, 99, 112));
                pintarPatron(texto, doc, "\"(.*?)\"", new Color(152, 195, 121));
                pintarPatron(texto, doc, "@[a-zA-Z]+", new Color(209, 154, 102));
                pintarPatron(texto, doc, "\\b(AGENTE|contexto|variable|EJECUTAR|EXPORTAR)\\b", new Color(198, 120, 221));
                pintarPatron(texto, doc, "\\b(PREGUNTAR|GENERAR|RESUMIR|ANALIZAR|TRADUCIR|CLASIFICAR|EXTRAER|CARGAR)\\b", new Color(97, 175, 239));
                pintarPatron(texto, doc, "\\b(SOBRE|DESDE|EN|COMO)\\b", new Color(229, 192, 123));
            } catch (Exception e) {
            }
        });
    }

    
    private void pintarPatron(String texto, StyledDocument doc, String regex, Color color) {
        Matcher matcher = Pattern.compile(regex).matcher(texto);
        Style estilo = editorCodigo.addStyle("Estilo", null);
        StyleConstants.setForeground(estilo, color);
        while (matcher.find()) {
            doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), estilo, false);
        }
    }


private void ejecutarCompilador() {
        consolaErrores.setText("Iniciando compilación...\n");
        String codigo = editorCodigo.getText();
        
        if (codigo.trim().isEmpty()) {
            consolaErrores.append("El editor está vacío.");
            return;
        }

        try {
            // Limpiar datos de compilaciones anteriores por seguridad antes de empezar
            ultimoAST = null;
            if (ultimosErrSin != null) ultimosErrSin.clear();
            if (ultimosErrSem != null) ultimosErrSem.clear();
            if (ultimaTablaSimbolos != null) ultimaTablaSimbolos.clear();

            // Fase lexica
            LexerAnalizador lexer = new LexerAnalizador(new StringReader(codigo));
            Token token = lexer.yylex();
            
            while (token != null && token.getTipo() != TipoToken.EOF) {
                token = lexer.yylex();
            }
            ultimosErrLex = lexer.getErroresLexicos();

            if (lexer.getErroresLexicos().isEmpty()) {
                consolaErrores.append("Análisis Léxico completado sin errores.\n");
                
                // Fase sintactica y AST
                LexerAnalizador lexerParaParser = new LexerAnalizador(new StringReader(codigo));
                Parser parser = new Parser(lexerParaParser);
                
                Programa ast = parser.programa();
                
                ultimoAST = ast;
                ultimosErrSin = parser.getErroresSintacticos();
                
                // Si la gramática es correcta, se valida la logica
                if (parser.getErroresSintacticos().isEmpty()) {
                    consolaErrores.append("Análisis Sintáctico completado sin errores.\n");
                    consolaErrores.append("AST Construido con éxito. Agentes detectados: " + ast.agentes.size() + "\n");
                    
                    //Fase semantica
                    com.mycompany.promptzalv3.semantico.AnalizadorSemantico semantico = new com.mycompany.promptzalv3.semantico.AnalizadorSemantico();
                    semantico.analizar(ast); 
                    
                    ultimosErrSem = semantico.getErroresSemanticos();
                    ultimaTablaSimbolos = semantico.getTablaSimbolos();
                    
                    if (semantico.getErroresSemanticos().isEmpty()) {
                        consolaErrores.append("Análisis Semántico completado. Lógica válida.\n");
                        consolaErrores.append("\n--- TABLA DE SÍMBOLOS GENERADA ---\n");
                        for (com.mycompany.promptzalv3.semantico.Simbolo s : semantico.getTablaSimbolos()) {
                            consolaErrores.append("  " + s.toString() + "\n");
                        }
                        
                        // Fase de Generacion
                        com.mycompany.promptzalv3.generador.GeneradorPrompt generador = new com.mycompany.promptzalv3.generador.GeneradorPrompt();
                        String promptFinal = generador.generar(ast);
                        vistaPrompt.setText(promptFinal);
                        consolaErrores.append("\n✓ PromptZal generado exitosamente en el panel derecho.\n");
                        
                    } else {
                        consolaErrores.append("ERRORES SEMÁNTICOS ENCONTRADOS:\n");
                        for (String error : semantico.getErroresSemanticos()) {
                            consolaErrores.append("  " + error + "\n");
                        }
                    }

                } else {
                    consolaErrores.append("ERRORES SINTÁCTICOS ENCONTRADOS:\n");
                    for (String error : parser.getErroresSintacticos()) {
                        consolaErrores.append("  " + error + "\n");
                    }
                }
                
            } else {
                consolaErrores.append("ERRORES LÉXICOS ENCONTRADOS. La compilación se detuvo.\n");
                for (String error : lexer.getErroresLexicos()) {
                    consolaErrores.append("  " + error + "\n");
                }
            }

        } catch (Exception ex) {
            consolaErrores.append("Error interno: " + ex.getMessage());
        }
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            new PantallaPrincipal().setVisible(true);
        });
    }
    
    
    private void generarReportesTablas() {
        com.mycompany.promptzalv3.reportes.GeneradorReportes gen = new com.mycompany.promptzalv3.reportes.GeneradorReportes();
        gen.generarReporteErrores(ultimosErrLex, ultimosErrSin, ultimosErrSem);
        
        if (ultimaTablaSimbolos != null && !ultimaTablaSimbolos.isEmpty()) {
            gen.generarTablaSimbolos(ultimaTablaSimbolos);
            JOptionPane.showMessageDialog(this, "Reportes HTML guardados exitosamente en la carpeta 'Reportes'.", "Reportes Generados", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Se generó el reporte de errores, pero la tabla de símbolos está vacía.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    
    private void generarReporteAST() {
        if (ultimoAST != null) {
            com.mycompany.promptzalv3.reportes.GeneradorReportes gen = new com.mycompany.promptzalv3.reportes.GeneradorReportes();
            gen.generarReporteAST(ultimoAST);
            JOptionPane.showMessageDialog(this, "Árbol AST (.dot, .png y .html) guardado en la carpeta 'Reportes'.", "AST Generado", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No hay un AST en memoria. Compila un código sintácticamente válido primero.", "AST no disponible", JOptionPane.WARNING_MESSAGE);
        }
    }
    
}