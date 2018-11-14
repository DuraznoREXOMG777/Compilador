/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Hp EliteDesk
 */
public class Ut {
    //LinkedList<Errores> listaErrores, 

    public static int countTokenTypeLine(LinkedList<Token> listaToken, int type, int line) {
        int counter = 0;
        for (int i = 0; i < listaToken.size(); i++) {
            if (listaToken.get(i).getNumero() == type && listaToken.get(i).getLinea() == line) {
                counter++;
            }
        }
        return counter;
    }

    public static int countTokenType(LinkedList<Token> listaToken, int type) {
        int counter = 0;
        for (int i = 0; i < listaToken.size(); i++) {
            if (listaToken.get(i).getNumero() == type) {
                counter++;
            }
        }
        return counter;
    }

    public static int countErrorLine(LinkedList<Errores> listaErrores, int line) {
        int counter = 0;
        for (int i = 0; i < listaErrores.size(); i++) {
            if (listaErrores.get(i).getLinea() == line) {
                counter++;
            }
        }
        return counter;
    }

    public static int countError(LinkedList<Errores> listaErrores) {
        return listaErrores.size();
    }

    public static int getAgrupacion(LinkedList<Token> ll) {
        return (countTokenType(ll, -29) + countTokenType(ll, -30) + countTokenType(ll, -31)
                + countTokenType(ll, -32) + countTokenType(ll, -33) + countTokenType(ll, -34));
    }

    public static int getAsignacion(LinkedList<Token> ll) {
        return (countTokenType(ll, -35) + countTokenType(ll, -36) + countTokenType(ll, -37)
                + countTokenType(ll, -38) + countTokenType(ll, -39) + countTokenType(ll, -40));
    }

    public static int getMatematicos(LinkedList<Token> ll) {
        return (countTokenType(ll, -8) + countTokenType(ll, -9) + countTokenType(ll, -10)
                + countTokenType(ll, -11) + countTokenType(ll, -12) + countTokenType(ll, -42));
    }

    public static int getMonogamos(LinkedList<Token> ll) {
        return (countTokenType(ll, -14) + countTokenType(ll, -13));
    }

    public static int getLogicos(LinkedList<Token> ll) {
        return (countTokenType(ll, -15) + countTokenType(ll, -16) + countTokenType(ll, -17) + countTokenType(ll, -43));
    }

    public static int getRelacionales(LinkedList<Token> ll) {
        return (countTokenType(ll, -18) + countTokenType(ll, -19) + countTokenType(ll, -20)
                + countTokenType(ll, -21) + countTokenType(ll, -22) + countTokenType(ll, -23));
    }

    public static int getDesplazamiento(LinkedList<Token> ll) {
        return (countTokenType(ll, -24) + countTokenType(ll, -25));
    }

    public static int getPuntuacion(LinkedList<Token> ll) {
        return (countTokenType(ll, -26) + countTokenType(ll, -27) + countTokenType(ll, -28) + countTokenType(ll, -41));
    }

    //Linea
    public static int getAgrupacionLinea(LinkedList<Token> ll, int i) {
        return (countTokenTypeLine(ll, -29, i) + countTokenTypeLine(ll, -30, i) + countTokenTypeLine(ll, -31, i)
                + countTokenTypeLine(ll, -32, i) + countTokenTypeLine(ll, -33, i) + countTokenTypeLine(ll, -34, i));
    }

    public static int getAsignacionLinea(LinkedList<Token> ll, int i) {
        return (countTokenTypeLine(ll, -35, i) + countTokenTypeLine(ll, -36, i) + countTokenTypeLine(ll, -37, i)
                + countTokenTypeLine(ll, -38, i) + countTokenTypeLine(ll, -39, i) + countTokenTypeLine(ll, -40, i));
    }

    public static int getMatematicosLinea(LinkedList<Token> ll, int i) {
        return (countTokenTypeLine(ll, -8, i) + countTokenTypeLine(ll, -9, i) + countTokenTypeLine(ll, -10, i)
                + countTokenTypeLine(ll, -11, i) + countTokenTypeLine(ll, -12, i) + countTokenTypeLine(ll, -42, i));
    }

