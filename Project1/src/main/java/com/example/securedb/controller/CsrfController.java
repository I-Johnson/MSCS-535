package com.example.securedb.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Authenticated endpoint a client calls first to obtain the CSRF token.
 * Reading the CsrfToken here makes Spring Security issue the XSRF-TOKEN
 * cookie; the client must then send the token value back in the
 * X-XSRF-TOKEN header on every POST or DELETE.
 */
@RestController
public class CsrfController {

    // GET https://localhost:8443/api/csrf  (any authenticated user)
    @GetMapping("/api/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("headerName", token.getHeaderName());
        body.put("token", token.getToken());
        return body;
    }
}
