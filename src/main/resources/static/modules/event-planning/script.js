/* =========================================================================
   EventCraft - Event Planning & Customization module
   Member 1 (Sujeewan) - CRUD frontend logic
   Talks to the REST API exposed by EventController.java at /api/events
   ========================================================================= */

const API_BASE = "/api/events";

// Visual identity per event type: gradient banner + icon + label.
// Used whenever the host hasn't set a custom coverImageUrl.
const EVENT_TYPE_META = {
    WEDDING: {
        label: "Wedding",
        gradient: "linear-gradient(135deg, #C9A9C7 0%, #E7D5C9 100%)",
        icon: `<svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                 <circle cx="9" cy="15" r="5"/><circle cx="15" cy="15" r="5"/>
                 <path d="M9 10 L11 4 L13 10"/>
               </svg>`
    },
    BIRTHDAY: {
        label: "Birthday",
        gradient: "linear-gradient(135deg, #D9B36C 0%, #C77B56 100%)",
        icon: `<svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                 <rect x="4" y="12" width="16" height="8" rx="2"/>
                 <line x1="9" y1="12" x2="9" y2="7"/><line x1="15" y1="12" x2="15" y2="7"/>
                 <circle cx="9" cy="5.5" r="1.2" fill="white" stroke="none"/>
                 <circle cx="15" cy="5.5" r="1.2" fill="white" stroke="none"/>
               </svg>`
    },
    CORPORATE: {
        label: "Corporate",
        gradient: "linear-gradient(135deg, #5F7096 0%, #3F4E72 100%)",
        icon: `<svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                 <rect x="3" y="8" width="18" height="12" rx="2"/>
                 <path d="M8 8V6a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                 <line x1="3" y1="13" x2="21" y2="13"/>
               </svg>`
    },
    COMMUNITY: {
        label: "Community",
        gradient: "linear-gradient(135deg, #93A98C 0%, #C8D6B9 100%)",
        icon: `<svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                 <circle cx="8" cy="9" r="3"/><circle cx="16" cy="9" r="3"/>
                 <path d="M3 20c0-3 2.5-5 5-5s5 2 5 5"/>
                 <path d="M11 20c0-3 2.5-5 5-5s5 2 5 5"/>
               </svg>`
    },
    OTHER: {
        label: "Other",
        gradient: "linear-gradient(135deg, #8C7BC9 0%, #C9BEE0 100%)",
        icon: `<svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                 <rect x="4" y="5" width="16" height="15" rx="2"/>
                 <line x1="4" y1="10" x2="20" y2="10"/>
                 <line x1="8" y1="3" x2="8" y2="7"/><line x1="16" y1="3" x2="16" y2="7"/>
                 <path d="M12 13l1 2 2 .3-1.5 1.4.4 2-1.9-1-1.9 1 .4-2L9 15.3l2-.3z" fill="white" stroke="none"/>
               </svg>`
    }
};

// ---- DOM references ----
const eventForm = document.getElementById("eventForm");
const formTitle = document.getElementById("formTitle");
const eventIdField = document.getElementById("eventId");
const hostUsernameField = document.getElementById("hostUsername");
const eventNameField = document.getElementById("eventName");
const eventTypeField = document.getElementById("eventType");
const eventDateField = document.getElementById("eventDate");
const themeField = document.getElementById("theme");
const visibilityField = document.getElementById("visibility");
const coverImageUrlField = document.getElementById("coverImageUrl");
const descriptionField = document.getElementById("description");
const submitBtn = document.getElementById("submitBtn");
const cancelEditBtn = document.getElementById("cancelEditBtn");
const formMessage = document.getElementById("formMessage");

const eventListEl = document.getElementById("eventList");
const filterHostField = document.getElementById("filterHost");
const filterBtn = document.getElementById("filterBtn");
const clearFilterBtn = document.getElementById("clearFilterBtn");

// ---- App state ----
let isEditMode = false;

// ---- Init ----
document.addEventListener("DOMContentLoaded", () => {
    loadEvents();
});

eventForm.addEventListener("submit", handleFormSubmit);
cancelEditBtn.addEventListener("click", resetForm);
filterBtn.addEventListener("click", () => loadEvents(filterHostField.value.trim()));
clearFilterBtn.addEventListener("click", () => {
    filterHostField.value = "";
    loadEvents();
});

/* ============================ CREATE / UPDATE ============================ */

async function handleFormSubmit(e) {
    e.preventDefault();
    clearMessage();

    const payload = {
        hostUsername: hostUsernameField.value.trim(),
        eventName: eventNameField.value.trim(),
        eventType: eventTypeField.value,
        eventDate: eventDateField.value,
        theme: themeField.value.trim(),
        visibility: visibilityField.value,
        coverImageUrl: coverImageUrlField.value.trim(),
        description: descriptionField.value.trim()
    };

    try {
        let response;
        if (isEditMode) {
            const id = eventIdField.value;
            response = await fetch(`${API_BASE}/${id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
        } else {
            response = await fetch(API_BASE, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
        }

        if (!response.ok) {
            const errorBody = await safeReadJson(response);
            throw new Error(errorBody?.message || `Request failed (${response.status})`);
        }

        showMessage(isEditMode ? "Event updated." : "Event saved as draft.", "success");
        resetForm();
        loadEvents();
    } catch (err) {
        showMessage(err.message, "error");
    }
}

/* ================================ READ ================================ */

async function loadEvents(hostUsername) {
    eventListEl.innerHTML = `<p class="empty-state">Loading events...</p>`;

    try {
        const url = hostUsername
            ? `${API_BASE}?hostUsername=${encodeURIComponent(hostUsername)}`
            : API_BASE;

        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Failed to load events (${response.status})`);
        }

        const events = await response.json();
        renderEvents(events);
    } catch (err) {
        eventListEl.innerHTML = `<p class="empty-state">${escapeHtml(err.message)}</p>`;
    }
}

