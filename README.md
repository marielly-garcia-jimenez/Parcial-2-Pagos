# 💳 Microservicio de Pagos - Procesamiento Transaccional

Este servicio es el encargado de la validación y registro de las transacciones financieras del sistema. Es la capa de pagos segura de la arquitectura.

---

## 🛠️ Stack Tecnológico
- **Base de Datos:** MongoDB (Colección `pagos`).
- **Framework:** Spring Boot 3.2.5.
- **Registro:** Cliente de Eureka Service.
- **Observabilidad:** Envío de logs a CloudWatch (LocalStack).

---

## 📋 Endpoints Principales (vía Gateway: 8080)
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/pagos/procesar` | Procesa un pago asociado a una orden. |
| `GET` | `/pagos/{id}` | Busca un pago por su ID único. |
| `GET` | `/pagos/orden/{oid}` | Busca el historial de pago de una orden. |
| `PUT` | `/pagos/{id}/reembolso` | Inicia un proceso de reembolso. |

---

## 🔒 Seguridad y Logs
Cada transacción genera un rastro de auditoría detallado que se envía de forma segura a CloudWatch para su posterior revisión por el equipo de operaciones.

---

## 🔗 Ecosistema Completo
Si deseas entender cómo se integra el pago en el flujo de órdenes, visita el repositorio de [Infraestructura y Guías](https://github.com/marielly-garcia-jimenez/Infraestructura-Examen).

---
<p align="center"> Servicio Core de Microservicios - 2026 </p>
