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
    private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentController(PaymentRepository paymentRepository, org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
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
        
        if (payment.getEstado() == null) {
            payment.setEstado("PAGADO");
        }
        Payment savedPayment = paymentRepository.save(payment);
        
        // Notificar a través de Kafka
        kafkaTemplate.send("payment_received_events", savedPayment);
        log.info("Evento de pago recibido enviado a Kafka para la orden: {}", savedPayment.getOrdenId());
        
        return savedPayment;
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

    @GetMapping("/orden/{ordenId}")
    public List<Payment> getPaymentsByOrder(@PathVariable String ordenId) {
        log.info("Obteniendo pagos para la orden: {}", ordenId);
        return paymentRepository.findByOrdenId(ordenId);
    }

    @PutMapping("/{id}/status")
    public Payment updateStatus(@PathVariable String id, @RequestBody String status) {
        log.info("Actualizando estado para el pago {}: {}", id, status);
        String cleanStatus = status.replace("\"", "");
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment != null) {
            payment.setEstado(cleanStatus);
            Payment updatedPayment = paymentRepository.save(payment);
            
            // Si el nuevo estado es PAGADO, notificamos para que la orden se actualice
            if ("PAGADO".equalsIgnoreCase(cleanStatus)) {
                kafkaTemplate.send("payment_received_events", updatedPayment);
                log.info("Evento payment_received_events enviado para pago {} (Estado: PAGADO)", updatedPayment.getId());
            }
            
            return updatedPayment;
        }
        return null;
    }

    @PutMapping("/{id}")
    public Payment updatePayment(@PathVariable String id, @RequestBody Payment paymentUpdate) {
        log.info("Actualizando pago {}", id);
        return paymentRepository.findById(id).map(existingPayment -> {
            boolean statusChanged = paymentUpdate.getEstado() != null && !paymentUpdate.getEstado().equalsIgnoreCase(existingPayment.getEstado());
            
            if (paymentUpdate.getMonto() != null) existingPayment.setMonto(paymentUpdate.getMonto());
            if (paymentUpdate.getEstado() != null) existingPayment.setEstado(paymentUpdate.getEstado());
            
            Payment saved = paymentRepository.save(existingPayment);
            
            if (statusChanged && "PAGADO".equalsIgnoreCase(saved.getEstado())) {
                kafkaTemplate.send("payment_received_events", saved);
                log.info("Evento payment_received_events enviado por actualización de pago {}", saved.getId());
            }
            
            return saved;
        }).orElse(null);
    }
}
