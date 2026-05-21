# Prode Mundial 2026

Aplicación web de pronósticos del Mundial 2026 con visualización de **BST** y **AVL**. Servidor HTTP embebido en Java (proyecto Maven), sin dependencias externas.

## Requisitos

- **Java JDK 21** (o 17+)
- **Apache Maven 3.9+**

Verificar instalación:

```bash
java -version
mvn -version
```

## Ejecución

Desde la carpeta del proyecto (`prode`):

```bash
mvn compile exec:java
```

Deberías ver en consola:

```
Servidor iniciado en puerto 8080
```

Abrir en el navegador: [http://localhost:8080](http://localhost:8080)

## Empaquetar y ejecutar JAR

```bash
mvn package
java -jar target/prode-1.0.0.jar
```

Ejecutar el JAR desde la raíz del proyecto para que encuentre `prode_data.txt`.

## Puerto personalizado

Por defecto usa el puerto **8080**. Para cambiarlo:

**Windows (PowerShell):**

```powershell
$env:PORT="3000"
mvn compile exec:java
```

**Linux / macOS:**

```bash
PORT=3000 mvn compile exec:java
```

## Persistencia

Los datos se guardan en `prode_data.txt` en la raíz del proyecto. Al reiniciar el servidor se cargan estudiantes, pronósticos y resultados desde ese archivo.

## Panel de administración

En la web, botón **Admin**. Contraseña por defecto: `profe2026`

Desde ahí se cargan resultados reales de grupos y del bracket (necesarios para calcular puntajes).

## Estructura del proyecto

```
prode/
├── pom.xml
├── prode_data.txt
├── README.md
└── src/
    └── main/
        └── java/
            └── Prode.java
```

## Solución de problemas

| Problema | Qué hacer |
|----------|-----------|
| `mvn` no reconocido | Instalar Maven y agregarlo al PATH |
| `javac` o `java` no reconocido | Instalar JDK 21+ |
| Puerto en uso | Cerrar el proceso en el 8080 o usar otro puerto con `PORT` |
| La página no carga | Confirmar que el servidor sigue corriendo y la URL es `http://localhost:8080` |
| No encuentra datos | Ejecutar Maven/JAR desde la carpeta raíz del proyecto |