    public static int getMonogamosLinea(LinkedList<Token> ll, int i) {
        return (countTokenTypeLine(ll, -14, i) + countTokenTypeLine(ll, -13, i));
    }

    public static int getLogicosLinea(LinkedList<Token> ll, int i) {
        return (countTokenTypeLine(ll, -15, i) + countTokenTypeLine(ll, -16, i) + countTokenTypeLine(ll, -17, i) + countTokenTypeLine(ll, -43, i));
    }

    public static int getRelacionalesLinea(LinkedList<Token> ll, int i) {
        return (countTokenTypeLine(ll, -18, i) + countTokenTypeLine(ll, -19, i) + countTokenTypeLine(ll, -20, i)
                + countTokenTypeLine(ll, -21, i) + countTokenTypeLine(ll, -22, i) + countTokenTypeLine(ll, -23, i));
    }

    public static int getDesplazamientoLinea(LinkedList<Token> ll, int i) {
        return (countTokenTypeLine(ll, -24, i) + countTokenTypeLine(ll, -25, i));
    }

    public static int getPuntuacionLinea(LinkedList<Token> ll, int i) {
        return (countTokenTypeLine(ll, -26, i) + countTokenTypeLine(ll, -27, i) + countTokenTypeLine(ll, -28, i) + countTokenTypeLine(ll, -41, i));
    }

    public static int getPalabrasReservadasByLine(LinkedList<Token> ll, int i) {
        int counter = 0;
        for (Token token : ll) {
            if (token.getLinea() == i && token.getNumero() < -43) {
                counter++;
            }
        }
        return counter;
    }

    public static int getPalabrasReservadas(LinkedList<Token> ll) {
        int counter = 0;
        for (Token token : ll) {
            if (token.getNumero() < -43) {
                counter++;
            }
        }
        return counter;
    }

    public static int getAll(LinkedList<Token> ll, LinkedList<Errores> le) {
        return ll.size() + le.size();
    }

    public static int getAllByLine(LinkedList<Token> ll, LinkedList<Errores> le, int i) {
        int counter = 0;
        for (Token token : ll) {
            if (token.getLinea() == i) {
                counter++;
            }
        }
        for (Errores errores : le) {
            if (errores.getLinea() == i) {
                counter++;
            }
        }
        return counter;
    }

