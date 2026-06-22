package com.fitproject.ventas.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración RabbitMQ del lado productor para MS-Ventas.
 *
 * <p>Declara el Exchange {@value #NOTIFICATIONS_EXCHANGE} y la constante de
 * routing key usada al confirmar una venta. Las colas y bindings son
 * responsabilidad de MS-Notificaciones.</p>
 */
@Configuration
public class RabbitMQConfig {

    /** Exchange compartido con MS-Notificaciones para eventos del ecosistema FitProject. */
    public static final String NOTIFICATIONS_EXCHANGE = "fit.notifications";

    /** Routing key para el evento de confirmación de venta. */
    public static final String SALE_CONFIRMED_KEY = "sale.confirmed";

    /**
     * Declara el TopicExchange. Si ya existe, Spring AMQP verifica la coherencia
     * sin recrearlo.
     *
     * @return instancia del exchange de notificaciones
     */
    @Bean
    public TopicExchange notificationsExchange() {
        return new TopicExchange(NOTIFICATIONS_EXCHANGE);
    }

    /**
     * Conversor JSON para serializar los eventos publicados como mensajes AMQP.
     *
     * @return conversor Jackson2 para mensajes RabbitMQ
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configura el {@link RabbitTemplate} con el conversor JSON.
     *
     * @param connectionFactory fábrica de conexiones gestionada por Spring Boot
     * @return template configurado para publicar mensajes JSON
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
