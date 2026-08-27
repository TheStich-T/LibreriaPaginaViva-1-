package org.lpv.model;

import java.time.LocalDateTime;

public class Usuario {
    
//create table usuarios (
//    id int auto_increment primary key,
//    username varchar(50) not null unique,
//    password_hash varchar(255) not null,
//    rol enum('admin', 'empleado', 'cajero') not null,
//    activo boolean default true,
//    fecha_creacion timestamp default current_timestamp
//);    
    private int id;
    private String username;
    private String rol;
    private String passwordHash; 
    private boolean activo; 
    private LocalDateTime fechaCreacion; 

    public Usuario() {
    }

    public Usuario(int id, String username, String rol, String passwordHash, boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.username = username;
        this.rol = rol;
        this.passwordHash = passwordHash;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

   
 
    
   
}
 
   