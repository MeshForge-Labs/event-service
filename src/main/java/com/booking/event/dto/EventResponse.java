package com.booking.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private String name;
    private String description;
    private String venue;
    private Instant eventDate;
    private int totalSeats;
    private int availableSeats;
    private long version;
    private Instant createdAt;
    private Instant updatedAt;
}
