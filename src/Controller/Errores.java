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

public class Errores {
    private int numero, linea;
    private String lexema, descripcion, tipoError;

    public Errores(int numero, String lexema, int linea, String descripcion, String tipoError) {
        this.numero = numero;
        this.lexema = lexema;
        this.linea = linea;
        this.descripcion = descripcion;
        this.tipoError = tipoError;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoError() {
        return tipoError;
    }

    public void setTipoError(String tipoError) {
        this.tipoError = tipoError;
    }
    
    public Object[] returnObject(){
        Object[] objeto = {numero, descripcion, lexema, tipoError, linea};
        return objeto;
    }
}
