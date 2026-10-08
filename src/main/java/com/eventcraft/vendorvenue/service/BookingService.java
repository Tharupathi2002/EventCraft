package com.eventcraft.vendorvenue.service;

import com.eventcraft.budgetexpense.entity.Budget;
import com.eventcraft.budgetexpense.entity.Expense;
import com.eventcraft.budgetexpense.service.BudgetService;
import com.eventcraft.budgetexpense.service.ExpenseService;
import com.eventcraft.eventplanning.entity.Event;
import com.eventcraft.eventplanning.entity.EventStatus;
import com.eventcraft.eventplanning.repository.EventRepository;
import com.eventcraft.vendorvenue.dto.BookingForm;
import com.eventcraft.vendorvenue.entity.Booking;
import com.eventcraft.vendorvenue.entity.Vendor;
import com.eventcraft.vendorvenue.entity.Venue;
import com.eventcraft.vendorvenue.repository.BookingRepository;
import com.eventcraft.vendorvenue.repository.VendorRepository;
import com.eventcraft.vendorvenue.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Bookings are always scoped to the logged-in host (hostUsername): a host only sees and changes
 * bookings for their own events; anything else behaves as "not found".
 *
 * Integration with the rest of EventCraft:
 *  - a booking belongs to one of the host's events (date and host come from the event);
 *  - confirming a booking adds an expense to that event's budget, cancelling/deleting removes it;
 *  - a venue/vendor cannot be booked twice for the same date, and event deletion removes bookings.
 */
@Service
@Transactional
public class BookingService {

    private static final Map<String, String> VENDOR_TO_EXPENSE_CATEGORY = Map.of(
            "Catering", "Catering",
            "Photography", "Photography",
            "Decor", "Decorations",
            "Music/DJ", "Entertainment");

    /** Budget expense category that matches a vendor's service type. */
    public static String expenseCategoryFor(Vendor vendor) {
        return VENDOR_TO_EXPENSE_CATEGORY.getOrDefault(vendor.getServiceType(), "Other");
    }

    private final BookingRepository bookingRepository;
    private final VenueRepository venueRepository;
    private final VendorRepository vendorRepository;
    private final EventRepository eventRepository;
    private final BudgetService budgetService;
    private final ExpenseService expenseService;

    public BookingService(BookingRepository bookingRepository,
                          VenueRepository venueRepository,
                          VendorRepository vendorRepository,
                          EventRepository eventRepository,
                          BudgetService budgetService,
                          ExpenseService expenseService) {
        this.bookingRepository = bookingRepository;
        this.venueRepository = venueRepository;
        this.vendorRepository = vendorRepository;
        this.eventRepository = eventRepository;
        this.budgetService = budgetService;
        this.expenseService = expenseService;
    }

    /** Result of a create/status change: the booking plus an optional note to show the host. */
    public record Outcome(Booking booking, String warning) {}

    // ---------- queries ----------

    @Transactional(readOnly = true)
    public List<Booking> findForHost(String host, String status, Long eventId) {
        List<Booking> bookings;
        if (eventId != null) {
            bookings = bookingRepository
                    .findByEvent_EventIdAndEvent_HostUsernameOrderByBookingDateDescBookingIdDesc(eventId, host);
            if (status != null && !status.isBlank()) {
                String wanted = status.toUpperCase();
                bookings = bookings.stream().filter(b -> wanted.equals(b.getBookingStatus())).toList();
            }
        } else if (status != null && !status.isBlank()) {
            bookings = bookingRepository
                    .findByEvent_HostUsernameAndBookingStatusOrderByBookingDateDescBookingIdDesc(host, status.toUpperCase());
        } else {
            bookings = bookingRepository.findByEvent_HostUsernameOrderByBookingDateDescBookingIdDesc(host);
        }
        bookings.forEach(this::enrich);
        return bookings;
    }

