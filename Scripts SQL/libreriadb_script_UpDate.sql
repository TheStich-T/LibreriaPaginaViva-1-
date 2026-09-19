use libreriadb_in4cm;
 
-- eliminar los usuarios de prueba del proyecto anterior
delete from usuarios where username in ('Raguay', 'Cajero', 'Empleado');
 
-- corregir el enum de roles: el backlog define admin, bodega, cajero (no "empleado")
alter table usuarios
    modify column rol enum('admin','bodega','cajero') not null;
 
-- procedimiento para cambiar contraseña 
drop procedure if exists sp_actualizar_password;
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
 
--  procedimiento para distinguir usuario inactivo de contraseña incorrecta
drop procedure if exists sp_buscar_usuario_por_username;
delimiter //
create procedure sp_buscar_usuario_por_username(
    in _username varchar(50)
)
begin
    select id, username, password_hash, rol, activo
    from usuarios
    where username = _username
    limit 1;
end //
delimiter ;
 
drop procedure if exists sp_listar_usuarios;
delimiter //
create procedure sp_listar_usuarios()
begin
    select id, username, rol, activo, fecha_creacion
    from usuarios
    order by username;
end //
delimiter ;
 
drop procedure if exists sp_buscar_usuario_por_id;
delimiter //
create procedure sp_buscar_usuario_por_id(
    in _id int
)
begin
    select id, username, rol, activo, fecha_creacion
    from usuarios
    where id = _id;
end //
delimiter ;
 
drop procedure if exists sp_actualizar_usuario;
delimiter //
create procedure sp_actualizar_usuario(
    in _id int,
    in _rol varchar(20)
)
begin
    update usuarios
    set rol = _rol
    where id = _id;
end //
delimiter ;
 
drop procedure if exists sp_desactivar_usuario;
delimiter //
create procedure sp_desactivar_usuario(
    in _id int
)
begin
    update usuarios
    set activo = false
    where id = _id;
end //
delimiter ;
 
drop procedure if exists sp_activar_usuario;
delimiter //
create procedure sp_activar_usuario(
    in _id int
)
begin
    update usuarios
    set activo = true
    where id = _id;
end //
delimiter ;
 
 
-- usuarios de prueba nuevos, ya con los roles correctos de este proyecto
delete from usuarios where username in ('admin1', 'cajero1', 'bodega1');
call sp_registrar_usuario('admin1', sha2('admin123', 256), 'admin');
call sp_registrar_usuario('cajero1', sha2('cajero123', 256), 'cajero');
call sp_registrar_usuario('bodega1', sha2('bodega123', 256), 'bodega');
 
 
-- editoriales — corregir typo de columna (direccion_editoria -> direccion_editorial)
alter table editoriales
	change column direccion_editoria direccion_editorial varchar(100);
 
-- libros — agregar control de inventario
alter table libros
	add column stock_actual int not null default 0 after nit_editorial,
	add column stock_minimo int not null default 0 after stock_actual,
	add column activo boolean not null default true after stock_minimo,
	add column fecha_actualizacion timestamp not null
	default current_timestamp on update current_timestamp after activo;
 
-- Quitar las llaves foráneas viejas antes de renombrar tablas/columnas
alter table detalle_compra drop foreign key fk_a_compra;
alter table detalle_compra drop foreign key fk_a_libros;
alter table compras drop foreign key fk_a_cliente;
 
-- compras a ventas  /  detalle_compra a detalle_venta (renombrar tablas)
rename table compras to ventas;
rename table detalle_compra to detalle_venta;
 
-- renombrar columnas de ventas
alter table ventas
    change column no_compra id_venta int not null auto_increment,
    change column fecha_compra fecha_venta timestamp default current_timestamp,
    change column total_compra total decimal(10,2);
 
-- agregar columnas nuevas de ventas
alter table ventas
	add column subtotal decimal(10,2) not null default 0 after id_venta,
	add column descuento decimal(10,2) not null default 0 after total,
	add column usuario_autoriza_descuento int null after descuento,
	add column estado enum('COMPLETADA','ANULADA','DEVUELTA')
	not null default 'COMPLETADA' after usuario_autoriza_descuento,
	add column id_usuario int null after estado,
    add column fecha_anulacion timestamp null after id_usuario,
    add column usuario_anulacion int null after fecha_anulacion,
    add column motivo_anulacion varchar(255) null after usuario_anulacion;
 
