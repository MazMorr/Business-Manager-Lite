package com.marcosoft.storageSoftware.infrastructure.security;

import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;

@Component
public class LicenseValidator {
    private static final String PUBLIC_KEY_RESOURCE_PATH = "/public.pem";
    private static final String LICENSE_FILE_PATTERN = "license_%s.lic";

    @Getter
    private LocalDate remainingTime;

    private final DisplayAlerts displayAlerts;
    private final PublicKey publicKey;

    public LicenseValidator(DisplayAlerts displayAlerts) throws CryptoException {
        this.displayAlerts = displayAlerts;
        this.publicKey = loadPublicKeyFromResources();
        remainingTime = LocalDate.now();
    }

    private PublicKey loadPublicKeyFromResources() throws CryptoException {
        try (InputStream is = getClass().getResourceAsStream(PUBLIC_KEY_RESOURCE_PATH)) {
            if (is == null) {
                throw new CryptoException("Archivo public.pem no encontrado en los recursos del JAR", null);
            }
            String pemData = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return deserializePublicKey(pemData);
        } catch (Exception e) {
            throw new CryptoException("Error al cargar clave pública desde recursos del JAR", e);
        }
    }

    public boolean validateLicense(String licenseName) {
        try {
            // 1. Obtener el directorio donde se encuentra el JAR
            Path appDirectory;
            try {
                Path jarPath = Paths.get(getClass().getProtectionDomain()
                        .getCodeSource().getLocation().toURI());
                appDirectory = jarPath.getParent();
                System.out.println("[DEBUG] Directorio de la aplicación: " + appDirectory);
            } catch (Exception e) {
                // Fallback: usar el directorio de trabajo actual si no se puede obtener la ubicación del JAR
                appDirectory = Paths.get("").toAbsolutePath();
                System.out.println("[DEBUG] Usando directorio de trabajo actual: " + appDirectory);
            }

            // 2. Construir la ruta completa del archivo de licencia
            String licenseFileName = String.format(LICENSE_FILE_PATTERN, licenseName.toLowerCase());
            Path licensePath = appDirectory.resolve(licenseFileName);
            System.out.println("[DEBUG] Ruta completa de licencia: " + licensePath);

            // 3. Verificar que el archivo de licencia existe y es legible
            if (!Files.exists(licensePath)) {
                String errorMsg = String.format(
                        "Archivo de licencia '%s' no encontrado en: %s\n" +
                                "Por favor, coloque el archivo de licencia en el mismo directorio que la aplicación.",
                        licenseFileName, appDirectory);
                throw new CryptoException(errorMsg, null);
            }

            if (!Files.isReadable(licensePath)) {
                throw new CryptoException("No se tienen permisos para leer el archivo de licencia", null);
            }

            // 4. Leer y validar el contenido de la licencia
            System.out.println("[DEBUG] Tamaño del archivo de licencia: " + Files.size(licensePath) + " bytes");
            LicenseData licenseData = verifyLicenseFile(licensePath.toString(), publicKey);

            // 5. Validar los datos obtenidos de la licencia
            if (licenseData.getClientName() == null || licenseData.getClientName().trim().isEmpty()) {
                throw new CryptoException("El nombre del cliente en la licencia no puede estar vacío", null);
            }

            if (licenseData.getExpirationDate() == null) {
                throw new CryptoException("Fecha de expiración inválida en la licencia", null);
            }

            // 6. Calcular días restantes y mostrar alertas si es necesario
            long daysRemaining = LocalDate.now().until(licenseData.getExpirationDate()).getDays();
            System.out.println("[DEBUG] Días restantes de licencia: " + daysRemaining);

            if (daysRemaining <= 0) {
                displayAlerts.showAlert("¡LICENCIA EXPIRADA!\nLa licencia expiró el: " + licenseData.getExpirationDate()
                        +"\n Contacte con Soporte al 55505961 para más información");
                return false;
            }

            if (daysRemaining <= 10) {
                displayAlerts.showAlert("ADVERTENCIA: La licencia expira en " + daysRemaining + " días");
            }

            System.out.println("[DEBUG] Licencia válida para: " + licenseData.getClientName());
            return true;

        } catch (CryptoException e) {
            System.err.println("ERROR DE LICENCIA: " + e.getMessage());
            e.printStackTrace();
            displayAlerts.showAlert("Error de licencia:\n" + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("ERROR INESPERADO: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            displayAlerts.showAlert("Error técnico al validar la licencia.\n" +
                    "Por favor, contacte al soporte técnico.");
            return false;
        }
    }

    private static String extractKeyContent(String pemData) {
        return pemData.replaceAll("-----BEGIN (.*) KEY-----", "")
                .replaceAll("-----END (.*) KEY-----", "")
                .replaceAll("\\s+", "");
    }

    private static PublicKey deserializePublicKey(String pemData) throws CryptoException {
        try {
            String publicKeyStr = extractKeyContent(pemData);
            byte[] publicBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new CryptoException("Error al deserializar clave pública", e);
        }
    }


    // Desescapar caracteres especiales de JSON
    private static String unescapeJson(String input) {
        return input.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\b", "\b")
                .replace("\\f", "\f")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    // Deserialización manual desde JSON
    private LicenseData jsonToLicenseData(String json)  {

        LicenseData data = new LicenseData();
        json = json.trim().substring(1, json.length() - 1); // Eliminar {}

        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim().replace("\"", "");
            String value = keyValue[1].trim();

            if (value.startsWith("\"")) {
                value = value.substring(1, value.length() - 1);
                value = unescapeJson(value);

                switch (key) {
                    case "clientName":
                        data.setClientName(value);
                        break;
                    case "expirationDate":
                        data.setExpirationDate(LocalDate.parse(value));
                        remainingTime = data.getExpirationDate();
                        break;
                    case "licenseId":
                        data.setLicenseId(value);
                        break;
                }
            } else if (key.equals("isUsed")) {
                data.setUsed(Boolean.parseBoolean(value));
            }
        }

        return data;
    }

    /**
     * Verifica una licencia desde un archivo y devuelve los datos si es válida
     */
    public LicenseData verifyLicenseFile(String filePath, PublicKey publicKey) throws CryptoException {
        try {
            String licenseContent = Files.readString(Paths.get(filePath));
            return verifyLicense(licenseContent, publicKey);
        } catch (Exception e) {
            throw new CryptoException("Error al verificar licencia desde archivo", e);
        }
    }

    /**
     * Verifica una licencia y devuelve los datos si es válida
     */
    public LicenseData verifyLicense(String license, PublicKey publicKey) throws CryptoException {
        try {
            String[] parts = license.split("\\.");
            if (parts.length != 2) {
                throw new CryptoException("Formato de licencia inválido", null);
            }

            byte[] jsonBytes = Base64.getUrlDecoder().decode(parts[0]);
            byte[] signature = Base64.getUrlDecoder().decode(parts[1]);

            String jsonData = new String(jsonBytes, StandardCharsets.UTF_8);

            if (!verifySignature(jsonData, signature, publicKey)) {
                throw new CryptoException("Firma digital inválida", null);
            }

            LicenseData data = jsonToLicenseData(jsonData);

            // Validar fecha de expiración
            if (data.getExpirationDate().isBefore(LocalDate.now())) {
                throw new CryptoException("Licencia expirada", null);
            }

            return data;
        } catch (Exception e) {
            throw new CryptoException("Error al verificar licencia", e);
        }
    }

    private static boolean verifySignature(String data, byte[] signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA512withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes(StandardCharsets.UTF_8));
        return sig.verify(signature);
    }

    public static class CryptoException extends Exception {
        public CryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}