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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PantallaPrincipal extends JFrame {

    // Componentes Superiores
    private JTextPane editorCodigo; 
    private JTextArea vistaPrompt;
    
    // Componentes Inferiores (Pestañas)
    private JTabbedPane panelPestanasInferior;
    private JTextArea consolaErrores;
    
    // Tablas
    private DefaultTableModel modeloTokens;
    private JTable tablaTokens;
    private DefaultTableModel modeloSimbolos;
    private JTable tablaSimbolos;
   
    // Paleta de Colores Base
    private final Color COLOR_FONDO = new Color(30, 30, 30);
    private final Color COLOR_TEXTO = new Color(212, 212, 212);
    private final Color COLOR_CONSOLA = new Color(45, 45, 45);
    private final Font FUENTE_CODIGO = new Font("Consolas", Font.PLAIN, 15);
    private final Font FUENTE_TABLAS = new Font("Segoe UI", Font.PLAIN, 13);
    
    // Datos de la última compilación para los reportes HTML
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
        //barra
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelSuperior.setBackground(new Color(43, 45, 48)); 
        
        JButton btnAbrir = crearBoton("📂 Abrir .pz", new Color(71, 95, 119));
        JButton btnGuardar = crearBoton("💾 Guardar .pz", new Color(71, 95, 119));
        JButton btnCompilar = crearBoton("▶ Compilar", new Color(67, 115, 83));
        JButton btnReportes = crearBoton("📊 Reportes HTML", new Color(165, 85, 82));
        JButton btnAST = crearBoton("🌳 Ver AST", new Color(112, 83, 140)); 
        
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

        // editor
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
        
        // Vista y herramientas
        vistaPrompt = new JTextArea("# Prompt final generado...\n\n(Se mostrará al compilar)");
        vistaPrompt.setEditable(false);
        vistaPrompt.setBackground(new Color(40, 44, 52));
        vistaPrompt.setForeground(new Color(152, 195, 121));
        vistaPrompt.setFont(FUENTE_CODIGO);
        vistaPrompt.setMargin(new Insets(10, 10, 10, 10));

        JPanel panelPromptSup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        panelPromptSup.setBackground(new Color(43, 45, 48));
        
        JButton btnCopiarPrompt = crearBoton("Copiar Prompt", new Color(71, 95, 119));
        JButton btnGuardarPrompt = crearBoton("Guardar .txt", new Color(71, 95, 119));
        
        btnCopiarPrompt.addActionListener(e -> copiarPrompt());
        btnGuardarPrompt.addActionListener(e -> guardarPromptArchivo());
        
        panelPromptSup.add(btnCopiarPrompt);
        panelPromptSup.add(btnGuardarPrompt);
        
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.add(panelPromptSup, BorderLayout.NORTH);
        panelDerecho.add(new JScrollPane(vistaPrompt), BorderLayout.CENTER);

        // Config pestañas inferiores ---
        panelPestanasInferior = new JTabbedPane();
        panelPestanasInferior.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Pestaña de Errores (Consola)
        consolaErrores = new JTextArea("Consola lista...\n");
        consolaErrores.setBackground(COLOR_CONSOLA);
        consolaErrores.setForeground(new Color(255, 100, 100));
        consolaErrores.setEditable(false);
        consolaErrores.setFont(FUENTE_CODIGO);
        
        // Pestaña de Tokens
        String[] columnasTokens = {"No.", "Lexema", "Tipo de Token", "Fila", "Columna"};
        modeloTokens = new DefaultTableModel(columnasTokens, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaTokens = new JTable(modeloTokens);
        configurarTabla(tablaTokens);
        tablaTokens.setDefaultRenderer(Object.class, new TokenColorRenderer()); 
        
        //Pestaña de Tabla de Símbolos
        String[] columnasSimbolos = {"Nombre", "Tipo", "Ámbito", "Fila Declaración"};
        modeloSimbolos = new DefaultTableModel(columnasSimbolos, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaSimbolos = new JTable(modeloSimbolos);
        configurarTabla(tablaSimbolos);

        // Agregar paneles al TabbedPane
        panelPestanasInferior.addTab("Consola de Salida", new JScrollPane(consolaErrores));
        panelPestanasInferior.addTab("Tabla de Tokens", new JScrollPane(tablaTokens));
        panelPestanasInferior.addTab("Tabla de Símbolos", new JScrollPane(tablaSimbolos));

        // SPLITS PANELS
        JSplitPane splitSuperior = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(editorCodigo), panelDerecho);
        splitSuperior.setResizeWeight(0.6);
        splitSuperior.setDividerSize(5);

        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitSuperior, panelPestanasInferior);
        splitPrincipal.setResizeWeight(0.75);
        splitPrincipal.setDividerSize(5);

        add(splitPrincipal, BorderLayout.CENTER);
    }


    private void configurarTabla(JTable tabla) {
        tabla.setBackground(COLOR_CONSOLA);
        tabla.setForeground(COLOR_TEXTO);
        tabla.setFont(FUENTE_TABLAS);
        tabla.setRowHeight(25);
        tabla.setGridColor(new Color(60, 63, 65));
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(new Color(60, 63, 65));
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        headerRenderer.setBorder(BorderFactory.createLineBorder(new Color(43, 45, 48)));
        
        for (int i = 0; i < tabla.getModel().getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        
        tabla.setFillsViewportHeight(true);
    }

    
    private class TokenColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String tipoToken = (String) table.getValueAt(row, 2);
            
            if (isSelected) {
                c.setBackground(new Color(75, 110, 175));
                c.setForeground(Color.WHITE);
                return c;
            }
            c.setBackground(COLOR_CONSOLA);
            
            switch (tipoToken) {
                case "CADENA": c.setForeground(new Color(152, 195, 121)); break;
                case "DIRECTIVA": c.setForeground(new Color(209, 154, 102)); break;
                case "AGENTE": case "CONTEXTO": case "VARIABLE": case "EJECUTAR": case "EXPORTAR":
                    c.setForeground(new Color(198, 120, 221)); break;
                case "COMANDO_IA": case "CARGAR":
                    c.setForeground(new Color(97, 175, 239)); break;
                case "CONECTOR":
                    c.setForeground(new Color(229, 192, 123)); break;
                default:
                    c.setForeground(COLOR_TEXTO); break;
            }
            return c;
        }
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
        vistaPrompt.setText("");
        String codigo = editorCodigo.getText();
        
        if (codigo.trim().isEmpty()) {
            consolaErrores.append("El editor está vacío.\n");
            return;
        }

        try {
            // Limpieza de datos
            ultimoAST = null;
            if (ultimosErrSin != null) ultimosErrSin.clear();
            if (ultimosErrSem != null) ultimosErrSem.clear();
            if (ultimaTablaSimbolos != null) ultimaTablaSimbolos.clear();
            modeloTokens.setRowCount(0);
            modeloSimbolos.setRowCount(0);

            // Fase Léxica (Llenado de Tabla de Tokens)
            LexerAnalizador lexer = new LexerAnalizador(new StringReader(codigo));
            Token token = lexer.yylex();
            int numToken = 1;
            
            while (token != null && token.getTipo() != TipoToken.EOF) {
                modeloTokens.addRow(new Object[]{numToken++, token.getLexema(), token.getTipo().toString(), token.getFila(), token.getColumna()});
                token = lexer.yylex();
            }
            ultimosErrLex = lexer.getErroresLexicos();

            if (lexer.getErroresLexicos().isEmpty()) {
                consolaErrores.append("✓ Análisis Léxico completado sin errores.\n");
                
                // Fase Sintáctica y AST
                LexerAnalizador lexerParaParser = new LexerAnalizador(new StringReader(codigo));
                Parser parser = new Parser(lexerParaParser);
                Programa ast = parser.programa();
                
                ultimoAST = ast;
                ultimosErrSin = parser.getErroresSintacticos();
                
                if (parser.getErroresSintacticos().isEmpty()) {
                    consolaErrores.append("✓ Análisis Sintáctico completado sin errores.\n");
                    consolaErrores.append("✓ AST Construido con éxito. Agentes detectados: " + ast.agentes.size() + "\n");
                    
                    // Fase Semántica
                    com.mycompany.promptzalv3.semantico.AnalizadorSemantico semantico = new com.mycompany.promptzalv3.semantico.AnalizadorSemantico();
                    semantico.analizar(ast); 
                    
                    ultimosErrSem = semantico.getErroresSemanticos();
                    ultimaTablaSimbolos = semantico.getTablaSimbolos();
                    
                    for (com.mycompany.promptzalv3.semantico.Simbolo s : semantico.getTablaSimbolos()) {
                        modeloSimbolos.addRow(new Object[]{s.nombre, s.tipo, s.ambito, s.fila});
                    }
                    
                    if (semantico.getErroresSemanticos().isEmpty()) {
                        consolaErrores.append("✓ Análisis Semántico completado. Lógica válida.\n");
                        
                        // Generación de Prompt
                        com.mycompany.promptzalv3.generador.GeneradorPrompt generador = new com.mycompany.promptzalv3.generador.GeneradorPrompt();
                        String promptFinal = generador.generar(ast);
                        vistaPrompt.setText(promptFinal);
                        consolaErrores.append("\n✓ PromptZal generado exitosamente en el panel derecho.\n");
                        
                        panelPestanasInferior.setSelectedIndex(1);
                        
                    } else {
                        consolaErrores.append("✖ ERRORES SEMÁNTICOS ENCONTRADOS:\n");
                        for (String error : semantico.getErroresSemanticos()) {
                            consolaErrores.append("  " + error + "\n");
                        }
                        panelPestanasInferior.setSelectedIndex(0); 
                    }

                } else {
                    consolaErrores.append("✖ ERRORES SINTÁCTICOS ENCONTRADOS:\n");
                    for (String error : parser.getErroresSintacticos()) {
                        consolaErrores.append("  " + error + "\n");
                    }
                    panelPestanasInferior.setSelectedIndex(0);
                }
                
            } else {
                consolaErrores.append("✖ ERRORES LÉXICOS ENCONTRADOS. La compilación se detuvo.\n");
                for (String error : lexer.getErroresLexicos()) {
                    consolaErrores.append("  " + error + "\n");
                }
                panelPestanasInferior.setSelectedIndex(0);
            }

        } catch (Exception ex) {
            consolaErrores.append("Error interno de ejecución: " + ex.getMessage());
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
    
    
    private void copiarPrompt() {
        String texto = vistaPrompt.getText();
        if (texto != null && !texto.trim().isEmpty()) {
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(texto), null);
            JOptionPane.showMessageDialog(this, "Prompt copiado al portapapeles exitosamente.", "Copiado", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    
    private void guardarPromptArchivo() {
        String texto = vistaPrompt.getText();
        if (texto == null || texto.trim().isEmpty() || texto.contains("(Se mostrará al compilar)")) {
            JOptionPane.showMessageDialog(this, "No hay un prompt válido para guardar. Compila tu código primero.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Archivo de Texto (*.txt)", "txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File archivo = chooser.getSelectedFile();
                if (!archivo.getName().toLowerCase().endsWith(".txt") && !archivo.getName().toLowerCase().endsWith(".md")) {
                    archivo = new File(archivo.getAbsolutePath() + ".txt");
                }
                Files.write(archivo.toPath(), texto.getBytes());
                JOptionPane.showMessageDialog(this, "Prompt guardado exitosamente en: " + archivo.getName(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al guardar el prompt: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}