-- drop database if exists libreriadb_in4cm;
create database if not exists libreriadb_in4cm;
use libreriadb_in4cm;

create table categorias(
	id_categoria int primary key auto_increment,
    nombre_categoria varchar(100)
);


-- editoriales
create table editoriales (
	nit varchar(20) primary key,
    nombre_editorial varchar(100) not null,
    telefono_editorial varchar(15),
    direccion_editoria varchar(100)    
);
 
-- autores
create table autores(
	id_autor int primary key auto_increment,
    nombre_autor varchar(100) not null,
    apellido_autor varchar(100) not null,
    nacionalidad varchar(100),
    biografia text
);
 
-- cliente
create table clientes(
	cui bigint primary key,
    nombre_cliente varchar(100),
    apellido_cliente varchar(100),
    correo_electronico varchar(100)
);
 
-- libros
create table libros(
	isbn varchar(20) primary key,
    titulo varchar(100) not null,
    fecha_publicacion date,
    precio decimal(8,2) not null,
    id_categoria int,
    nit_editorial varchar(20)
);
 
-- autores_libro
create table autores_libro(
	id_autor_libro int auto_increment primary key,
    id_autor int,
    isbn varchar(20)
);
 
-- compras
create table compras(
	no_compra int primary key auto_increment,
    fecha_compra timestamp default current_timestamp,
    total_compra decimal(8,2),
    cui_cliente bigint
);
-- detalle_compra
create table detalle_compra(
	id_detalle_compra int primary key auto_increment,
    no_compra int,
    isbn varchar(20)
);
-- agregamos la relaciones a la tablas, entidades.
alter table autores_libro
add constraint fk_a_autor foreign key (id_autor) references autores(id_autor) on delete cascade,
add constraint fk_a_libro foreign key (isbn) references libros(isbn) on delete cascade;
 
alter table compras
add constraint fk_a_cliente foreign key (cui_cliente) references clientes (cui) on delete cascade;

 
alter table detalle_compra
add constraint fk_a_compra foreign key (no_compra) references compras(no_compra) on delete cascade,
add constraint fk_a_libros foreign key (isbn) references libros(isbn) on delete cascade;
 
alter table libros
add constraint fk_a_categorias foreign key (id_categoria) references categorias(id_categoria) on delete cascade,
add constraint fk_a_editoriales foreign key (nit_editorial) references editoriales(nit) on delete cascade;

use libreriadb_in4cm;

-- =============================================================================
-- 1. crud: categorias
-- =============================================================================
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
    select id_categoria, nombre_categoria from categorias;
end $$

create procedure sp_buscarcategoria(
    in _id_categoria int
)
begin
    select id_categoria, nombre_categoria 
    from categorias 
    where id_categoria = _id_categoria;
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

create procedure sp_eliminarcategoria(
    in _id_categoria int
)
begin
    delete from categorias where id_categoria = _id_categoria;
end $$

delimiter ;

-- =============================================================================
-- 2. crud: editoriales
-- =============================================================================
delimiter $$

create procedure sp_insertareditorial(
    in _nit varchar(20),
    in _nombre_editorial varchar(100),
    in _telefono_editorial varchar(15),
    in _direccion_editoria varchar(100)
)
begin
    insert into editoriales(nit, nombre_editorial, telefono_editorial, direccion_editoria) 
    values (_nit, _nombre_editorial, _telefono_editorial, _direccion_editoria);
end $$

create procedure sp_listareditoriales()
begin
    select nit, nombre_editorial, telefono_editorial, direccion_editoria from editoriales;
end $$

create procedure sp_buscareditorial(
    in _nit varchar(20)
)
begin
    select nit, nombre_editorial, telefono_editorial, direccion_editoria 
    from editoriales 
    where nit = _nit;
end $$

create procedure sp_actualizareditorial(
    in _nit varchar(20),
    in _nombre_editorial varchar(100),
    in _telefono_editorial varchar(15),
    in _direccion_editoria varchar(100)
)
begin
    update editoriales 
    set nombre_editorial = _nombre_editorial, 
        telefono_editorial = _telefono_editorial, 
        direccion_editoria = _direccion_editoria 
    where nit = _nit;