-- renombrar y agregar columnas de detalle_venta
alter table detalle_venta
    change column id_detalle_compra id_detalle int not null auto_increment,
    change column no_compra id_venta int;
 
alter table detalle_venta
    add column cantidad int not null default 1 after isbn,
    add column precio_unitario decimal(10,2) not null default 0 after cantidad,
    add column subtotal decimal(10,2) not null default 0 after precio_unitario;
 
-- 2.4 recrear las llaves foráneas con los nuevos nombres
alter table ventas
    add constraint fk_venta_cliente foreign key (cui_cliente) references clientes(cui) on delete cascade,
    add constraint fk_venta_usuario foreign key (id_usuario) references usuarios(id) on delete set null,
    add constraint fk_venta_autoriza foreign key (usuario_autoriza_descuento) references usuarios(id) on delete set null,
    add constraint fk_venta_anulacion foreign key (usuario_anulacion) references usuarios(id) on delete set null;
 
alter table detalle_venta
    add constraint fk_detalle_venta foreign key (id_venta) references ventas(id_venta) on delete cascade,
    add constraint fk_detalle_libro foreign key (isbn) references libros(isbn) on delete cascade;
 
-- proveedores
create table if not exists proveedores (
    nit_proveedor varchar(20) primary key,
    nombre_proveedor varchar(100) not null,
    telefono_proveedor varchar(15),
    direccion_proveedor varchar(100)
);
 
-- movimientos_inventario
create table if not exists movimientos_inventario (
    id_movimiento int primary key auto_increment,
    isbn varchar(20) not null,
    tipo_movimiento enum('INGRESO','VENTA','MERMA','TRASLADO','DEVOLUCION','AJUSTE') not null,
    cantidad int not null,
    fecha_movimiento timestamp default current_timestamp,
    id_usuario int null,
    observacion varchar(255),
    nit_proveedor varchar(20) null
);
alter table movimientos_inventario
    add constraint fk_movimiento_libro foreign key (isbn) references libros(isbn) on delete cascade,
    add constraint fk_movimiento_usuario foreign key (id_usuario) references usuarios(id) on delete set null,
    add constraint fk_movimiento_proveedor foreign key (nit_proveedor) references proveedores(nit_proveedor) on delete set null;
 
-- procedimientos almacenados: reemplazar los que apuntaban a compras/detalle_compra
drop procedure if exists sp_insertarcompra;
drop procedure if exists sp_listarcompras;
drop procedure if exists sp_buscarcompra;
drop procedure if exists sp_actualizarcompra;
drop procedure if exists sp_eliminarcompra;
drop procedure if exists sp_insertardetallecompra;
drop procedure if exists sp_listardetallecompra;
drop procedure if exists sp_buscardetallecompra;
drop procedure if exists sp_actualizardetallecompra;
drop procedure if exists sp_eliminardetallecompra;
drop procedure if exists sp_insertarventa;
drop procedure if exists sp_listarventas;
drop procedure if exists sp_buscarventa;
drop procedure if exists sp_anularventa;
drop procedure if exists sp_eliminarventa;
drop procedure if exists sp_insertardetalleventa;
drop procedure if exists sp_listardetalleventa;
drop procedure if exists sp_eliminardetalleventa;
drop procedure if exists sp_actualizarstocklibro;
drop procedure if exists sp_listarstockcritico;
drop procedure if exists sp_insertarproveedor;
drop procedure if exists sp_listarproveedores;
drop procedure if exists sp_actualizarproveedor;
drop procedure if exists sp_eliminarproveedor;
drop procedure if exists sp_registrarmovimiento;
drop procedure if exists sp_listarmovimientos;
drop procedure if exists sp_buscarlibroporisbn;
drop procedure if exists sp_buscarlibropotitulo;
drop procedure if exists sp_buscarlibrosporautor;
drop procedure if exists sp_ventasdeldiaporusuario;
delimiter $$
 
create procedure sp_insertarventa(
    in _subtotal decimal(10,2),
    in _descuento decimal(10,2),
    in _usuario_autoriza_descuento int,
    in _total decimal(10,2),
    in _cui_cliente bigint,
    in _id_usuario int
)
begin
    insert into ventas(subtotal, descuento, usuario_autoriza_descuento, total, cui_cliente, id_usuario)
    values (_subtotal, _descuento, _usuario_autoriza_descuento, _total, _cui_cliente, _id_usuario);
