package com.booking.event.service;

import com.booking.event.dto.CreateEventRequest;
import com.booking.event.dto.EventResponse;
import com.booking.event.dto.ReserveSeatsRequest;
import com.booking.event.exception.NotEnoughSeatsException;
import com.booking.event.exception.ResourceNotFoundException;
import com.booking.event.model.Event;
import com.booking.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<EventResponse> findAll() {
        return eventRepository.findAllByOrderByEventDateAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponse findById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Event not found: id={}", id);
                    return new ResourceNotFoundException("Event not found: " + id);
                });
        return toResponse(event);
    }

    public EventResponse create(CreateEventRequest request) {
        Event event = Event.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .venue(request.getVenue().trim())
                .eventDate(request.getEventDate())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .build();
        event = eventRepository.save(event);
        log.info("Event created: id={}, name={}, totalSeats={}", event.getId(), event.getName(), event.getTotalSeats());
        return toResponse(event);
    }

    public EventResponse reserve(Long eventId, ReserveSeatsRequest request) {
        int quantity = request.getQuantity();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Event not found for reserve: id={}", eventId);
                    return new ResourceNotFoundException("Event not found: " + eventId);
                });
        if (event.getAvailableSeats() < quantity) {
            log.warn("Insufficient seats: eventId={}, available={}, requested={}", eventId, event.getAvailableSeats(), quantity);
            throw new NotEnoughSeatsException("Not enough seats available. Available: " + event.getAvailableSeats() + ", requested: " + quantity);
        }
        event.setAvailableSeats(event.getAvailableSeats() - quantity);
        try {
            event = eventRepository.save(event);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock conflict on event id={}", eventId);
            throw new com.booking.event.exception.OptimisticLockException("Event was updated by another request. Please retry.");
        }
        log.info("Seats reserved: eventId={}, quantity={}, remaining={}", eventId, quantity, event.getAvailableSeats());
        return toResponse(event);
    }

    private EventResponse toResponse(Event e) {
        return EventResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .venue(e.getVenue())
                .eventDate(e.getEventDate())
                .totalSeats(e.getTotalSeats())
                .availableSeats(e.getAvailableSeats())
                .version(e.getVersion())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
