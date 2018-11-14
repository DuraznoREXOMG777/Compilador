/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.util.LinkedList;
import java.util.Stack;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 *
 * @author Hp EliteDesk
 */
public class LexicAnalyzer {

    private JFrame panel;

    private LinkedList<Token> tokenList;
    private LinkedList<Errores> errorList;
    private LinkedList<Variable> variableList;
    LinkedList<String[]> contador;
    LinkedList<int[]> contadorSemantica1;
    LinkedList<String> asignaciones;
    LinkedList<Integer> lineaAsignaciones;
    static LinkedList<Semantica2> valoresSemantica;

    private boolean tokenized = false, flag = true;
    private int state, col, lastState, resetFlag, linea;
    private int lineaError;
    private char caracter;
    private Variable var;

    private String[][] matrix;
    private static String[][] suma;
    private static String[][] division;
    private static String[][] elevacion;
    private static int[][] rm;
    private String[] base, errorMessages, token, error, reservedWords;
    private String text, lexema, preLexema;
    private int[][] counters, matrixS;
    private int[][] producciones;

    private Stack<Integer> pilaSintactica;
    private static Stack<Integer> pilaAmbito;

    public LexicAnalyzer(JTextArea taBase, JFrame panel) {
        producciones = Ut.producciones;

        matrix = LoadArray.loadCSV();
        matrixS = LoadArray.loadCSV2();
        pilaSintactica = new Stack();
        errorMessages = LoadArray.loadFile("errorMessages", 47);
        reservedWords = LoadArray.loadFile("reservedWords", 51);
        suma = LoadArray.loadSuma();
        division = LoadArray.loadDivision();
        elevacion = LoadArray.loadElevacion();
        rm = LoadArray.loadRM();
        matrix[42][12] = "" + 42;
        matrix[42][13] = "" + 43;
        text = taBase.getText();
        state = 0;
        col = 1;
        preLexema = "";
        lexema = "";

        tokenList = new LinkedList();
        errorList = new LinkedList();
        variableList = new LinkedList();
        contadorSemantica1 = new LinkedList();
        asignaciones = new LinkedList();
        lineaAsignaciones = new LinkedList();
        valoresSemantica = new LinkedList();

        token = new String[3];
        error = new String[4];
        base = text.split("\n");
        linea = 0;
        counters = new int[base.length + 1][17];
        contador = new LinkedList();
        this.panel = panel;
    }

    public void stringAnalyzer() {
        for (int i = 0; i < base.length; i++) { //Basicamente terminará cuando acabe todas las líneas de código.
            for (int j = 0; j < base[i].length(); j++) {
                lastState = state;
                if (j == 0) {
                    base[i] += "\n";
                }
                caracter = base[i].charAt(j);
                col = calcColumn(caracter);
                state = Integer.parseInt(matrix[state][col]);

                if (state < 0) {
                    if (state == -44 && !matchReservedWord(lexema)) {
                        state = 503;
                    } else {
                        if (state == -44) {
                            state -= getIntReservedWord(lexema);
                        }
                        tokenList.add(new Token(state, lexema, i + 1));
                        countEverythingInLife(new Token(state, lexema, i + 1), null);
                        state = 0;
                        lexema = "";
                        tokenized = true;
                        if ((j + 1) != base[i].length()) {
                            j--; //De a fuerzas tiene que regresar al tokenizar, aquí no hay problema.
                        }
                    }
                }
                if (state >= 500) {
                    preLexema = lexema;
                    if (state != 503) {
                        lexema += caracter;
                    }
                    errorList.add(new Errores(state, lexema, i + 1, errorMessages[state - 500], "Léxico"));
                    countEverythingInLife(null, new Errores(state, lexema, i + 1, errorMessages[state - 500], "Léxico"));
                    lexema = "";
                    state = 0;
                    tokenized = true;
                    //Condición de "'".
                    if (!preLexema.equals("'") && lastState != 0) {
                        j--;
                    }
                    if ((j + 1) == base[i].length()) {
                        j++;
                    }
                }

                //¿Cuándo agregar valores? Siempre que se tokeniza no se agrega.
                if (!tokenized) {
                    lexema += caracter;
                }
                if (lastState == 0 && state == 0) {
                    lexema = "";
                }
                tokenized = false;
            }
            linea = i + 1;
        }
        if (state == 42 || state == 43 && lexema.length() > 0) {
            tokenList.add(new Token(-2, lexema, linea));
            state = 0;
            lexema = "";
            countEverythingInLife(new Token(-2, lexema, linea), null);
        }
        countLastLine(base.length);

        //Aquí quitamos todos los comentarios.
        syntaxAnalyzer();
        //Aquí empieza el Sintáctico.
    }

