package com.exam.payment_service.exception;

import com.exam.payment_service.dto.RetryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "payments_retry_jobs";

    public GlobalExceptionHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Error detectado en Payment Service: {}", ex.getMessage());
        
        Object body = request.getAttribute("failedObject", WebRequest.SCOPE_REQUEST);
        
        if (body != null) {
            log.info("Enviando a Kafka para reintento inicial...");
            RetryMessage<Object> retryMessage = new RetryMessage<>(
                    body,
                    new RetryMessage.StepStatus("PENDING", "Pendiente de enviar correo"),
                    new RetryMessage.StepStatus("PENDING", "Pendiente de actualizar")
            );
            kafkaTemplate.send(TOPIC, retryMessage);
            log.info("Mensaje de reintento enviado al tópico: {}", TOPIC);
        } else {
            log.warn("Fallo en operación de reintento (Broker), no se re-envía a Kafka desde aquí.");
        }

        return org.springframework.http.ResponseEntity
                .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + ex.getMessage());
    }
}
