-- tabla de usuarios
create table usuarios (
    id int auto_increment primary key,
    username varchar(50) not null unique,
    password_hash varchar(255) not null,
    rol enum('admin', 'empleado', 'cajero') not null,
    activo boolean default true,
    fecha_creacion timestamp default current_timestamp
);
 
-- procedimiento para registrar usuario
delimiter //
create procedure sp_registrar_usuario(
    in _username varchar(50), 
    in _password_hash varchar(255), 
    in _rol varchar(20)
)
begin
    insert into usuarios (username, password_hash, rol) 
    values (_username, _password_hash, _rol);
end //
delimiter ;
 
-- procedimiento para iniciar sesión
delimiter //
create procedure sp_iniciar_sesion(
    in _username varchar(50), 
    in _password_hash varchar(255)
)
begin
    select id, username, rol 
    from usuarios 
    where username = _username 
      and password_hash = _password_hash 
      and activo = true 
    limit 1;
end //
delimiter ;

-- procedimiento para cambiar contraseña
delimiter //
create procedure sp_actualizar_password(
    in _username varchar(50),
    in _password_hash varchar(255)
)
begin
    update usuarios
    set password_hash = _password_hash
    where username = _username;
end //
delimiter ;

call sp_registrar_usuario('Raguay',sha2('admin',256), 'admin'); 
call sp_iniciar_sesion('Raguay',sha2('admin', 256)); 

call sp_registrar_usuario('Cajero',sha2('cajero',256), 'cajero'); 
call sp_iniciar_sesion('Cajero',sha2('cajero', 256));  

call sp_registrar_usuario('Empleado',sha2('empleado',256), 'empleado'); 
call sp_iniciar_sesion('Empleado',sha2('empleado', 256));   

select * from usuarios; 