end $$
 
create procedure sp_listarventas()
begin
    select id_venta, fecha_venta, subtotal, descuento, total, estado, cui_cliente, id_usuario
    from ventas;
end $$
 
create procedure sp_buscarventa(
    in _id_venta int
)
begin
    select id_venta, fecha_venta, subtotal, descuento, total, estado, cui_cliente, id_usuario
    from ventas
    where id_venta = _id_venta;
end $$
 
create procedure sp_anularventa(
    in _id_venta int,
    in _usuario_anulacion int,
    in _motivo_anulacion varchar(255)
)
begin
    update ventas
    set estado = 'ANULADA',
        fecha_anulacion = current_timestamp,
        usuario_anulacion = _usuario_anulacion,
        motivo_anulacion = _motivo_anulacion
    where id_venta = _id_venta;
end $$
 
create procedure sp_eliminarventa(
    in _id_venta int
)
begin
    delete from ventas where id_venta = _id_venta;
end $$
 
create procedure sp_insertardetalleventa(
    in _id_venta int,
    in _isbn varchar(20),
    in _cantidad int,
    in _precio_unitario decimal(10,2)
)
begin
    insert into detalle_venta(id_venta, isbn, cantidad, precio_unitario, subtotal)
    values (_id_venta, _isbn, _cantidad, _precio_unitario, _cantidad * _precio_unitario);
end $$
 
create procedure sp_listardetalleventa(
    in _id_venta int
)
begin
    select id_detalle, id_venta, isbn, cantidad, precio_unitario, subtotal
    from detalle_venta
    where id_venta = _id_venta;
end $$
 
create procedure sp_eliminardetalleventa(
    in _id_detalle int
)
begin
    delete from detalle_venta where id_detalle = _id_detalle;
end $$
 
create procedure sp_actualizarstocklibro(
    in _isbn varchar(20),
    in _stock_actual int,
    in _stock_minimo int
)
begin
    update libros
    set stock_actual = _stock_actual,
        stock_minimo = _stock_minimo
    where isbn = _isbn;
end $$
 
create procedure sp_listarstockcritico()
begin
    select isbn, titulo, stock_actual, stock_minimo
    from libros
    where stock_actual <= stock_minimo and activo = true;
end $$
 
create procedure sp_insertarproveedor(
    in _nit_proveedor varchar(20),
    in _nombre_proveedor varchar(100),
    in _telefono_proveedor varchar(15),
    in _direccion_proveedor varchar(100)
)
begin
    insert into proveedores(nit_proveedor, nombre_proveedor, telefono_proveedor, direccion_proveedor)
    values (_nit_proveedor, _nombre_proveedor, _telefono_proveedor, _direccion_proveedor);
end $$
 
create procedure sp_listarproveedores()
begin
    select nit_proveedor, nombre_proveedor, telefono_proveedor, direccion_proveedor
    from proveedores;
end $$
 
create procedure sp_actualizarproveedor(
    in _nit_proveedor varchar(20),
    in _nombre_proveedor varchar(100),
    in _telefono_proveedor varchar(15),
    in _direccion_proveedor varchar(100)
)
begin
    update proveedores
    set nombre_proveedor = _nombre_proveedor,
        telefono_proveedor = _telefono_proveedor,
        direccion_proveedor = _direccion_proveedor
    where nit_proveedor = _nit_proveedor;
end $$
 
create procedure sp_eliminarproveedor(
    in _nit_proveedor varchar(20)
)
begin
    delete from proveedores where nit_proveedor = _nit_proveedor;
end $$
 
create procedure sp_registrarmovimiento(
    in _isbn varchar(20),
    in _tipo_movimiento varchar(20),
    in _cantidad int,
    in _id_usuario int,
    in _observacion varchar(255),
    in _nit_proveedor varchar(20)
)
begin
    insert into movimientos_inventario(isbn, tipo_movimiento, cantidad, id_usuario, observacion, nit_proveedor)
    values (_isbn, _tipo_movimiento, _cantidad, _id_usuario, _observacion, _nit_proveedor);
end $$
 
