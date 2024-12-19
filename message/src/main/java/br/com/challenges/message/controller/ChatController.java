package br.com.challenges.message.controller;

import br.com.challenges.message.adapters.repository.MessageGroupRepository;
import br.com.challenges.message.application.dto.ChatInput;
import br.com.challenges.message.application.dto.ChatOutput;
import br.com.challenges.message.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class ChatController {
    @Autowired
    private KafkaTemplate<String, ChatInput> kafkaTemplate;
    @Autowired
    private ChatRoomService chatRoomService;
    @Autowired
    private MessageGroupRepository messageGroupRepository;

    @MessageMapping("/new-message")
    @SendTo("/topic/livechat")
    public ChatOutput newMessage(@Payload ChatInput dto){
        if (dto.roomId() == null || dto.user() == null) {
            throw new IllegalArgumentException("Room ID and User cannot be null");
        }

        chatRoomService.addUserToRoom(dto.roomId(), dto.user());
        messageGroupRepository.save(dto);
        kafkaTemplate.send("message-group-sent", dto);

        return  new ChatOutput(HtmlUtils.htmlEscape(dto.user() + ": " + dto.message()));
    }
}
