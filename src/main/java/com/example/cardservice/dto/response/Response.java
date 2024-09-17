package com.example.cardservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Response {
    private boolean success;
    private String message;
    private Object data;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Object> dataList;
    private Map<String, Object> meta = new LinkedHashMap<>();
    private HttpStatus httpStatus;

    public Response(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public Response(String message, Object data, HttpStatus httpStatus) {
        this.message = message;
        if (data instanceof List) {
            this.dataList = (List) data;
        } else {
            this.data = data;
        }
        this.httpStatus = httpStatus;
    }
}
