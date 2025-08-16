package com.example.slabiak.appointmentscheduler.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.example.slabiak.appointmentscheduler.entity.Appointment;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Service
public class GoogleCalendarService {

    public void createCalendarEvent(OAuth2AuthorizedClient authorizedClient, Appointment appointment) throws IOException {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        Credential credential = new GoogleCredential().setAccessToken(accessToken);

        Calendar service = new Calendar.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("Appointment Scheduler")
                .build();

        Event event = new Event()
                .setSummary("Appointment with " + appointment.getCustomer().getFirstName())
                .setDescription("Appointment for service: " + appointment.getWork().getName());

        // Set the start time
        DateTime startDateTime = new DateTime(appointment.getStart());
        EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone("America/Los_Angeles"); // IMPORTANT: Change to your timezone if needed
        event.setStart(start);

        // Set the end time
        DateTime endDateTime = new DateTime(appointment.getEnd());
        EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone("America/Los_Angeles"); // IMPORTANT: Change to your timezone if needed
        event.setEnd(end);

        // Add attendees (the provider and the customer)
        EventAttendee[] attendees = new EventAttendee[]{
                new EventAttendee().setEmail(appointment.getProvider().getEmail()),
                new EventAttendee().setEmail(appointment.getCustomer().getEmail()),
        };
        event.setAttendees(Arrays.asList(attendees));

        // Insert the event into the primary calendar of the authenticated user (the provider)
        String calendarId = "primary";
        service.events().insert(calendarId, event).setSendNotifications(true).execute();
    }
}