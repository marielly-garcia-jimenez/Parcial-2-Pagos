package com.exam.payment_service.controller;

import com.exam.payment_service.model.Payment;
import com.exam.payment_service.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestController
@RequestMapping("/pagos")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public Payment createPayment(@RequestBody Payment payment, WebRequest request) {
        log.info("Intentando crear pago para la orden: {}", payment.getOrdenId());
        request.setAttribute("failedObject", payment, WebRequest.SCOPE_REQUEST);
        
        // Disparar reintento si es "fail" o "fail_permanent"
        if (payment.getOrdenId() == null || 
            payment.getOrdenId().equalsIgnoreCase("fail") || 
            payment.getOrdenId().equalsIgnoreCase("fail_permanent")) {
            
            throw new RuntimeException("Error simulado para iniciar ciclo de reintentos");
        }
        
        payment.setEstado("COMPLETADO");
        return paymentRepository.save(payment);
    }

    @PostMapping("/retry")
    public Payment saveRetry(@RequestBody Payment payment) {
        log.info("Reintentando guardar pago desde Broker: {}", payment.getId());
        if (payment.getOrdenId() != null && payment.getOrdenId().equalsIgnoreCase("fail_permanent")) {
            log.warn("Simulando fallo permanente en Pago para prueba de 5 intentos");
            throw new RuntimeException("Fallo simulado permanentemente en pago");
        }
        return paymentRepository.save(payment);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        log.info("Obteniendo todos los pagos");
        return paymentRepository.findAll();
    }

    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable String id) {
        log.info("Obteniendo pago con id: {}", id);
        return paymentRepository.findById(id).orElse(null);
    }
}
