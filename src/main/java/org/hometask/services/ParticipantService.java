package org.hometask.services;

import org.hometask.dtos.participants.Participant;

import org.hometask.dtos.participants.ParticipantCreate;

import org.hometask.dtos.participants.ParticipantUpdate;
import org.hometask.dtos.participants.Person;
import org.hometask.entities.ParticipantEntity;
import org.hometask.entities.PersonEntity;
import org.hometask.mappers.ParticipantMapper;
import org.hometask.repositories.ParticipantRepository;
import org.hometask.repositories.PaymentTypeRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ParticipantService {

    @Resource
    private ParticipantRepository participantRepository;

    @Resource
    private ParticipantMapper participantMapper;

    @Resource
    private PersonService personService;

    @Resource
    private EventService eventService;

    @Resource
    private PaymentTypeRepository paymentTypeRepository;


    public Participant addParticipant(ParticipantCreate participantCreate) throws Exception {
        if (eventService.getEvent(participantCreate.getEventId()).getEventDate().isBefore(LocalDateTime.now())){
            throw new Exception("Can´t add participants to past event");
        } else {
            ParticipantEntity participantEntity = participantMapper
                    .participantCreateToParticipantEntity(participantCreate);
            Boolean personExistsInDatabase = personService.personExistsByIdNumber(participantCreate.getIdNumber());
            if (personExistsInDatabase) {
                participantEntity.setPersonId(personService.getPersonByIdNumber(participantCreate.getIdNumber()).getId());
            } else {
                participantEntity.setPersonId(personService.addPerson(participantCreate).getId());
            }
            participantEntity.setPaymentTypeEntity(paymentTypeRepository
                    .getReferenceById(participantCreate.getPaymentTypeId()));
            participantRepository.save(participantEntity);

            return participantMapper.participantEntityToParticipant(participantEntity);
        }
    }


    public List<Participant> findEventParticipants(UUID eventId) {
        List<ParticipantEntity> participantEntities = participantRepository.findParticipantsBy(eventId);
        return participantMapper.participantEntitiesToParticipants(participantEntities);
    }

    public Participant updateParticipant(UUID participantId, ParticipantUpdate participantUpdate) throws Exception {
        if (eventService.getEvent(participantUpdate.getEventId()).getEventDate().isBefore(LocalDateTime.now())){
            throw new Exception("Can´t update participants of past event");
        } else {
            ParticipantEntity participantEntity = participantRepository.findById(participantId).orElseThrow();
            if (sumPersonParticipations(participantId) == 1) {
                participantEntity.setPersonId(personService.updatePerson(
                        participantEntity.getPersonId(), participantUpdate).getId());
            } else {
                participantEntity.setPersonId(personService.addPerson(
                        participantMapper.participantUpdateToParticipantCreate(participantUpdate)).getId());
            }
            participantEntity.setPaymentTypeEntity(
                    paymentTypeRepository.getReferenceById(participantUpdate.getPaymentTypeId()));
            participantEntity.setParticipantsCount(participantUpdate.getParticipantsCount());
            participantEntity.setAdditionalInfo(participantUpdate.getAdditionalInfo());
            participantRepository.save(participantEntity);

            return participantMapper.participantEntityToParticipant(participantEntity);
        }
    }

    public void deleteParticipant(UUID participantId) {
        if (sumPersonParticipations(participantId) == 1) {
            personService.deletePerson(participantRepository.findById(participantId).orElseThrow().getPersonId());
            participantRepository.deleteById(participantId);
        } else {
            participantRepository.deleteById(participantId);
        }
    }

    public void deleteAllParticipants(UUID eventId) {
        participantRepository.deleteAllByEventId(eventId);
    }

    public Integer sumPersonParticipations (UUID participantId){
        return participantRepository.countById(participantId);
    }

    public Integer sumEventParticipants(UUID eventId) {
        Integer participantsCounter = 0;
        List<Participant> participants = findEventParticipants(eventId);
        for (Participant participant : participants) {
            participantsCounter += participant.getParticipantsCount();
        }
        return participantsCounter;
    }
}