end $$

create procedure sp_eliminareditorial(
    in _nit varchar(20)
)
begin
    delete from editoriales where nit = _nit;
end $$

delimiter ;

-- =============================================================================
-- 3. crud: autores
-- =============================================================================
delimiter $$

create procedure sp_insertarautor(
    in _nombre_autor varchar(100),
    in _apellido_autor varchar(100),
    in _nacionalidad varchar(100),
    in _biografia text
)
begin
    insert into autores(nombre_autor, apellido_autor, nacionalidad, biografia) 
    values (_nombre_autor, _apellido_autor, _nacionalidad, _biografia);
end $$

create procedure sp_listarautores()
begin
    select id_autor, nombre_autor, apellido_autor, nacionalidad, biografia from autores;
end $$

create procedure sp_buscarautor(
    in _id_autor int
)
begin
    select id_autor, nombre_autor, apellido_autor, nacionalidad, biografia 
    from autores 
    where id_autor = _id_autor;
end $$

create procedure sp_actualizarautor(
    in _id_autor int,
    in _nombre_autor varchar(100),
    in _apellido_autor varchar(100),
    in _nacionalidad varchar(100),
    in _biografia text
)
begin
    update autores 
    set nombre_autor = _nombre_autor, 
        apellido_autor = _apellido_autor, 
        nacionalidad = _nacionalidad, 
        biografia = _biografia 
    where id_autor = _id_autor;
end $$

create procedure sp_eliminarautor(
    in _id_autor int
)
begin
    delete from autores where id_autor = _id_autor;
end $$

delimiter ;

-- =============================================================================
-- 4. crud: clientes
-- =============================================================================
delimiter $$

create procedure sp_insertarcliente(
    in _cui bigint,
    in _nombre_cliente varchar(100),
    in _apellido_cliente varchar(100),
    in _correo_electronico varchar(100)
)
begin
    insert into clientes(cui, nombre_cliente, apellido_cliente, correo_electronico) 
    values (_cui, _nombre_cliente, _apellido_cliente, _correo_electronico);
end $$

create procedure sp_listarclientes()
begin
    select cui, nombre_cliente, apellido_cliente, correo_electronico from clientes;
end $$

create procedure sp_buscarcliente(
    in _cui bigint
)
begin
    select cui, nombre_cliente, apellido_cliente, correo_electronico 
    from clientes 
    where cui = _cui;
end $$

create procedure sp_actualizarcliente(
    in _cui bigint,
    in _nombre_cliente varchar(100),
    in _apellido_cliente varchar(100),
    in _correo_electronico varchar(100)
)
begin
    update clientes 
    set nombre_cliente = _nombre_cliente, 
        apellido_cliente = _apellido_cliente, 
        correo_electronico = _correo_electronico 
    where cui = _cui;
end $$

create procedure sp_eliminarcliente(
    in _cui bigint
)
begin
    delete from clientes where cui = _cui;
end $$

delimiter ;

-- =============================================================================
-- 5. crud: libros
-- =============================================================================
delimiter $$

create procedure sp_insertarlibro(
    in _isbn varchar(20),
    in _titulo varchar(100),
    in _fecha_publicacion date,
    in _precio decimal(8,2),
    in _id_categoria int,
    in _nit_editorial varchar(20)
)
begin
    insert into libros(isbn, titulo, fecha_publicacion, precio, id_categoria, nit_editorial) 
    values (_isbn, _titulo, _fecha_publicacion, _precio, _id_categoria, _nit_editorial);
end $$

create procedure sp_listarlibros()
begin
    select isbn, titulo, fecha_publicacion, precio, id_categoria, nit_editorial from libros;
end $$

create procedure sp_buscarlibro(
    in _isbn varchar(20)
)
begin
    select isbn, titulo, fecha_publicacion, precio, id_categoria, nit_editorial 
    from libros 
    where isbn = _isbn;
