/**
 * EventCraft - Task and Schedule Management Application Controller
 * Handles user interactions, form submissions, UI state, and API integration.
 */

import { TaskAPI, ScheduleAPI } from './api.js';

// Application State
const state = {
    currentEventId: 1,
    activeTab: 'tasks', // 'tasks' or 'schedules'
    tasks: [],
    schedules: [],
    metrics: null,
    taskFilterStatus: '',
    deleteTarget: null // { type: 'task' | 'schedule', id: number, name: string }
};

// DOM Elements
const elements = {
    // Tabs
    tabTasksBtn: document.getElementById('tabTasksBtn'),
    tabSchedulesBtn: document.getElementById('tabSchedulesBtn'),
    tasksView: document.getElementById('tasksView'),
    schedulesView: document.getElementById('schedulesView'),
    
    // Badges & Metrics
    tasksBadge: document.getElementById('tasksBadge'),
    schedulesBadge: document.getElementById('schedulesBadge'),
    metricTotal: document.getElementById('metricTotal'),
    metricCompleted: document.getElementById('metricCompleted'),
    metricPending: document.getElementById('metricPending'),
    metricProgress: document.getElementById('metricProgress'),

    // Containers
    tasksContainer: document.getElementById('tasksContainer'),
    schedulesContainer: document.getElementById('schedulesContainer'),

    // Task Controls
    btnOpenCreateTask: document.getElementById('btnOpenCreateTask'),
    taskStatusFilter: document.getElementById('taskStatusFilter'),

    // Schedule Controls
    btnOpenCreateSchedule: document.getElementById('btnOpenCreateSchedule'),

    // Task Modal
    taskModal: document.getElementById('taskModal'),
    taskForm: document.getElementById('taskForm'),
    taskModalTitle: document.getElementById('taskModalTitle'),
    taskIdInput: document.getElementById('taskIdInput'),
    taskTitleInput: document.getElementById('taskTitleInput'),
    taskDescInput: document.getElementById('taskDescInput'),
    taskDeadlineInput: document.getElementById('taskDeadlineInput'),
    taskPriorityInput: document.getElementById('taskPriorityInput'),
    taskStatusGroup: document.getElementById('taskStatusGroup'),
    taskStatusInput: document.getElementById('taskStatusInput'),
    taskProgressGroup: document.getElementById('taskProgressGroup'),
    taskProgressInput: document.getElementById('taskProgressInput'),
    taskProgressVal: document.getElementById('taskProgressVal'),
    taskAssigneeInput: document.getElementById('taskAssigneeInput'),
    btnCancelTask: document.getElementById('btnCancelTask'),

    // Schedule Modal
    scheduleModal: document.getElementById('scheduleModal'),
    scheduleForm: document.getElementById('scheduleForm'),
    scheduleModalTitle: document.getElementById('scheduleModalTitle'),
    scheduleIdInput: document.getElementById('scheduleIdInput'),
    activityNameInput: document.getElementById('activityNameInput'),
    scheduleDescInput: document.getElementById('scheduleDescInput'),
    startTimeInput: document.getElementById('startTimeInput'),
    endTimeInput: document.getElementById('endTimeInput'),
    activityTypeInput: document.getElementById('activityTypeInput'),
    locationInput: document.getElementById('locationInput'),
    btnCancelSchedule: document.getElementById('btnCancelSchedule'),

    // Delete Confirmation Modal
    deleteModal: document.getElementById('deleteModal'),
    deleteItemName: document.getElementById('deleteItemName'),
    btnConfirmDelete: document.getElementById('btnConfirmDelete'),
    btnCancelDelete: document.getElementById('btnCancelDelete'),

    // Toast Container
    toastContainer: document.getElementById('toastContainer')
};

// ==========================================================================
// Notification Toasts
// ==========================================================================
function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = '✓';
    if (type === 'error') icon = '✕';
    if (type === 'warning') icon = '⚠';

    toast.innerHTML = `<span><strong>${icon}</strong></span> <span>${escapeHtml(message)}</span>`;
    elements.toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}

