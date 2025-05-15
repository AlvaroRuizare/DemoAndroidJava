package com.example.formulario.Data;

import java.io.Serializable;

public class UsuarioModel implements Serializable {
    int id;
    String fotoPerfil, nombre, contrasena, correo, fechaNacimiento, tipoUsuario, rutaFoto;
    int deBaja;
    boolean seleccionado, oculto;


    public UsuarioModel(int id, String imagen, String nombre, String contrasena, String correo, String fechaNacimiento, String tipoUsuario, String rutaFoto, int deBaja, boolean seleccionado) {
        this.id = id;
        this.fotoPerfil = imagen;
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.correo = correo;
        this.fechaNacimiento = fechaNacimiento;
        this.tipoUsuario = tipoUsuario;
        this.rutaFoto = rutaFoto;
        this.deBaja = deBaja;
        this.seleccionado = seleccionado;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public void setDeBaja(int deBaja) {
        this.deBaja = deBaja;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }


    public int getId() {
        return id;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContrasena() {
        return contrasena;
    }

    public String getCorreo() {
        return correo;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public boolean isBaja() {
        boolean bajaBool = false;

        if(deBaja == 1){
            bajaBool = true;
        }

        return bajaBool;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }
}