create procedure sp_listarmovimientos(
    in _isbn varchar(20)
)
begin
    select id_movimiento, isbn, tipo_movimiento, cantidad, fecha_movimiento, id_usuario, observacion, nit_proveedor
    from movimientos_inventario
    where isbn = _isbn
    order by fecha_movimiento desc;
end $$
 
-- búsqueda de libros y ventas del día
 
create procedure sp_buscarlibroporisbn(
    in _isbn varchar(20)
)
begin
    select isbn, titulo, fecha_publicacion, precio, id_categoria, nit_editorial,
           stock_actual, stock_minimo, activo
    from libros
    where isbn = _isbn;
end $$
 
create procedure sp_buscarlibropotitulo(
    in _titulo varchar(150)
)
begin
    select isbn, titulo, fecha_publicacion, precio, id_categoria, nit_editorial,
           stock_actual, stock_minimo, activo
    from libros
    where titulo like concat('%', _titulo, '%')
      and activo = true;
end $$
 
create procedure sp_buscarlibrosporautor(
    in _autor varchar(150)
)
begin
    select l.isbn, l.titulo, l.precio, l.stock_actual, a.nombre_autor
    from libros l
    inner join autores_libro al on l.isbn = al.isbn
    inner join autores a on al.id_autor = a.id_autor
    where a.nombre_autor like concat('%', _autor, '%')
      and l.activo = true;
end $$
 
create procedure sp_ventasdeldiaporusuario(
    in _id_usuario int
)
begin
    select id_venta, fecha_venta, subtotal, descuento, total, estado
    from ventas
    where id_usuario = _id_usuario
      and date(fecha_venta) = curdate()
    order by fecha_venta desc;
end $$
 
delimiter ;
 
-- vistas — reemplazar y crear las que dependían de compras y detalle_compra
drop view if exists vw_lista_compras;
drop view if exists vw_lista_detalle_compra;
drop view if exists vw_factura_compras;
 
create or replace view vw_lista_ventas as select
    v.id_venta as 'no. venta',
    v.fecha_venta as 'fecha/hora',
    v.subtotal as 'subtotal',
    v.descuento as 'descuento',
    v.total as 'total',
    v.estado as 'estado',
    v.cui_cliente as 'cui cliente',
    concat(cl.nombre_cliente, ' ', cl.apellido_cliente) as 'cliente'
from ventas v
inner join clientes cl on v.cui_cliente = cl.cui;
 
create or replace view vw_lista_detalle_venta as select
    dv.id_detalle as 'id detalle',
    dv.id_venta as 'no. venta',
    l.titulo as 'libro',
    l.isbn as 'isbn',
    dv.cantidad as 'cantidad',
    dv.precio_unitario as 'precio unitario',
    dv.subtotal as 'subtotal'
from detalle_venta dv
inner join libros l on dv.isbn = l.isbn;
 
create or replace view vw_factura_ventas as select
    v.id_venta as 'numero_factura',
    v.fecha_venta as 'fecha_emision',
    cl.cui as 'cui_cliente',
    concat(cl.nombre_cliente, ' ', cl.apellido_cliente) as 'nombre_cliente',
    cl.correo_electronico as 'correo_cliente',
    l.isbn as 'isbn_libro',
    l.titulo as 'descripcion_libro',
    dv.precio_unitario as 'precio_articulo',
    dv.cantidad as 'cantidad',
    dv.subtotal as 'subtotal_articulo',
    v.descuento as 'descuento',
    v.total as 'gran_total'
from ventas v
inner join clientes cl on v.cui_cliente = cl.cui
inner join detalle_venta dv on v.id_venta = dv.id_venta
inner join libros l on dv.isbn = l.isbn;
 
create or replace view vw_lista_libros as select
    l.isbn as 'isbn',
    l.titulo as 'título',
    l.fecha_publicacion as 'fecha de publicación',
    l.precio as 'precio',
    c.nombre_categoria as 'categoría',
    e.nombre_editorial as 'editorial',
    l.stock_actual as 'stock actual',
    l.stock_minimo as 'stock mínimo',
    l.activo as 'activo'
from libros l
inner join categorias c on l.id_categoria = c.id_categoria
inner join editoriales e on l.nit_editorial = e.nit;
 