end $$

create procedure sp_actualizarlibro(
    in _isbn varchar(20),
    in _titulo varchar(100),
    in _fecha_publicacion date,
    in _precio decimal(8,2),
    in _id_categoria int,
    in _nit_editorial varchar(20)
)
begin
    update libros 
    set titulo = _titulo, 
        fecha_publicacion = _fecha_publicacion, 
        precio = _precio, 
        id_categoria = _id_categoria, 
        nit_editorial = _nit_editorial 
    where isbn = _isbn;
end $$

create procedure sp_eliminarlibro(
    in _isbn varchar(20)
)
begin
    delete from libros where isbn = _isbn;
end $$

delimiter ;

-- =============================================================================
-- 6. crud: autores_libro (tabla intermedia)
-- =============================================================================
delimiter $$

create procedure sp_insertarautorlibro(
    in _id_autor int,
    in _isbn varchar(20)
)
begin
    insert into autores_libro(id_autor, isbn) 
    values (_id_autor, _isbn);
end $$

create procedure sp_listarautoreslibro()
begin
    select id_autor_libro, id_autor, isbn from autores_libro;
end $$

create procedure sp_buscarautorlibro(
    in _id_autor_libro int
)
begin
    select id_autor_libro, id_autor, isbn 
    from autores_libro 
    where id_autor_libro = _id_autor_libro;
end $$

create procedure sp_actualizarautorlibro(
    in _id_autor_libro int,
    in _id_autor int,
    in _isbn varchar(20)
)
begin
    update autores_libro 
    set id_autor = _id_autor, 
        isbn = _isbn 
    where id_autor_libro = _id_autor_libro;
end $$

create procedure sp_eliminarautorlibro(
    in _id_autor_libro int
)
begin
    delete from autores_libro where id_autor_libro = _id_autor_libro;
end $$

delimiter ;

-- =============================================================================
-- 7. crud: compras
-- =============================================================================
delimiter $$

create procedure sp_insertarcompra(
    in _total_compra decimal(8,2),
    in _cui_cliente bigint
)
begin
    -- se omite fecha_compra para que tome el current_timestamp por defecto
    insert into compras(total_compra, cui_cliente) 
    values (_total_compra, _cui_cliente);
end $$

create procedure sp_listarcompras()
begin
    select no_compra, fecha_compra, total_compra, cui_cliente from compras;
end $$

create procedure sp_buscarcompra(
    in _no_compra int
)
begin
    select no_compra, fecha_compra, total_compra, cui_cliente 
    from compras 
    where no_compra = _no_compra;
end $$

create procedure sp_actualizarcompra(
    in _no_compra int,
    in _total_compra decimal(8,2),
    in _cui_cliente bigint
)
begin
    update compras 
    set total_compra = _total_compra, 
        cui_cliente = _cui_cliente 
    where no_compra = _no_compra;
end $$

create procedure sp_eliminarcompra(
    in _no_compra int
)
begin
    delete from compras where no_compra = _no_compra;
end $$

delimiter ;

-- =============================================================================
-- 8. crud: detalle_compra
-- =============================================================================
delimiter $$

create procedure sp_insertardetallecompra(
    in _no_compra int,
    in _isbn varchar(20)
)
begin
    insert into detalle_compra(no_compra, isbn) 
    values (_no_compra, _isbn);
end $$

create procedure sp_listardetallecompra()
begin
    select id_detalle_compra, no_compra, isbn from detalle_compra;
end $$

create procedure sp_buscardetallecompra(
    in _id_detalle_compra int
)
begin
    select id_detalle_compra, no_compra, isbn 
    from detalle_compra 
    where id_detalle_compra = _id_detalle_compra;
end $$

create procedure sp_actualizardetallecompra(
    in _id_detalle_compra int,
    in _no_compra int,
    in _isbn varchar(20)
)
begin
    update detalle_compra 
    set no_compra = _no_compra, 
        isbn = _isbn 
    where id_detalle_compra = _id_detalle_compra;
end $$

