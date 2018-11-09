/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.util.LinkedList;

/**
 *
 * @author Antonio López Higuer
 */
public class Variable {
    private String tipo, clase, id, tarr, tparr, alcance;
    private String ambito, dimarr, nopar;
    private String value;

    //Reglas de Funciones.
    private boolean forward, forwarded;
    private boolean changes;
    private boolean utilizar;

    //Reglas de Arreglos
    private boolean position, fullArray;
    private boolean errorArray;
    
        private LinkedList<Variable> listaArreglo;


    public LinkedList<Variable> getListaArreglo() {
        return listaArreglo;
    }

    public void setListaArreglo(LinkedList<Variable> listaArreglo) {
        this.listaArreglo = listaArreglo;
    }
    
    
    public boolean isUtilizar() {
        return utilizar;
    }

    public void setUtilizar(boolean utilizar) {
        this.utilizar = utilizar;
    }

    public boolean isChanges() {
        return changes;
    }

    public void setChanges(boolean changes) {
        this.changes = changes;
    }

    public boolean isErrorArray() {
        return errorArray;
    }

    public void setErrorArray(boolean errorArray) {
        this.errorArray = errorArray;
    }
    
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlcance() {
        return alcance;
    }

    public void setAlcance(String alcance) {
        this.alcance = alcance;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isForward() {
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }

    public boolean isForwarded() {
        return forwarded;
    }

    public void setForwarded(boolean forwarded) {
        this.forwarded = forwarded;
    }

    public boolean isPosition() {
        return position;
    }

    public void setPosition(boolean position) {
        this.position = position;
    }

    public boolean isFullArray() {
        return fullArray;
    }

    public void setFullArray(boolean fullArray) {
        this.fullArray = fullArray;
    }

    public Variable(String tipo, String clase, String id, String ambito, String tarr, String dimarr, String nopar, String tparr) {
        this.tipo = tipo;
        this.clase = clase;
        this.id = id;
        this.ambito = ambito;
        this.tarr = tarr;
        this.dimarr = dimarr;
        this.nopar = nopar;
        this.tparr = tparr;
    }
    
    public Variable(Variable variable){
        this.tipo = variable.getTipo();
        this.clase = variable.getClase();
        this.id = variable.getNombre();
        this.ambito = variable.getAmbito();
        this.tarr = variable.getTarr();
        this.dimarr = variable.getDimarr();
        this.nopar = variable.getNopar();
        this.tparr = variable.getTparr();
        this.forward = variable.isForward();
        this.changes = variable.isChanges();
        this.position = variable.isPosition();
        this.fullArray = variable.isFullArray();
    }

    public Variable() {
        tipo = "-";
        clase = "-";
        id = "-";
        ambito = "-";
        tarr = "-";
        dimarr = "-";
        nopar = "-";
        tparr = "-";
        utilizar = true;
    }

    public Variable(String id, String ambito) {
        this.id = id;
        this.ambito = ambito;
    }

    public Variable(String ambito, String clase, String id) {
        this.clase = clase;
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public String getNombre() {
        return id;
    }

    public void setNombre(String id) {
        this.id = id;
    }

    public String getAmbito() {
        return ambito;
    }

    public void setAmbito(String ambito) {
        this.ambito = ambito;
    }

    public String getTarr() {
        return tarr;
    }

    public void setTarr(String tarr) {
        this.tarr = tarr;
    }

    public String getDimarr() {
        return dimarr;
    }

    public void setDimarr(String dimarr) {
        this.dimarr = dimarr;
    }

    public String getNopar() {
        return nopar;
    }

    public void setNopar(String nopar) {
        this.nopar = nopar;
    }

    public String getTparr() {
        return tparr;
    }

    public void setTparr(String tparr) {
        this.tparr = tparr;
    }
    
    
}
