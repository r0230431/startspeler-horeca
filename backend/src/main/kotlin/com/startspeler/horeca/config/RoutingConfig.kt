package com.startspeler.horeca.config

import com.startspeler.horeca.repository.CafeTableRepository
import com.startspeler.horeca.repository.CategoryRepository
import com.startspeler.horeca.repository.CrewMemberRepository
import com.startspeler.horeca.repository.CustomerRepository
import com.startspeler.horeca.repository.DiscountRepository
import com.startspeler.horeca.repository.OrderRepository
import com.startspeler.horeca.repository.PaymentRepository
import com.startspeler.horeca.repository.ProductRepository
import com.startspeler.horeca.routes.authRoutes
import com.startspeler.horeca.routes.cafeTableRoutes
import com.startspeler.horeca.routes.categoryRoutes
import com.startspeler.horeca.routes.crewMemberRoutes
import com.startspeler.horeca.routes.customerRoutes
import com.startspeler.horeca.routes.discountRoutes
import com.startspeler.horeca.routes.orderRoutes
import com.startspeler.horeca.routes.paymentRoutes
import com.startspeler.horeca.routes.productRoutes
import com.startspeler.horeca.security.JwtConfig
import com.startspeler.horeca.security.PasswordHasher
import com.startspeler.horeca.service.AuthService
import com.startspeler.horeca.service.CafeTableService
import com.startspeler.horeca.service.CategoryService
import com.startspeler.horeca.service.CrewMemberService
import com.startspeler.horeca.service.CustomerService
import com.startspeler.horeca.service.DiscountService
import com.startspeler.horeca.service.OrderService
import com.startspeler.horeca.service.PaymentService
import com.startspeler.horeca.service.ProductImageStorage
import com.startspeler.horeca.service.ProductService
import io.ktor.server.application.Application
import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.routing
import java.io.File


fun Application.configureRouting() {
    routing {
        val crewMemberRepository = CrewMemberRepository()
        val customerRepository = CustomerRepository()
        val categoryRepository = CategoryRepository()
        val productRepository = ProductRepository()
        val cafeTableRepository = CafeTableRepository()
        val discountRepository = DiscountRepository()
        val orderRepository = OrderRepository()
        val paymentRepository = PaymentRepository()

        val passwordHasher = PasswordHasher()
        val productImageStorage = ProductImageStorage()

        val jwtConfig = JwtConfig(
            secret = environment.config.property("jwt.secret").getString(),
            issuer = environment.config.property("jwt.issuer").getString(),
            audience = environment.config.property("jwt.audience").getString(),
            expirationMillis = environment.config.property("jwt.expirationMs").getString().toLong()
        )

        val authService = AuthService(
            crewMemberRepository = crewMemberRepository,
            passwordHasher = passwordHasher,
            jwtConfig = jwtConfig
        )

        val crewMemberService = CrewMemberService(
            crewMemberRepository = crewMemberRepository,
            passwordHasher = passwordHasher
        )

        val customerService = CustomerService(
            customerRepository = customerRepository,
        )

        val categoryService = CategoryService(
            categoryRepository = categoryRepository
        )

        val productService = ProductService(
            productRepository = productRepository,
            productImageStorage = productImageStorage
        )

        val cafeTableService = CafeTableService(
            cafeTableRepository = cafeTableRepository
        )

        val discountService = DiscountService(
            discountRepository = discountRepository,
        )

        val orderService = OrderService(
            customerRepository = customerRepository,
            cafeTableRepository = cafeTableRepository,
            productRepository = productRepository,
            orderRepository = orderRepository
        )

        val paymentService = PaymentService(
            paymentRepository = paymentRepository,
            orderRepository = orderRepository,
            discountRepository = discountRepository,
        )

        staticFiles("/uploads/products", File("uploads/products"))

        authRoutes(authService)
        crewMemberRoutes(crewMemberService)
        customerRoutes(customerService)
        categoryRoutes(categoryService)
        productRoutes(productService)
        cafeTableRoutes(cafeTableService)
        discountRoutes(discountService)
        orderRoutes(orderService)
        paymentRoutes(paymentService)
    }
}