create procedure sp_eliminardetallecompra(
    in _id_detalle_compra int
)
begin
    delete from detalle_compra where id_detalle_compra = _id_detalle_compra;
end $$

delimiter ;


use libreriadb_in4cm;

-- =============================================================================
-- 1. vistas de listado simple (tablas sin llaves foráneas)
-- =============================================================================

-- vista para listar categorías
create or replace view vw_lista_categorias as
select 
    id_categoria as 'id categoría',
    nombre_categoria as 'categoría'
from categorias;

-- vista para listar editoriales
create or replace view vw_lista_editoriales as
select 
    nit as 'nit editorial',
    nombre_editorial as 'editorial',
    telefono_editorial as 'teléfono',
    direccion_editoria as 'dirección'
from editoriales;

-- vista para listar autores
create or replace view vw_lista_autores as
select 
    id_autor as 'id autor',
    concat(nombre_autor, ' ', apellido_autor) as 'autor',
    nacionalidad as 'nacionalidad',
    biografia as 'biografía'
from autores;

-- vista para listar clientes
create or replace view vw_lista_clientes as
select 
    cui as 'cui cliente',
    concat(nombre_cliente, ' ', apellido_cliente) as 'cliente',
    correo_electronico as 'correo electrónico'
from clientes;


-- =============================================================================
-- 2. vistas con joins (tablas con llaves foráneas)
-- =============================================================================

-- vista para listar libros (une con categorías y editoriales)
create or replace view vw_lista_libros as
select 
    l.isbn as 'isbn',
    l.titulo as 'título',
    l.fecha_publicacion as 'fecha de publicación',
    l.precio as 'precio',
    c.nombre_categoria as 'categoría',
    e.nombre_editorial as 'editorial'
from libros l
inner join categorias c on l.id_categoria = c.id_categoria
inner join editoriales e on l.nit_editorial = e.nit;

-- vista para listar la relación autores-libro 
create or replace view vw_lista_autores_libro as
select 
    al.id_autor_libro as 'id relación',
    concat(a.nombre_autor, ' ', a.apellido_autor) as 'autor',
    l.titulo as 'título del libro',
    l.isbn as 'isbn'
from autores_libro al
inner join autores a on al.id_autor = a.id_autor
inner join libros l on al.isbn = l.isbn;

-- vista para listar compras (une con clientes)
create or replace view vw_lista_compras as
select 
    co.no_compra as 'no. compra',
    co.fecha_compra as 'fecha/hora',
    co.total_compra as 'total',
    co.cui_cliente as 'cui cliente',
    concat(cl.nombre_cliente, ' ', cl.apellido_cliente) as 'cliente'
from compras co
inner join clientes cl on co.cui_cliente = cl.cui;

-- vista para listar el detalle de las compras (une con libros)
create or replace view vw_lista_detalle_compra as
select 
    dc.id_detalle_compra as 'id detalle',
    dc.no_compra as 'no. compra',
    l.titulo as 'libro',
    l.isbn as 'isbn',
    l.precio as 'precio unitario'
from detalle_compra dc
inner join libros l on dc.isbn = l.isbn;


-- =============================================================================
-- 3. vista para generar la factura de compras
-- =============================================================================
-- esta vista unifica los datos del cliente, el encabezado de la compra y el 
-- desglose de los libros adquiridos en una sola estructura estilo factura.

create or replace view vw_factura_compras as
select 
    co.no_compra as 'numero_factura',
    co.fecha_compra as 'fecha_emision',
    cl.cui as 'cui_cliente',
    concat(cl.nombre_cliente, ' ', cl.apellido_cliente) as 'nombre_cliente',
    cl.correo_electronico as 'correo_cliente',
    l.isbn as 'isbn_libro',
    l.titulo as 'descripcion_libro',
    l.precio as 'precio_articulo',
    co.total_compra as 'gran_total'
from compras co
inner join clientes cl on co.cui_cliente = cl.cui
inner join detalle_compra dc on co.no_compra = dc.no_compra
inner join libros l on dc.isbn = l.isbn;