    private void syntaxAnalyzer() {
        boolean declarando = false, decArray = false, listaPar = false,
                decFuncion = false, verificando = false;
        boolean constt = false;
        boolean conSigno = false;
        boolean noAgarrar = false;
        String lastClass = null, lastType = null, lastId, verLastId = "", verLastToken = "";

        /*
         Variables Declaradas para Ámbito.
         listaEnteros: maneja los números de las cantidades de los arreglos.
         pilaAmbito:
         */
        LinkedList<String> pilaIDs = new LinkedList();
        LinkedList<String> listaEnteros = new LinkedList();
        LinkedList<String> listaParametros = new LinkedList();
        String lastID = "";

        /*
         Variables declaradas para semántica.
         pilaOperandos: maneja todos los operandos.
         pilaOperadores: alberga los operadores de todo tipo.
         Banderas Agregadas: 316 - 319.
         */
        boolean entraEXP = false;
        boolean errorVer = false;
        int[] contadorTemporales = new int[8];

        Stack<Variable> pilaOperando = new Stack();
        Stack<String> pilaOperador = new Stack();

        /*
         Semántica 2
         Regla 1: Accomplished
         Regla 2: In Progress

         EMM2
         */
        boolean constanteSigno = false;
        boolean entraCase = false;
        boolean entraExpCase = false;
        boolean arregloVerificar = false;

        Variable caseType = null;
        LinkedList<LinkedList<Variable>> cases = new LinkedList();
        LinkedList<Variable> selectCase = new LinkedList();

        LinkedList<Variable> listaArreglo = new LinkedList();
        LinkedList<Variable> listaFuncion = new LinkedList();
        boolean seguirVerificando = false;

        Variable ultimoArreglo = new Variable();
        Stack<Variable> pilaOperandoArr = new Stack();
        Stack<String> pilaOperadorArr = new Stack();
        int arrayStackPos = 0;

        Stack<Integer> ultimoArreglosPos = new Stack();
        Stack<Variable> ultimoArreglos = new Stack();
        Stack<Variable> errorNoEsArreglos = new Stack();

        Stack<String> stackTipo = new Stack();
        int funcionAnidada = 0;
        int arrayAnidado = 0;

        Variable lastIdentificador = new Variable();
        Variable lastVariable = new Variable();
        Token lastToken = null;

        boolean sigueEXP = false;
        boolean errorNoEsArreglo = false;
        boolean empiezaFuncion = false;

        //Reglas Funciones
        Variable lastMetodo = new Variable();
        int lastMetodoDec = 0;
        Variable methodDec = new Variable();
        boolean duplicatedMethod = false;
        boolean newMethod = false;

        LinkedList<Variable> parametersMethodT = new LinkedList();
        LinkedList<Variable> parametersMethod = new LinkedList();
        int dupNumber = 0;
        int numFunc = 0;

        Stack<Variable> pilaReturn = new Stack();
        Stack<Integer> pilaNecesitaReturn = new Stack();
        Stack<Integer> pilaReturnNumber = new Stack();
        Stack<String> pilaReturnTipo = new Stack();

        boolean write = false;
        boolean masMas = false;
        boolean entroErrorMas = false;
        String typu = "";

        /*
         Semántica 3
         */
        LinkedList<Variable> scanf = new LinkedList();
        Stack<Variable> open = new Stack();
        Stack<Variable> strins = new Stack();
        Stack<Variable> strcat = new Stack();
        Stack<Variable> expo = new Stack();
        Stack<String> pilaFunciones = new Stack();

        Stack<Boolean> funcionPascal = new Stack();

        boolean strCpy = false;
        boolean strCat = false;

        /*Get your shit together
         Variables especiales para parámetros.
         -------------------------------------------
         */
        LinkedList<String> linkedPar = new LinkedList();
        String tipoPar;

        String funcID = null;
        int posFunc = 0;
        /*
         -------------------------------------------
         */

        pilaAmbito = new Stack();
        int ambito = 0;
        int lastAmbito = 0;
        int nopar = 0;

        LinkedList<Token> tokenList = (LinkedList<Token>) this.tokenList.clone();
        //Limpiar la listaDeTokens de comentarios.
        for (int i = 0; i < tokenList.size(); i++) {
            if (tokenList.get(i).getNumero() == -2) {
                tokenList.remove(i--);
            }
        }

        tokenList.add(new Token(-95, "$", base.length)); //Se puede eliminar.

        pilaSintactica.push(-95);
        pilaSintactica.push(1);
        pilaAmbito.push(0);

        while (!pilaSintactica.empty()) {
            if (tokenList.isEmpty()) {
                return;
            }

            System.out.println("");
            System.out.println("Pila: " + pilaSintactica.peek());
            System.out.println("Lista: " + tokenList.getFirst().getNumero() + " : " + tokenList.getFirst().getLexema());

            /*
             Obtener el valor para saber si la producción va con Epsilon o no.
             Si va en epsilon por la producción que genera variables, matar esta.
             Otra opción sería de que si la pila no trae nada pues que no haga nada.

             ¿Cómo obtener que es var, function, procedure o arreglo o arreglo de parametro?
             De la forma más floja podría asignar distintas banderas a estos, pero se debe poder optimizar.
             */
            if (pilaSintactica.peek() == 300) {
                pilaSintactica.pop();
                lastAmbito = pilaAmbito.peek();

                if (!duplicatedMethod) {
                    pilaAmbito.push(++ambito);
                } else {
                    pilaAmbito.push(Integer.parseInt(methodDec.getAmbito()) + 1);
                }
            } else if (pilaSintactica.peek() == 2005) {
                Semantica2 sm2 = new Semantica2();
                sm2.setRegla("1190");
                sm2.setTp("++id || id++");
                sm2.setLinea("" + tokenList.getFirst().getLinea());
                sm2.setAmbito("" + pilaAmbito.peek());
                if (!entroErrorMas) {
                    sm2.setValorr("++id");
                    sm2.setEstado("Aceptado");
                    valoresSemantica.add(sm2);
                }

                masMas = false;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 2004) {

                Semantica2 sm2 = new Semantica2();
                sm2.setRegla("1190");
                sm2.setTp("++id || id++");
                sm2.setLinea("" + tokenList.getFirst().getLinea());
                sm2.setAmbito("" + pilaAmbito.peek());
                if (masMas) {
                    entroErrorMas = true;
                    sm2.setValorr("++id++");
                    sm2.setEstado("Error");
                    errorList.add(new Errores(560, "++ id ++", tokenList.getFirst().getLinea(), "Operación unaria incorrecta.", "Semántica 2"));
                } else {
                    entroErrorMas = false;
                    sm2.setEstado("Aceptado");
                    sm2.setValorr("id++");
                }
                valoresSemantica.add(sm2);
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 3001) {
                funcionPascal.pop();
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 3000) {
                funcionPascal.push(true);
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 2003) {
                masMas = true;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 2001) {
                write = false;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 2000) {
                write = true;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 1002) {
                sigueEXP = false;
                
                if(!pilaOperando.isEmpty()){
                    Variable var = pilaOperando.pop();
                    errorList.add(new Errores(581, var.getTipo(), tokenList.getFirst().getLinea(), "No se ha asignado valor.", "Semántica 2"));
                }
                contadorTemporales = new int[8];
                pilaOperando = new Stack();
                pilaOperador = new Stack();

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 1001) {
                sigueEXP = true;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 400) {
                pilaSintactica.pop();
                noAgarrar = true;
            } else if (pilaSintactica.peek() == 401) {
                pilaSintactica.pop();
                noAgarrar = false;
            } else if (pilaSintactica.peek() == 391) {
                conSigno = true;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 375) {
                Semantica2 sm3 = new Semantica2();
                sm3.setRegla("limpiarpantalla");
                sm3.setTp("limpiarpantalla/14");
                sm3.setLinea("" + tokenList.getFirst().getLinea());
                sm3.setAmbito("" + pilaAmbito.peek());
                sm3.setValorr("void");
                sm3.setEstado("Aceptado");
                valoresSemantica.add(sm3);

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 370) {
                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalSqrt = new Variable();
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalSqrt = pilaOperandoArr.pop();
                    } else {
                        expPascalSqrt = pilaOperando.pop();
                    }

                    Semantica2 sm4 = new Semantica2();
                    sm4.setRegla("sqrt-P1");
                    sm4.setTp("valor/8");
                    sm4.setLinea("" + tokenList.getFirst().getLinea());
                    sm4.setAmbito("" + pilaAmbito.peek());
                    sm4.setValorr(Ut.semanticaTresNombre(expPascalSqrt));

                    if (Ut.isNumerical(expPascalSqrt.getTipo())) {
                        sm4.setEstado("Aceptado");
                    } else {
                        sm4.setEstado("Error");
                        errorList.add(new Errores(581, expPascalSqrt.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'sqrtt'.", "Semántica 3"));
                    }

                    valoresSemantica.add(sm4);

                    Variable sqrtVariable = new Variable();
                    sqrtVariable.setTipo("integer");

                    Semantica2 sm3 = new Semantica2();
                    sm3.setRegla("sqrt");
                    sm3.setTp("sqrt/10");
                    sm3.setLinea("" + tokenList.getFirst().getLinea());
                    sm3.setAmbito("" + pilaAmbito.peek());
                    sm3.setValorr("integer");
                    sm3.setEstado("Aceptado");
                    valoresSemantica.add(sm3);

                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        pilaOperandoArr.push(sqrtVariable);
                    } else {
                        pilaOperando.push(sqrtVariable);
                    }
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 369) {

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 368) {
                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalSqr = new Variable();
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalSqr = pilaOperandoArr.pop();
                    } else {
                        expPascalSqr = pilaOperando.pop();
                    }

                    Semantica2 sm4 = new Semantica2();
                    sm4.setRegla("sqr-P1");
                    sm4.setTp("valor/1");
                    sm4.setLinea("" + tokenList.getFirst().getLinea());
                    sm4.setAmbito("" + pilaAmbito.peek());
                    sm4.setValorr(Ut.semanticaTresNombre(expPascalSqr));

                    if (Ut.isNumerical(expPascalSqr.getTipo())) {
                        sm4.setEstado("Aceptado");
                    } else {
                        sm4.setEstado("Error");
                        errorList.add(new Errores(580, expPascalSqr.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'sqr'.", "Semántica 3"));
                    }

                    valoresSemantica.add(sm4);

                    Variable sqrVariable = new Variable();
                    String tipo = "";
                    String estado = "";
                    if (Ut.isNumerical(expPascalSqr.getTipo())) {
                        if (expPascalSqr.getTipo().equals("char") || expPascalSqr.getTipo().equals("integer")) {
                            tipo = "integer";
                        } else if (expPascalSqr.getTipo().equals("real") || expPascalSqr.getTipo().equals("exp")) {
                            tipo = "expo";
                        }
                        estado = "Aceptado";
                    } else {
                        estado = "Error";
                        tipo = "varian";
                    }

                    Semantica2 sm3 = new Semantica2();
                    sm3.setRegla("sqr");
                    sm3.setTp("sqr/15");
                    sm3.setLinea("" + tokenList.getFirst().getLinea());
                    sm3.setAmbito("" + pilaAmbito.peek());
                    sm3.setValorr(tipo);
                    sm3.setEstado(estado);
                    valoresSemantica.add(sm3);

                    sqrVariable.setTipo(tipo);

                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        pilaOperandoArr.push(sqrVariable);
                    } else {
                        pilaOperando.push(sqrVariable);
                    }
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 367) {

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 366) {
                if (!expo.isEmpty()) {
                    Variable expoSecond = expo.pop();
                    Variable expoFirst = expo.pop();

                    Semantica2 sm4 = new Semantica2();
                    sm4.setRegla("expo-P1");
                    sm4.setTp("valor/1");
                    sm4.setLinea("" + tokenList.getFirst().getLinea());
                    sm4.setAmbito("" + pilaAmbito.peek());
                    sm4.setValorr(Ut.semanticaTresNombre(expoFirst));

                    Semantica2 sm5 = new Semantica2();
                    sm5.setRegla("expo-P2");
                    sm5.setTp("integer/2");
                    sm5.setLinea("" + tokenList.getFirst().getLinea());
                    sm5.setAmbito("" + pilaAmbito.peek());
                    sm5.setValorr(Ut.semanticaTresNombre(expoSecond));

                    if (Ut.isNumerical(expoFirst.getTipo())) {
                        sm4.setEstado("Aceptado");
                    } else {
                        sm4.setEstado("Error");
                        errorList.add(new Errores(579, expoSecond.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'expo', parámetro 1, se espera númerico.", "Semántica 3"));
                    }

                    if (expoSecond.getTipo().equals("integer")) {
                        sm5.setEstado("Aceptado");
                    } else {
                        sm5.setEstado("Error");
                        errorList.add(new Errores(579, expoSecond.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'expo', parámetro 2, se espera integer.", "Semántica 3"));
                    }

                    valoresSemantica.add(sm4);
                    valoresSemantica.add(sm5);

                    String tipo = "";
                    String estado = "";
                    if (Ut.isNumerical(expoFirst.getTipo())) {
                        tipo = Ut.ruleOneUp(expoFirst.getTipo());
                        estado = "Aceptado";
                    } else {
                        estado = "Error";
                        tipo = "varian";
                    }

                    Semantica2 sm3 = new Semantica2();
                    sm3.setRegla("expo");
                    sm3.setTp("expo/17");
                    sm3.setLinea("" + tokenList.getFirst().getLinea());
                    sm3.setAmbito("" + pilaAmbito.peek());
                    sm3.setValorr(tipo);
                    sm3.setEstado(estado);
                    valoresSemantica.add(sm3);

                    Variable expoVariable = new Variable();
                    expoVariable.setTipo(tipo);
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        pilaOperandoArr.push(expoVariable);
                    } else {
                        pilaOperando.push(expoVariable);
                    }
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 365) {
                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalExpo;
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalExpo = pilaOperandoArr.pop();
                    } else {
                        expPascalExpo = pilaOperando.pop();
                    }
                    expo.push(expPascalExpo);
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 364) {

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 363) {
                String tipo = pilaFunciones.pop();
                if (tipo.equals("strcpy") || tipo.equals("strcat")) {
                    Variable strSecond = strcat.pop();
                    Variable strFirst = strcat.pop();

                    Semantica2 sm4 = new Semantica2();
                    sm4.setRegla(tipo + "-P1");
                    sm4.setTp("string/3");
                    sm4.setLinea("" + tokenList.getFirst().getLinea());
                    sm4.setAmbito("" + pilaAmbito.peek());
                    sm4.setValorr(Ut.semanticaTresNombre(strFirst));

                    Semantica2 sm5 = new Semantica2();
                    sm5.setRegla(tipo + "-P2");
                    sm5.setTp("string/3");
                    sm5.setLinea("" + tokenList.getFirst().getLinea());
                    sm5.setAmbito("" + pilaAmbito.peek());
                    sm5.setValorr(Ut.semanticaTresNombre(strSecond));

                    if (strFirst.getTipo().equals("string")) {
                        sm4.setEstado("Aceptado");
                    } else {
                        sm4.setEstado("Error");
                        errorList.add(new Errores(578, strFirst.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'strCat'.", "Semántica 3"));
                    }

                    if (strSecond.getTipo().equals("string")) {
                        sm5.setEstado("Aceptado");
                    } else {
                        sm5.setEstado("Error");
                        errorList.add(new Errores(578, strFirst.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'strCat', se espera string.", "Semántica 3"));
                    }

                    valoresSemantica.add(sm4);
                    valoresSemantica.add(sm5);

                    Semantica2 sm3 = new Semantica2();
                    sm3.setRegla(tipo);
                    sm3.setTp(tipo + "/12");
                    sm3.setLinea("" + tokenList.getFirst().getLinea());
                    sm3.setAmbito("" + pilaAmbito.peek());
                    sm3.setValorr("string");
                    sm3.setEstado("Aceptado");
                    valoresSemantica.add(sm3);

                    Variable strVariable = new Variable();
                    strVariable.setTipo("string");
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        pilaOperandoArr.push(strVariable);
                    } else {
                        pilaOperando.push(strVariable);
                    }

                } else if (tipo.equals("strcmp")) {
                    Variable strSecond = strcat.pop();
                    Variable strFirst = strcat.pop();

                    Semantica2 sm4 = new Semantica2();
                    sm4.setRegla(tipo + "-P1");
                    sm4.setTp("string/3");
                    sm4.setLinea("" + tokenList.getFirst().getLinea());
                    sm4.setAmbito("" + pilaAmbito.peek());
                    sm4.setValorr(Ut.semanticaTresNombre(strFirst));

                    Semantica2 sm5 = new Semantica2();
                    sm5.setRegla(tipo + "-P2");
                    sm5.setTp("string/3");
                    sm5.setLinea("" + tokenList.getFirst().getLinea());
                    sm5.setAmbito("" + pilaAmbito.peek());
                    sm5.setValorr(Ut.semanticaTresNombre(strSecond));

                    if (strFirst.getTipo().equals("string")) {
                        sm4.setEstado("Aceptado");
                    } else {
                        sm4.setEstado("Error");
                        errorList.add(new Errores(577, strFirst.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'str'.", "Semántica 3"));
                    }

                    if (strSecond.getTipo().equals("string")) {
                        sm5.setEstado("Aceptado");
                    } else {
                        sm5.setEstado("Error");
                        errorList.add(new Errores(577, strFirst.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'str', se espera string.", "Semántica 3"));
                    }

                    valoresSemantica.add(sm4);
                    valoresSemantica.add(sm5);

                    Semantica2 sm3 = new Semantica2();
                    sm3.setRegla("strcmp");
                    sm3.setTp("strcmp/13");
                    sm3.setLinea("" + tokenList.getFirst().getLinea());
                    sm3.setAmbito("" + pilaAmbito.peek());
                    sm3.setValorr("bool");
                    sm3.setEstado("Aceptado");
                    valoresSemantica.add(sm3);

                    Variable strVariable = new Variable();
                    strVariable.setTipo("bool");
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        pilaOperandoArr.push(strVariable);
                    } else {
                        pilaOperando.push(strVariable);
                    }
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 362) {
                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalStrC = new Variable();
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalStrC = pilaOperandoArr.pop();
                    } else {
                        expPascalStrC = pilaOperando.pop();
                    }
                    strcat.push(expPascalStrC);
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 373) {
                pilaFunciones.push("strcmp");
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 361) {
                pilaFunciones.push("strcat");
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 360) {
                pilaFunciones.push("strcpy");
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 359) {
                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalstrins = new Variable();
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalstrins = pilaOperandoArr.pop();
                    } else {
                        expPascalstrins = pilaOperando.pop();
                    }
                    strins.push(expPascalstrins);
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 358) {
                Variable strinsThird = strins.pop();
                Variable strinsSecond = strins.pop();
                Variable strinsFirst = strins.pop();

                Semantica2 sm4 = new Semantica2();
                sm4.setRegla("strins-P1");
                sm4.setTp("string/9");
                sm4.setLinea("" + tokenList.getFirst().getLinea());
                sm4.setAmbito("" + pilaAmbito.peek());
                sm4.setValorr(Ut.semanticaTresNombre(strinsFirst));

                Semantica2 sm5 = new Semantica2();
                sm5.setRegla("strins-P2");
                sm5.setTp("string/3");
                sm5.setLinea("" + tokenList.getFirst().getLinea());
                sm5.setAmbito("" + pilaAmbito.peek());
                sm5.setValorr(Ut.semanticaTresNombre(strinsSecond));

                Semantica2 sm6 = new Semantica2();
                sm6.setRegla("strins-P3");
                sm6.setTp("integer/2");
                sm6.setLinea("" + tokenList.getFirst().getLinea());
                sm6.setAmbito("" + pilaAmbito.peek());
                sm6.setValorr(Ut.semanticaTresNombre(strinsThird));

                if (!strinsFirst.getNombre().equals("") || strinsFirst.getNombre().equals("-")) {
                    if (strinsFirst.getTipo().equals("string")) {
                        sm4.setEstado("Aceptado");
                    } else {
                        sm4.setEstado("Error");
                        errorList.add(new Errores(575, strinsFirst.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'strins'.", "Semántica 3"));
                    }
                } else {
                    sm4.setEstado("Error");
                    errorList.add(new Errores(575, "value", tokenList.getFirst().getLinea(), "Tipo de valor mal ingresado en función 'strins'.", "Semántica 3"));
                }

                if (strinsSecond.getTipo().equals("string")) {
                    sm5.setEstado("Aceptado");
                } else {
                    sm5.setEstado("Error");
                    errorList.add(new Errores(575, strinsSecond.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'strins', se espera string.", "Semántica 3"));
                }

                if (strinsThird.getTipo().equals("integer")) {
                    sm6.setEstado("Aceptado");

                } else {
                    sm6.setEstado("Aceptado");
                    errorList.add(new Errores(575, strinsThird.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'strins'. Se espera integer.", "Semántica 3"));
                }

                valoresSemantica.add(sm4);
                valoresSemantica.add(sm5);
                valoresSemantica.add(sm6);

                Semantica2 sm3 = new Semantica2();
                sm3.setRegla("strins");
                sm3.setTp("strins/12");
                sm3.setLinea("" + tokenList.getFirst().getLinea());
                sm3.setAmbito("" + pilaAmbito.peek());
                sm3.setValorr("bool");
                sm3.setEstado("Aceptado");
                valoresSemantica.add(sm3);

                Variable toVariable = new Variable();
                toVariable.setTipo("bool");
                if (funcionAnidada > 0 || arrayAnidado > 0) {
                    pilaOperandoArr.push(toVariable);
                } else {
                    pilaOperando.push(toVariable);
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 357) {

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 356) {
                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalLen = new Variable();
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalLen = pilaOperandoArr.pop();
                    } else {
                        expPascalLen = pilaOperando.pop();
                    }
                    Semantica2 sm2 = new Semantica2();
                    sm2.setRegla("strlen-P1");
                    sm2.setTp("string/3");
                    sm2.setLinea("" + tokenList.getFirst().getLinea());
                    sm2.setAmbito("" + pilaAmbito.peek());
                    sm2.setValorr(Ut.semanticaTresNombre(expPascalLen));

                    if (expPascalLen.getTipo().equals("string")) {
                        sm2.setEstado("Aceptado");
                    } else {
                        sm2.setEstado("Error");
                        errorList.add(new Errores(576, expPascalLen.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'strlen'.", "Semántica 3"));
                    }
                    valoresSemantica.add(sm2);

                    Semantica2 sm3 = new Semantica2();
                    sm3.setRegla("strlen");
                    sm3.setTp("strlen/10");
                    sm3.setLinea("" + tokenList.getFirst().getLinea());
                    sm3.setAmbito("" + pilaAmbito.peek());
                    sm3.setValorr("integer");
                    sm3.setEstado("Aceptado");
                    valoresSemantica.add(sm3);

                    Variable toVariable = new Variable();
                    toVariable.setTipo("integer");
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        pilaOperandoArr.push(toVariable);
                    } else {
                        pilaOperando.push(toVariable);
                    }

                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 355) {

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 354) {
                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalTo = new Variable();
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalTo = pilaOperandoArr.pop();
                    } else {
                        expPascalTo = pilaOperando.pop();
                    }
                    String tipoFunc = pilaFunciones.pop();
                    Semantica2 sm2 = new Semantica2();
                    sm2.setRegla(tipoFunc + "-P1");
                    sm2.setTp("valor/4");
                    sm2.setLinea("" + tokenList.getFirst().getLinea());
                    sm2.setAmbito("" + pilaAmbito.peek());
                    sm2.setValorr(Ut.semanticaTresNombre(expPascalTo));
                    if (expPascalTo.getTipo().equals("string") || expPascalTo.getTipo().equals("char")
                            || expPascalTo.getTipo().equals("varian")) {
                        sm2.setEstado("Aceptado");
                    } else {
                        sm2.setEstado("Error");
                        errorList.add(new Errores(574, expPascalTo.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'toLower/toUpper'.", "Semántica 3"));
                    }
                    
                    valoresSemantica.add(sm2);
                    
                    String tipo;
                    if (expPascalTo.getTipo().equals("char")) {
                        tipo = "char";
                    } else if (expPascalTo.getTipo().equals("string")) {
                        tipo = "string";
                    } else {
                        tipo = "varian";
                    }

                    Semantica2 sm3 = new Semantica2();
                    sm3.setRegla(tipoFunc);
                    sm3.setTp(tipoFunc + "/16");
                    sm3.setLinea("" + tokenList.getFirst().getLinea());
                    sm3.setAmbito("" + pilaAmbito.peek());
                    sm3.setValorr(tipo);
                    sm3.setEstado("Aceptado");
                    valoresSemantica.add(sm3);

                    Variable toVariable = new Variable();

                    toVariable.setTipo(tipo);
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        pilaOperandoArr.push(toVariable);
                    } else {
                        pilaOperando.push(toVariable);
                    }
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 353) {
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 372) {
                pilaFunciones.push("toLower");
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 352) {
                pilaFunciones.push("toUpper");
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 351) {
                for (int i = 0; i < open.size(); i++) {
                    Variable var = open.get(i);

                    Semantica2 sm2 = new Semantica2();
                    sm2.setRegla("open-P" + (i + 2));
                    sm2.setTp("valor/6");
                    sm2.setLinea("" + tokenList.getFirst().getLinea());
                    sm2.setAmbito("" + pilaAmbito.peek());
                    sm2.setValorr(Ut.semanticaTresNombre(var));
                    //Aquí o arriba.
                    if (!var.getTipo().equals("string")) {
                        errorList.add(new Errores(573, var.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato erróneo en función 'open', parametro " + (i + 1), "Semántica 3"));
                        sm2.setEstado("Error");
                    } else {
                        sm2.setEstado("Aceptado");
                    }
                    valoresSemantica.add(sm2);
                }
                //Sigo metiendo el tipo de dato.
                Semantica2 sm3 = new Semantica2();
                sm3.setRegla("open");
                sm3.setTp("open/13");
                sm3.setLinea("" + tokenList.getFirst().getLinea());
                sm3.setAmbito("" + pilaAmbito.peek());
                sm3.setValorr("bool");
                sm3.setEstado("Aceptado");
                valoresSemantica.add(sm3);

                Variable boolOpen = new Variable();
                boolOpen.setTipo("bool");
                if (funcionAnidada > 0 || arrayAnidado > 0) {
                    pilaOperandoArr.push(boolOpen);
                } else {
                    pilaOperando.push(boolOpen);
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 350) {
                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalOpen = new Variable();
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalOpen = pilaOperandoArr.pop();
                    } else {
                        expPascalOpen = pilaOperando.pop();
                    }
                    open.add(expPascalOpen);
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 349) {
                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalOpen = new Variable();
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalOpen = pilaOperandoArr.pop();
                    } else {
                        expPascalOpen = pilaOperando.pop();
                    }

                    Semantica2 sm2 = new Semantica2();
                    sm2.setRegla("open-P1");
                    sm2.setTp("file/5");
                    sm2.setLinea("" + tokenList.getFirst().getLinea());
                    sm2.setAmbito("" + pilaAmbito.peek());
                    sm2.setValorr(Ut.semanticaTresNombre(expPascalOpen));

                    if (expPascalOpen.getTipo().equals("file")) {
                        sm2.setEstado("Aceptado");
                    } else {
                        sm2.setEstado("Error");
                        errorList.add(new Errores(572, expPascalOpen.getTipo(), tokenList.getFirst().getLinea(), "Tipo de identificador erróneo en función 'open', se espera file", "Semántica 3"));
                    }
                    valoresSemantica.add(sm2);
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 348) {
                Variable boolClose = new Variable();

                Semantica2 sm3 = new Semantica2();
                sm3.setRegla("close");
                sm3.setTp("close/13");
                sm3.setLinea("" + tokenList.getFirst().getLinea());
                sm3.setAmbito("" + pilaAmbito.peek());
                sm3.setValorr("bool");
                sm3.setEstado("Aceptado");
                valoresSemantica.add(sm3);

                boolClose.setTipo("bool");
                if (funcionAnidada > 0 || arrayAnidado > 0) {
                    pilaOperandoArr.push(boolClose);
                } else {
                    pilaOperando.push(boolClose);
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 347) {
                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalClose = new Variable();
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalClose = pilaOperandoArr.pop();
                    } else {
                        expPascalClose = pilaOperando.pop();
                    }

                    Semantica2 sm2 = new Semantica2();
                    sm2.setRegla("close-P1");
                    sm2.setTp("file/5");
                    sm2.setLinea("" + tokenList.getFirst().getLinea());
                    sm2.setAmbito("" + pilaAmbito.peek());
                    sm2.setValorr(Ut.semanticaTresNombre(expPascalClose));
                    if (expPascalClose.getTipo().equals("file")) {
                        sm2.setEstado("Aceptado");
                    } else {
                        sm2.setEstado("Error");
                        errorList.add(new Errores(571, expPascalClose.getTipo(), tokenList.getFirst().getLinea(), "Tipo de identificador erróneo en función 'close', se espera file", "Semántica 3"));
                    }
                    valoresSemantica.add(sm2);
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 346) {
                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalNf = new Variable();
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalNf = pilaOperandoArr.pop();
                    } else {
                        expPascalNf = pilaOperando.pop();
                    }
                    scanf.add(expPascalNf);
                }
                pilaSintactica.pop();

            } else if (pilaSintactica.peek() == 345) {

                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalNf = new Variable();

                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalNf = pilaOperandoArr.pop();
                    } else {
                        expPascalNf = pilaOperando.pop();
                    }

                    String tipo = pilaFunciones.peek();
                    Semantica2 sm2 = new Semantica2();
                    sm2.setRegla(tipo + "-P1");
                    sm2.setTp("file/5");
                    sm2.setLinea("" + tokenList.getFirst().getLinea());
                    sm2.setAmbito("" + pilaAmbito.peek());
                    sm2.setValorr(Ut.semanticaTresNombre(expPascalNf));
                    if (expPascalNf.getTipo().equals("file")) {
                        sm2.setEstado("Aceptado");
                    } else {
                        sm2.setEstado("Error");
                        errorList.add(new Errores(570, expPascalNf.getTipo(), tokenList.getFirst().getLinea(), "Tipo de identificar erróneo en función 'printf/scanf', se espera file ", "Semántica 3"));
                    }
                    valoresSemantica.add(sm2);
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 344) {
                String tipo = pilaFunciones.pop();
                //Se revisa la lista de los valores.
                for (int i = 0; i < scanf.size(); i++) {
                    Variable var = scanf.get(i);

                    Semantica2 sm2 = new Semantica2();
                    sm2.setRegla(tipo + "-P" + (i + 2));
                    sm2.setTp("valor/6");
                    sm2.setLinea("" + tokenList.getFirst().getLinea());
                    sm2.setAmbito("" + pilaAmbito.peek());
                    sm2.setValorr(Ut.semanticaTresNombre(var));
                    //Aquí o arriba.
                    if (var.getTipo().equals("file")) {
                        sm2.setEstado("Error");
                        errorList.add(new Errores(570, var.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato erróneo en función 'printf/scanf' ", "Semántica 3"));
                    } else {
                        sm2.setEstado("Aceptado");
                    }
                    valoresSemantica.add(sm2);
                }

                Semantica2 sm3 = new Semantica2();
                sm3.setRegla(tipo);
                sm3.setTp(tipo + "/13");
                sm3.setLinea("" + tokenList.getFirst().getLinea());
                sm3.setAmbito("" + pilaAmbito.peek());
                sm3.setValorr("bool");
                sm3.setEstado("Aceptado");
                valoresSemantica.add(sm3);

                //Sigo metiendo el tipo de dato.
                Variable boolNf = new Variable();
                boolNf.setTipo("bool");
                if (funcionAnidada > 0 || arrayAnidado > 0) {
                    pilaOperandoArr.push(boolNf);
                } else {
                    pilaOperando.push(boolNf);
                }

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 371) {
                pilaFunciones.push("scanf");
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 343) {
                pilaFunciones.push("printf");
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 342) {
                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalChr = new Variable();
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalChr = pilaOperandoArr.pop();
                    } else {
                        expPascalChr = pilaOperando.pop();
                    }

                    Semantica2 sm2 = new Semantica2();
                    sm2.setRegla("chr-P1");
                    sm2.setTp("int/2");
                    sm2.setLinea("" + tokenList.getFirst().getLinea());
                    sm2.setAmbito("" + pilaAmbito.peek());
                    sm2.setValorr(Ut.semanticaTresNombre(expPascalChr));

                    if (expPascalChr.getTipo().equals("integer")) {
                        sm2.setEstado("Aceptado");
                    } else {
                        sm2.setEstado("Error");
                        errorList.add(new Errores(569, expPascalChr.getTipo(), tokenList.getFirst().getLinea(), "Tipoo de dato en función 'chr' ", "Semántica 3"));
                    }
                    valoresSemantica.add(sm2);

                    Semantica2 sm3 = new Semantica2();
                    sm3.setRegla("chr");
                    sm3.setTp("chr/11");
                    sm3.setLinea("" + tokenList.getFirst().getLinea());
                    sm3.setAmbito("" + pilaAmbito.peek());
                    sm3.setValorr("integer");
                    sm3.setEstado("Aceptado");
                    valoresSemantica.add(sm3);

                    Variable chrVariable = new Variable();
                    chrVariable.setTipo("integer");
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        pilaOperandoArr.push(chrVariable);
                    } else {
                        pilaOperando.push(chrVariable);
                    }
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 341) {

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 340) {
                if (pilaOperando.size() > 0) { //Revisar que tenga algo.
                    Variable expPascalAsc;
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        expPascalAsc = pilaOperandoArr.pop();
                    } else {
                        expPascalAsc = pilaOperando.pop();
                    }

                    Semantica2 sm2 = new Semantica2();
                    sm2.setRegla("asc-P1");
                    sm2.setTp("char/7");
                    sm2.setLinea("" + tokenList.getFirst().getLinea());
                    sm2.setAmbito("" + pilaAmbito.peek());
                    sm2.setValorr(Ut.semanticaTresNombre(expPascalAsc));
                    if (expPascalAsc.getTipo().equals("integer")) {
                        sm2.setEstado("Aceptado");
                    } else {
                        sm2.setEstado("Error");
                        errorList.add(new Errores(568, expPascalAsc.getTipo(), tokenList.getFirst().getLinea(), "Tipo de dato mal ingresado en función 'asc'.", "Semántica 3"));
                    }
                    valoresSemantica.add(sm2);

                    Semantica2 sm3 = new Semantica2();
                    sm3.setRegla("asc");
                    sm3.setTp("asc/10");
                    sm3.setLinea("" + tokenList.getFirst().getLinea());
                    sm3.setAmbito("" + pilaAmbito.peek());
                    sm3.setValorr("integer");
                    sm3.setEstado("Aceptado");
                    valoresSemantica.add(sm3);

                    Variable ascVariable = new Variable();
                    ascVariable.setTipo("integer");
                    if (funcionAnidada > 0 || arrayAnidado > 0) {
                        pilaOperandoArr.push(ascVariable);
                    } else {
                        pilaOperando.push(ascVariable);
                    }
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 339) {

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 338) {
                if (pilaOperando.size() > 0) {
                    Variable retur = pilaOperando.pop();
                    pilaReturn.push(retur);
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 337) {
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 336) {
                if (!methodDec.isForward()) {
                    //    errorList.add(new Errores(602, methodDec.getNombre() + "(): forward", numFunc, "No se ha definido el método.", "Semántica 2"));
                } else {
                    //IntroducirManuel
                }
                if (methodDec.getClase().equals("function")) {
                    pilaNecesitaReturn.push(pilaAmbito.peek());
                    pilaReturnTipo.push("function");
                } else {
                    pilaNecesitaReturn.push(pilaAmbito.peek());
                    pilaReturnTipo.push("procedure");
                }
                duplicatedMethod = false;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 335) {
                //Aquí actualizaremos los datos de nuestra función.
                //Entra a forward.

                if (methodDec.isForward()) {
                    errorList.add(new Errores(602, methodDec.getNombre() + "(): forward", numFunc, "Ya se ha definido este método.", "Semántica 2"));
                } else {
                    this.actualizarParametrosFwd(methodDec, variableList);
                }
                //No es forward.

                duplicatedMethod = false;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 334) {
                if (!ultimoArreglos.isEmpty()) {
                    Variable funcion = ultimoArreglos.pop();
                    LinkedList<Variable> parametros = listaParametros(variableList, funcion);
                    for (int i = 0; i < Integer.parseInt(funcion.getNopar()); i++) {
                        Semantica2 sm2 = new Semantica2();
                        sm2.setRegla("1080");
                        sm2.setTp(parametros.get(i).getTipo());
                        sm2.setLinea("" + tokenList.getFirst().getLinea());
                        sm2.setAmbito("" + pilaAmbito.peek());

                        if (i < listaFuncion.size()) {
                            sm2.setValorr(listaFuncion.get(i).getTipo());
                            sm2.setEstado("Aceptado");

                            Semantica2 sm3 = new Semantica2();
                            sm3.setRegla("1090");
                            sm3.setTp(parametros.get(i).getTipo());
                            sm3.setLinea("" + tokenList.getFirst().getLinea());
                            sm3.setAmbito("" + pilaAmbito.peek());
                            sm3.setValorr(listaFuncion.get(i).getTipo());
                            if (!parametros.get(i).getTipo().equals(listaFuncion.get(i).getTipo())) {
                                sm3.setEstado("Error");
                                errorList.add(new Errores(563, listaFuncion.get(i).getTipo(), tokenList.getFirst().getLinea(), "Diferencia de tipo en parametro.", "Semántica 2"));
                            } else {
                                sm3.setEstado("Aceptado");
                            }
                            valoresSemantica.add(sm3);
                        } else {
                            sm2.setValorr("null");
                            sm2.setEstado("Error");
                            errorList.add(new Errores(562, parametros.get(i).getTipo(), tokenList.getFirst().getLinea(), "Falta parametro.", "Semántica 2"));
                        }
                        valoresSemantica.add(sm2);
                    }

                    if (listaFuncion.size() > parametros.size()) {
                        for (int i = parametros.size();
                                i < listaFuncion.size(); i++) {
                            Semantica2 sm2 = new Semantica2();
                            sm2.setRegla("1080");
                            sm2.setTp("null");
                            sm2.setLinea("" + tokenList.getFirst().getLinea());
                            sm2.setAmbito("" + pilaAmbito.peek());
                            sm2.setValorr(listaFuncion.get(i).getTipo());
                            sm2.setEstado("Error");
                            valoresSemantica.add(sm2);
                            errorList.add(new Errores(564, listaFuncion.get(i).getTipo(), tokenList.getFirst().getLinea(), "Parametros extras.", "Semántica 2"));
                        }
                    }
                }
                listaFuncion = new LinkedList();
                funcionAnidada--;
                if (funcionAnidada == 0) {
                    empiezaFuncion = false;
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 333) {
                if (pilaOperandoArr.size() > 0) {
                    Variable var = new Variable(pilaOperandoArr.pop());
                    listaFuncion.add(var);
                } else {
                    var = new Variable();
                    listaArreglo.add(var);
                    errorList.add(new Errores(567, "Empty", tokenList.getFirst().getLinea(), "No se ha encontrado valor", "Semántica 2"));
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 332) {
                empiezaFuncion = true;

                if ((lastIdentificador.getClase().equals("function")
                        || lastIdentificador.getClase().equals("arrfunction")
                        || lastIdentificador.getClase().equals("procedure"))) {
                    //Empieza Regla 10 y 11.
                    int valor = arrayAnidado + funcionAnidada;
                    if (haveEqual(pilaOperador) || valor > 1 || write) {
                        Semantica2 sm2 = new Semantica2();
                        sm2.setRegla("1100");
                        sm2.setTp("function");
                        sm2.setLinea("" + tokenList.getFirst().getLinea());
                        sm2.setAmbito("" + pilaAmbito.peek());
                        sm2.setValorr(lastIdentificador.getClase());
                        if (!lastIdentificador.getClase().equals("function")) {
                            sm2.setEstado("Error");
                            errorList.add(new Errores(565, lastIdentificador.getClase() + " " + lastIdentificador.getNombre(),
                                    tokenList.getFirst().getLinea(), "La variable no es de tipo función", "Semántica 2"));
                        } else {
                            sm2.setEstado("Aceptado");
                        }
                        valoresSemantica.add(sm2);
                    } else {
                        Semantica2 sm2 = new Semantica2();
                        sm2.setRegla("1110");
                        sm2.setTp("function");
                        sm2.setLinea("" + tokenList.getFirst().getLinea());
                        sm2.setAmbito("" + pilaAmbito.peek());
                        sm2.setValorr(lastIdentificador.getClase());
                        if (!lastIdentificador.getClase().equals("procedure")) {
                            errorList.add(new Errores(566, lastIdentificador.getClase() + " " + lastIdentificador.getNombre(),
                                    tokenList.getFirst().getLinea(), "La variable no es de tipo procedure", "Semántica 2"));
                            sm2.setEstado("Error");
                        } else {
                            sm2.setEstado("Aceptado");
                        }
                        valoresSemantica.add(sm2);
                    }
                        //Terminar regla 10 y 11.

                    //errorNoEsArreglo = true;
                } else {
                    //seguirVerificando = true;
                }
                ultimoArreglos.push(lastIdentificador);
                stackTipo.push("function");
                if (arrayAnidado >= 1 || funcionAnidada >= 1) {
                    ultimoArreglosPos.push(pilaOperandoArr.size());
                } else {
                    ultimoArreglosPos.push(pilaOperando.size());
                }

                funcionAnidada++;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 331) {
                if (lastIdentificador.getDimarr().equals("-")) {
                    errorList.add(new Errores(561, lastIdentificador.getClase() + " " + lastIdentificador.getNombre(), tokenList.getFirst().getLinea(), "La variable no es de tipo arreglo", "Semántica 2"));
                    errorNoEsArreglo = true;
                } else {
                    seguirVerificando = true;
                }
                ultimoArreglos.push(lastIdentificador);
                stackTipo.push("arr");
                if (arrayAnidado >= 1 || funcionAnidada >= 1) {
                    ultimoArreglosPos.push(pilaOperandoArr.size());
                } else {
                    ultimoArreglosPos.push(pilaOperando.size());
                }
                arrayAnidado++;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 330) {

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 329) {
                if (!ultimoArreglos.isEmpty()) {
                    arrayStackPos = ultimoArreglosPos.pop();
                    ultimoArreglo = ultimoArreglos.pop();

                    if (!errorNoEsArreglo) { //Si no es arreglo no entra.
                        boolean total = false;
                        if (haveTodo(listaArreglo)) {
                            ultimoArreglo.setFullArray(true);
                            total = true;
                            ultimoArreglo.setListaArreglo(listaArreglo);
                        } else {
                            ultimoArreglo.setPosition(true);
                        }

                        for (int i = 0; i < Integer.parseInt(ultimoArreglo.getDimarr()); i++) {
                            boolean success = true;
                            Semantica2 sm2 = new Semantica2();
                            sm2.setRegla("1040");
                            sm2.setTp("integer");
                            sm2.setLinea("" + tokenList.getFirst().getLinea());
                            sm2.setAmbito("" + pilaAmbito.peek());

                            //Regla 4 Dimensiones.
                            if (i < listaArreglo.size()) {
                                Variable listaArreglo1 = listaArreglo.get(i);
                                sm2.setValorr(listaArreglo1.getNombre());
                                sm2.setEstado("Aceptado");

                                Semantica2 sm3 = new Semantica2();
                                sm3.setRegla("1050");
                                sm3.setTp("integer");
                                sm3.setLinea("" + tokenList.getFirst().getLinea());
                                sm3.setAmbito("" + pilaAmbito.peek());

                                if (!listaArreglo1.getTipo().equals("todo")) {
                                    if (!(listaArreglo1.getTipo().equals("integer") || listaArreglo1.getTipo().equals("varian"))) {
                                        sm3.setValorr(listaArreglo1.getTipo());
                                        sm3.setEstado("Error");
                                        success = false;
                                        errorList.add(new Errores(557, ultimoArreglo.getNombre() + "[" + listaArreglo1.getTipo() + "]", tokenList.getFirst().getLinea(), "Incompatibilidad de tipos: se espera integer. Dimensión: " + (i + 1), "Semántica 2"));
                                    } else {
                                        sm3.setValorr("integer");
                                        sm3.setEstado("Aceptado");
                                        try {
                                            int number = Integer.parseInt(listaArreglo1.getNombre());
                                            Semantica2 sm4 = new Semantica2();
                                            sm4.setRegla("1060");
                                            sm4.setTp("integer");
                                            sm4.setLinea("" + tokenList.getFirst().getLinea());
                                            sm4.setAmbito("" + pilaAmbito.peek());
                                            if (!Ut.entraEnRango(ultimoArreglo, number, i + 1)) {
                                                sm4.setEstado("Error");
                                                sm4.setValorr("" + number);
                                                success = false;
                                                errorList.add(new Errores(558, "" + number, tokenList.getFirst().getLinea(), "Fuera de rango " + ultimoArreglo.getNombre() + " dimensión : " + (i + 1), "Semántica 2"));
                                            } else {
                                                sm4.setEstado("Aceptado");
                                                sm4.setValorr("" + number);
                                            }
                                            valoresSemantica.add(sm4);
                                        } catch (Exception e) {
                                        }
                                    }
                                } else {
                                    sm3.setValorr("todo");
                                    sm3.setEstado("Aceptado");
                                    //IntroducirManuel
                                }
                                valoresSemantica.add(sm3);
                            } else {
                                sm2.setValorr("null");
                                sm2.setEstado("Error");
                                errorList.add(new Errores(556, "arreglo[]?", tokenList.getFirst().getLinea(), "Faltan dimensiones", "Semántica 2"));
                            }
                            valoresSemantica.add(sm2);

                            //Esto es lo que estaba dentro de success
                            if (arrayAnidado > 1 || funcionAnidada > 1) {
                                if (!pilaOperandoArr.isEmpty()) {
                                    if (arrayStackPos == 0) {
                                        arrayStackPos++;
                                    }
                                    pilaOperandoArr.set(arrayStackPos - 1, ultimoArreglo);
                                } else {
                                    pilaOperandoArr.push(ultimoArreglo);
                                }
                            } else {
                                if (!pilaOperando.isEmpty()) {
                                    if (arrayStackPos == 0) {
                                        arrayStackPos++;
                                    }
                                    pilaOperando.set(arrayStackPos - 1, ultimoArreglo);
                                } else {
                                    pilaOperando.push(ultimoArreglo);
                                }
                            }
                        }

                        if (listaArreglo.size() > Integer.parseInt(ultimoArreglo.getDimarr())) {
                            for (int i = Integer.parseInt(ultimoArreglo.getDimarr());
                                    i < listaArreglo.size(); i++) {
                                Semantica2 sm2 = new Semantica2();
                                sm2.setRegla("1040");
                                sm2.setTp("null");
                                sm2.setLinea("" + tokenList.getFirst().getLinea());
                                sm2.setAmbito("" + pilaAmbito.peek());
                                sm2.setValorr(listaArreglo.get(i).getTipo());
                                valoresSemantica.add(sm2);
                            }
                        }

                    } else {
                        //Si no es arreglo.
                        ultimoArreglo.setTipo("varian");
                        if (arrayAnidado > 1) {
                            if (!pilaOperandoArr.isEmpty()) {
                                if (arrayStackPos == 0) {
                                    arrayStackPos++;
                                }
                                pilaOperandoArr.set(arrayStackPos - 1, ultimoArreglo);
                            } else {
                                pilaOperandoArr.push(ultimoArreglo);
                            }
                        } else {
                            if (!pilaOperando.isEmpty()) {
                                if (arrayStackPos == 0) {
                                    arrayStackPos++;
                                }
                                pilaOperando.set(arrayStackPos - 1, ultimoArreglo);
                            } else {
                                pilaOperando.push(ultimoArreglo);
                            }
                        }
                    }
                }

                listaArreglo = new LinkedList();
                arrayAnidado--;

                if (arrayAnidado == 0) {
                    arregloVerificar = false;
                    errorNoEsArreglo = false;
                }

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 328) {
                //Cambiado a arreglo
                if (pilaOperandoArr.size() > 0) {
                    Variable var = new Variable(pilaOperandoArr.pop());
                    listaArreglo.add(var);
                } else {
                    var = new Variable();
                    listaArreglo.add(var);
                    errorList.add(new Errores(553, "Empty", tokenList.getFirst().getLinea(), "No se ha encontrado valor, se espera integer", "Semántica 2"));
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 327) {
                if (!arregloVerificar) {
                    arregloVerificar = true;
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 326) {
                constanteSigno = false;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 325) {
                constanteSigno = true;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 324) {
                entraExpCase = false;
                Semantica2 sm2 = new Semantica2();
                sm2.setRegla("1020");
                sm2.setTp("integer||char||string");
                sm2.setLinea("" + tokenList.getFirst().getLinea());
                sm2.setAmbito("" + pilaAmbito.peek());

                if (!pilaOperando.isEmpty()) {
                    Variable var = pilaOperando.pop();
                    sm2.setValorr(var.getTipo());
                    if ((Ut.typeNumber(var.getTipo()) > 1 && Ut.typeNumber(var.getTipo()) < 6
                            && !var.getTipo().equals("bool")) || var.getTipo().equals("varian")
                            && !var.getTipo().equals("real") && !var.getTipo().equals("expo")) {
                        caseType = new Variable(var);
                        sm2.setEstado("Aceptado");
                    } else {
                        var.setTipo("varian");
                        sm2.setEstado("Error");
                        caseType = new Variable(var);
                        errorList.add(new Errores(555, var.getTipo(), tokenList.getFirst().getLinea(), "Se espera expresión de tipo char, string o int.", "Semántica 2"));
                    }
                } else {
                    sm2.setEstado("Error");
                    sm2.setValorr("null");
                    errorList.add(new Errores(555, "Empty", tokenList.getFirst().getLinea(), "Se espera expresión de tipo char, string o int.", "Semántica 2"));
                }
                valoresSemantica.add(sm2);
                pilaSintactica.pop();

            } else if (pilaSintactica.peek() == 323) {
                entraExpCase = true;
                selectCase = new LinkedList();
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 322) {
                entraCase = false;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 321) {
                entraCase = true;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 320) {
                //Se tiene verificar qué hay en la pila.
                Semantica2 sm2 = new Semantica2();
                sm2.setTp("bool");
                sm2.setLinea("" + tokenList.getFirst().getLinea());
                sm2.setAmbito("" + pilaAmbito.peek());
                switch (typu) {
                    case "while":
                        sm2.setRegla("1012");
                        break;
                    case "until":
                        sm2.setRegla("1011");
                        break;
                    case "if":
                        sm2.setRegla("1010");
                        break;
                }
                if (!pilaOperando.isEmpty()) {
                    Variable vIf = pilaOperando.pop();
                    sm2.setValorr(vIf.getTipo());

                    lineaAsignaciones.add(lineaError);
                    asignaciones.add(typu + " ← " + Ut.checarArreglo(contadorTemporales) + Ut.imprimirValor(vIf.getTipo()));
                    contadorSemantica1.add(contadorTemporales);

                    if (!vIf.getTipo().equals("bool")) {
                        if (vIf.getTipo().equals("varian")) {
                            vIf.setTipo("integer");
                        }
                        sm2.setEstado("Error");
                        errorList.add(new Errores(554, vIf.getTipo(), tokenList.getFirst().getLinea(), "Expresión incorrecta, se espera bool", "Semántica 2"));
                    } else {
                        sm2.setEstado("Aceptado");
                    }
                } else {
                    sm2.setValorr("null");
                    sm2.setEstado("Error");
                }
                valoresSemantica.add(sm2);
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 316) {

                entraEXP = true;
                if (!(sigueEXP && (arregloVerificar || empiezaFuncion || !funcionPascal.isEmpty()))) {
                    pilaOperando = new Stack();
                    pilaOperador = new Stack();
                    contadorTemporales = new int[8];
                }

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 317) {
                if (!sigueEXP) {
                    entraEXP = false;
                }
                pilaSintactica.pop();

            } else if (pilaSintactica.peek() == 318) {

                //Operadores Binarios
                if (arregloVerificar) {
                    operacionBinaria(pilaOperandoArr, pilaOperadorArr, tokenList, errorList, lineaAsignaciones, lineaError,
                            asignaciones, contadorSemantica1, contadorTemporales, arregloVerificar);
                } else {
                    operacionBinaria(pilaOperando, pilaOperador, tokenList, errorList, lineaAsignaciones, lineaError,
                            asignaciones, contadorSemantica1, contadorTemporales, arregloVerificar);
                }
                pilaSintactica.pop();

            } else if (pilaSintactica.peek() == 319) {

                //Operadores Unarios
                if (arregloVerificar) {
                    operacionUnaria(pilaOperandoArr, pilaOperadorArr, tokenList, errorList, lineaAsignaciones, lineaError,
                            asignaciones, contadorSemantica1, contadorTemporales, arregloVerificar);
                } else {
                    operacionUnaria(pilaOperando, pilaOperador, tokenList, errorList, lineaAsignaciones, lineaError,
                            asignaciones, contadorSemantica1, contadorTemporales, arregloVerificar);
                }
                pilaSintactica.pop();

            } else if (pilaSintactica.peek() == 315) {

                if (!verLastId.equals("")) {
                    if (!esArreglo(variableList, verLastId)) {

                        errorList.add(new Errores(551, verLastId, tokenList.getFirst().getLinea(), "Tipo de variable erroneo.", "Ambito"));
                    }
                }

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 301) {
                //Toca mantener precedencia y contar di una vez el ámbito que termina jsjs.
                if (pilaNecesitaReturn.size() > 0) {
                    if (pilaNecesitaReturn.peek().equals(pilaAmbito.peek())) {
                        Semantica2 sm2 = new Semantica2();
                        sm2.setRegla("1140");
                        sm2.setTp("return");
                        sm2.setLinea("" + tokenList.getFirst().getLinea());
                        sm2.setAmbito("" + pilaAmbito.peek());
                        if (!(pilaReturn.size() > 0)) {
                            sm2.setEstado("Error");
                            sm2.setValorr("null");
                            errorList.add(new Errores(558, methodDec.getNombre(), lastToken.getLinea(), "No se cuenta con return", "Semántica 2"));
                        } else {
                            sm2.setValorr("return");
                            sm2.setEstado("Aceptado");
                            if (pilaReturnTipo.peek().equals("procedure")) {
                                sm2.setRegla("1150");
                                sm2.setTp("null");
                                sm2.setValorr("return");
                                sm2.setEstado("Error");
                                errorList.add(new Errores(559, methodDec.getNombre(), lastToken.getLinea(), "procedure no espera return", "Semántica 2"));
                            } else {
                                sm2.setRegla("1150");
                                sm2.setTp("null");
                                sm2.setValorr("null");
                                sm2.setEstado("Aceptado");
                            }
                        }
                        valoresSemantica.add(sm2);
                        pilaReturnTipo.pop();
                        pilaNecesitaReturn.pop();
                        pilaReturn = new Stack();
                    }
                }

                contador.add(contarVariables(variableList, pilaAmbito.peek()));
                pilaSintactica.pop();
                pilaAmbito.pop();
            } else if (pilaSintactica.peek() == 302) {
                verificando = true;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 303) {
                verificando = false;
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 304) {
                pilaSintactica.pop();
                declarando = true;
            } else if (pilaSintactica.peek() == 307 || pilaSintactica.peek() == 309 || pilaSintactica.peek() == 312) {
                if (pilaSintactica.peek() == 307) {
                    decFuncion = true;
                    parametersMethod = new LinkedList();
                    declarando = false;
                }
                if (pilaSintactica.peek() == 312) {
                    listaPar = false;
                    if (duplicatedMethod) {
                        Semantica2 sm2 = new Semantica2();
                        sm2.setRegla("1180");
                        sm2.setTp("");
                        sm2.setValorr("");
                        sm2.setLinea("" + tokenList.getFirst().getLinea());
                        sm2.setAmbito("" + pilaAmbito.peek());
                        sm2.setEstado("Aceptado");
                        valoresSemantica.add(sm2);
                        actualizarParametros(parametersMethod, methodDec, variableList);
                    }
                }
                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 311) {
                pilaSintactica.pop();
                listaPar = true;
            } else if (pilaSintactica.peek() == 313) {
                    //Tipo de Dato

                //Aquí actualizaremos los datos de nuestra función.
                if (!duplicatedMethod) {
                    methodDec = new Variable();
                    methodDec.setNombre(funcID);
                    methodDec.setAmbito("" + lastAmbito);
                    methodDec.setClase(lastClass);
                    methodDec.setNopar("" + nopar);
                    methodDec.setTparr("" + ambito);
                    methodDec.setTipo(lastType);
                }
                if (lastClass.equals("function") || lastClass.equals("procedure")) {
                    if (listaEnteros.size() > 0) {
                        errorList.add(new Errores(550, "[]", tokenList.getFirst().getLinea(), "Error en declaración. Función no puede ser de tipo arreglo.", "Ambito"));
                        methodDec.setTipo(lastType);
                    }
                    if (lastClass.equals("function") && lastType.equals("file")) {
                        errorList.add(new Errores(550, "file", tokenList.getFirst().getLinea(), "Error en declaración. Función no puede ser de tipo file.", "Ambito"));
                        methodDec.setTipo("varian");
                    }
                    if (lastClass.equals("procedure") && !lastType.equals("file")) {
                        errorList.add(new Errores(550, "file", tokenList.getFirst().getLinea(), "Error en declaración. Función no puede ser de tipo file.", "Ambito"));
                        methodDec.setTipo("varian");
                    }
                    if (lastType.equals(methodDec.getTipo())) {
                        //IntroducirManuel
                    } else {
                        if (!methodDec.getTipo().equals("varian")) {
                            errorList.add(new Errores(590, methodDec.getTipo() + " <!> " + lastType, numFunc, "Diferencia de tipos en método declarado a utilizado.", "Semántica 2"));
                        }
                    }
                }

//                if (listaEnteros.size() > 0 || lastType.equals("file")) {
//
////                    v.setClase("arr" + lastClass);
////                    v.setDimarr("" + (listaEnteros.size() / 2));
////                    v.setTarr(tamanoArreglo(listaEnteros));
//                }
                if (duplicatedMethod) {

                    if (!methodDec.getTipo().equals(lastType)) {
                        actualizarFuncion(methodDec, lastAmbito, lastType);
                        //errorList.add(new Errores(591, methodDec.getTipo() + " <!> " + lastType, numFunc, "Diferencia de tipos en método declarado a utilizado.", "Semántica 2"));
                    }
                } else if (posFunc >= 0) {
                    variableList.set(posFunc, methodDec);

                } else {
                    variableList.add(methodDec);
                }

                nopar = 0;
                listaEnteros = new LinkedList();

                pilaSintactica.pop();
            } else if (pilaSintactica.peek() == 308) {
                pilaSintactica.pop();
                decArray = true;
            } else if (pilaSintactica.peek() == 310) {
                pilaSintactica.pop();
                decArray = false;

                //Ahora si, echarle a todas las variables que son arreglo.
                if (pilaIDs.size() > 0) {
                    while (pilaIDs.size() > 0) {
                        Variable v = new Variable();
                        if (listaPar) {
                            v.setClase("arrPar");
                            v.setTparr(funcID);
                            v.setNopar(++nopar + "");
                        } else {
                            v.setClase("arr");
                        }
                        v.setAmbito("" + pilaAmbito.peek());
                        v.setTipo(lastType);
                        v.setNombre(pilaIDs.getFirst());
                        v.setTarr(tamanoArreglo(listaEnteros)); //Aquí es donde debo juntar las cantidades.
                        v.setDimarr("" + (listaEnteros.size() / 2));
                        if (listaEnteros.size() > 0 && listaEnteros.size() % 2 == 0) {
                            variableList.add(v);
                        } else {
                            errorList.add(new Errores(550, pilaIDs.getFirst(), tokenList.getFirst().getLinea(), "Error en declaración", "Ambito"));
                        }
                        pilaIDs.removeFirst();
                    }
                    listaEnteros = new LinkedList();
                }
                if (listaPar) {
                    listaPar = false;
                }
            } else if (pilaSintactica.peek() == 305 || pilaSintactica.peek() == 306) {
                if (pilaSintactica.peek() == 306) {
                    declarando = false;
                }

                pilaSintactica.pop();
                    //Al llegar 306 se acaba la declaración.

                //Crear las variables.
                if (pilaIDs.size() > 0 && !decArray) {
                    while (pilaIDs.size() > 0) {
                        Variable v = new Variable();

                        if (lastClass.equals("use")) {
                            v.setClase("const");
                            v.setTipo(lastType);
                            v.setNombre(pilaIDs.getFirst());
                            v.setAmbito("" + pilaAmbito.peek());
                        } else {
                            if (listaPar) {
                                v.setClase("par");
                                v.setNopar(++nopar + "");
                                v.setTparr(funcID);
                                v.setUtilizar(true);
                            } else {
                                v.setClase(lastClass);
                            }
                            v.setAmbito(pilaAmbito.peek() + "");
                            v.setTipo(lastType);
                            v.setNombre(pilaIDs.getFirst());
                        }
                        if (!duplicatedMethod) {
                            variableList.add(v);
                        } else {
                            parametersMethod.add(v);
                        }

                        pilaIDs.removeFirst();
                    }
                }
            } else if (pilaSintactica.peek() == 308) { //Activaremos la bandera de decArray.
                decArray = true;
            } else if (pilaSintactica.peek() == tokenList.getFirst().getNumero()) {
                if (tokenList.getFirst().getNumero() == -65 || tokenList.getFirst().getNumero() == -69
                        || tokenList.getFirst().getNumero() == -62) {
                    typu = tokenList.getFirst().getLexema();
                }
                if (verificando) {

                    //MKV
                    if (tokenList.getFirst().getNumero() == -1) {
                        Semantica2 sm2 = new Semantica2();
                        sm2.setRegla("1120");
                        sm2.setTp(tokenList.getFirst().getLexema());
                        sm2.setLinea("" + tokenList.getFirst().getLinea());
                        sm2.setAmbito("" + pilaAmbito.peek());
                        sm2.setValorr(tokenList.getFirst().getLexema());
                        if (variableExists(variableList, tokenList.getFirst().getLexema())) {
                            sm2.setEstado("Aceptado");
                            if (var.getClase().equals("function")
                                    || var.getClase().equals("procedure")) {
                                Token t = tokenList.getFirst();
                                tokenList.removeFirst();

                                if (!tokenList.getFirst().getLexema().equals("(")) {
                                    errorList.add(new Errores(550, t.getLexema(), tokenList.getFirst().getLinea(), "Se espera " + t.getLexema() + " ()", "Ambito"));
                                }
                                tokenList.push(t);
                            }
                            if (var.getClase().equals("arr")
                                    || var.getClase().equals("arrPar")) {
                                Token t = tokenList.getFirst();
                                tokenList.removeFirst();

                                if (!tokenList.getFirst().getLexema().equals("[")) {
                                    errorList.add(new Errores(550, t.getLexema(), t.getLinea(), "Se espera " + t.getLexema() + " []", "Ambito"));
                                }
                                tokenList.push(t);
                            }

                            if (!verLastToken.equals("[")) { //Está mal, pero no sé en que me afecta.
                                verLastId = tokenList.getFirst().getLexema();
                            }
                        } else {
                            errorVer = true;
                            sm2.setTp("null");
                            sm2.setEstado("Error");
                            var = new Variable();
                            var.setNombre(tokenList.getFirst().getLexema());
                            var.setTipo("varian");

                            contadorTemporales[Ut.typeNumber(var.getTipo())]++;
                            errorList.add(new Errores(549, tokenList.getFirst().getLexema(), tokenList.getFirst().getLinea(), "Variable no declarada", "Ambito"));
                        }
                        lastIdentificador = new Variable(var);
                        valoresSemantica.add(sm2);
                    }

                    /*
                     En el área de verificación se revisan las expresiones de asignación.
                     Primero se verificará si la variable existe, si esta existe, se puede
                     seguir con el proceso. Si no generaremos una variable temporal.

                     No me preocuparé por si llega una función o algo más, por el momento.
                     No sé que hacer si me llega un function.
                     ¿solo agarro el valor y lo manejo?
                     Me enfocaré en el caso ideal que llegue =.
                     */
                    if (entraEXP) {
                        if (tokenList.getFirst().getNumero() == -1) {
                            if (pilaOperando.isEmpty()) {
                                lineaError = tokenList.getFirst().getLinea();
                            }
                            if (!noAgarrar) {
                                if (arrayAnidado >= 1 || funcionAnidada >= 1) {
                                    pilaOperandoArr.push(var);
                                    if (!var.getDimarr().equals("-")) {
                                        ultimoArreglo = new Variable(var);
                                        arrayStackPos = pilaOperandoArr.size();
                                        lastVariable = new Variable(var);
                                    }
                                } else {
                                    pilaOperando.push(var);
                                    if (!var.getDimarr().equals("-")) {
                                        ultimoArreglo = new Variable(var);
                                        arrayStackPos = pilaOperando.size();
                                        lastVariable = new Variable(var);

                                    }
                                }
                            }
                        } else if (tokenList.getFirst().getNumero() == -3 || tokenList.getFirst().getNumero() == -4
                                || tokenList.getFirst().getNumero() == -5 || tokenList.getFirst().getNumero() == -6
                                || tokenList.getFirst().getNumero() == -7 || tokenList.getFirst().getNumero() == -77
                                || tokenList.getFirst().getNumero() == -78) {

                            var = new Variable();
                            var.setNombre(tokenList.getFirst().getLexema());
                            setVarTipo(tokenList.getFirst().getNumero());
                            if (!noAgarrar) {
                                if (arregloVerificar || empiezaFuncion) {
                                    pilaOperandoArr.push(var);
                                } else {
                                    pilaOperando.push(var);
                                }
                            }
                        } else if (tokenList.getFirst().getNumero() == -35 || tokenList.getFirst().getNumero() == -8
                                || tokenList.getFirst().getNumero() == -9 || tokenList.getFirst().getNumero() == -10
                                || tokenList.getFirst().getNumero() == -11 || tokenList.getFirst().getNumero() == -13
                                || tokenList.getFirst().getNumero() == -14 || tokenList.getFirst().getNumero() == -15
                                || tokenList.getFirst().getNumero() == -16 || tokenList.getFirst().getNumero() == -17
                                || tokenList.getFirst().getNumero() == -42 || tokenList.getFirst().getNumero() == -43
                                || tokenList.getFirst().getNumero() == -18 || tokenList.getFirst().getNumero() == -19
                                || tokenList.getFirst().getNumero() == -20 || tokenList.getFirst().getNumero() == -21
                                || tokenList.getFirst().getNumero() == -22 || tokenList.getFirst().getNumero() == -23) {
                            if (arregloVerificar) {
                                pilaOperadorArr.push(tokenList.getFirst().getLexema());
                            } else {
                                pilaOperador.push(tokenList.getFirst().getLexema());
                            }

                        }
                    }

                    //MK1
                    if (entraCase) {
                        if (constanteSigno) {

                            if (tokenList.getFirst().getNumero() == -3 || tokenList.getFirst().getNumero() == -4
                                    || tokenList.getFirst().getNumero() == -5 || tokenList.getFirst().getNumero() == -6
                                    || tokenList.getFirst().getNumero() == -7 || tokenList.getFirst().getNumero() == -77
                                    || tokenList.getFirst().getNumero() == -78) {
                                setVarTipo(tokenList.getFirst().getNumero());

                                Semantica2 sm2 = new Semantica2();
                                sm2.setRegla("1021");
                                sm2.setTp(caseType.getTipo());
                                sm2.setLinea("" + tokenList.getFirst().getLinea());
                                sm2.setAmbito("" + pilaAmbito.peek());
                                sm2.setValorr(var.getTipo());

                                if (caseType.getTipo().equals("varian")) {
                                    sm2.setTp("integer||char||string");
                                    if (Ut.isCaseable(var.getTipo())) {
                                        caseType = new Variable(var);
                                        sm2.setEstado("Aceptado");
                                    } else {
                                        sm2.setEstado("Error");
                                        errorList.add(new Errores(555, tokenList.getFirst().getLexema(), tokenList.getFirst().getLinea(), "Incompatibilidad de tipos: No cumple con el tipo de case", "Semántica 2"));
                                    }
                                } else if (!caseType.getTipo().equals("varian") && !var.getTipo().equals(caseType.getTipo())) {
                                    errorList.add(new Errores(555, tokenList.getFirst().getLexema() + " != " + caseType.getTipo(),
                                            tokenList.getFirst().getLinea(), "Incompatibilidad de tipos: No cumple con el tipo de case", "Semántica 2"));
                                    sm2.setEstado("Error");
                                }
                                valoresSemantica.add(sm2);
                            }
                        }
                    }

                    if (arregloVerificar) {
                        if (tokenList.getFirst().getNumero() == -76) {
                            Variable var = new Variable();
                            var.setNombre("todo");
                            var.setTipo("todo");
                            pilaOperandoArr.add(var);
                        }
                    }
                    errorVer = false;

                } else {
                    /*
                     Primer caso, guardar cuando están en A.
                     Obtienes el tipo si es VAR-FUN-PROC
                     */

                    if (tokenList.getFirst().getNumero() == -47 || tokenList.getFirst().getNumero() == -48
                            || tokenList.getFirst().getNumero() == -49 || tokenList.getFirst().getNumero() == -51) {
                        lastClass = tokenList.getFirst().getLexema();
                    }

                    /*--------------------------------------------
                     Almacenaremos el ID de Función por separado para usos poder settear en las variables.
                     De igual manera crearemos la variable en esa posición para mantener el espacio y la lista
                     respete el lugar de como va llegando cada variable. Hacerle un update después.
                     */
                    if (decFuncion) {
                        funcID = tokenList.getFirst().getLexema();
                        posFunc = variableList.size();
                        numFunc = tokenList.getFirst().getLinea();
                        if (!getDuplicated(variableList, pilaIDs, funcID, pilaAmbito.peek())) {
                            newMethod = true;
                            duplicatedMethod = false;
                            variableList.add(new Variable("" + ambito, lastClass, funcID));
                        } else {
                            methodDec = this.getDuplicatedFunction(variableList, funcID, pilaAmbito.peek());
                            dupNumber = tokenList.getFirst().getLinea();
                            duplicatedMethod = true;
                            //parametersMethod = this.listaParametros(variableList, methodDec);
                            newMethod = false;
                        }
                        decFuncion = false;
                    }

                    /* ---------------------------------------------
                     Variable Auxiliar para no pegarme un plomazo
                     Toda esta parte es para listaParametros.
                     */
                    boolean bol = false;
                    if (listaPar) {
                        if (decArray) {
                            if (tokenList.getFirst().getNumero() == -3 || tokenList.getFirst().getNumero() == -1) {
                                if (tokenList.getFirst().getNumero() == -3) {
                                    listaEnteros.add(tokenList.getFirst().getLexema());
                                } else if (tokenList.getFirst().getNumero() == -1 && variableExists(variableList, tokenList.getFirst().getLexema())) {
                                    listaEnteros.add(tokenList.getFirst().getLexema());
                                    bol = true;
                                } else {
                                    bol = true;
                                    errorList.add(new Errores(549, tokenList.getFirst().getLexema(), tokenList.getFirst().getLinea(), "Variable no declarada", "Ambito"));
                                }
                            }
                        } else {
                            if (tokenList.getFirst().getNumero() == -1) { //Obtienes ID
                                linkedPar.add(tokenList.getFirst().getLexema());
                            }
                            if (tokenList.getFirst().getNumero() > -60 && tokenList.getFirst().getNumero() < -51) { //Obtienes el tipo.
                                tipoPar = tokenList.getFirst().getLexema();
                            }
                        }
                    }

                    //--------------------------------------------------
                    if (declarando) {
                        if (lastClass.equals("use")) {
                            if (tokenList.getFirst().getNumero() == -3) {
                                lastType = "integer";
                            } else if (tokenList.getFirst().getNumero() == -4) {
                                lastType = "real";
                            } else if (tokenList.getFirst().getNumero() == -5) {
                                lastType = "exp";
                            } else if (tokenList.getFirst().getNumero() == -6) {
                                lastType = "char";
                            } else if (tokenList.getFirst().getNumero() == -7) {
                                lastType = "string";
                            } else if (tokenList.getFirst().getNumero() == -77 || tokenList.getFirst().getNumero() == -78) {
                                lastType = "bool";
                            }
                        }
                        if (tokenList.getFirst().getNumero() == -1) //Obtienes ID
                        {
                            if (decArray && !listaPar) {
                                if (tokenList.getFirst().getNumero() == -1 && variableExists(variableList, tokenList.getFirst().getLexema())) {
                                    listaEnteros.add(tokenList.getFirst().getLexema());
                                } else {
                                    errorList.add(new Errores(549, tokenList.getFirst().getLexema(), tokenList.getFirst().getLinea(), "Variable no declarada", "Ambito"));
                                }
                            } else {
                                if (!getDuplicated(variableList, pilaIDs, tokenList.getFirst().getLexema(), pilaAmbito.peek()) && !bol) {
                                    pilaIDs.add(tokenList.getFirst().getLexema());
                                } else {
                                    if (duplicatedMethod) {
                                        pilaIDs.add(tokenList.getFirst().getLexema());
                                    }
                                    if (!duplicatedMethod) {
                                        errorList.add(new Errores(548, tokenList.getFirst().getLexema(), tokenList.getFirst().getLinea(), "Variable duplicada", "Ambito"));
                                    }
                                }
                            }
                        }
                        //Obtienes el tipo.
                        if (tokenList.getFirst().getNumero() > -60 && tokenList.getFirst().getNumero() < -51) {
                            lastType = tokenList.getFirst().getLexema();
                        }
                        if (decArray && !listaPar) {
                            if (tokenList.getFirst().getNumero() == -3) {
                                listaEnteros.add(tokenList.getFirst().getLexema());
                            }
                        }
                    }
                }
                verLastToken = tokenList.getFirst().getLexema();
                if (!noAgarrar) {
                    lastVariable = var;
                }
                pilaSintactica.pop();
                lastToken = tokenList.getFirst();
                tokenList.removeFirst();
            } else if (pilaSintactica.peek() > 0 && tokenList.getFirst().getNumero() < 0) { //Checker = Valor Matriz.
                int checker = matrixS[pilaSintactica.peek() - 1][Math.abs(tokenList.getFirst().getNumero())];
                System.out.println("checker:" + checker);
                int pilaPeek = pilaSintactica.peek() - 1;
                int listaNumero = Math.abs(tokenList.getFirst().getNumero());
                if (pilaSintactica.peek() == 39) {
                    System.out.println("");
                }
//                System.out.println("checker: " + checker);
                if (checker > 0 && checker < 122) {
                    pilaSintactica.pop();
                    cargarProduccion(checker - 1);
                } else if (checker == 122) {
                    pilaSintactica.pop();
                } else {
//                    if(tokenList.getFirst().getNumero() != -95)
                    errorList.add(new Errores(checker, tokenList.getFirst().getLexema(), tokenList.getFirst().getLinea(), errorMessages[checker - 500], "Sintáctico"));
                    lastToken = tokenList.getFirst();
                    tokenList.removeFirst();
                }
            } else if (pilaSintactica.peek() < 0 && pilaSintactica.peek() != tokenList.getFirst().getNumero()) {
                errorList.add(new Errores(1000, tokenList.getFirst().getLexema(), tokenList.getFirst().getLinea(), "Fuerza Bruta", "Sintáctico"));
                return;
            }
        }
        contador.add(contarVariables(variableList, pilaAmbito.peek()));
        JOptionPane.showMessageDialog(panel, "Proceso finalizado.");
    }

    public LinkedList<String[]> getContador() {
        return contador;
    }

    private void cargarProduccion(int prod) {
        for (int i = 0; i < producciones[prod].length; i++) {
            pilaSintactica.add(producciones[prod][i]);
        }
    }

    private void countLastLine(int line) {
        int total = 0;
        for (int i = 0; i < counters[0].length; i++) {
            total = countTotal(i);
            counters[line][i] = total;
        }
    }

    private int countTotal(int i) {
        int total = 0;
        for (int j = 0; j < counters.length - 1; j++) {
            total += counters[j][i];
        }
        return total;
    }

    private String tamanoArreglo(LinkedList<String> listaEntero) {
        int i = 0;
        String cadena = "Error";
        if (listaEntero.size() > 0) {
            if (listaEntero.size() % 2 == 0) {
                cadena = "";
                for (String listaEntero1 : listaEntero) {
                    if (i % 2 == 0 && i > 0) {
                        cadena += ",";
                    }
                    if (i > 0 && i % 2 != 0) {
                        cadena += "-" + listaEntero1;
                    } else {
                        cadena += listaEntero1;
                    }
                    i++;
                }
            }
        }
        return cadena;
    }

    private boolean esArreglo(LinkedList<Variable> listaVariable, String checar) {
        for (int ambitoPila : pilaAmbito) {
            for (Variable variable : listaVariable) {
                if (variable.getAmbito() != null) {
                    if (Integer.parseInt(variable.getAmbito()) == ambitoPila && variable.getNombre().equals(checar) && !variable.getDimarr().equals("-")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String[] contarVariables(LinkedList<Variable> listaVariable, int ambito) {
        String[] contadorAmbito = new String[9];
        String manzana = "";

        int contarr = 0;
        int contVar = 0, contConst = 0, contFunc = 0, contProce = 0, contArr = 0, contPar = 0, contArrPar = 0;

        for (int ambitoPila : pilaAmbito) {
            for (Variable variable : listaVariable) {
                if (Integer.parseInt(variable.getAmbito()) == ambitoPila) {
                    if (variable.getClase().equals("var")) {
                        contVar++;
                    }
                    if (variable.getClase().equals("const")) {
                        contConst++;
                    }
                    if (variable.getClase().equals("function")) {
                        contFunc++;
                    }
                    if (variable.getClase().equals("procedure")) {
                        contProce++;
                    }
                    if (variable.getClase().equals("arr")) {
                        contArr++;
                    }
                    if (variable.getClase().equals("par")) {
                        contPar++;
                    }
                    if (variable.getClase().equals("arrPar")) {
                        contArrPar++;
                    }
                }
            }
            if (pilaAmbito.size() == 1 && ambitoPila == 0) {
                manzana = "-";
            } else if (contarr == 0) {
                manzana = "" + ambitoPila;
                contarr++;
            } else {
                if (ambito != ambitoPila) {
                    manzana += ", " + ambitoPila;
                }
            }
        }
        contadorAmbito[0] = "" + ambito;
        contadorAmbito[1] = "" + contVar;
        contadorAmbito[2] = "" + contConst;
        contadorAmbito[3] = "" + contFunc;
        contadorAmbito[4] = "" + contProce;
        contadorAmbito[5] = "" + contArr;
        contadorAmbito[6] = "" + contPar;
        contadorAmbito[7] = "" + contArrPar;
        contadorAmbito[8] = manzana;

        return contadorAmbito;
    }

    private int calcColumn(char calc) {
        int value;
        switch (calc) {
            case '(':
                value = 0;
                break;
            case ')':
                value = 1;
                break;
            case '{':
                value = 2;
                break;
            case '}':
                value = 3;
                break;
            case '[':
                value = 4;
                break;
            case ']':
                value = 5;
                break;
            case ';':
                value = 6;
                break;
            case '.':
                value = 7;
                break;
            case ',':
                value = 8;
                break;
            case '\'':
                value = 9;
                break;
            case '\"':
                value = 10;
                break;
            case '+':
                value = 11;
                break;
            case '-':
                value = 12;
                break;
            case '*':
                value = 13;
                break;
            case '/':
                value = 14;
                break;
            case '%':
                value = 15;
                break;
            case '<':
                value = 16;
                break;
            case '>':
                value = 17;
                break;
            case '=':
                value = 18;
                break;
            case 'e':
                return 21;
            case '_':
                value = 23;
                break;
            case '@':
                value = 24;
                break;
            case '&':
                value = 25;
                break;
            case '|':
                value = 26;
                break;
            case '\n':
                value = 27;
                break;
            case '\t':
            case ' ':
                value = 28;
                break;
            case '!':
                value = 29;
                break;
            case ':':
                value = 31;
                break;
            case '^':
                value = 32;
                break;
            case '#':
                value = 33;
                break;
            default:
                value = 30;
                break;
        }
        if (Character.isDigit(calc)) {
            value = 19;
        }
        if (Character.isUpperCase(calc)) {
            value = 20;
        }
        if (Character.isLowerCase(calc) && !Character.toString(calc).equals("e")) {
            value = 22;
        }
        return value;
    }

    private boolean matchReservedWord(String rw) {
        for (int i = 0; i < reservedWords.length; i++) {
            if (rw.equals(reservedWords[i])) {
                return true;
            }
        }
        return false;
    }

    private int getIntReservedWord(String rw) {
        for (int i = 0; i < reservedWords.length; i++) {
            if (rw.equals(reservedWords[i])) {
                return i;
            }
        }
        return 0;
    }

    private boolean getDuplicated(LinkedList<Variable> listaVariable, LinkedList<String> pilaID, String checar, int ambito) {
        for (String pilaID1 : pilaID) {
            if (checar.equals(pilaID1)) {
                return true;
            }
        }
        for (Variable variable : listaVariable) {
            String s = variable.getAmbito();
            if (s != null) {
                if (Integer.parseInt(variable.getAmbito()) == ambito && variable.getNombre().equals(checar)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Variable getDuplicatedFunction(LinkedList<Variable> listaVariable, String checar, int ambito) {
        for (Variable variable : listaVariable) {
            String s = variable.getAmbito();
            if (s != null) {
                if (Integer.parseInt(variable.getAmbito()) == ambito && variable.getNombre().equals(checar)) {
                    return variable;
                }
            }
        }
        return null;
    }

    private boolean variableExists(LinkedList<Variable> listaVariable, String checar) {
        Stack<Integer> pilaAmbito = (Stack) this.pilaAmbito.clone();
        Stack<Integer> pilis = new Stack();
        while (!pilaAmbito.isEmpty()) {
            pilis.push(pilaAmbito.pop());
        }
        int ambito = -1;
        for (int ambitoPila : pilis) {
            for (Variable variable : listaVariable) {
                try {
                    ambito = Integer.parseInt(variable.getAmbito());
                } catch (Exception e) {
                }
                if (ambito == ambitoPila && variable.getNombre().equals(checar)) {
                    var = variable;
                    return true;
                }
            }
        }
        return false;
    }

    private int getAmbitoVariable(LinkedList<Variable> listaVariable, String checar) {
        Stack<Integer> pilaAmbito = (Stack) this.pilaAmbito.clone();
        Stack<Integer> pilis = new Stack();
        while (!pilaAmbito.isEmpty()) {
            pilis.push(pilaAmbito.pop());
        }

        for (int ambitoPila : pilis) {
            for (Variable variable : listaVariable) {
                if (Integer.parseInt(variable.getAmbito()) == ambitoPila && variable.getNombre().equals(checar)) {
                    return ambitoPila;
                }
            }
        }
        return -1;
    }

    private int getAmbitoVariableFunc(LinkedList<Variable> listaVariable, String checar) {
        Stack<Integer> pilaAmbito = (Stack) this.pilaAmbito.clone();
        Stack<Integer> pilis = new Stack();
        while (!pilaAmbito.isEmpty()) {
            pilis.push(pilaAmbito.pop());
        }

        for (int ambitoPila : pilis) {
            int pos = 0;
            for (Variable variable : listaVariable) {
                if (Integer.parseInt(variable.getAmbito()) == ambitoPila && variable.getNombre().equals(checar)) {
                    if (listaVariable.get(pos + 1).getTparr().equals(checar)) {
                        return Integer.parseInt(listaVariable.get(pos + 1).getAmbito());
                    }
                }
                pos++;
            }
        }
        return -1;
    }

    private LinkedList<Variable> listaParametros(LinkedList<Variable> listaVariable, Variable checar) {
        int ambito = getAmbitoVariable(listaVariable, checar.getNombre()) + 1;
        ambito = getAmbitoVariableFunc(listaVariable, checar.getNombre());
        Stack<Integer> pilaAmbito = (Stack) this.pilaAmbito.clone();
        Stack<Integer> pilis = new Stack();

        LinkedList<Variable> parametros = new LinkedList();

        //Invertir ámbitos.
        while (!pilaAmbito.isEmpty()) {
            pilis.push(pilaAmbito.pop());
        }
        int contador = 0;
        if (parametros.size() != Integer.parseInt(checar.getNopar())) {
            for (int i = listaVariable.size() - 1; i >= 0; i--) {
                Variable variable = listaVariable.get(i);
                if (Integer.parseInt(variable.getAmbito()) == ambito && variable.getTparr().equals(checar.getNombre()) && variable.isUtilizar()) {
                    parametros.push(variable);
                    if (parametros.size() == Integer.parseInt(checar.getNopar())) {
                        return parametros;
                    }
                }
            }
        }

        return null;
    }

    public LinkedList<Token> getTokenList() {
        return tokenList;
    }

    public LinkedList<Errores> getErrorList() {
        return errorList;
    }

    public int getLineNumbers() {
        return base.length;
    }

    private void countEverythingInLife(Token t, Errores e) {
        if (t != null) {
            if (t.getNumero() == -1) {
                counters[t.getLinea() - 1][0]++;
            } else if (t.getNumero() == -2) {
                counters[t.getLinea() - 1][1]++;
            } else if (t.getNumero() < -43) {
                counters[t.getLinea() - 1][2]++;
            } else if (t.getNumero() == -3) {
                counters[t.getLinea() - 1][3]++;
            } else if (t.getNumero() == -7) {
                counters[t.getLinea() - 1][4]++;
            } else if (t.getNumero() == -4) {
                counters[t.getLinea() - 1][5]++;
            } else if (t.getNumero() == -6) {
                counters[t.getLinea() - 1][6]++;
            } else if (t.getNumero() > -13 && t.getNumero() < -7 || t.getNumero() == -42) {
                counters[t.getLinea() - 1][7]++;
            } else if (t.getNumero() == -13 || t.getNumero() == -14) {
                counters[t.getLinea() - 1][8]++;
            } else if (t.getNumero() > -18 && t.getNumero() < -14 || t.getNumero() == -43) {
                counters[t.getLinea() - 1][9]++;
            } else if (t.getNumero() > -24 && t.getNumero() < -17) {
                counters[t.getLinea() - 1][10]++;
            } else if (t.getNumero() == -24 || t.getNumero() == -25) {
                counters[t.getLinea() - 1][11]++;
            } else if (t.getNumero() > -29 && t.getNumero() < -25 || t.getNumero() == -41) {
                counters[t.getLinea() - 1][12]++;
            } else if (t.getNumero() > -35 && t.getNumero() < -28) {
                counters[t.getLinea() - 1][13]++;
            } else if (t.getNumero() > -41 && t.getNumero() < -34) {
                counters[t.getLinea() - 1][14]++;
            } else if (t.getNumero() == -5) {
                counters[t.getLinea() - 1][15]++;
            }
        } else if (e != null) {
            counters[e.getLinea() - 1][16]++;
        }
    }

    public LinkedList<int[]> getContadorSemantica1() {
        return contadorSemantica1;
    }

    public LinkedList<String> getAsignaciones() {
        return asignaciones;
    }

    public LinkedList<Integer> getLineaAsignaciones() {
        return lineaAsignaciones;
    }

    public int[][] getCounters() {
        return counters;
    }

    public LinkedList<Variable> getVariableList() {
        return variableList;
    }

    private void setVarTipo(int numero) {
        switch (numero) {
            case -3:
                var.setTipo("integer");
                break;
            case -4:
                var.setTipo("real");
                break;
            case -5:
                var.setTipo("exp");
                break;
            case -6:
                var.setTipo("char");
                break;
            case -7:
                var.setTipo("string");
                break;
            case -77:
            case -78:
                var.setTipo("bool");
                break;
        }
    }

    private boolean haveEqual(Stack<String> pilaOperando) {
        for (String pilaOperando1 : pilaOperando) {
            if (pilaOperando1.equals("=")) {
                return true;
            }
        }
        return false;
    }

    private boolean haveTodo(LinkedList<Variable> listaArreglo) {
        for (Variable arreglo : listaArreglo) {
            if (arreglo.getTipo().equals("todo")) {
                return true;
            }
        }
        return false;
    }

    private static void operacionBinaria(Stack<Variable> pilaOperando, Stack<String> pilaOperador,
            LinkedList<Token> tokenList, LinkedList<Errores> errorList,
            LinkedList<Integer> lineaAsignaciones, int lineaError, LinkedList<String> asignaciones,
            LinkedList<int[]> contadorSemantica1, int[] contadorTemporales, boolean arregloVerificar) {

        if (!pilaOperando.isEmpty() && !pilaOperador.isEmpty()) {
            Variable v1 = pilaOperando.pop();
            Variable v2 = pilaOperando.pop();
            String op = pilaOperador.pop();
            Variable v3 = new Variable();
            if ((v1.getClase().equals("arr") || v1.getClase().equals("arrPar")
                    || v2.getClase().equals("arr") || v2.getClase().equals("arrPar"))) {
                if (v2.isFullArray() || v1.isFullArray()) {

                }
            }

            if (!op.equals("=")) {
                if (!(v1.isErrorArray() || v2.isErrorArray())) {
                    if ((v1.getClase().equals("arr") || v1.getClase().equals("arrPar")
                            || v1.getClase().equals("arrfunction"))) { //Verificar operaciones de arreglos.
                        if (v1.isFullArray()) {  //Caso 1. a[todo] + v[todo];
                            if ((v2.getClase().equals("arr") || v2.getClase().equals("arrPar"))) {
                                if (v2.isFullArray()) {
                                    if (!op.equals("=")) {
                                        errorList.add(new Errores(560, v2.getTipo() + " " + op + " " + v1.getTipo(), tokenList.getFirst().getLinea(), "No e pueden realizar operaciones con arreglos completos", "Semántica 2"));
                                    }
                                } else if (v2.isPosition()) { //Caso 2. b[todo] * a[5];
                                    v3.setErrorArray(true);
                                    errorList.add(new Errores(560, "arr[position]" + " " + op + " " + "arr[todo]", tokenList.getFirst().getLinea(), "Incompatibilidad de arreglos: No puede comparar un arreglo completo a una posición", "Semántica 2"));
                                } else {
                                    v3.setErrorArray(true);
                                    errorList.add(new Errores(560, "arr" + " " + op + " " + "arr[todo]", tokenList.getFirst().getLinea(), "Incompatibilidad de arreglos: No puede comparar un arreglo completo a una posición", "Semántica 2"));
                                }
                            } else { //Caso 3. b[todo] * a;
                                v3.setErrorArray(true);
                                errorList.add(new Errores(560, "arr[]" + " " + op + " " + v1.getTipo(), tokenList.getFirst().getLinea(), "Incompatibilidad de tipos: No puede utilizar un arreglo completo contra una posición", "Semántica 2"));
                            }
                        } else if (v1.isPosition()) {
                            if ((v2.getClase().equals("arr") || v2.getClase().equals("arrPar"))) {
                                if (v2.isFullArray()) { //Caso 4. a[0] * a[todo]
                                    v3.setErrorArray(true);

                                    errorList.add(new Errores(560, "arr[todo]" + " " + op + " " + "arr[position]", tokenList.getFirst().getLinea(), "Incompatibilidad de arreglos: No puede comparar un arreglo completo a una posición", "Semántica 2"));
                                } else if (!v2.isPosition()) { //Caso 5. b[todo] * a[5];
                                    v3.setErrorArray(true);
                                    errorList.add(new Errores(560, "arr" + " " + op + " " + "arr[pos]", tokenList.getFirst().getLinea(), "Incompatibilidad de arreglos: No puede comparar un arreglo completo a una posición", "Semántica 2"));
                                }
                            } //Caso 3 var * a[5] correcto.
                        }
                    } else if ((v2.getClase().equals("arr") || v2.getClase().equals("arrPar")
                            || v2.getClase().equals("arrfunction"))) { //Verificar operaciones de arreglos.
                        if (v2.isFullArray()) {  //Caso 1. a[todo] + v[todo];
                            if ((v1.getClase().equals("arr") || v1.getClase().equals("arrPar"))) {
                                if (v1.isFullArray()) {
                                    errorList.add(new Errores(560, v2.getTipo() + " " + op + " " + v1.getTipo(), tokenList.getFirst().getLinea(), "No se pueden realizar operaciones con arreglos completos", "Semántica 2"));
                                } else if (v1.isPosition()) { //Caso 2. b[todo] * a[5];
                                    v3.setErrorArray(true);
                                    errorList.add(new Errores(560, "arr[position]" + " " + op + " " + "arr[todo]", tokenList.getFirst().getLinea(), "Incompatibilidad de arreglos: No puede comparar un arreglo completo a una posición", "Semántica 2"));
                                } else {
                                    v3.setErrorArray(true);
                                    errorList.add(new Errores(560, "arr" + " " + op + " " + "arr[todo]", tokenList.getFirst().getLinea(), "Incompatibilidad de arreglos: No puede comparar un arreglo completo a una posición", "Semántica 2"));
                                }
                            } else { //Caso 3. b[todo] * a;
                                if (!op.equals("=")) {
                                    v3.setErrorArray(true);
                                    errorList.add(new Errores(560, "arr[]" + " " + op + " " + v2.getTipo(), tokenList.getFirst().getLinea(), "Incompatibilidad de tipos: No puede utilizar un arreglo completo contra una posición", "Semántica 2"));
                                }
                            }
                        } else if (v2.isPosition()) {
                            if ((v1.getClase().equals("arr") || v1.getClase().equals("arrPar"))) {
                                if (v1.isFullArray() || v1.getClase().equals("arrfunction")) { //Caso 4. a[0] * a[todo]
                                    v3.setErrorArray(true);
                                    errorList.add(new Errores(560, "arr[todo]" + " " + op + " " + "arr[position]", tokenList.getFirst().getLinea(), "Incompatibilidad de arreglos: No puede comparar un arreglo completo a una posición", "Semántica 2"));
                                } else if (!v1.isPosition()) { //Caso 5. b[todo] * a[5];
                                    v3.setErrorArray(true);
                                    errorList.add(new Errores(560, "arr" + " " + op + " " + "arr[pos]", tokenList.getFirst().getLinea(), "Incompatibilidad de arreglos: No puede comparar un arreglo completo a una posición", "Semántica 2"));
                                }
                            } //Caso 3 var * a[5] correcto.
                        }
                    }
                }
            }

            switch (op) {
                case "+":
                    if (v1.getTipo().equals("varian") && Ut.isNumerical(v2.getTipo())) {
                        v3.setTipo(v2.getTipo());
                    } else if (v2.getTipo().equals("varian") && Ut.isNumerical(v1.getTipo())) {
                        v3.setTipo(v1.getTipo());
                    } else {
                        v3.setTipo(suma[Ut.typeNumber(v2.getTipo())][Ut.typeNumber(v1.getTipo())]);
                        if (v3.getTipo().equals("varian")) {
                            errorList.add(new Errores(552, v2.getTipo() + " " + op + " " + v1.getTipo(), tokenList.getFirst().getLinea(), "Incompatibilidad de tipos en operación aritmética", "Semántica 1"));
                        }
                    }
                    break;
                case "/":
                    if (v1.getTipo().equals("varian") && Ut.isNumerical(v2.getTipo())) {
                        v3.setTipo(v2.getTipo());
                    } else if (v2.getTipo().equals("varian") && Ut.isNumerical(v1.getTipo())) {
                        v3.setTipo(v1.getTipo());
                    } else {
                        v3.setTipo(division[Ut.typeNumber(v2.getTipo())][Ut.typeNumber(v1.getTipo())]);
                        if (v3.getTipo().equals("varian")) {
                            errorList.add(new Errores(552, v2.getTipo() + " " + op + " " + v1.getTipo(), tokenList.getFirst().getLinea(), "Incompatibilidad de tipos en operación aritmética", "Semántica 1"));
                        }
                    }
                    break;
                case "-":
                case "*":
                    if (v1.getTipo().equals("varian") && Ut.isNumerical(v2.getTipo())) {
                        v3.setTipo(v2.getTipo());
                    } else if (v2.getTipo().equals("varian") && Ut.isNumerical(v1.getTipo())) {
                        v3.setTipo(v1.getTipo());
                    } else {
                        int val = rm[Ut.typeNumber(v2.getTipo())][Ut.typeNumber(v1.getTipo())];
                        if (val == 1) {
                            int t1 = Ut.typeHierarchy(v2.getTipo());
                            int t2 = Ut.typeHierarchy(v1.getTipo());
                            if (t1 >= t2) {
                                v3.setTipo(v2.getTipo());
                            } else {
                                v3.setTipo(v1.getTipo());
                            }
                        } else {
                            v3.setTipo("varian");
                            if (v3.getTipo().equals("varian")) {
                                errorList.add(new Errores(552, v2.getTipo() + " " + op + " " + v1.getTipo(), tokenList.getFirst().getLinea(), "Incompatibilidad de tipos en operación aritmética", "Semántica 1"));
                            }
                        }
                    }
                    break;
                case "^":
                    if (v1.getTipo().equals("varian") && Ut.isNumerical(v2.getTipo())) {
                        v3.setTipo(v2.getTipo());
                    } else if (v2.getTipo().equals("varian") && Ut.isNumerical(v1.getTipo())) {
                        v3.setTipo(v1.getTipo());
                    } else {
                        v3.setTipo(elevacion[Ut.typeNumber(v2.getTipo())][Ut.typeNumber(v1.getTipo())]);
                        if (v3.getTipo().equals("varian")) {
                            errorList.add(new Errores(552, v2.getTipo() + " " + op + " " + v1.getTipo(), tokenList.getFirst().getLinea(), "Incompatibilidad de tipos en operación aritmética", "Semántica 1"));
                        }
                    }
                    break;
                case "#":
                case "&&":
                case "||":
                    if (v2.getTipo().equals("bool") && v1.getTipo().equals("bool")) {
                        v3.setTipo("bool");
                    } else if (v2.getTipo().equals("bool") && v1.getTipo().equals("varian")) {
                        v3.setTipo("bool");
                    } else if (v2.getTipo().equals("varian") && v1.getTipo().equals("varian")) {
                        v3.setTipo("bool");
                    } else if (v1.getTipo().equals("bool") && v2.getTipo().equals("varian")) {
                        v3.setTipo("bool");
                    } else {
                        v3.setTipo("varian");
                        if (v3.getTipo().equals("varian")) {
                            errorList.add(new Errores(552, v2.getTipo() + " " + op + " " + v1.getTipo(), tokenList.getFirst().getLinea(), "Incompatibilidad de tipos en operación lógica", "Semántica 1"));
                        }
                    }
                    break;
                case "<":
                case "<=":
                case ">=":
                case ">":
                    if (v2.getTipo().equals("varian") && v1.getTipo().equals("bool")
                            || v1.getTipo().equals("varian") && v2.getTipo().equals("bool")
                            || v2.getTipo().equals("varian") && v1.getTipo().equals("string")
                            || v1.getTipo().equals("varian") && v2.getTipo().equals("string")) {
                        String tipo1 = v1.getTipo();
                        String tipo2 = v2.getTipo();
                        v3.setTipo("varian");
                        if (v2.getTipo().equals("varian")) {
                            tipo2 = "integer";
                        }
                        if (v1.getTipo().equals("varian")) {
                            tipo1 = "integer";
                        }

                        v3.setTipo("varian");
                        if (v3.getTipo().equals("varian")) {
                            errorList.add(new Errores(552, tipo2 + " " + op + " " + tipo1, tokenList.getFirst().getLinea(), "Incompatibilidad de tipos para la operación", "Semántica 1"));
                        }
                    } else if (v2.getTipo().equals("varian") && v1.getTipo().equals("varian")) {
                        v3.setTipo("varian");
                    } else if (!v2.getTipo().equals("varian") && v1.getTipo().equals("varian")) {
                        v3.setTipo("bool");
                    } else if (v2.getTipo().equals("varian") && !v1.getTipo().equals("varian")) {
                        v3.setTipo("bool");
                    } else if (Ut.typeCompatibility2(v2.getTipo(), v1.getTipo())) {
                        v3.setTipo("bool");
                    } else {
                        String tipo1 = v1.getTipo();
                        String tipo2 = v2.getTipo();
                        v3.setTipo("varian");
                        if (v2.getTipo().equals("varian")) {
                            tipo2 = "integer";
                        }
                        if (v1.getTipo().equals("varian")) {
                            tipo1 = "integer";
                        }

                        if (v3.getTipo().equals("varian")) {
                            errorList.add(new Errores(552, tipo2 + " " + op + " " + tipo1, tokenList.getFirst().getLinea(), "Incompatibilidad de tipos para la operación", "Semántica 1"));
                        }
                    }
                    break;
                case "==":
                case "!=":
                    if (v2.getTipo().equals("varian") && v1.getTipo().equals("varian")) {
                        v3.setTipo("bool");
                    } else if (!v2.getTipo().equals("varian") && v1.getTipo().equals("varian")) {
                        v3.setTipo("bool");
                    } else if (v2.getTipo().equals("varian") && !v1.getTipo().equals("varian")) {
                        v3.setTipo("bool");
                    } else if (Ut.typeCompatibility(v2.getTipo(), v1.getTipo())) {
                        v3.setTipo("bool");
                    } else {
                        v3.setTipo("varian");
                        if (v3.getTipo().equals("varian")) {
                            errorList.add(new Errores(552, v2.getTipo() + " " + op + " " + v1.getTipo(), tokenList.getFirst().getLinea(), "Incompatibilidad de tipos para la operación", "Semántica 1"));
                        }
                    }
                    break;
                case "=":
                    v3.setTipo("as");
                    //Regla 7.
                    Semantica2 sm2 = new Semantica2();
                    sm2.setRegla("1070");
                    sm2.setTp("var||par||arr");
                    sm2.setLinea("" + tokenList.getFirst().getLinea());
                    sm2.setAmbito("" + pilaAmbito.peek());
                    sm2.setValorr(v2.getClase());
                    if (Ut.isClassAsignable(v2.getClase())) { //Regla Semántica 2-7.
                        sm2.setEstado("Aceptado");

                        Semantica2 sm4 = new Semantica2();
                        sm4.setRegla("1031");
                        sm4.setTp(v2.getNombre());
                        sm4.setLinea("" + tokenList.getFirst().getLinea());
                        sm4.setAmbito("" + pilaAmbito.peek());
                        sm4.setValorr(v1.getNombre());

                        if (Ut.isArrayAsignable(v1, v2)) { //Regla Semántica 3-7
                            sm4.setEstado("Aceptado");

                            Semantica2 sm3 = new Semantica2();
                            sm3.setRegla("1030");
                            sm3.setTp(v2.getTipo());
                            sm3.setLinea("" + tokenList.getFirst().getLinea());
                            sm3.setAmbito("" + pilaAmbito.peek());
                            sm3.setValorr(v1.getTipo());
                            if (!Ut.isAsignable(v1.getTipo(), v2.getTipo())) { //Regla Semántica 1 --- Regla 3
                                sm3.setEstado("Error");
                                errorList.add(new Errores(553, v2.getNombre() + " = " + v1.getTipo(), lineaError, Ut.assignError(v1.getTipo(), v2.getTipo()), "Semántica 1"));
                            } else {
                                sm3.setEstado("Aceptado");
                            }
                            valoresSemantica.add(sm3);
                        } else {
                            sm4.setEstado("Error");
                            errorList.add(new Errores(602, v2.getNombre() + " = " + v1.getTipo(), lineaError, "Diferencia en dimensiones de arreglo", "Semántica 2"));
                        }
                        if (v2.getClase().equals("arr") || v2.getClase().equals("arrpar")) {
                            valoresSemantica.add(sm4);
                        }
                    } else {
                        sm2.setEstado("Aceptado");
                        errorList.add(new Errores(559, v2.getNombre() + " = " + v1.getTipo(), lineaError, "No se le puede asignar a un " + v2.getClase(), "Semántica 2"));
                    }
                    valoresSemantica.add(sm2);

                    lineaAsignaciones.add(lineaError);
                    asignaciones.add(Ut.imprimirValor(v2.getTipo()) + " ← " + Ut.checarArreglo(contadorTemporales) + Ut.imprimirValor(v1.getTipo()));
                    contadorSemantica1.add(contadorTemporales);
                    break;
            }
            if (!v3.getTipo().equals("") || !v3.getTipo().equals(null)) {
                if (!v3.getTipo().equals("as")) {
                    pilaOperando.push(v3);
                }
            }
            if (!v3.getTipo().equals("as")) {
                contadorTemporales[Ut.typeNumber(v3.getTipo())]++;
            }

//                    System.out.println("Lexema 1: " + v2.getNombre() + ", " + v2.getTipo());
//                    System.out.println("Lexema 2: " + v1.getNombre() + ", " + v1.getTipo());
//                    System.out.println("Operador: " + op);
//                    System.out.println("Resultado: " + v3.getNombre() + ", " + v3.getTipo());
        }
    }

    private static void operacionUnaria(Stack<Variable> pilaOperando, Stack<String> pilaOperador,
            LinkedList<Token> tokenList, LinkedList<Errores> errorList,
            LinkedList<Integer> lineaAsignaciones, int lineaError, LinkedList<String> asignaciones,
            LinkedList<int[]> contadorSemantica1, int[] contadorTemporales, boolean arregloVerificar) {
        Variable v1 = pilaOperando.pop();
        String op = pilaOperador.pop();

        Variable v2 = new Variable();
        switch (op) {
            case "!":
                if (!v1.getTipo().equals("bool")) {
                    v2.setTipo("varian");
                    errorList.add(new Errores(552, op + " " + v1.getTipo(), tokenList.getFirst().getLinea(), "Incompatibilidad de tipos", "Semántica 1"));
                } else {
                    v2.setTipo("bool");
                }
                break;
            case "++":
            case "--":
            case "-":
            case "+":
                if (Ut.isNumerical(v1.getTipo())) {
                    v2.setTipo(v1.getTipo());
                } else {
                    v2.setTipo("varian");
                    errorList.add(new Errores(552, op + " " + v1.getTipo(), tokenList.getFirst().getLinea(), "Incompatibilidad de tipos", "Semántica 1"));
                }
        }
        pilaOperando.push(v2);
        contadorTemporales[Ut.typeNumber(v2.getTipo())]++;
    }

    private void actualizarParametros(LinkedList<Variable> parametersMethod, Variable methodDec,
            LinkedList<Variable> listaVariable) {

        LinkedList<Variable> parameters = this.listaParametros(listaVariable, methodDec); //Parametros actuales.
        LinkedList<Variable> funcionFinal = new LinkedList();

        //No es ell highestSize. Es el esperado.
        int highestSize = (parameters.size() > parametersMethod.size())
                ? parameters.size() : parametersMethod.size();

        for (int i = 0; i < highestSize; i++) {
            Variable var1 = null;
            Variable var2 = null;
            try {
                var1 = parameters.get(i); //Variable esperada.
                var2 = parametersMethod.get(i); //Variable que llega.
                var2.setChanges(true);

                Semantica2 sm2 = new Semantica2();
                sm2.setRegla("1160");
                sm2.setTp(var1.getNombre());
                sm2.setLinea("" + tokenList.getFirst().getLinea());
                sm2.setAmbito("" + pilaAmbito.peek());
                sm2.setValorr(var2.getNombre());

                if (!var2.getNombre().equals(var1.getNombre())) { //Regla 17
                    sm2.setEstado("Error");
                    funcionFinal.add(var1);
                    errorList.add(new Errores(564, var2.getNombre() + "<!>" + var1.getNombre(), tokenList.getFirst().getLinea(), "Parámetro con identificador distinto.", "Semántica 2"));
                } else {
                    Semantica2 sm3 = new Semantica2();
                    sm3.setRegla("1170");
                    sm3.setTp(var1.getTipo());
                    sm3.setLinea("" + tokenList.getFirst().getLinea());
                    sm3.setAmbito("" + pilaAmbito.peek());
                    sm3.setValorr(var2.getTipo());
                    if (!var2.getTipo().equals(var1.getTipo())) { //Regla 18
                        sm3.setEstado("Error");
                        errorList.add(new Errores(563, var2.getNombre() + " " + var2.getTipo() + "<!>" + var1.getTipo(), tokenList.getFirst().getLinea(), "Parámetro con tipo de dato distinto.", "Semántica 2"));
                    } else {
                        sm3.setEstado("Aceptado");
                        //IntroducitValorExcelManuel.
                    }
                    valoresSemantica.add(sm3);
                    sm2.setEstado("Aceptado");
                }
                valoresSemantica.add(sm2);
                funcionFinal.add(var2);
            } catch (Exception e) {
                if (parameters.size() > parametersMethod.size()) {
                    var1 = parameters.get(i);

                    Semantica2 sm2 = new Semantica2();
                    sm2.setRegla("1160");
                    sm2.setTp(var1.getNombre());
                    sm2.setLinea("" + tokenList.getFirst().getLinea());
                    sm2.setAmbito("" + pilaAmbito.peek());
                    sm2.setValorr("nulll");
                    sm2.setEstado("Error");
                    valoresSemantica.add(sm2);

                    var1.setUtilizar(false);
                    errorList.add(new Errores(562, var1.getNombre(), tokenList.getFirst().getLinea(), "Parámetro no vuelto a declarar.", "Semántica 2"));
                    funcionFinal.add(var1);
                } else {
                    var2 = parametersMethod.get(i);

                    Semantica2 sm2 = new Semantica2();
                    sm2.setRegla("1160");
                    sm2.setTp("null");
                    sm2.setLinea("" + tokenList.getFirst().getLinea());
                    sm2.setAmbito("" + pilaAmbito.peek());
                    sm2.setValorr(var2.getNombre());
                    sm2.setEstado("Error");
                    valoresSemantica.add(sm2);

                    var2.setUtilizar(true);
                    var2.setChanges(true);
                    errorList.add(new Errores(561, var2.getNombre(), tokenList.getFirst().getLinea(), "Parámetro nuevo declarado.", "Semántica 2"));
                    funcionFinal.add(var2);
                }
            }

        }
        /*
         Se obtuvo la lista con las funciones ya definidas.
         Ahora toca actualizar la lista. Se borrará el registro ya usado
         y utilizará el nuevo.
         */
        int ambito = getAmbitoVariable(listaVariable, methodDec.getNombre()) + 1;
        LinkedList<Variable> parametros = new LinkedList();

        for (int i = 0; i < listaVariable.size(); i++) {
            Variable variable = listaVariable.get(i);
            if (Integer.parseInt(variable.getAmbito()) == ambito && variable.getTparr().equals(methodDec.getNombre())) {
                listaVariable.remove(i--);
            }
            if (Integer.parseInt(variable.getAmbito()) == ambito && variable.getNombre().equals(methodDec.getNombre())) {
                listaVariable.remove(i--);
            }
        }

        methodDec.setChanges(true);
        funcionFinal.push(methodDec);
        for (Variable parametro : funcionFinal) {
            listaVariable.add(parametro);
        }
    }

    private void actualizarParametrosFwd(Variable methodDec, LinkedList<Variable> listaVariable) {
        LinkedList<Variable> parameters = this.listaParametros(listaVariable, methodDec);
        LinkedList<Variable> parametersFwd = new LinkedList();

        for (Variable parameters1 : parameters) {
            parameters1.setForward(true);
            parametersFwd.add(parameters1);
        }

        /*
         Se actualizó la función a forward.
         */
        int ambito = getAmbitoVariable(listaVariable, methodDec.getNombre()) + 1;

        for (int i = 0; i < listaVariable.size(); i++) {
            Variable variable = listaVariable.get(i);
            if (Integer.parseInt(variable.getAmbito()) == ambito && variable.getTparr().equals(methodDec.getNombre())) {
                listaVariable.remove(i--);
            }
            if (Integer.parseInt(variable.getAmbito()) == ambito && variable.getNombre().equals(methodDec.getNombre())) {
                listaVariable.remove(i--);
            }
        }

        methodDec.setForward(true);
        parametersFwd.push(methodDec);
        for (Variable parametro : parametersFwd) {
            listaVariable.add(parametro);
        }
    }

    private void actualizarFuncion(Variable methodDec, int lastAmbito, String tipo) {
        for (int i = 0; i < variableList.size(); i++) {
            Variable var = variableList.get(i);
            String varNom = var.getNombre();
            String varAm = var.getAmbito();
            String varNom1 = methodDec.getNombre();
            if (var.getNombre().equals(methodDec.getNombre()) && var.getAmbito().equals("" + lastAmbito)) {
                var.setTipo(tipo);
                variableList.set(i, var);
                return;
            }
        }
    }

    public LinkedList<Semantica2> getValoresSemantica() {
        return valoresSemantica;
    }

    public void setValoresSemantica(LinkedList<Semantica2> valoresSemantica) {
        LexicAnalyzer.valoresSemantica = valoresSemantica;
    }

}
