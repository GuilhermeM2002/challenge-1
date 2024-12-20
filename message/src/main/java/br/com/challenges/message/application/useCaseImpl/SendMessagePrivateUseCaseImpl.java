package br.com.challenges.message.application.useCaseImpl;

import br.com.challenges.message.application.dto.MessageDto;
import br.com.challenges.message.avro.MessageAvro;
import br.com.challenges.message.core.domain.MessagePrivate;
import br.com.challenges.message.core.useCases.SendMassagePrivateUseCase;
import br.com.challenges.message.adapters.repository.MessagePrivateRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

@Service
public class SendMessagePrivateUseCaseImpl implements SendMassagePrivateUseCase {
    @Autowired
    private MessagePrivateRepository repository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private KafkaTemplate<String, MessageAvro> kafkaTemplate;
    @Override
    public MessageDto sendMessage(MessageDto dto) {
        var message = mapper.map(dto, MessagePrivate.class);
        repository.save(message);
        var messageDto = mapper.map(message, MessageDto.class);

        var messageAvro = new MessageAvro();
        messageAvro.setContent(message.getContent());
        messageAvro.setDate(message.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        messageAvro.setId(message.getId().toString());
        messageAvro.setWhoSend(message.getWhoSend());
        messageAvro.setWhoReceive(message.getWhoReceive());

        kafkaTemplate.send("message-sent", messageAvro.getWhoSend().toString(), messageAvro);

        return messageDto;
    }
}