create or replace view vw_lista_editoriales as select
    nit as 'nit editorial',
    nombre_editorial as 'editorial',
    telefono_editorial as 'teléfono',
    direccion_editorial as 'dirección'
from editoriales;
 
create or replace view vw_stock_critico as select
    l.isbn as 'isbn',
    l.titulo as 'título',
    l.stock_actual as 'stock actual',
    l.stock_minimo as 'stock mínimo'
from libros l
where l.stock_actual <= l.stock_minimo
and l.activo = true;
 
create or replace view vw_lista_proveedores as select
    nit_proveedor as 'nit proveedor',
    nombre_proveedor as 'proveedor',
    telefono_proveedor as 'teléfono',
    direccion_proveedor as 'dirección'
from proveedores;
 
create or replace view vw_lista_movimientos_inventario as select
    m.id_movimiento as 'id movimiento',
    l.titulo as 'libro',
    m.isbn as 'isbn',
    m.tipo_movimiento as 'tipo',
    m.cantidad as 'cantidad',
    m.fecha_movimiento as 'fecha',
    m.id_usuario as 'id usuario',
    m.observacion as 'observación'
from movimientos_inventario m
inner join libros l on m.isbn = l.isbn;
 
-- Stock de prueba para poder registrar ventas
-- Stock de prueba para poder registrar ventas (los 32 libros del DML)
CALL sp_actualizarstocklibro('978-0-123', 50, 5);
CALL sp_actualizarstocklibro('978-0-124', 50, 5);
CALL sp_actualizarstocklibro('978-0-125', 50, 5);
CALL sp_actualizarstocklibro('978-0-126', 50, 5);
CALL sp_actualizarstocklibro('978-0-127', 50, 5);
CALL sp_actualizarstocklibro('978-0-128', 50, 5);
CALL sp_actualizarstocklibro('978-0-129', 50, 5);
CALL sp_actualizarstocklibro('978-0-130', 50, 5);
CALL sp_actualizarstocklibro('978-0-131', 50, 5);
CALL sp_actualizarstocklibro('978-0-132', 50, 5);
CALL sp_actualizarstocklibro('978-0-133', 50, 5);
CALL sp_actualizarstocklibro('978-0-134', 50, 5);
CALL sp_actualizarstocklibro('978-0-135', 50, 5);
CALL sp_actualizarstocklibro('978-0-136', 50, 5);
CALL sp_actualizarstocklibro('978-0-137', 50, 5);
CALL sp_actualizarstocklibro('978-0-138', 50, 5);
CALL sp_actualizarstocklibro('978-0-139', 50, 5);
CALL sp_actualizarstocklibro('978-0-140', 50, 5);
CALL sp_actualizarstocklibro('978-0-141', 50, 5);
CALL sp_actualizarstocklibro('978-0-142', 50, 5);
CALL sp_actualizarstocklibro('978-0-143', 50, 5);
CALL sp_actualizarstocklibro('978-0-144', 50, 5);
CALL sp_actualizarstocklibro('978-0-145', 50, 5);
CALL sp_actualizarstocklibro('978-0-146', 50, 5);
CALL sp_actualizarstocklibro('978-0-147', 50, 5);
CALL sp_actualizarstocklibro('978-0-148', 50, 5);
CALL sp_actualizarstocklibro('978-0-149', 50, 5);
CALL sp_actualizarstocklibro('978-0-150', 50, 5);
CALL sp_actualizarstocklibro('978-0-151', 50, 5);
CALL sp_actualizarstocklibro('978-0-152', 50, 5);
CALL sp_actualizarstocklibro('978-0-153', 50, 5);
CALL sp_actualizarstocklibro('978-0-154', 50, 5);
 
 
-- corrección US-3.4: sincronizar procedimientos de libros con stock_actual, stock_minimo, activo, y con lo que realmente llama LibrosDAOImpl
drop procedure if exists sp_listarlibros;
drop procedure if exists sp_insertarlibro;
drop procedure if exists sp_actualizarlibro;
drop procedure if exists sp_buscarlibropotitulo;
drop procedure if exists sp_desactivarlibro;
drop procedure if exists sp_activarlibro;
drop procedure if exists sp_buscarlibroportitulo;
delimiter $$
 
create procedure sp_listarlibros()
begin
    select isbn, titulo, fecha_publicacion, precio, id_categoria, nit_editorial,
           stock_actual, stock_minimo, activo
    from libros;