function escapeHtml(text) {
    if (!text) return '';
    const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    return String(text).replace(/[&<>"']/g, m => map[m]);
}

// Convert ISO string to format suitable for <input type="datetime-local">
function formatForDateTimeInput(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const pad = num => String(num).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

// Format friendly date string
function formatFriendlyDate(dateStr) {
    if (!dateStr) return 'N/A';
    const d = new Date(dateStr);
    return d.toLocaleString('en-US', {
        month: 'short', day: 'numeric', year: 'numeric',
        hour: 'numeric', minute: '2-digit', hour12: true
    });
}

function formatTimeOnly(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true });
}

// ==========================================================================
// Initialization & Tab Navigation
// ==========================================================================
document.addEventListener('DOMContentLoaded', () => {
    bindEvents();
    loadDashboardMetrics();
    loadTasks();
    loadSchedules();
});

function bindEvents() {
    // Tab switching
    elements.tabTasksBtn.addEventListener('click', () => switchTab('tasks'));
    elements.tabSchedulesBtn.addEventListener('click', () => switchTab('schedules'));

    // Task form events
    elements.btnOpenCreateTask.addEventListener('click', () => openCreateTaskModal());
    elements.btnCancelTask.addEventListener('click', () => elements.taskModal.close());
    elements.taskForm.addEventListener('submit', handleTaskFormSubmit);
    elements.taskStatusFilter.addEventListener('change', (e) => {
        state.taskFilterStatus = e.target.value;
        loadTasks();
    });

    elements.taskProgressInput.addEventListener('input', (e) => {
        const val = e.target.value;
        elements.taskProgressVal.textContent = `${val}%`;
        if (val === '100') {
            elements.taskStatusInput.value = 'Completed';
        } else if (val > 0 && elements.taskStatusInput.value === 'Pending') {
            elements.taskStatusInput.value = 'In Progress';
        }
    });

    elements.taskStatusInput.addEventListener('change', (e) => {
        if (e.target.value === 'Completed') {
            elements.taskProgressInput.value = 100;
            elements.taskProgressVal.textContent = '100%';
        }
    });

    // Schedule form events
    elements.btnOpenCreateSchedule.addEventListener('click', () => openCreateScheduleModal());
    elements.btnCancelSchedule.addEventListener('click', () => elements.scheduleModal.close());
    elements.scheduleForm.addEventListener('submit', handleScheduleFormSubmit);

    // Delete confirmation events
    elements.btnCancelDelete.addEventListener('click', () => elements.deleteModal.close());
    elements.btnConfirmDelete.addEventListener('click', handleConfirmDelete);
}

function switchTab(tab) {
    state.activeTab = tab;
    if (tab === 'tasks') {
        elements.tabTasksBtn.classList.add('active');
        elements.tabSchedulesBtn.classList.remove('active');
        elements.tasksView.style.display = 'block';
        elements.schedulesView.style.display = 'none';
        loadTasks();
    } else {
        elements.tabSchedulesBtn.classList.add('active');
        elements.tabTasksBtn.classList.remove('active');
        elements.schedulesView.style.display = 'block';
        elements.tasksView.style.display = 'none';
        loadSchedules();
    }
}

// ==========================================================================
// Metrics & Dashboard Header
// ==========================================================================
async function loadDashboardMetrics() {
    try {
        const res = await TaskAPI.getMetrics(state.currentEventId);
        if (res && res.data) {
            const m = res.data;
            state.metrics = m;
            elements.metricTotal.textContent = m.totalTasks;
            elements.metricCompleted.textContent = m.completedTasks;
            elements.metricPending.textContent = m.pendingTasks;
            elements.metricProgress.textContent = `${m.completionPercentage}%`;
        }
    } catch (err) {
        console.warn('Could not load dashboard metrics:', err.message);
    }
}

// ==========================================================================
// Tasks Operations (CRUD)
// ==========================================================================
async function loadTasks() {
    showLoading(elements.tasksContainer, 'Loading tasks from database...');
    try {
        const response = await TaskAPI.getAll(state.currentEventId, state.taskFilterStatus);
        state.tasks = response.data || [];
        elements.tasksBadge.textContent = state.tasks.length;
        renderTasks(state.tasks);
        loadDashboardMetrics();
    } catch (err) {
        showErrorState(elements.tasksContainer, 'Failed to load tasks', err.message, () => loadTasks());
    }
}

function renderTasks(tasks) {
    if (!tasks || tasks.length === 0) {
        elements.tasksContainer.innerHTML = `
            <div class="state-container" style="grid-column: 1 / -1;">
                <div class="empty-icon">📋</div>
                <div class="empty-title">No tasks found</div>
                <div class="empty-subtitle">Create a task to assign deadlines and track event preparation progress.</div>
                <button class="btn btn-primary btn-sm" style="margin-top: 1rem;" onclick="document.getElementById('btnOpenCreateTask').click()">
                    + Add First Task
                </button>
            </div>
        `;
        return;
    }

    elements.tasksContainer.innerHTML = tasks.map(task => {
        const isComplete = task.status === 'Completed' || task.progress === 100;
        const progressClass = isComplete ? 'complete' : '';
        const priorityClass = `priority-${task.priority}`;
        const statusClass = `status-${task.status.replace(/\s+/g, '-')}`;

        return `
            <div class="task-card" data-id="${task.taskId}">
                <div>
                    <div class="task-card-header">
                        <span class="badge ${priorityClass}">${escapeHtml(task.priority)}</span>
                        <span class="badge ${statusClass}">${escapeHtml(task.status)}</span>
                    </div>
                    <h3 class="task-title">${escapeHtml(task.title)}</h3>
                    <p class="task-desc">${escapeHtml(task.description || 'No detailed instructions provided.')}</p>
                </div>

                <div>
                    <div class="progress-container">
                        <div class="progress-header">
                            <span>Progress</span>
                            <span>${task.progress}%</span>
                        </div>
                        <div class="progress-track">
                            <div class="progress-fill ${progressClass}" style="width: ${task.progress}%"></div>
                        </div>
                    </div>

                    <div class="task-footer">
                        <div class="task-deadline">
                            <span>📅 Due:</span>
                            <strong>${formatFriendlyDate(task.deadline)}</strong>
                        </div>
                        <div class="task-assignee">
                            <span>👤 ${escapeHtml(task.assigneeName || 'Unassigned')}</span>
                        </div>
                    </div>

                    <div class="card-actions">
                        ${!isComplete ? `
                            <button class="btn btn-sm btn-secondary btn-quick-done" data-id="${task.taskId}">
                                ✓ Mark Done
                            </button>
                        ` : ''}
                        <button class="btn btn-sm btn-secondary btn-edit-task" data-id="${task.taskId}">
                            ✏ Edit
                        </button>
                        <button class="btn btn-sm btn-danger btn-delete-task" data-id="${task.taskId}" data-name="${escapeHtml(task.title)}">
                            🗑 Delete
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');

    // Attach card button listeners
    elements.tasksContainer.querySelectorAll('.btn-edit-task').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = parseInt(e.currentTarget.dataset.id, 10);
            openEditTaskModal(id);
        });
    });

    elements.tasksContainer.querySelectorAll('.btn-delete-task').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = parseInt(e.currentTarget.dataset.id, 10);
            const name = e.currentTarget.dataset.name;
            promptDelete('task', id, name);
        });
    });

    elements.tasksContainer.querySelectorAll('.btn-quick-done').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const id = parseInt(e.currentTarget.dataset.id, 10);
            await quickCompleteTask(id);
        });
    });
}

function openCreateTaskModal() {
    elements.taskForm.reset();
    elements.taskModalTitle.textContent = 'Create New Task';
    elements.taskIdInput.value = '';
    elements.taskStatusGroup.style.display = 'none';
    elements.taskProgressGroup.style.display = 'none';
    elements.taskPriorityInput.value = 'Medium';
    elements.taskProgressInput.value = 0;
    elements.taskProgressVal.textContent = '0%';

    // Set default deadline to tomorrow 5:00 PM
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(17, 0, 0, 0);
    elements.taskDeadlineInput.value = formatForDateTimeInput(tomorrow);

    elements.taskModal.showModal();
}

async function openEditTaskModal(taskId) {
    try {
        const res = await TaskAPI.getById(taskId);
        const task = res.data;

        elements.taskModalTitle.textContent = 'Edit Task';
        elements.taskIdInput.value = task.taskId;
        elements.taskTitleInput.value = task.title;
        elements.taskDescInput.value = task.description || '';
        elements.taskDeadlineInput.value = formatForDateTimeInput(task.deadline);
        elements.taskPriorityInput.value = task.priority;

        elements.taskStatusGroup.style.display = 'block';
        elements.taskStatusInput.value = task.status;

        elements.taskProgressGroup.style.display = 'block';
        elements.taskProgressInput.value = task.progress;
        elements.taskProgressVal.textContent = `${task.progress}%`;

        elements.taskAssigneeInput.value = task.assignedTo || '';

        elements.taskModal.showModal();
    } catch (err) {
        showToast(`Failed to load task details: ${err.message}`, 'error');
    }
}

async function handleTaskFormSubmit(e) {
    e.preventDefault();

    const taskId = elements.taskIdInput.value;
    const title = elements.taskTitleInput.value.trim();
    const description = elements.taskDescInput.value.trim();
    const deadlineVal = elements.taskDeadlineInput.value;
    const priority = elements.taskPriorityInput.value;
    const assignedTo = elements.taskAssigneeInput.value ? parseInt(elements.taskAssigneeInput.value, 10) : null;

    if (!title) {
        showToast('Please enter a task title', 'error');
        return;
    }

    if (!deadlineVal) {
        showToast('Please specify a valid deadline', 'error');
        return;
    }

    const selectedDeadline = new Date(deadlineVal);

    if (!taskId) {
        // CREATE Mode (POST)
        if (selectedDeadline <= new Date()) {
            showToast('Deadline must be set to a future date/time', 'error');
            return;
        }

        const payload = {
            eventId: state.currentEventId,
            title,
            description,
            deadline: deadlineVal,
            priority,
            assignedTo,
            createdBy: 1 // Host ID
        };

        try {
            await TaskAPI.create(payload);
            showToast('Task created successfully!', 'success');
            elements.taskModal.close();
            loadTasks();
        } catch (err) {
            showToast(err.message, 'error');
        }
    } else {
        // EDIT Mode (PUT)
        const progress = parseInt(elements.taskProgressInput.value, 10);
        const status = elements.taskStatusInput.value;

        const payload = {
            title,
            description,
            deadline: deadlineVal,
            priority,
            status,
            progress,
            assignedTo
        };

        try {
            await TaskAPI.update(taskId, payload);
            showToast('Task updated successfully!', 'success');
            elements.taskModal.close();
            loadTasks();
        } catch (err) {
            showToast(err.message, 'error');
        }
    }
}

async function quickCompleteTask(taskId) {
    try {
        await TaskAPI.update(taskId, { progress: 100, status: 'Completed' });
        showToast('Task marked as Completed!', 'success');
        loadTasks();
    } catch (err) {
        showToast(`Failed to update task: ${err.message}`, 'error');
    }
}

// ==========================================================================
// Schedules Operations (CRUD & Conflict Alert)
// ==========================================================================
async function loadSchedules() {
    showLoading(elements.schedulesContainer, 'Loading timeline agenda from database...');
    try {
        const response = await ScheduleAPI.getAll(state.currentEventId);
        state.schedules = response.data || [];
        elements.schedulesBadge.textContent = state.schedules.length;
        renderSchedules(state.schedules);
    } catch (err) {
        showErrorState(elements.schedulesContainer, 'Failed to load event schedule', err.message, () => loadSchedules());
    }
}

function renderSchedules(schedules) {
    if (!schedules || schedules.length === 0) {
        elements.schedulesContainer.innerHTML = `
            <div class="state-container">
                <div class="empty-icon">⏱</div>
                <div class="empty-title">No schedule items added</div>
                <div class="empty-subtitle">Add activities, keynote sessions, and milestones to build your event agenda.</div>
                <button class="btn btn-primary btn-sm" style="margin-top: 1rem;" onclick="document.getElementById('btnOpenCreateSchedule').click()">
                    + Add Timeline Activity
                </button>
            </div>
        `;
        return;
    }

    const html = schedules.map(item => {
        const startTimeStr = formatTimeOnly(item.startTime);
        const endTimeStr = formatTimeOnly(item.endTime);
        const duration = item.durationMinutes ? `${item.durationMinutes} mins` : '';

        return `
            <div class="timeline-item" data-id="${item.scheduleId}">
                <div class="timeline-dot"></div>
                <div class="timeline-card">
                    <div class="timeline-time-col">
                        <div class="time-range">${startTimeStr} – ${endTimeStr}</div>
                        <span class="duration-tag">⏱ ${duration}</span>
                    </div>

                    <div class="timeline-info-col">
                        <div class="timeline-title-row">
                            <span class="timeline-activity-name">${escapeHtml(item.activityName)}</span>
                            <span class="badge priority-Medium">${escapeHtml(item.activityType)}</span>
                        </div>
                        <p class="timeline-desc">${escapeHtml(item.description || 'No additional details.')}</p>
                        ${item.locationOrStage ? `
                            <span class="timeline-location">📍 ${escapeHtml(item.locationOrStage)}</span>
                        ` : ''}
                    </div>

                    <div class="card-actions" style="margin-top: 0;">
                        <button class="btn btn-sm btn-secondary btn-edit-schedule" data-id="${item.scheduleId}">
                            ✏ Edit
                        </button>
                        <button class="btn btn-sm btn-danger btn-delete-schedule" data-id="${item.scheduleId}" data-name="${escapeHtml(item.activityName)}">
                            🗑 Delete
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');

    elements.schedulesContainer.innerHTML = `<div class="timeline-container">${html}</div>`;

    // Attach schedule action listeners
    elements.schedulesContainer.querySelectorAll('.btn-edit-schedule').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = parseInt(e.currentTarget.dataset.id, 10);
            openEditScheduleModal(id);
        });
    });

    elements.schedulesContainer.querySelectorAll('.btn-delete-schedule').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = parseInt(e.currentTarget.dataset.id, 10);
            const name = e.currentTarget.dataset.name;
            promptDelete('schedule', id, name);
        });
    });
}

