/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

/**
 *
 * @author Hp EliteDesk
 */
public class Token {
    int numero, linea;
    String lexema;

    public Token(int numero, String lexema, int linea) {
        this.numero = numero;
        this.lexema = lexema;
        this.linea = linea;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    public int getLinea() {
        return linea;
    }

    public void setLinea(int linea) {
        this.linea = linea;
    }
    
    public Object[] returnObject(){
        Object[] objeto = {numero, lexema, linea};
        return objeto;
    }
}