function renderEvents(events) {
    if (!events || events.length === 0) {
        eventListEl.innerHTML = `<p class="empty-state">No events yet. Create one on the left and it will show up here.</p>`;
        return;
    }

    eventListEl.innerHTML = events.map(renderEventCard).join("");

    events.forEach(ev => {
        document.getElementById(`edit-${ev.eventId}`)?.addEventListener("click", () => startEdit(ev));
        document.getElementById(`delete-${ev.eventId}`)?.addEventListener("click", () => deleteEvent(ev.eventId));
        document.getElementById(`publish-${ev.eventId}`)?.addEventListener("click", () => publishEvent(ev.eventId));
        document.getElementById(`cancel-${ev.eventId}`)?.addEventListener("click", () => cancelEvent(ev.eventId));
    });
}

function renderEventCard(ev) {
    const badgeClass =
        ev.status === "PUBLISHED" ? "badge-published" :
        ev.status === "CANCELLED" ? "badge-cancelled" : "badge-draft";

    const typeMeta = EVENT_TYPE_META[ev.eventType] || EVENT_TYPE_META.OTHER;

    // Banner: host's own cover photo if they set one, otherwise the
    // gradient + icon that matches the chosen event type.
    const bannerStyle = ev.coverImageUrl
        ? `style="background-image: url('${escapeHtml(ev.coverImageUrl)}');"`
        : `style="background-image: ${typeMeta.gradient};"`;

    const bannerIcon = ev.coverImageUrl ? "" : `<div class="type-icon">${typeMeta.icon}</div>`;

    return `
    <div class="event-card">
        <div class="event-banner" ${bannerStyle}>
            <span class="type-chip">${escapeHtml(typeMeta.label)}</span>
            ${bannerIcon}
        </div>
        <div class="event-body">
            <span class="badge ${badgeClass}">${escapeHtml(ev.status)}</span>
            <div class="event-title">${escapeHtml(ev.eventName)}</div>
            <div class="event-meta">${escapeHtml(ev.eventDate)}</div>
            <div class="event-meta">Host: ${escapeHtml(ev.hostUsername)}</div>
            <div class="event-meta">${escapeHtml(ev.visibility)} &middot; ${ev.theme ? escapeHtml(ev.theme) : "No theme set"}</div>

            <div class="event-actions">
                <button id="edit-${ev.eventId}" class="btn btn-ghost btn-sm">Edit</button>
                ${ev.status !== "PUBLISHED" ? `<button id="publish-${ev.eventId}" class="btn btn-success btn-sm">Publish</button>` : ""}
                ${ev.status !== "CANCELLED" ? `<button id="cancel-${ev.eventId}" class="btn btn-ghost btn-sm">Cancel</button>` : ""}
                <button id="delete-${ev.eventId}" class="btn btn-danger btn-sm">Delete</button>
            </div>
        </div>
    </div>`;
}

/* ================================ EDIT ================================ */

function startEdit(ev) {
    isEditMode = true;
    formTitle.textContent = `Editing: ${ev.eventName}`;
    submitBtn.textContent = "Update event";
    cancelEditBtn.style.display = "inline-block";

    eventIdField.value = ev.eventId;
    hostUsernameField.value = ev.hostUsername;
    eventNameField.value = ev.eventName;
    eventTypeField.value = ev.eventType;
    eventDateField.value = ev.eventDate;
    themeField.value = ev.theme || "";
    visibilityField.value = ev.visibility;
    coverImageUrlField.value = ev.coverImageUrl || "";
    descriptionField.value = ev.description || "";

    window.scrollTo({ top: 0, behavior: "smooth" });
}

function resetForm() {
    isEditMode = false;
    formTitle.textContent = "Create a new event";
    submitBtn.textContent = "Save draft";
    cancelEditBtn.style.display = "none";
    eventForm.reset();
    eventIdField.value = "";
}

/* =============================== DELETE =============================== */

async function deleteEvent(eventId) {
    if (!confirm("Delete this event? This cannot be undone.")) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/${eventId}`, { method: "DELETE" });
        if (!response.ok && response.status !== 204) {
            const errorBody = await safeReadJson(response);
            throw new Error(errorBody?.message || `Delete failed (${response.status})`);
        }
        loadEvents(filterHostField.value.trim() || undefined);
    } catch (err) {
        alert(err.message);
    }
}

/* ========================= PUBLISH / CANCEL ============================ */

async function publishEvent(eventId) {
    await patchStatus(eventId, "publish");
}

async function cancelEvent(eventId) {
    await patchStatus(eventId, "cancel");
}

async function patchStatus(eventId, action) {
    try {
        const response = await fetch(`${API_BASE}/${eventId}/${action}`, { method: "PATCH" });
        if (!response.ok) {
            const errorBody = await safeReadJson(response);
            throw new Error(errorBody?.message || `Action failed (${response.status})`);
        }
        loadEvents(filterHostField.value.trim() || undefined);
    } catch (err) {
        alert(err.message);
    }
}

/* ================================ UTILS ================================ */

function showMessage(text, type) {
    formMessage.textContent = text;
    formMessage.className = `form-message ${type}`;
}

function clearMessage() {
    formMessage.textContent = "";
    formMessage.className = "form-message";
}

async function safeReadJson(response) {
    try {
        return await response.json();
    } catch {
        return null;
    }
}

function escapeHtml(value) {
    if (value === null || value === undefined) return "";
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
