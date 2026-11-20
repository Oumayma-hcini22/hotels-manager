package com.sip.hsm.exceptions;

// Lancée quand on cherche un élément qui n'existe pas (ex : hôtel n°99)
// -> renvoie une erreur 404 NOT FOUND
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