    public static void generateExcel(File file, LinkedList<Token> listaToken, LinkedList<Errores> listaErrores,
            int lineas, int[][] counters, LinkedList<Variable> listaVariables, LinkedList<String[]> listaContadores,
            LinkedList<int[]> linstaContadores2, LinkedList<String> asignacion, LinkedList<Integer> lineaAsignaciones,
            LinkedList<Semantica2> listaSemantica) {
        //Workbook  + Sheets
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Lista de Contadores");
        XSSFSheet sheet2 = workbook.createSheet("Lista de Tokens");
        XSSFSheet sheet3 = workbook.createSheet("Lista de Errores");
        XSSFSheet sheet4 = workbook.createSheet("Lista de Variables");
        XSSFSheet sheet5 = workbook.createSheet("Contador de Ambito");
        XSSFSheet sheet6 = workbook.createSheet("Contadores Semántica 1");
        XSSFSheet sheet7 = workbook.createSheet("Estados Semántica 2");

        //Sheet Writers
        setSheet(countersList(lineas, listaToken, listaErrores, counters), sheet);
        setSheet(tokenList(listaToken), sheet2);
        setSheet(errorList(listaErrores), sheet3);
        setSheet(variableList(listaVariables), sheet4);
        setSheet(counterAList(listaContadores), sheet5);
        setSheet(counterAList(linstaContadores2, asignacion, lineaAsignaciones), sheet6);
        setSheet(counterAList(listaSemantica, 0), sheet7);

        //Write into file.
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("empty-statement")
    private static Object[][] countersList(int i, LinkedList<Token> listaTokens, LinkedList<Errores> listaErrores, int[][] counters) {
        Object[][] listaContadores = new Object[i + 2][18];

        //Encabezados
        Object[] primeraLinea = {" ", "ID", "Comentarios", "Palabras Reservadas", "Enteras", "Texto", "Decimal", "Carácter", "Matemáticos", "Monógamo",
            "Lógicos", "Relacionales", "Desplazamiento", "Signos de Puntuación", "Signos de Agrupación", "Signos de Asignación", "Exponenciales", "Errores"};

        listaContadores[0] = primeraLinea;

        for (int j = 1; j < listaContadores.length; j++) {
            for (int k = 0; k < listaContadores[0].length; k++) {
                Object datatype = null;
                if (k == 0) {
                    datatype = "Linea " + (j);
                }
                if ((j + 1) == listaContadores.length && k == 0) {
                    datatype = "Total";
                }
                switch (k) {
                    case 1:
                        datatype = counters[j - 1][0];
                        break;
                    case 2:
                        datatype = counters[j - 1][1];
                        break;
                    case 3:
                        datatype = counters[j - 1][2];
                        break;
                    case 4:
                        datatype = counters[j - 1][3];
                        break;
                    case 5:
                        datatype = counters[j - 1][4];
                        break;
                    case 6:
                        datatype = counters[j - 1][5];
                        break;
                    case 7:
                        datatype = counters[j - 1][6];
                        break;
                    case 8:
                        datatype = counters[j - 1][7];
                        break;
                    case 9:
                        datatype = counters[j - 1][8];
                        break;
                    case 10:
                        datatype = counters[j - 1][9];
                        break;
                    case 11:
                        datatype = counters[j - 1][10];
                        break;
                    case 12:
                        datatype = counters[j - 1][11];
                        break;
                    case 13:
                        datatype = counters[j - 1][12];
                        break;
                    case 14:
                        datatype = counters[j - 1][13];
                        break;
                    case 15:
                        datatype = counters[j - 1][14];
                        break;
                    case 16:
                        datatype = counters[j - 1][15];
                        break;
                    case 17:
                        datatype = counters[j - 1][16];
                        break;
                }
                listaContadores[j][k] = datatype;
            }
        }
        listaContadores[i + 1][0] = "Total";
        listaContadores[i + 1][1] = countTokenType(listaTokens, -1);
        listaContadores[i + 1][2] = countTokenType(listaTokens, -2);
        listaContadores[i + 1][3] = getPalabrasReservadas(listaTokens);
        listaContadores[i + 1][4] = countTokenType(listaTokens, -3);
        listaContadores[i + 1][5] = countTokenType(listaTokens, -7);
        listaContadores[i + 1][6] = countTokenType(listaTokens, -4);
        listaContadores[i + 1][7] = countTokenType(listaTokens, -6);
        listaContadores[i + 1][8] = getMatematicos(listaTokens);
        listaContadores[i + 1][9] = getMonogamos(listaTokens);
        listaContadores[i + 1][10] = getLogicos(listaTokens);
        listaContadores[i + 1][11] = getRelacionales(listaTokens);
        listaContadores[i + 1][12] = getDesplazamiento(listaTokens);
        listaContadores[i + 1][13] = getPuntuacion(listaTokens);
        listaContadores[i + 1][14] = getAgrupacion(listaTokens);
        listaContadores[i + 1][15] = getAsignacion(listaTokens);
        listaContadores[i + 1][16] = countTokenType(listaTokens, -5);
        listaContadores[i + 1][17] = countError(listaErrores);
        return listaContadores;
    }

    private static Object[][] tokenList(LinkedList<Token> listaTokens) {
        Object[][] listaTokensSheet = new Object[listaTokens.size() + 1][4];

        Object[] primeraLinea = {"Token", "Lexema", "Linea", "Incluido"};
        listaTokensSheet[0] = primeraLinea;

        int counter = 0;
        for (Token token : listaTokens) {
            listaTokensSheet[++counter][0] = token.getNumero();
            listaTokensSheet[counter][1] = token.getLexema();
            listaTokensSheet[counter][2] = token.getLinea();
            if (token.getNumero() != -2) {
                listaTokensSheet[counter][3] = "Sí";
            } else {
                listaTokensSheet[counter][3] = "No";
            }
        }
        return listaTokensSheet;
    }

    private static Object[][] errorList(LinkedList<Errores> listaErrores) {
        Object[][] listaTokensSheet = new Object[listaErrores.size() + 1][5];

        Object[] primeraLinea = {"Token", "Descripción", "Lexema", "Tipo Error", "Línea"};
        listaTokensSheet[0] = primeraLinea;

        int counter = 0;
        for (Errores errores : listaErrores) {
            listaTokensSheet[++counter][0] = errores.getNumero();
            listaTokensSheet[counter][1] = errores.getDescripcion();
            listaTokensSheet[counter][2] = errores.getLexema();
            listaTokensSheet[counter][3] = errores.getTipoError();
            listaTokensSheet[counter][4] = errores.getLinea();
        }
        return listaTokensSheet;
    }

    private static Object[][] variableList(LinkedList<Variable> listaVariables) {
        Object[][] listaTokensSheet = new Object[listaVariables.size() + 1][10];

        Object[] primeraLinea = {"ID", "Tipo", "Clase", "Amb", "Tarr", "DimArr", "NoPar", "TParr", "Cambios", "Forward"};
        listaTokensSheet[0] = primeraLinea;

        int counter = 0;
        for (Variable variable : listaVariables) {
            listaTokensSheet[++counter][0] = variable.getNombre();
            listaTokensSheet[counter][1] = variable.getTipo();
            listaTokensSheet[counter][2] = variable.getClase();
            listaTokensSheet[counter][3] = variable.getAmbito();
            listaTokensSheet[counter][4] = variable.getTarr();
            listaTokensSheet[counter][5] = variable.getDimarr();
            listaTokensSheet[counter][6] = variable.getNopar();
            listaTokensSheet[counter][7] = variable.getTparr();
            listaTokensSheet[counter][8] = trueFalse(variable.isChanges());
            listaTokensSheet[counter][9] = trueFalse(variable.isForward());
        }
        return listaTokensSheet;
    }
    
    private static int trueFalse(boolean bol){
        if(bol)
            return 1;
        else
            return 0;
    }

    private static void setSheet(Object[][] matrix, Sheet sheet) {
        int rowCounter = 0;
        for (Object[] bMatrix : matrix) {
            Row row = sheet.createRow(rowCounter++);
            int colNum = 0;
            for (Object field : bMatrix) {
                Cell cell = row.createCell(colNum++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }
        }
    }

    private static Object[][] counterAList(LinkedList<String[]> listaContadores) {
        Object[][] listaTokensSheet = new Object[listaContadores.size() + 1][9];
        LinkedList<String[]> listaOrdenada = new LinkedList();
        for (int i = 0; i < listaContadores.size(); i++) {
            for (String[] listaOrdenada1 : listaContadores) {
                if (Integer.parseInt(listaOrdenada1[0]) == i) {
                    listaOrdenada.add(listaOrdenada1);
                }
            }
        }

        Object[] primeraLinea = {"Ambito", "Variables", "Constantes", "Functions", "Procedures", "Arreglos", "Parámetros", "ParArr", "Scope"};
        listaTokensSheet[0] = primeraLinea;
        int counter = 1;
        for (String[] listaContadore : listaOrdenada) {
            for (int i = 0; i < listaContadore.length; i++) {
                if (listaContadore[i].equals("")) {
                    listaContadore[i] = "-";
                }
            }
            listaTokensSheet[counter++] = listaContadore;
        }
        return listaTokensSheet;
    }

    private static Object[][] counterAList(LinkedList<int[]> linstaContadores2, LinkedList<String> asignacion, LinkedList<Integer> lineaAsignaciones) {
        Object[][] listaTokensSheet = new Object[linstaContadores2.size() + 1][9];

        Object[] primeraLinea = {"Línea", "Ch", "I", "R", "E", "B", "S", "V", "Asignación"};
        listaTokensSheet[0] = primeraLinea;
        int counter = 1;
        String s = "-";
        for (int i = 0; i < linstaContadores2.size(); i++) {
            listaTokensSheet[counter][0] = lineaAsignaciones.get(i);
            if (linstaContadores2.get(i)[3] == 0) {
                listaTokensSheet[counter][1] = s;
            } else {
                listaTokensSheet[counter][1] = linstaContadores2.get(i)[3];
            }
            if (linstaContadores2.get(i)[2] == 0) {
                listaTokensSheet[counter][2] = s;
            } else {
                listaTokensSheet[counter][2] = linstaContadores2.get(i)[2];
            }
            if (linstaContadores2.get(i)[0] == 0) {
                listaTokensSheet[counter][3] = s;
            } else {
                listaTokensSheet[counter][3] = linstaContadores2.get(i)[0];
            }
            if (linstaContadores2.get(i)[1] == 0) {
                listaTokensSheet[counter][4] = s;
            } else {
                listaTokensSheet[counter][4] = linstaContadores2.get(i)[1];
            }
            if (linstaContadores2.get(i)[4] == 0) {
                listaTokensSheet[counter][5] = s;
            } else {
                listaTokensSheet[counter][5] = linstaContadores2.get(i)[4];
            }
            if (linstaContadores2.get(i)[5] == 0) {
                listaTokensSheet[counter][6] = s;
            } else {
                listaTokensSheet[counter][6] = linstaContadores2.get(i)[5];
            }
            if (linstaContadores2.get(i)[7] == 0) {
                listaTokensSheet[counter][7] = s;
            } else {
                listaTokensSheet[counter][7] = linstaContadores2.get(i)[7];
            }
            listaTokensSheet[counter][8] = asignacion.get(i);
            counter++;
        }
        return listaTokensSheet;
    }
    
    private static Object[][] counterAList(LinkedList<Semantica2> listaSemantica, int cero) {
        Object[][] listaTokensSheet = new Object[listaSemantica.size() + 1][6];

        Object[] primeraLinea = {"Regla", "TP", "ValorR", "Linena", "Estado", "Ambito"};
        listaTokensSheet[0] = primeraLinea;
        int counter = 1;
        String s = "-";
        for (int i = 0; i < listaSemantica.size(); i++) {
            listaTokensSheet[counter][0] = listaSemantica.get(i).getRegla();
            listaTokensSheet[counter][1] = listaSemantica.get(i).getTp();
            listaTokensSheet[counter][2] = listaSemantica.get(i).getValorr();
            listaTokensSheet[counter][3] = listaSemantica.get(i).getLinea();
            listaTokensSheet[counter][4] = listaSemantica.get(i).getEstado();
            listaTokensSheet[counter][5] = listaSemantica.get(i).getAmbito();
            counter++;
        }
        return listaTokensSheet;
    }

    public static int typeNumber(String type) {
        switch (type) {
            case "real":
                return 0;
            case "expo":
            case "exp":
                return 1;
            case "integer":
                return 2;
            case "char":
                return 3;
            case "bool":
                return 4;
            case "string":
                return 5;
            case "file":
                return 6;
            case "varian":
                return 7;
            default:
                return -1;
        }
    }

    public static int typeHierarchy(String type) {
        switch (type) {
            case "real":
                return 4;
            case "expo":
            case "exp":
                return 5;
            case "integer":
                return 3;
            case "char":
                return 2;
            case "varian":
                return 1;
            default:
                return -1;
        }
    }

    public static boolean typeCompatibility(String type1, String type2) {
        if (isNumerical(type1) && isNumerical(type2)) {
            return true;
        } else {
            if (type1.equals("string") && type2.equals("string")) {
                return true;
            } else if (type1.equals("bool") && type2.equals("bool")) {

            }
        }
        return false;
    }

    public static boolean typeCompatibility2(String type1, String type2) {
        if (isNumerical(type1) && isNumerical(type2)) {
            return true;
        }
        return false;
    }

    public static boolean isNumerical(String type) {
        switch (type) {
            case "real":
            case "exp":
            case "expo":
            case "integer":
            case "char":
            case "varian":
                return true;
        }
        return false;
    }

    public static boolean isAsignable(String type1, String type2) {
        if (type1.equals("varian") || type2.equals("varian")) {
            return true;
        } else if (isNumerical(type1) && isNumerical(type2)) {
            if (typeHierarchy(type2) >= typeHierarchy(type1)) {
                return true;
            }
        } else {
            if (type1.equals(type2)) {
                return true;
            }
        }
        return false;
    }

    public static String assignError(String type1, String type2) {
        if (isNumerical(type1) && isNumerical(type2)) {
            if (typeHierarchy(type1) >= typeHierarchy(type2)) {
                return "Incompatibilidad de Asignación: Perdida de precisión en la conversión " + type1 + " a " + type2 + ".";
            }
        } else if (!type1.equals(type2)) {
            return "Incompatibilidad de Asignación: No puedes asignar un " + type1 + " a un " + type2 + ".";
        }
        return null;
    }

    public static String checarArreglo(int[] arreglo) {
        int x = 0;
        for (int i = 0; i < arreglo.length; i++) {
            x += arreglo[i];
        }
        if (x == 0) {
            return "";
        }
        return "T";
    }

    public static String imprimirValor(String tipo) {
        switch (tipo) {
            case "real":
                return "R";
            case "exp":
                return "E";
            case "integer":
                return "I";
            case "char":
                return "C";
            case "bool":
                return "B";
            case "string":
                return "S";
            case "file":
                return "F";
            case "varian":
                return "V";
        }
        return "";
    }
    
    public static String ruleOneUp(String tipo) {
        switch (tipo) {
            case "real":
                return "exp";
            case "exp":
                return "exp";
            case "integer":
                return "real";
            case "char":
                return "integer";
        }
        return "";
    }

    public static boolean isCaseable(String tipo) {
        switch (tipo) {
            case "string":
            case "integer":
            case "char":
            case "varian":
                return true;
        }
        return false;
    }

    public static boolean arregloTodo(Variable arreglo, LinkedList<Variable> listaArreglo) {
        if (listaArreglo.getFirst().getTipo().equals("todo")) {
            for (Variable listaArreglo1 : listaArreglo) {
                if (!listaArreglo1.getTipo().equals("todo")) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean entraEnRango(Variable arreglo, int numero, int dimension) {
        String[] tarrDividido = arreglo.getTarr().split("-|,");
        return (numero >= Integer.parseInt(tarrDividido[(dimension * 2) - 2])
                && numero <= Integer.parseInt(tarrDividido[(dimension * 2) - 1]));
    }

    public final static int[][] producciones = {
        {2, -26, -1, -44}, //PROGRAM 1
        {303, -46, 6, 27, 302, -45, 52, 5, 4, 3}, //BLOQUE 2 //SE AGREGÓ 302 Y  303 - BANDERA CHECAR DECLARADAS.
        {306, 7, -26, 10, -47}, //A 3 - SE AGREGÓ BANDERA 304, 305 Y 306.
        {8, 313, 24, -41, 12, 300, -1, 307, -48}, //B 4 - SE AGREGÓ 300 - BANDERAS DE ÁMBITO DEC. --SE ELIMINÓ -26
        {8, 313, 24, -41, 12, 300, -1, 307, -49}, //B 5 - SE AGREGÓ 300 - BANDERAS DE ÁMBITO DEC. --SE ELIMINÓ -26
        {306, 9, 305, -26, 40, -35, -41, -1, -51, 304}, //C 6
        {6, 27, -26}, //D 7
        {7, 305, -26, 10}, //E 8 -- SE AGREGÓ 305
        {4, 301, 335,-26, -50}, //F 9 - SE AGREGÓ 301 - BANDERAS DE ÁMBITO DEC.
        {4, 301, 2, 336}, //F 10 - SE AGREGÓ 301 - BANDERAS DE ÁMBITO DEC.
        {9, 305, -26, 40, -35, -41, -1, 304}, //G 11
        {305, 24, -41, 11, -1, 304}, //DECLARACIONVARIABLE 12 -- SE CAMBIARON BANDERAS 304 Y 305 desde A.
        {11, -1, -27}, //H 13
        {25, -52}, //TIPO 14
        {25, -54}, //TIPO 15
        {25, -55}, //TIPO 16
        {25, -56}, //TIPO 17
        {25, -57}, //TIPO 18
        {25, -58}, //TIPO 19
        {25, -59}, //TIPO 20
        {310, -30, 26, 309, 22, -28, -28, 22, 308, -29}, //I 21
        {26, 309, 22, -28, -28, 22, 308, -27}, //J 22
        {23, 17}, //EXPPASCAL 23
        {23, 318, 17, -18}, //K 24
        {23, 318, 17, -19}, //K 25
        {23, 318, 17, -22}, //K 26
        {23, 318, 17, -23}, //K 27
        {23, 318, 17, -21}, //K 28
        {23, 318, 17, -20}, //K 29
        {19, 20, 18}, //SIMEPLEEXPPASCAL 30
        {319, -8}, //L 31
        {319, -9}, //L 32
        {19, 318, 20, -17}, //M 33
        {19, 318, 20, -8}, //M 34
        {19, 318, 20, -9}, //M 35
        {21, 15}, //TERMINPASCAL 36
        {21, 318, 15, -10}, //N 37
        {21, 318, 15, -11}, //N 38
        {21, 318, 15, -43}, //N 39
        {21, 318, 15, -16}, //N 40
        {16, 42}, //ELEVACION 41
        {16, 318, 42, -42}, //O 42
        {337,-30, 13, -29}, //LISTAPARAMETROS 43
        {14, 312, 10, 311}, //P 44
        {14, 312, 10, 311, -26}, //Q 45
        {2001, 28, -60, 2000}, //ESTATUTOS 46
        {-30, 30, -1, -29, -61}, //ESTATUTOS 47
        {31, 27, -63, 317, 1002, 320, 22, 316, 1001, -62}, //ESTATUTOS 48 if EXPPASCAL then ESTATUTOS U
        {1002, 317, 320, 22, 316, 1001, -65, 32, -26, 27, -64}, //ESTATUTOS 49
        {-68, 27, 40, -67, 40, -35, -26, -1, -66}, //ESTATUTOS 50
        {27, -70, 1002, 317 ,320, 22, 316, 1001, -69}, //ESTATUTOS 51 while EXPPASCAL
        {-46, 32, -26, 27, -45}, //ESTATUTOS 52
        {322, -46, 34, 27, -41, 33, 326, 38, 325, -72, 317, 324, 22, 316, 323, -71, 321}, //ESTATUTOS 53 case EXPPASCAL of CONSTSSIGNO : ESTATUSO ELSE ESTATUTOS end
        {317, 338, 22,316 , -73}, //ESTATUTOS 54 retur EXPPASCAL
        {1002, 317, 22, 316, 1001}, //ESTATUTOS 55 EXPPascal
        {-30, 29, 22, -29}, //R 56
        {29, 22, -27}, //S 57
        {35}, //T 58
        {27, -74}, //U 59
        {32, -26, 27}, //V 60
        {33, 326, 38, 325, -27}, //X 61
        {34, 27, -41, 33, 326, 38, 325,-75}, //Y 62
        {27, -74}, //Y 63
        {315, 37, -32, 328, 36, -31, 327}, //ARREGLO 64
        {317, 22, 316}, //Z 65
        {-76}, //Z 66
        {35}, //A1 67
        {-4}, //CONSTANTES/SIGNO 68
        {-7}, //CONSTANTES/SIGNO 69
        {-6}, //CONSTANTES/SIGNO 70
        {-3}, //CONSTANTES/SIGNO 71
        {-5}, //CONSTANTES/SIGNO 72
        {-77}, //CONSTANTES/SIGNO 73
        {-78}, //CONSTANTES/SIGNO 74
        {-35}, //ASGINACIÓN 75
        {-36}, //ASGINACIÓN 76
        {-39}, //ASGINACIÓN 77
        {-38}, //ASGINACIÓN 78
        {-37}, //ASGINACIÓN 79
        {41, -8}, //DECLARACIONCONSTANTES 80
        {41, -9}, //DECLARACIONCONSTANTES 81
        {38}, //DECLARACIONCONSTANTES 82
        {-4}, //B1 83
        {-3}, //B1 84
        {375, -30, -29, -79}, //FUNCIONES 85
        {-30, 317, 370, 22, 316, -29, -80, 369}, //FUNCIONES 86
        {-30, 317, 368, 22, 316, -29, -81, 367}, //FUNCIONES 87
        {366, -30, 317, 365, 22, 316, -27, 317, 365, 22, 316, -29, -53, 364}, //FUNCIONES 88
        {363, 48, -88, 373}, //FUNCIONES 89 strcmp
        {363, 48, -89, 361}, //FUNCIONES 90 strcpy
        {363, 48, -90, 360}, //FUNCIONES 91 strcat
        {-30, 358,317, 359, 22, 316, -27, 
             317, 359, 22, 316,  -27, 
             317, 359, 22, 316,  -29, -82, 357}, //FUNCIONES 92 strins
        {-30,  317, 356, 22, 316,  -29, -83, 355}, //FUNCIONES 93 strLen
        {353, 49, -91, 352}, //FUNCIONES 94 toUpper
        {353, 49, -92, 372}, //FUNCIONES 95 toLower
        {351, -30,  317, 350, 22, 316,  -27,  317, 350, 22, 316,  -27, 317, 349,-1, 316, -29, -84}, //FUNCIONES 96
        {348, -30, 317, 347,-1, 316, -29, -85}, //FUNCIONES 97 close ( id )
        {344, 50, -93, 371}, //FUNCIONES 98 scanf
        {344, 50, -94, 343}, //FUNCIONES 99 printf
        { 317, 340, 22, 316,  -86, 339}, //FUNCIONES 100 asc EXPPASCAL
        { 317, 342, 22, 316, -87, 341}, //FUNCIONES 101 chr EXPPASCAL
        {-30, 317, 362, 22, 316, -27, 317, 362, 22, 316, -29}, //C1 102
        {-30, 317, 354, 22, 316,  -29}, //D1 103
        {-30, 51,  317, 346, 22, 316,  -27, 317, 345, -1, 316, -29}, //E1 104 ( id , EXPPASCAL F1 )
        {51,  317, 346, 22, 316,  -27}, //F1 105 , EXPPASCAL F1
        {38}, //FACTORPASCAL 106
        {319, -30, 22, -29, -15}, //FACTORPASCAL 107 --NOT -15 / -95
        {3001, 47, 3000}, //FACTORPASCAL 108
        {43, -1}, //FACTORPASCAL 109
        {2005, 43, 319, -1, -13, 2003}, //FACTORPASCAL 110
        {2005, 43, 319, -1, -14, 2003}, //FACTORPASCAL 111
        {44, 329, 35, 331}, //G1 112
        {334, -30, 45, -29, 332}, //G1 113 ( I1 )
        {319, -13, 2004}, //G1 114
        {319, -14, 2004}, //G1 115
        {318, 22, 39}, //H1 116
        {46, 317, 333, 22, 316}, //I1 117
        {46, 317, 333, 22, 316,-27}, //J1 118 
        {306, 7, 305, -26, 10, 304, -47}, //K1 119 --REPETIDA
        {-30, 22, -29}, //FACTORPASCAL 120
        {318, 22, 39}, //G1 121
    };

    static boolean isClassAsignable(String clase) {
        switch (clase) {
            case "arr":
            case "arrPar":
            case "par":
            case "var":
                return true;
        }
        return false;
    }

    static boolean isArrayAsignable(Variable v1, Variable v2) {
        if (v2.getTipo().equals("varian")) {
            return true;
        }
        
        if (v2.getClase().equals("arr") || v1.getClase().equals("arrPar")) {
            if (v2.isPosition()) {
                if (v1.getClase().equals("arrPar") || v1.getClase().equals("arr")) {
                    if (!v1.isPosition()) {
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            } else if (v2.isFullArray()) {
                if (v1.isFullArray()) {
                    return equalArray(v1, v2);
                } else {
                    return true;
                }

            }
        }
        return true;
    }

    static boolean equalArray(Variable v1, Variable v2) {
        boolean diferenteTodo = true;
        if (v1.getTipo().equals(v2.getTipo()) && v1.getDimarr().equals(v2.getDimarr())
                && v1.getTarr().equals(v2.getTarr())) {
            LinkedList<Variable> var1 = v1.getListaArreglo();
            LinkedList<Variable> var2 = v2.getListaArreglo();
            String varN1;
            String varN2;
            for (int i = 0; i < v2.getListaArreglo().size(); i++) {
                varN1 = var1.get(i).getTipo();
                varN2 = var2.get(i).getTipo();
                if(!v1.getListaArreglo().get(i).getTipo().equals(v2.getListaArreglo().get(i).getTipo())){
                    diferenteTodo = false;
                }
            }
            return diferenteTodo;
        }
        return false;
    }
    
    
    public static String semanticaTresNombre(Variable var){
        String nombre = "";
        if(!(var.getClase().equals("arr") || var.getClase().equals("var")))
            nombre = "valor/";
        else
            nombre = "id/";
        nombre+=var.getTipo();
        return nombre;
    }

}
