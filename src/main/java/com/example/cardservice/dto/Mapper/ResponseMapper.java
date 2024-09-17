package com.example.cardservice.dto.Mapper;

import com.example.cardservice.dto.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {
    public Response toResponse(String message, Object data, HttpStatus status) {
        return new Response(message, data, status);
    }

    public Response toResponse(String message, HttpStatus status) {
        return new Response(message, status);
    }
}

