# Storage Software

Software para la gestión de inventarios en pequeñas empresas como ferreterías o cafeterías, desarrollado con Spring Boot y JavaFX.

## Descripción

Este proyecto es una aplicación de escritorio que permite a los usuarios gestionar productos, categorías y transacciones en un entorno de inventario. Utiliza Spring Boot para la lógica de negocio y la persistencia de datos, y JavaFX para la interfaz gráfica de usuario.

## Características

- Gestión de productos
- Gestión de categorías
- Registro de transacciones
- Interfaz gráfica de usuario con JavaFX
- Persistencia de datos con Spring Data JPA

## Requisitos

- Java 21
- Maven
- JavaFX 21.0.0
- Spring Boot 3.4.3

## Instalación

1. Clona el repositorio:

    ```bash
    git clone https://github.com/MazMorr/Storage-Software-SpringBoot-Javafx.git
    cd Storage-Software-SpringBoot-Javafx
    ```

2. Configura tu entorno de desarrollo para usar Java 21 y Maven.

3. Asegúrate de tener JavaFX configurado en tu entorno. Puedes descargar JavaFX desde [aquí](https://gluonhq.com/products/javafx/).

4. Construye el proyecto con Maven:

    ```bash
    mvn clean install
    ```

## Ejecución

Para ejecutar la aplicación, usa el siguiente comando:

```bash
mvn javafx:run
```
## Uso
Al iniciar la aplicación, verás la ventana principal con opciones para gestionar productos, categorías y transacciones.
Usa los menús y botones para navegar y realizar operaciones en el inventario.
Los cambios se guardarán automáticamente en la base de datos.
Estructura del Proyecto
src/main/java/com/marcosoft/storageSoftware: Contiene el código fuente de la aplicación.
controller: Controladores de JavaFX.
model: Clases de modelo de datos.
repository: Interfaces de repositorio de Spring Data JPA.
service: Clases de servicio que contienen la lógica de negocio.
src/main/resources: Contiene los archivos de recursos como archivos FXML y propiedades.
src/test/java/com/marcosoft/storageSoftware: Contiene las pruebas unitarias.

## Contribución
NO SE ACEPTAN CONTRIBUCIONES

## Contacto
Para cualquier pregunta o sugerencia, por favor contacta a marconchelo12@gmail.com.

