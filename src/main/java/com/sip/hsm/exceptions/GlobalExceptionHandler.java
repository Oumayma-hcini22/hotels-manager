package com.sip.hsm.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// @RestControllerAdvice : intercepte les exceptions lancées par TOUS les controllers
// et les transforme en réponse JSON claire
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 : élément introuvable
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Introuvable", ex.getMessage(), null);
    }

    // 400 : règle métier non respectée
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        return build(HttpStatus.BAD_REQUEST, "Règle métier", ex.getMessage(), null);
    }

    // 400 : erreurs de validation (@NotBlank, @Email, @Min...) -> liste des champs en erreur
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> champs = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(err -> champs.put(err.getField(), err.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Validation", "Certains champs sont invalides", champs);
    }

    // 400 : JSON mal écrit ou valeur d'enum inconnue (ex : "type": "TRIPLE")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBadJson(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Requête invalide",
                "Le corps de la requête est mal formé ou contient une valeur inconnue", null);
    }

    // 400 : paramètre d'URL invalide (ex : ?type=TRIPLE ou ?debut=12-2026)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleBadParam(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Paramètre invalide",
                "Valeur invalide pour le paramètre '" + ex.getName() + "' : " + ex.getValue(), null);
    }

    // 400 : paramètre obligatoire manquant (ex : /chambres/disponibles sans ?debut=)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST, "Paramètre manquant",
                "Le paramètre '" + ex.getParameterName() + "' est obligatoire", null);
    }

    // 401 : email ou mot de passe incorrect
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Connexion refusée", "Email ou mot de passe incorrect", null);
    }

    // 401 : compte pas encore activé par l'admin (R8)
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(DisabledException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Connexion refusée",
                "Votre compte est en attente d'activation par l'administrateur", null);
    }

    // 409 : violation d'une contrainte en base (doublon, élément lié à d'autres données...)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT, "Conflit",
                "Cette donnée existe déjà ou est liée à d'autres éléments", null);
    }

    // 500 : toute autre erreur imprévue
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur serveur", ex.getMessage(), null);
    }

    // construit le JSON de réponse
    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String erreur,
                                                      String message, Map<String, String> champs) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("erreur", erreur);
        body.put("message", message);
        if (champs != null) {
            body.put("champs", champs);
        }
        return ResponseEntity.status(status).body(body);
    }
}