function openCreateScheduleModal() {
    elements.scheduleForm.reset();
    elements.scheduleModalTitle.textContent = 'Add Timeline Activity';
    elements.scheduleIdInput.value = '';
    elements.activityTypeInput.value = 'Activity';

    // Default to event day (e.g. 2026-11-20 14:00 to 15:00)
    const start = new Date('2026-11-20T14:00:00');
    const end = new Date('2026-11-20T15:00:00');
    elements.startTimeInput.value = formatForDateTimeInput(start);
    elements.endTimeInput.value = formatForDateTimeInput(end);

    elements.scheduleModal.showModal();
}

async function openEditScheduleModal(scheduleId) {
    try {
        const res = await ScheduleAPI.getById(scheduleId);
        const item = res.data;

        elements.scheduleModalTitle.textContent = 'Edit Schedule Activity';
        elements.scheduleIdInput.value = item.scheduleId;
        elements.activityNameInput.value = item.activityName;
        elements.scheduleDescInput.value = item.description || '';
        elements.startTimeInput.value = formatForDateTimeInput(item.startTime);
        elements.endTimeInput.value = formatForDateTimeInput(item.endTime);
        elements.activityTypeInput.value = item.activityType;
        elements.locationInput.value = item.locationOrStage || '';

        elements.scheduleModal.showModal();
    } catch (err) {
        showToast(`Failed to load schedule details: ${err.message}`, 'error');
    }
}

