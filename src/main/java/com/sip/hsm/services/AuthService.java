package com.sip.hsm.services;

import com.sip.hsm.dto.AuthResponse;
import com.sip.hsm.dto.LoginRequest;
import com.sip.hsm.dto.MessageResponse;
import com.sip.hsm.dto.RegisterRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    MessageResponse register(RegisterRequest request);
}