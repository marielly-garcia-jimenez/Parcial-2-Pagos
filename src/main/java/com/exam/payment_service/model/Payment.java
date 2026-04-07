package com.exam.payment_service.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "payments")
public class Payment {
    @Id
    private String id;
    
    @JsonProperty("ordenId")
    @JsonAlias({"orderId", "ordenId"})
    @Field("ordenId")
    private String ordenId;
    
    @JsonProperty("monto")
    @JsonAlias({"amount", "monto"})
    @Field("monto")
    private Double monto;
    
    @JsonProperty("estado")
    @JsonAlias({"status", "estado"})
    @Field("estado")
    private String estado;

    public Payment() {}

    public Payment(String id, String ordenId, Double monto, String estado) {
        this.id = id;
        this.ordenId = ordenId;
        this.monto = monto;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrdenId() {
        return ordenId;
    }

    public void setOrdenId(String ordenId) {
        this.ordenId = ordenId;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
