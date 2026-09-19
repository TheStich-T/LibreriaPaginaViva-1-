# LibreriaPaginaViva — Sistema de Gestión de Librería

Aplicación de escritorio construida con **JavaFX** y **MySQL** para la gestión de la librería "Página Viva": usuarios, libros, autores, categorías, editoriales, proveedores, ventas, facturación, inventario y reportes.

> **Propósito educativo**
>
> Este proyecto fue construido a nivel **educativo** para demostrar los alcances y aptitudes adquiridas por **estudiantes de informática** en el curso de **Programación 1**. Sirve como evidencia de aprendizaje sobre programación orientada a objetos, patrones de diseño, persistencia de datos, construcción de interfaces gráficas de escritorio y trabajo colaborativo con metodología Scrum.

---

## Contenido

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Requisitos](#requisitos)
- [Configuración de la base de datos](#configuración-de-la-base-de-datos)
- [Ejecución](#ejecución)
- [Roles y permisos](#roles-y-permisos)
- [Metodología y flujo de ramas](#metodología-y-flujo-de-ramas)
- [Créditos](#créditos)

---

## Características

- **Autenticación:** inicio de sesión con contraseña cifrada en SHA-256 y cambio de contraseña desde el sistema.
- **Gestión de sesión:** usuario actual disponible durante toda la ejecución mediante `SessionContext`; cierre de sesión desde cada panel.
- **Dashboards por rol:** cada rol (administrador, bodega y cajero) tiene su propio panel con menú de navegación y accesos rápidos. El panel de administración muestra indicadores como libros activos, usuarios activos y ventas de hoy.
- **Gestión de usuarios:** registro con nombre, apellido, correo, usuario y rol; edición, activación y desactivación de usuarios.
- **Catálogo:** CRUD de libros, autores, categorías, editoriales y proveedores; búsqueda de libros por ISBN, título o autor; actualización de precios.
- **Módulo de ventas:** nueva venta, detalle de venta por líneas de artículo, lista de ventas, factura, aplicación de descuentos con autorización y anulación de ventas.
- **Inventario:** registro de ingresos y salidas, consulta de stock actual y alerta de libros con stock crítico.
- **Reportes:** ventas diarias, semanales y mensuales; libros más vendidos; reporte de inventario y stock valorizado.
- **Validaciones:** mensajes de error y alertas uniformes mediante `ValidarException`.
- **Persistencia:** base de datos MySQL accedida mediante procedimientos almacenados (SP) y vistas.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Interfaz gráfica | JavaFX 21 con vistas FXML y hojas de estilo CSS (diseño con Scene Builder) |
| Base de datos | MySQL 8 (procedimientos almacenados y vistas) |
| Conector | MySQL Connector/J 8 (`com.mysql.cj.jdbc.Driver`) |
| IDE | Apache NetBeans (proyecto Ant) |
| Gestión del proyecto | Scrum, Trello y Git/GitHub |

## Arquitectura

El proyecto sigue una arquitectura por capas que separa responsabilidades:

```
┌──────────────────────────────────────────────┐
│  Vista (FXML + CSS + Controladores)          │
│  src/org/lpv/view · src/org/lpv/controller   │
├──────────────────────────────────────────────┤
│  Lógica / Gestión de sesión y permisos       │
│  src/org/lpv/system · src/org/lpv/manager    │
├──────────────────────────────────────────────┤
│  Acceso a datos (DAO)                        │
│  src/org/lpv/dao · src/org/lpv/dao/impl      │
├──────────────────────────────────────────────┤
│  Modelo de entidades                         │
│  src/org/lpv/model                           │
├──────────────────────────────────────────────┤
│  Base de datos MySQL (tablas, vistas y SPs)  │
└──────────────────────────────────────────────┘
```

- **Modelo:** clases de entidad (`Usuario`, `Libros`, `Autor`, `Venta`, `detalleVenta`, `Proveedor`, `MovimientoInventario`, etc.).
- **DAO:** interfaces (`CRUD`, `UsuarioDAO`, `LibrosDAO`, `VentaDAO`, …) e implementaciones en `dao/impl` que invocan los procedimientos almacenados.
- **Controladores:** lógica de las vistas JavaFX (eventos, tablas, validaciones y navegación).
- **Singleton:** `Conexion` (conexión a la BD) y `SessionContext` (usuario autenticado).
- **Permisos:** `RolPermisos` define qué módulos puede usar cada rol y a qué dashboard redirige el login.

## Estructura del proyecto

```
LibreriaPaginaViva/
├── Scripts SQL/
│   ├── libreriadb_script_ddl.sql      # Base de datos, tablas, SPs y vistas iniciales
│   ├── libreriadb_script_dml.sql      # Datos de ejemplo
│   ├── libreriadb_script_login.sql    # Tabla de usuarios y SPs de sesión
│   └── libreriadb_script_UpDate.sql   # Actualizaciones: ventas, inventario, reportes, usuarios
├── src/
│   ├── db.properties                  # Credenciales locales (NO versionado)
│   └── org/lpv/
│       ├── controller/                # Controladores JavaFX de cada vista
│       ├── dao/                       # Interfaces de acceso a datos
│       │   └── impl/                  # Implementaciones con MySQL
│       ├── exception/                 # ValidarException (validaciones centralizadas)
│       ├── manager/                   # SessionContext y RolPermisos
│       ├── model/                     # Entidades del dominio
│       ├── system/                    # main (punto de entrada y navegación)
│       ├── util/                      # Conexion, SecurityUtil (hash SHA-256)
│       └── view/                      # Vistas FXML
│           └── style/                 # Hojas de estilo CSS
├── nbproject/                         # Configuración de NetBeans
├── db.properties.example              # Plantilla de credenciales (versionada)
├── build.xml                          # Script Ant
└── manifest.mf
```

## Requisitos

- **JDK 21** (o superior compatible con JavaFX 21).
- **JavaFX SDK 21** configurado en NetBeans como librería `JavaFX21`.
- **MySQL 8** y MySQL Workbench (recomendado para ejecutar los scripts).
- **Connector/J 8** (`mysql-connector-j`) agregado al proyecto como librería `MySQL_Connector_8`.
- **Apache NetBeans** como IDE.

## Configuración de la base de datos

### 1. Crear la base de datos

Ejecuta los scripts de la carpeta `Scripts SQL/` en MySQL Workbench **en este orden**:

1. `libreriadb_script_ddl.sql`
2. `libreriadb_script_dml.sql`
3. `libreriadb_script_login.sql`
4. `libreriadb_script_UpDate.sql`

Esto crea la base de datos `libreriadb_in4cm` con sus tablas, procedimientos almacenados, vistas, datos de ejemplo y usuarios de prueba.

### 2. Configurar la conexión

La conexión se lee desde `src/db.properties` (recurso copiado al classpath en la compilación):

```properties
db.url=jdbc:mysql://localhost:3306/libreriadb_in4cm?serverTimezone=America/Guatemala
db.user=TU_USUARIO
db.password=TU_CONTRASEÑA
```

Para configurar tu entorno:

1. Copia `db.properties.example` como `src/db.properties`.
2. Ajusta los valores (URL, usuario y contraseña de tu MySQL).
3. Recompila para que el archivo se copie a `build/classes`.

> `src/db.properties` está en `.gitignore` y no se versiona: **no subas credenciales reales al repositorio**. Si el archivo falta o está incompleto, `Conexion` lanza un error claro al arrancar.

### Usuarios de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin1` | `admin123` | admin |
| `cajero1` | `cajero123` | cajero |
| `bodega1` | `bodega123` | bodega |

> Estos usuarios son solo para desarrollo y pruebas.

## Ejecución

Desde Apache NetBeans:

1. Abrir el proyecto (`File > Open Project`).
2. Verificar que las librerías `JavaFX21` y `MySQL_Connector_8` estén en el classpath.
3. Verificar que las opciones de ejecución apunten a tu JavaFX SDK (`Project Properties > Run > VM Options`):
```
   --module-path "C:\javafx-sdk-21.0.11\lib" --add-modules javafx.controls,javafx.fxml
```
   Ajusta la ruta a donde tengas instalado el SDK.
4. Configurar la base de datos y las credenciales.
5. Ejecutar la clase principal `org.lpv.system.main`.

## Roles y permisos

El sistema distingue tres roles:

| Rol | Acceso |
|---|---|
| `admin` | Panel de administración completo: usuarios, catálogo, ventas, inventario y reportes |
| `bodega` | Panel de bodega: consulta y gestión de libros, y consulta y gestión de stock (ingresos y salidas) |
| `cajero` | Panel de cajero: proceso de ventas (nueva venta, detalle, lista de ventas) y consulta de stock |

El login redirige a cada rol a su propio dashboard.

## Metodología y flujo de ramas

El proyecto se desarrolló con **Scrum** en 4 sprints, usando un tablero de Trello para el product backlog y el seguimiento de tareas:

[Tablero de Trello: Proyecto librería página viva](AQUI_VA_EL_ENLACE_DE_TRELLO)

- **`main`:** versión estable y entrega final.
- **`develop`:** rama de integración de las funcionalidades.
- **`ft/<historia-de-usuario>`:** una rama por cada Historia de Usuario del backlog (por ejemplo `ft/gestion-administrativa`), que se integra a `develop` al terminar.
- **Flujo de tarjetas:** To Do → In Progress → Code Review → Testing → Done.

## Créditos

Proyecto educativo desarrollado para el curso de Programación 1 (estudiantes de informática).

| Nombre | Usuario de GitHub |
|---|---|
| Diego Raguay | [RaguayDiego-02](https://github.com/RaguayDiego-02) |
| Levi Salazar | [levi-Salazar432](https://github.com/levi-Salazar432) |
| Antony Pérez | [TheStich-T](https://github.com/TheStich-T) |
| Javier Sian | [srrJavier](https://github.com/srrJavier) |