end $$
 
create procedure sp_insertarlibro(
    in _isbn varchar(20),
    in _titulo varchar(100),
    in _fecha_publicacion date,
    in _precio decimal(8,2),
    in _id_categoria int,
    in _nit_editorial varchar(20),
    in _stock_actual int,
    in _stock_minimo int
)
begin
    insert into libros(isbn, titulo, fecha_publicacion, precio, id_categoria, nit_editorial,
                        stock_actual, stock_minimo)
    values (_isbn, _titulo, _fecha_publicacion, _precio, _id_categoria, _nit_editorial,
            _stock_actual, _stock_minimo);
end $$
 
create procedure sp_actualizarlibro(
    in _isbn varchar(20),
    in _titulo varchar(100),
    in _fecha_publicacion date,
    in _precio decimal(8,2),
    in _id_categoria int,
    in _nit_editorial varchar(20),
    in _stock_minimo int
)
begin
    update libros
    set titulo = _titulo,
        fecha_publicacion = _fecha_publicacion,
        precio = _precio,
        id_categoria = _id_categoria,
        nit_editorial = _nit_editorial,
        stock_minimo = _stock_minimo
    where isbn = _isbn;
end $$
 
create procedure sp_buscarlibroportitulo(
    in _titulo varchar(150)
)
begin
    select isbn, titulo, fecha_publicacion, precio, id_categoria, nit_editorial,
           stock_actual, stock_minimo, activo
    from libros
    where titulo like concat('%', _titulo, '%')
      and activo = true;
end $$
 
create procedure sp_desactivarlibro(
    in _isbn varchar(20)
)
begin
    update libros
    set activo = false
    where isbn = _isbn;
end $$
 
create procedure sp_activarlibro(
    in _isbn varchar(20)
)
begin
    update libros
    set activo = true
    where isbn = _isbn;
end $$
 
