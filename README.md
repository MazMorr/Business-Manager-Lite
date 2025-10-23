# Business Manager Lite

Software de gestión de inventarios para pequeñas y medianas empresas (ferreterías, cafeterías, etc.), desarrollado con Spring Boot y JavaFX.

## Descripción

BusinessManager es una aplicación de escritorio que permite gestionar productos, categorías, almacenes y transacciones de inventario. Utiliza Spring Boot para la lógica de negocio y persistencia, y JavaFX para la interfaz gráfica.

## Características principales

- Gestión de productos y categorías
- Gestión de almacenes
- Registro y consulta de transacciones (compras, ventas, movimientos)
- Exportación de datos a PDF y Excel
- Seguridad y autenticación
- Interfaz gráfica moderna con JavaFX
- Persistencia de datos con H2 y Spring Data JPA

## Requisitos

- Java 24
- Maven
- JavaFX 24.0.2
- Spring Boot 3.5.6

## Instalación

1. Clona el repositorio:

     ```bash
     git clone https://github.com/MazMorr/Storage-Software-SpringBoot-Javafx.git
     cd Storage-Software-SpringBoot-Javafx
     ```

2. Configura tu entorno para usar Java 24 y Maven.
3. Descarga y configura JavaFX 24.0.2 en tu entorno ([descargar JavaFX](https://gluonhq.com/products/javafx/)).
4. Construye el proyecto con Maven:

     ```bash
     mvn clean install
     ```

## Ejecución

Para ejecutar la aplicación:

```bash
mvn javafx:run
```

O ejecuta el JAR generado:

```bash
java -jar target/BusinessManager-1.0.1.jar
```

## Estructura del Proyecto

- `src/main/java/com/marcosoft/storageSoftware/`: Código fuente principal
  - `application/controller/`: Controladores JavaFX
    - `application/dto/`: Objetos de transferencia de datos
    - `application/mapper/`: Mapeadores entre entidades y DTOs
    - `domain/model/`: Modelos de datos
    - `domain/repository/`: Repositorios Spring Data JPA
    - `domain/service/`: Lógica de negocio
    - `infrastructure/config/`: Configuración de la aplicación
    - `infrastructure/security/`: Seguridad y autenticación
    - `infrastructure/service/`: Servicios de infraestructura
    - `infrastructure/util/`: Utilidades
- `src/main/resources/`: Recursos (archivos FXML, imágenes, estilos, propiedades)
- `src/test/java/com/marcosoft/storageSoftware/`: Pruebas unitarias
- `data/`: Base de datos H2 (`StorageDatabase.mv.db`)
- `target/`: Archivos generados y JAR ejecutable

## Licencia

Este proyecto está bajo la Licencia Apache 2.0. Consulta el archivo `license.txt` para más detalles.

## Contacto

Para preguntas o sugerencias: <marconchelo12@gmail.com>