    /** The host's events that can still take bookings (not cancelled), soonest first. */
    @Transactional(readOnly = true)
    public List<Event> findBookableEvents(String host) {
        List<Event> events = new ArrayList<>();
        for (Event e : eventRepository.findByHostUsername(host)) {
            if (e.getStatus() != EventStatus.CANCELLED) {
                events.add(e);
            }
        }
        events.sort(Comparator.comparing(Event::getEventDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return events;
    }

    @Transactional(readOnly = true)
    public Optional<Event> findOwnedEvent(Long eventId, String host) {
        if (eventId == null) return Optional.empty();
        return eventRepository.findById(eventId).filter(e -> host != null && host.equals(e.getHostUsername()));
    }

    @Transactional(readOnly = true)
    public long confirmedCount(String host) {
        return bookingRepository.countConfirmedForHost(host);
    }

    @Transactional(readOnly = true)
    public BigDecimal confirmedTotal(String host) {
        BigDecimal total = bookingRepository.sumConfirmedForHost(host);
        return total != null ? total : BigDecimal.ZERO;
    }

    /** Venues that can be booked on this date: not under maintenance/booked and free that day. */
    @Transactional(readOnly = true)
    public List<Venue> findAvailableVenues(LocalDate date) {
        if (date == null) return List.of();
        return venueRepository.findAll().stream()
                .filter(v -> isBookable(v, date))
                .sorted(Comparator.comparing(Venue::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** @throws IllegalArgumentException if the venue doesn't exist or isn't free on that date. */
    @Transactional(readOnly = true)
    public Venue requireAvailableVenue(Long venueId, LocalDate date) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("The selected venue could not be found."));
        if (!isBookable(venue, date)) {
            throw new IllegalArgumentException(venue.getName() + " is not available on " + date + ".");
        }
        return venue;
    }

    /** The venue currently booked (not cancelled) for this event, if any. */
    @Transactional(readOnly = true)
    public Optional<Venue> findActiveVenueForEvent(Long eventId) {
        return bookingRepository.findByEvent_EventId(eventId).stream()
                .filter(b -> "VENUE".equals(b.getBookingType()) && b.getVenueId() != null
                        && !Booking.CANCELLED.equals(b.getBookingStatus()))
                .findFirst()
                .flatMap(b -> venueRepository.findById(b.getVenueId()));
    }

    private boolean isBookable(Venue venue, LocalDate date) {
        String status = venue.getStatus();
        boolean statusOk = status == null || "AVAILABLE".equalsIgnoreCase(status);
        return statusOk && !bookingRepository.venueTaken(venue.getVenueId(), date);
    }

    // ---------- commands ----------

    /**
     * Books a venue for a freshly created event and confirms it straight away, which adds the
     * venue's rate to the event's budget (the budget must already exist).
     */
    public Outcome bookVenueForEvent(Long eventId, Long venueId, String host) {
        BookingForm form = new BookingForm();
        form.setEventId(eventId);
        form.setVenueId(venueId);
        Outcome created = create(form, host);
        Outcome confirmed = updateStatus(created.booking().getBookingId(), host, Booking.CONFIRMED);
        String warning = created.warning() != null ? created.warning() : confirmed.warning();
        return new Outcome(confirmed.booking(), warning);
    }

    /** @throws IllegalArgumentException with a user-readable message when the request is not valid. */
    public Outcome create(BookingForm form, String host) {
        Event event = findOwnedEvent(form.getEventId(), host)
                .orElseThrow(() -> new IllegalArgumentException("Please choose one of your own events."));
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalArgumentException("A cancelled event cannot take bookings.");
        }
        boolean hasVenue = form.getVenueId() != null;
        boolean hasVendor = form.getVendorId() != null;
        if (hasVenue == hasVendor) {
            throw new IllegalArgumentException("Choose either a venue or a vendor for each booking request.");
        }

        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setSpecialRequirements(form.getSpecialRequirements());
        String warning = null;

        if (hasVenue) {
            Venue venue = venueRepository.findById(form.getVenueId())
                    .orElseThrow(() -> new IllegalArgumentException("Venue not found."));
            if ("MAINTENANCE".equalsIgnoreCase(venue.getStatus())) {
                throw new IllegalArgumentException(venue.getName() + " is under maintenance and cannot be booked.");
            }
            if (bookingRepository.venueTaken(venue.getVenueId(), event.getEventDate())) {
                throw new IllegalArgumentException(venue.getName() + " is already booked on " + event.getEventDate() + ".");
            }
            booking.setVenueId(venue.getVenueId());
            booking.setBookingType("VENUE");
            booking.setAmount(venue.getRatePerDay());
            if (event.getTotalSeats() != null && venue.getCapacity() != null && venue.getCapacity() > 0
                    && venue.getCapacity() < event.getTotalSeats()) {
                warning = "Note: " + venue.getName() + " holds " + venue.getCapacity()
                        + " guests but this event's seat map has " + event.getTotalSeats() + " seats.";
            }
        } else {
            Vendor vendor = vendorRepository.findById(form.getVendorId())
                    .orElseThrow(() -> new IllegalArgumentException("Vendor not found."));
            if ("INACTIVE".equalsIgnoreCase(vendor.getStatus())) {
                throw new IllegalArgumentException(vendor.getBusinessName() + " is not accepting bookings.");
            }
            if (bookingRepository.vendorTaken(vendor.getVendorId(), event.getEventDate())) {
                throw new IllegalArgumentException(vendor.getBusinessName() + " is already booked on " + event.getEventDate() + ".");
            }
            booking.setVendorId(vendor.getVendorId());
            booking.setBookingType("VENDOR");
            booking.setAmount(vendor.getBasePrice());
        }
        booking.setBookingDate(LocalDate.now());
        booking.setBookingStatus(Booking.PENDING);
        Booking saved = bookingRepository.save(booking);
        enrich(saved);
        return new Outcome(saved, warning);
    }

    /** Changes the status of one of the host's bookings and keeps the event budget in step. */
    public Outcome updateStatus(Long id, String host, String status) {
        String wanted = status == null ? "" : status.toUpperCase();
        if (!List.of(Booking.PENDING, Booking.CONFIRMED, Booking.CANCELLED).contains(wanted)) {
            throw new IllegalArgumentException("Unknown booking status.");
        }
        Booking booking = findOwnedOrThrow(id, host);
        String warning = null;

        if (Booking.CONFIRMED.equals(wanted)) {
            // Re-confirming a cancelled booking must not clash with a booking made since.
            if (Booking.CANCELLED.equals(booking.getBookingStatus())) {
                LocalDate date = booking.getEventDate();
                boolean taken = booking.getVenueId() != null
                        ? bookingRepository.venueTaken(booking.getVenueId(), date)
                        : bookingRepository.vendorTaken(booking.getVendorId(), date);
                if (taken) {
                    throw new IllegalArgumentException("That date has been booked by someone else since this was cancelled.");
                }
            }
            booking.setBookingStatus(Booking.CONFIRMED);
            warning = addExpenseIfPossible(booking);
        } else {
            booking.setBookingStatus(wanted);
            if (Booking.CANCELLED.equals(wanted)) {
                warning = removeExpense(booking);
            }
        }
        Booking saved = bookingRepository.save(booking);
        enrich(saved);
        return new Outcome(saved, warning);
    }

    public String delete(Long id, String host) {
        Booking booking = findOwnedOrThrow(id, host);
        String warning = removeExpense(booking);
        bookingRepository.delete(booking);
        return warning;
    }

    /** Called by event-planning when an event is deleted (its budget and expenses go separately). */
    public void deleteByEventId(Long eventId) {
        bookingRepository.deleteAll(bookingRepository.findByEvent_EventId(eventId));
    }

    // ---------- budget integration ----------

    /** Adds this booking's cost to its event's budget. Returns a note for the host, or null. */
    private String addExpenseIfPossible(Booking booking) {
        if (booking.getExpenseId() != null) {
            return null; // already recorded
        }
        Optional<Budget> budget = budgetService.findOwnedByEvent(booking.getEventId(), booking.getHostName());
        if (budget.isEmpty()) {
            return "Confirmed, but \"" + booking.getEventName()
                    + "\" has no budget yet, so the cost was not added. Create a budget for it, then confirm again.";
        }
        enrich(booking);
        String name = booking.getVenue() != null ? booking.getVenue().getName()
                : booking.getVendor() != null ? booking.getVendor().getBusinessName() : "booking";
        String category = "Venue";
        if (booking.getVendor() != null) {
            category = VENDOR_TO_EXPENSE_CATEGORY.getOrDefault(booking.getVendor().getServiceType(), "Other");
        }
        String description = ("VENUE".equals(booking.getBookingType()) ? "Venue: " : "Vendor: ") + name
                + " (booking BK-" + booking.getBookingId() + ")";
        Expense expense = new Expense(budget.get(), category,
                description.length() > 200 ? description.substring(0, 200) : description,
                booking.getAmount(), LocalDate.now());
        booking.setExpenseId(expenseService.create(budget.get(), expense).getId());
        return null;
    }

    /** Removes the budget expense created for this booking. Returns a note if it had to be kept. */
    private String removeExpense(Booking booking) {
        Long expenseId = booking.getExpenseId();
        if (expenseId == null) {
            return null;
        }
        boolean removed = expenseService.deleteIfUnpaid(expenseId);
        if (!removed) {
            return "The budget expense for this booking was already paid, so it was kept in the budget.";
        }
        booking.setExpenseId(null);
        return null;
    }

    // ---------- helpers ----------

    private Booking findOwnedOrThrow(Long id, String host) {
        Booking booking = bookingRepository.findById(id)
                .filter(b -> host != null && host.equals(b.getHostName()))
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));
        enrich(booking);
        return booking;
    }

    private void enrich(Booking booking) {
        if (booking.getVenueId() != null) {
            venueRepository.findById(booking.getVenueId()).ifPresent(booking::setVenue);
        }
        if (booking.getVendorId() != null) {
            vendorRepository.findById(booking.getVendorId()).ifPresent(booking::setVendor);
        }
    }
}