async function handleScheduleFormSubmit(e) {
    e.preventDefault();

    const scheduleId = elements.scheduleIdInput.value;
    const activityName = elements.activityNameInput.value.trim();
    const description = elements.scheduleDescInput.value.trim();
    const startTimeVal = elements.startTimeInput.value;
    const endTimeVal = elements.endTimeInput.value;
    const activityType = elements.activityTypeInput.value;
    const locationOrStage = elements.locationInput.value.trim();

    if (!activityName) {
        showToast('Please enter an activity name', 'error');
        return;
    }

    if (!startTimeVal || !endTimeVal) {
        showToast('Please provide both start and end time', 'error');
        return;
    }

    const start = new Date(startTimeVal);
    const end = new Date(endTimeVal);

    if (end <= start) {
        showToast('End time must be strictly after Start time', 'error');
        return;
    }

    if (!scheduleId) {
        // CREATE Mode (POST)
        const payload = {
            eventId: state.currentEventId,
            activityName,
            description,
            startTime: startTimeVal,
            endTime: endTimeVal,
            activityType,
            locationOrStage
        };

        try {
            await ScheduleAPI.create(payload);
            showToast('Timeline activity added successfully!', 'success');
            elements.scheduleModal.close();
            loadSchedules();
        } catch (err) {
            // Check if HTTP 409 conflict
            if (err.status === 409) {
                showToast(`Conflict Alert: ${err.message}`, 'error');
            } else {
                showToast(err.message, 'error');
            }
        }
    } else {
        // EDIT Mode (PUT)
        const payload = {
            activityName,
            description,
            startTime: startTimeVal,
            endTime: endTimeVal,
            activityType,
            locationOrStage
        };

        try {
            await ScheduleAPI.update(scheduleId, payload);
            showToast('Schedule activity updated successfully!', 'success');
            elements.scheduleModal.close();
            loadSchedules();
        } catch (err) {
            if (err.status === 409) {
                showToast(`Conflict Alert: ${err.message}`, 'error');
            } else {
                showToast(err.message, 'error');
            }
        }
    }
}