delimiter ;
 
 
-- datos de ejemplo: proveedores (necesarios para probar Salida de Inventario
delete from proveedores where nit_proveedor in ('P001-A', 'P002-B', 'P003-C');
 
CALL sp_insertarproveedor('P001-A', 'Distribuidora Central', '22551001', 'Zona 4, Ciudad');
CALL sp_insertarproveedor('P002-B', 'Suministros del Libro S.A.', '22551002', 'Zona 9, Ciudad');
CALL sp_insertarproveedor('P003-C', 'Importadora Literaria', '22551003', 'Zona 1, Ciudad');
drop procedure if exists sp_listarsalidas;
delimiter $$
create procedure sp_listarsalidas()
begin
    select id_movimiento, isbn, tipo_movimiento, cantidad, fecha_movimiento,
           id_usuario, observacion, nit_proveedor
    from movimientos_inventario
    where tipo_movimiento in ('MERMA','TRASLADO','DEVOLUCION')
    order by fecha_movimiento desc;
end $$
delimiter ;
 
-- Sprint 4: Categorías 
drop procedure if exists sp_insertarcategoria;
drop procedure if exists sp_listarcategorias;
drop procedure if exists sp_actualizarcategoria;
delimiter $$
 
create procedure sp_insertarcategoria(
    in _nombre_categoria varchar(100)
)
begin
    insert into categorias(nombre_categoria)
    values (_nombre_categoria);
end $$
 
create procedure sp_listarcategorias()
begin
    select id_categoria, nombre_categoria
    from categorias
    order by nombre_categoria;
end $$
 
create procedure sp_actualizarcategoria(
    in _id_categoria int,
    in _nombre_categoria varchar(100)
)
begin
    update categorias
    set nombre_categoria = _nombre_categoria
    where id_categoria = _id_categoria;
end $$
 
delimiter ;
 
-- Sprint 4: Dashboard Administrativo 
drop procedure if exists sp_totalventas;
drop procedure if exists sp_totallibrosactivos;
drop procedure if exists sp_totalusuariosactivos;
delimiter $$
 
create procedure sp_totalventas()
begin
    select coalesce(sum(total), 0) as total_ventas
    from ventas
    where estado = 'COMPLETADA';
end $$
 
create procedure sp_totallibrosactivos()
begin
    select count(*) as total_libros
    from libros
    where activo = true;
end $$
 
create procedure sp_totalusuariosactivos()
begin
    select count(*) as total_usuarios
    from usuarios
    where activo = true;
end $$
 
delimiter ;
 
 
-- Sprint 4: Reportes de Ventas 
drop procedure if exists sp_reporteventasdiario;
drop procedure if exists sp_reporteventassemanal;
drop procedure if exists sp_reporteventasmensual;
delimiter $$
 
create procedure sp_reporteventasdiario(
    in _fecha date
)
begin
    select id_venta, fecha_venta, subtotal, descuento, total, estado, id_usuario
    from ventas
    where date(fecha_venta) = _fecha
      and estado = 'COMPLETADA'
    order by fecha_venta;
end $$
 
create procedure sp_reporteventassemanal(
    in _fecha date
)
begin
    select id_venta, fecha_venta, subtotal, descuento, total, estado, id_usuario
    from ventas
    where yearweek(fecha_venta, 1) = yearweek(_fecha, 1)
      and estado = 'COMPLETADA'
    order by fecha_venta;
end $$
 
create procedure sp_reporteventasmensual(
    in _anio int,
    in _mes int
)
begin
    select id_venta, fecha_venta, subtotal, descuento, total, estado, id_usuario
    from ventas
    where year(fecha_venta) = _anio
      and month(fecha_venta) = _mes
      and estado = 'COMPLETADA'
    order by fecha_venta;
end $$
 
delimiter ;
 
 
-- Sprint 4
 
drop procedure if exists sp_libromasvendidos;
drop procedure if exists sp_stockvalorizado;
delimiter $$
 
create procedure sp_libromasvendidos(
    in _limite int
)
begin
    select l.isbn, l.titulo, sum(dv.cantidad) as unidades_vendidas
    from detalle_venta dv
    inner join libros l on dv.isbn = l.isbn
    inner join ventas v on dv.id_venta = v.id_venta
    where v.estado = 'COMPLETADA'
    group by l.isbn, l.titulo
    order by unidades_vendidas desc
    limit _limite;
end $$
 
create procedure sp_stockvalorizado()
begin
    select isbn, titulo, stock_actual, precio,
           (stock_actual * precio) as valor_inventario
    from libros
    where activo = true
    order by valor_inventario desc;
end $$

delimiter ;

DROP PROCEDURE IF EXISTS sp_insertareditorial;
DROP PROCEDURE IF EXISTS sp_listareditoriales;
DROP PROCEDURE IF EXISTS sp_buscareditorial;
DROP PROCEDURE IF EXISTS sp_actualizareditorial;

DELIMITER //

CREATE PROCEDURE sp_insertareditorial(
    IN _nit varchar(20),
    IN _nombre_editorial varchar(100),
    IN _telefono_editorial varchar(15),
    IN _direccion_editorial varchar(100)
)
BEGIN
    INSERT INTO editoriales(nit, nombre_editorial, telefono_editorial, direccion_editorial)
    VALUES (_nit, _nombre_editorial, _telefono_editorial, _direccion_editorial);
END //

CREATE PROCEDURE sp_listareditoriales()
BEGIN
    SELECT nit, nombre_editorial, telefono_editorial, direccion_editorial FROM editoriales;
END //

CREATE PROCEDURE sp_buscareditorial(
    IN _nit varchar(20)
)
BEGIN
    SELECT nit, nombre_editorial, telefono_editorial, direccion_editorial
    FROM editoriales
    WHERE nit = _nit;
END //

CREATE PROCEDURE sp_actualizareditorial(
    IN _nit varchar(20),
    IN _nombre_editorial varchar(100),
    IN _telefono_editorial varchar(15),
    IN _direccion_editorial varchar(100)
)
BEGIN
    UPDATE editoriales
    SET nombre_editorial = _nombre_editorial,
        telefono_editorial = _telefono_editorial,
        direccion_editorial = _direccion_editorial
    WHERE nit = _nit;
END //

DELIMITER ;

set @sql_col = (
    select if(count(*) > 0,
        'alter table editoriales change column direccion_editoria direccion_editorial varchar(100)',
        'do 0')
    from information_schema.columns
    where table_schema = database()
      and table_name   = 'editoriales'
      and column_name  = 'direccion_editoria'
);
prepare stmt_col from @sql_col;
execute stmt_col;
deallocate prepare stmt_col;


drop procedure if exists sp_eliminareditorial;

delimiter $$

create procedure sp_eliminareditorial(
    in _nit varchar(20)
)
begin
    delete from editoriales where nit = _nit;
end $$

delimiter ;


create or replace view vw_lista_editoriales as
select
    nit                 as 'nit editorial',
    nombre_editorial    as 'editorial',
    telefono_editorial  as 'teléfono',
    direccion_editorial as 'dirección'
from editoriales;

-- Corrección pedida por el Product Owner: agregar nombre, apellido y correo a la tabla usuarios para el módulo de Gestión de Usuarios

-- 1. Agregar columna 'nombre' si no existe
SET @existe_nombre = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name   = 'usuarios'
      AND column_name  = 'nombre'
);
SET @sql_nombre = IF(@existe_nombre = 0,
    'ALTER TABLE usuarios ADD COLUMN nombre varchar(100) NULL AFTER username',
    'DO 0');
