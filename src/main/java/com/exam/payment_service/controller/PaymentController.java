package com.exam.payment_service.controller;

import com.exam.payment_service.model.Payment;
import com.exam.payment_service.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pagos")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping("/procesar")
    public Payment processPayment(@RequestBody Payment payment) {
        log.info("Procesando pago para la orden: {}", payment.getOrdenId());
        payment.setEstado("COMPLETADO");
        return paymentRepository.save(payment);
    }

    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable String id) {
        log.info("Obteniendo pago con id: {}", id);
        return paymentRepository.findById(id).orElse(null);
    }

    @GetMapping("/orden/{ordenId}")
    public Payment getPaymentByOrder(@PathVariable String ordenId) {
        log.info("Obteniendo pago para la orden: {}", ordenId);
        return paymentRepository.findByOrdenId(ordenId).orElse(null);
    }

    @PutMapping("/{id}/reembolso")
    public Payment refundPayment(@PathVariable String id) {
        log.info("Reembolsando pago con id: {}", id);
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment != null) {
            payment.setEstado("REEMBOLSADO");
            return paymentRepository.save(payment);
        }
        return null;
    }
}