// ==========================================================================
// Delete Confirmation Handling
// ==========================================================================
function promptDelete(type, id, name) {
    state.deleteTarget = { type, id, name };
    elements.deleteItemName.textContent = `"${name}" (${type.toUpperCase()})`;
    elements.deleteModal.showModal();
}

async function handleConfirmDelete() {
    if (!state.deleteTarget) return;

    const { type, id } = state.deleteTarget;
    elements.deleteModal.close();

    try {
        if (type === 'task') {
            await TaskAPI.delete(id);
            showToast('Task deleted successfully', 'success');
            loadTasks();
        } else if (type === 'schedule') {
            await ScheduleAPI.delete(id);
            showToast('Schedule activity removed successfully', 'success');
            loadSchedules();
        }
    } catch (err) {
        showToast(`Failed to delete: ${err.message}`, 'error');
    } finally {
        state.deleteTarget = null;
    }
}

// ==========================================================================
// UI Helpers (Loading & Error States)
// ==========================================================================
function showLoading(container, text = 'Loading...') {
    container.innerHTML = `
        <div class="state-container" style="grid-column: 1 / -1;">
            <div class="spinner"></div>
            <div class="empty-subtitle">${escapeHtml(text)}</div>
        </div>
    `;
}

function showErrorState(container, title, message, retryCallback) {
    container.innerHTML = `
        <div class="state-container" style="grid-column: 1 / -1; border-color: var(--danger);">
            <div class="empty-icon">⚠️</div>
            <div class="empty-title" style="color: var(--danger);">${escapeHtml(title)}</div>
            <div class="empty-subtitle">${escapeHtml(message)}</div>
            <button class="btn btn-secondary btn-sm" style="margin-top: 1rem;" id="btnRetry">
                ↺ Try Again
            </button>
        </div>
    `;
    const btn = container.querySelector('#btnRetry');
    if (btn && retryCallback) {
        btn.addEventListener('click', retryCallback);
    }
}