PREPARE stmt FROM @sql_nombre;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Agregar columna 'apellido' si no existe
SET @existe_apellido = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name   = 'usuarios'
      AND column_name  = 'apellido'
);
SET @sql_apellido = IF(@existe_apellido = 0,
    'ALTER TABLE usuarios ADD COLUMN apellido varchar(100) NULL AFTER nombre',
    'DO 0');
PREPARE stmt FROM @sql_apellido;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. Agregar columna 'correo' si no existe
SET @existe_correo = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name   = 'usuarios'
      AND column_name  = 'correo'
);
SET @sql_correo = IF(@existe_correo = 0,
    'ALTER TABLE usuarios ADD COLUMN correo varchar(100) NULL AFTER apellido',
    'DO 0');
PREPARE stmt FROM @sql_correo;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. Reemplazar sp_registrar_usuario para que acepte los 3 campos nuevos
DROP PROCEDURE IF EXISTS sp_registrar_usuario;
DELIMITER //
CREATE PROCEDURE sp_registrar_usuario(
    IN _username     varchar(50),
    IN _password_hash varchar(255),
    IN _rol          varchar(20),
    IN _nombre       varchar(100),
    IN _apellido     varchar(100),
    IN _correo       varchar(100)
)
BEGIN
    INSERT INTO usuarios (username, password_hash, rol, nombre, apellido, correo)
    VALUES (_username, _password_hash, _rol, _nombre, _apellido, _correo);
END //
DELIMITER ;

-- 5. Reemplazar sp_listar_usuarios para incluir los campos nuevos
DROP PROCEDURE IF EXISTS sp_listar_usuarios;
DELIMITER //
CREATE PROCEDURE sp_listar_usuarios()
BEGIN
    SELECT id, username, nombre, apellido, correo, rol, activo, fecha_creacion
    FROM usuarios
    ORDER BY username;
END //
DELIMITER ;

-- 6. Reemplazar sp_buscar_usuario_por_id para incluir los campos nuevos
DROP PROCEDURE IF EXISTS sp_buscar_usuario_por_id;
DELIMITER //
CREATE PROCEDURE sp_buscar_usuario_por_id(
    IN _id int
)
BEGIN
    SELECT id, username, nombre, apellido, correo, rol, activo, fecha_creacion
    FROM usuarios
    WHERE id = _id;
END //
DELIMITER ;

-- 7. Reemplazar sp_actualizar_usuario para que también guarde nombre, apellido y correo
DROP PROCEDURE IF EXISTS sp_actualizar_usuario;
DELIMITER //
CREATE PROCEDURE sp_actualizar_usuario(
    IN _id int,
    IN _rol varchar(20),
    IN _nombre varchar(100),
    IN _apellido varchar(100),
    IN _correo varchar(100)
)
BEGIN
    UPDATE usuarios
    SET rol = _rol,
        nombre = _nombre,
        apellido = _apellido,
        correo = _correo
    WHERE id = _id;
END //
DELIMITER ;

-- Sprint 4: Dashboard Administrativo - ventas del día (solo COMPLETADAS, de todos los usuarios)
drop procedure if exists sp_totalventasdia;
delimiter $$

create procedure sp_totalventasdia()
begin
    select coalesce(sum(total), 0) as total_ventas
    from ventas
    where estado = 'COMPLETADA'
      and date(fecha_venta) = curdate();
end $$

delimiter ;