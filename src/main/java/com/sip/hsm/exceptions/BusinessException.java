package com.sip.hsm.exceptions;

// Lancée quand une règle métier n'est pas respectée
// (ex : chambre déjà réservée, dates invalides, capacité dépassée...)
// -> renvoie une erreur 400 BAD REQUEST
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
