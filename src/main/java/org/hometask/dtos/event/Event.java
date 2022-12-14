package org.hometask.dtos.event;


import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data

public class Event implements Serializable {
    private UUID eventId;
    private String name;
    private String address;
    private LocalDateTime eventDate;
    private String additionalInfo;

}