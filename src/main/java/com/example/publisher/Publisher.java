package com.example.publisher;

// Importa as classes necessárias para trabalhar com a API JMS, IBM MQ, e bibliotecas adicionais
import javax.jms.*;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.JMSC;
import com.ibm.msg.client.wmq.WMQConstants;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.time.Instant;

public class Publisher {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        try {
            // Configura a fábrica de conexões para se conectar ao IBM MQ
            MQConnectionFactory factory = new MQConnectionFactory();
            // Define o hostname do servidor IBM MQ
            factory.setHostName("localhost");
            // Define a porta onde o servidor IBM MQ está ouvindo
            factory.setPort(1515);
            // Define o tipo de transporte como TCP/IP para uma conexão de cliente
            factory.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
            // Configura o nome do gerenciador de filas do IBM MQ
            factory.setQueueManager("QMSERPRO");
            // Configura o canal de comunicação do IBM MQ
            factory.setChannel("ADMIN.CHL");
            // Cria uma conexão usando as configurações da fábrica de conexões
            Connection connection = factory.createConnection();
            // Cria uma sessão JMS sem transações e com confirmação automática de mensagens
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // Cria um tópico com o nome especificado, que será utilizado para enviar mensagens
            Topic topic = session.createTopic("topic://TOPICO1");
            // Cria um produtor de mensagens associado ao tópico configurado
            MessageProducer producer = session.createProducer(topic);
            // Define o modo de entrega como PERSISTENT para que as mensagens sejam persistentes
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            // Inicia a conexão para começar a enviar mensagens
            connection.start();
            // Cria um serviço agendador para enviar mensagens periodicamente
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            // Agenda o envio de mensagens a cada 10 segundos
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    // Gera o timestamp atual
                    String timestamp = Instant.now().toString();
                    // Gera um código alfanumérico aleatório de 10 caracteres
                    String randomCode = generateRandomAlphanumericCode(10);
                    // Cria o payload da mensagem em formato JSON
                    String payload = String.format("{\"timestamp\": \"%s\", \"message\": \"%s\"}", timestamp, randomCode);
                
                    // Cria uma mensagem de texto com o payload gerado
                    TextMessage message = session.createTextMessage(payload);
                    // Envia a mensagem para o tópico
                    producer.send(message);
                    System.out.println("Mensagem publicada no tópico: " + payload);

                } catch (JMSException e) {
                    // Imprime o stack trace se houver uma exceção ao enviar a mensagem
                    e.printStackTrace();
                }
            }, 0, 10, TimeUnit.SECONDS); // Intervalo inicial de 0 segundos e intervalo de 10 segundos entre mensagens

        } catch (JMSException e) {
            // Imprime o stack trace se houver uma exceção ao configurar ou iniciar a conexão
            e.printStackTrace();
        }
    }

    // Método para gerar um código alfanumérico aleatório de comprimento especificado
    private static String generateRandomAlphanumericCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"; // Caracteres permitidos
        Random random = new Random(); // Inicializa o gerador de números aleatórios
        StringBuilder code = new StringBuilder(length); // Inicializa o StringBuilder para o código

        // Loop para preencher o código com caracteres aleatórios
        for (int i = 0; i < length; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString(); // Retorna o código gerado
    }
}
