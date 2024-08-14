//package ir.daneshrefah.scm.uaa.controller.certificate;
//
//import ir.daneshrefah.scm.uaa.service.certificate.CertificateService;
//import lombok.RequiredArgsConstructor;
//import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
//import org.springframework.core.io.InputStreamResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.OutputStreamWriter;
//import java.security.KeyPair;
//import java.security.cert.X509Certificate;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
//
///**
// * Description of the class or purpose of the file.
// *
// * @author reza jamshidi
// * @version 1.0
// * @since 2024-11-18
// */
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/api/certificate")
//@CrossOrigin
//public class CertificateController {
//
//    private final CertificateService certificateService;
//
//    @PostMapping("/generate")
//    public ResponseEntity<InputStreamResource> generateCertificate(
//            @RequestParam String subjectDN,
//            @RequestParam String issuerDN,
////            @RequestParam String signatureAlgorithm,
//            @RequestParam int validityDays
//    ) throws Exception {
//        KeyPair keyPair = certificateService.generateKeyPair(CertificateService.EncryptionAlgorithm.RSA, 2048).get();
//        X509Certificate certificate = certificateService.generateSelfSignedCertificate(keyPair, subjectDN, issuerDN, validityDays);
//
//        // Convert private key and certificate to PEM format
//        ByteArrayOutputStream privateKeyBaos = new ByteArrayOutputStream();
//        ByteArrayOutputStream certificateBaos = new ByteArrayOutputStream();
//        certificateService.exportKeyToPEM(keyPair.getPrivate());
//        certificateService.exportCertificateToPEM(certificate);
//
//        try (JcaPEMWriter privateKeyWriter = new JcaPEMWriter(new OutputStreamWriter(privateKeyBaos));
//             JcaPEMWriter certificateWriter = new JcaPEMWriter(new OutputStreamWriter(certificateBaos))) {
//            privateKeyWriter.writeObject(keyPair.getPrivate());
//            certificateWriter.writeObject(certificate);
//        }
//
//        // Create zip file containing both PEM files
//        ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
//        try (ZipOutputStream zipOut = new ZipOutputStream(zipBaos)) {
//            // Add private key to zip
//            zipOut.putNextEntry(new ZipEntry("private-key.pem"));
//            zipOut.write(privateKeyBaos.toByteArray());
//            zipOut.closeEntry();
//
//            // Add certificate to zip
//            zipOut.putNextEntry(new ZipEntry("certificate.crt"));
//            zipOut.write(certificateBaos.toByteArray());
//            zipOut.closeEntry();
//        }
//
//        // Return the zip file as response
//        ByteArrayInputStream bis = new ByteArrayInputStream(zipBaos.toByteArray());
//
//        InputStreamResource resource = new InputStreamResource(bis);
//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate_and_key.zip");
//
//        return ResponseEntity.ok()
//                .headers(headers)
//                .contentLength(zipBaos.size())
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(resource);
//    }
//}
