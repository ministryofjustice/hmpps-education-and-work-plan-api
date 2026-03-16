package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiChatClientConfiguration {

  @Bean
  fun chatClient(chatClientBuilder: ChatClient.Builder): ChatClient = chatClientBuilder.build()
}
