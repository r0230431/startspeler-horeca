package com.startspeler.horeca.service

import com.startspeler.horeca.database.enums.DiscountType
import com.startspeler.horeca.database.enums.OrderStatus
import com.startspeler.horeca.database.enums.PaymentMethod
import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.dto.payment.CreatePaymentRequest
import com.startspeler.horeca.dto.payment.PaymentMethodTotalResponse
import com.startspeler.horeca.dto.payment.PaymentResponse
import com.startspeler.horeca.dto.payment.PaymentSummaryResponse
import com.startspeler.horeca.repository.DiscountRepository
import com.startspeler.horeca.repository.OrderRepository
import com.startspeler.horeca.repository.PaymentRepository
import com.startspeler.horeca.database.tables.PaymentsTable
import com.startspeler.horeca.util.DateTimeProvider
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import kotlin.collections.map

class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val discountRepository: DiscountRepository
) {

    fun getAllFiltered(
        from: String? = null,
        to: String? = null,
        paymentMethod: PaymentMethod? = null
    ): ServiceResult<List<PaymentResponse>> {
        val fromDateTime = try {
            from?.let { LocalDateTime.parse(it) }
        } catch (e: DateTimeParseException) {
            return ServiceResult.Error("Ongeldige from-datum. Gebruik formaat yyyy-MM-ddTHH:mm:ss.")
        }

        val toDateTime = try {
            to?.let { LocalDateTime.parse(it) }
        } catch (e: DateTimeParseException) {
            return ServiceResult.Error("Ongeldige to-datum. Gebruik formaat yyyy-MM-ddTHH:mm:ss.")
        }

        if (fromDateTime != null && toDateTime != null && fromDateTime.isAfter(toDateTime)) {
            return ServiceResult.Error("De from-datum mag niet na de to-datum liggen.")
        }

        val fromUtc = fromDateTime?.let { DateTimeProvider.brusselsToUtc(it) }
        val toUtc = toDateTime?.let { DateTimeProvider.brusselsToUtc(it) }

        val payments = paymentRepository
            .findAllFiltered(
                paidAtFromUtc = fromUtc,
                paidAtToUtc = toUtc,
                paymentMethod = paymentMethod
            )
            .map { row ->
                val paymentId = row[PaymentsTable.id].value
                val orderIds = orderRepository.findOrderIdsByPaymentId(paymentId)
                paymentRepository.toPaymentResponse(row, orderIds)
            }

        return ServiceResult.Success(payments)
    }

    fun getById(id: Int): PaymentResponse? {
        val row = paymentRepository.findById(id) ?: return null
        val orderIds = orderRepository.findOrderIdsByPaymentId(id)
        return paymentRepository.toPaymentResponse(row, orderIds)
    }

    fun create(request: CreatePaymentRequest): ServiceResult<PaymentResponse> {
        if (request.orderIds.isEmpty()) {
            return ServiceResult.Error("Er moet minstens één bestelling geselecteerd worden.")
        }

        val orders = orderRepository.findByIds(request.orderIds)

        if (orders.size != request.orderIds.distinct().size) {
            return ServiceResult.Error("Eén of meerdere bestellingen werden niet gevonden.")
        }

        if (orders.any { it.status == OrderStatus.CANCELLED }) {
            return ServiceResult.Error("Geannuleerde bestellingen kunnen niet betaald worden.")
        }

        if (orders.any { it.paymentId != null }) {
            return ServiceResult.Error("Eén of meerdere bestellingen zijn al betaald.")
        }

        val distinctCustomerIds = orders.map { it.customerId }.distinct()
        if (distinctCustomerIds.size > 1) {
            return ServiceResult.Error("Je kan enkel bestellingen van dezelfde klant samen afrekenen.")
        }

        val subtotal = orders
            .flatMap { it.lines }
            .fold(BigDecimal.ZERO) { acc, line ->
                acc + (line.unitPriceSnapshot.toBigDecimal() * BigDecimal(line.quantity))
            }
            .setScale(2, RoundingMode.HALF_UP)

        // Valideer en laad alle geselecteerde kortingen
        val selectedDiscounts = request.discountIds.distinct().map { discountId ->
            val discount = discountRepository.findById(discountId)
                ?: return ServiceResult.Error("De geselecteerde korting (id=$discountId) bestaat niet.")
            if (!discount.isActive) {
                return ServiceResult.Error("Korting '${discount.name}' is niet actief.")
            }
            discount
        }

        // Eerst % kortingen verrekenen, daarna vaste bedragen
        val percentageDiscounts = selectedDiscounts.filter { it.discountType == DiscountType.PERCENTAGE }
        val fixedDiscounts = selectedDiscounts.filter { it.discountType == DiscountType.FIXED_AMOUNT }

        var discountAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)

        for (discount in percentageDiscounts) {
            val amount = subtotal
                .multiply(discount.discountValue.toBigDecimal())
                .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
            discountAmount = discountAmount.add(amount)
        }

        for (discount in fixedDiscounts) {
            discountAmount = discountAmount.add(discount.discountValue.toBigDecimal().setScale(2, RoundingMode.HALF_UP))
        }

        discountAmount = discountAmount.min(subtotal)

        val totalAmount = (subtotal - discountAmount).setScale(2, RoundingMode.HALF_UP)

        val paymentId = paymentRepository.create(
            paymentMethod = request.paymentMethod,
            subtotalAmount = subtotal,
            discountAmount = discountAmount,
            totalAmount = totalAmount
        )

        paymentRepository.linkDiscounts(paymentId, request.discountIds.distinct())

        val linked = orderRepository.assignPaymentToOrders(paymentId, request.orderIds)
        if (!linked) {
            return ServiceResult.Error("Betaling werd aangemaakt, maar koppelen aan bestellingen is mislukt.")
        }

        val paymentRow = paymentRepository.findById(paymentId)
            ?: return ServiceResult.Error("Betaling werd aangemaakt maar kon niet opgehaald worden.")

        val response = paymentRepository.toPaymentResponse(
            row = paymentRow,
            orderIds = orderRepository.findOrderIdsByPaymentId(paymentId)
        )

        return ServiceResult.Success(response)
    }

    fun getSummary(from: String?, to: String?): ServiceResult<PaymentSummaryResponse> {
        val fromDateTime = try {
            from?.let { LocalDateTime.parse(it) }
        } catch (e: DateTimeParseException) {
            return ServiceResult.Error("Ongeldige from-datum. Gebruik formaat yyyy-MM-ddTHH:mm:ss.")
        }

        val toDateTime = try {
            to?.let { LocalDateTime.parse(it) }
        } catch (e: DateTimeParseException) {
            return ServiceResult.Error("Ongeldige to-datum. Gebruik formaat yyyy-MM-ddTHH:mm:ss.")
        }

        if (fromDateTime != null && toDateTime != null && fromDateTime.isAfter(toDateTime)) {
            return ServiceResult.Error("De from-datum mag niet na de to-datum liggen.")
        }

        val fromUtc = fromDateTime?.let { DateTimeProvider.brusselsToUtc(it) }
        val toUtc = toDateTime?.let { DateTimeProvider.brusselsToUtc(it) }

        val totals = paymentRepository.getSummary(
            paidAtFromUtc = fromUtc,
            paidAtToUtc = toUtc
        )

        val totalsByMethod = totals.map { (paymentMethod, totalAmount) ->
            PaymentMethodTotalResponse(
                paymentMethod = paymentMethod,
                totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP).toPlainString()
            )
        }

        val grandTotal = totals
            .fold(BigDecimal.ZERO) { acc, (_, totalAmount) -> acc + totalAmount }
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()

        return ServiceResult.Success(
            PaymentSummaryResponse(
                totalsByMethod = totalsByMethod,
                grandTotal = grandTotal,
                from = from,
                to = to
            )
        )
    }
}

private fun BigDecimal.coerceAtMost(other: BigDecimal): BigDecimal =
    if (this > other) other else this