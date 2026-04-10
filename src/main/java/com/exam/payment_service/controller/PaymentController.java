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
        if (payment.getOrdenId() == null || payment.getOrdenId().equals("fail")) {
            throw new RuntimeException("Error simulado en creación de pago");
        }
        payment.setEstado("COMPLETADO");
        return paymentRepository.save(payment);
    }

    @PostMapping("/retry")
    public Payment createPaymentRetry(@RequestBody Payment payment) {
        log.info("Reintentando crear pago desde Broker: {}", payment.getOrdenId());
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
