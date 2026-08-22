use libreriadb_in4cm;

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
alter table ventas -- NO APROBADO 
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

-- 4.4 recrear las llaves foráneas con los nuevos nombres
alter table ventas
    add constraint fk_venta_cliente foreign key (cui_cliente) references clientes(cui) on delete cascade,
    add constraint fk_venta_usuario foreign key (id_usuario) references usuarios(id) on delete set null,
    add constraint fk_venta_autoriza foreign key (usuario_autoriza_descuento) references usuarios(id) on delete set null,
    add constraint fk_venta_anulacion foreign key (usuario_anulacion) references usuarios(id) on delete set null;

alter table detalle_venta
    add constraint fk_detalle_venta foreign key (id_venta) references ventas(id_venta) on delete cascade,
    add constraint fk_detalle_libro foreign key (isbn) references libros(isbn) on delete cascade;

-- proveedores
create table proveedores (
    nit_proveedor varchar(20) primary key,
    nombre_proveedor varchar(100) not null,
    telefono_proveedor varchar(15),
    direccion_proveedor varchar(100)
);

-- movimientos_inventario
create table movimientos_inventario (
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

-- procedimientos almacenados reemplazar los que apuntaban a compras y detalle_compra y agregar los nuevos que necesitan libros/proveedores/movimientos_inventario
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
delimiter $$

-- crud: ventas
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

-- crud: detalle_venta
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

-- stock de libros
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

-- crud: proveedores
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

-- crud: movimientos_inventario
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

-- se reemplaza para agregar las columnas de stock a la vista de libros
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

-- se reemplaza para usar el nombre de columna corregido (direccion_editorial)
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