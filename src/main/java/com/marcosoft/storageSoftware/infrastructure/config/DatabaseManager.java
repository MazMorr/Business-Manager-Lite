package com.marcosoft.storageSoftware.infrastructure.config;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class DatabaseManager {
    private static Connection connection;

    public static void initializeConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:file:C:/BusinessManager/Database", "LazyCompile", "</>zzz");
    }

    public static void closeAllConnections() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            initializeConnection();
        }
        return connection;
    }

    public void exportDB(String destinationPath) throws SQLException {
        // Asegurar que el archivo no exista antes de crear la copia
        File backupFile = new File(destinationPath);
        if (backupFile.exists()) {
            backupFile.delete();
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String backupCommand = "BACKUP TO '" + destinationPath.replace("\\", "\\\\") + "'";
            stmt.execute(backupCommand);
        }
    }

    public void importDB(String sourcePath) throws Exception {
        closeAllConnections();

        String dbFolder = "C:/BusinessManager/";
        File folder = new File(dbFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Eliminar archivos existentes de la base de datos
        File[] dbFiles = {
                new File(dbFolder + "Database.mv.db"),
                new File(dbFolder + "Database.trace.db")
        };

        for (File file : dbFiles) {
            if (file.exists() && !file.delete()) {
                throw new IOException("No se pudo eliminar el archivo: " + file.getName());
            }
        }

        // Copiar el nuevo archivo de base de datos
        Path source = Paths.get(sourcePath);
        Path destination = Paths.get(dbFolder + "Database.mv.db");

        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

        // Reiniciar conexión
        initializeConnection();
    }
}