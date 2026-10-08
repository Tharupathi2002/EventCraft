/**
 * EventCraft - Task & Schedule Management API Service Layer
 * Centralized HTTP service communicating with Spring Boot REST endpoints.
 */

const BASE_URL = '/api';

/**
 * Generic fetch wrapper to handle JSON encoding, headers, and standard error extraction.
 */
async function apiRequest(endpoint, options = {}) {
    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        },
        ...options
    };

    try {
        const response = await fetch(`${BASE_URL}${endpoint}`, config);
        const data = await response.json().catch(() => null);

        if (!response.ok) {
            let errorMsg = (data && data.message) ? data.message : `HTTP error ${response.status}: ${response.statusText}`;
            if (data && data.data && typeof data.data === 'object') {
                // Collect validation field errors if available
                const fieldErrors = Object.entries(data.data).map(([field, msg]) => `${field}: ${msg}`).join(', ');
                if (fieldErrors) errorMsg += ` (${fieldErrors})`;
            }
            const error = new Error(errorMsg);
            error.status = response.status;
            error.payload = data;
            throw error;
        }

        return data;
    } catch (err) {
        console.error(`API Error on [${options.method || 'GET'}] ${endpoint}:`, err);
        throw err;
    }
}

/**
 * Task Management API Service
 */
export const TaskAPI = {
    /**
     * GET /api/tasks (with optional query parameters)
     */
    async getAll(eventId = 1, status = '', assigneeId = '') {
        const params = new URLSearchParams();
        if (eventId) params.append('eventId', eventId);
        if (status) params.append('status', status);
        if (assigneeId) params.append('assigneeId', assigneeId);

        const query = params.toString() ? `?${params.toString()}` : '';
        return apiRequest(`/tasks${query}`, { method: 'GET' });
    },

    /**
     * GET /api/tasks/{id}
     */
    async getById(taskId) {
        return apiRequest(`/tasks/${taskId}`, { method: 'GET' });
    },

    /**
     * POST /api/tasks
     */
    async create(taskData) {
        return apiRequest('/tasks', {
            method: 'POST',
            body: JSON.stringify(taskData)
        });
    },

    /**
     * PUT /api/tasks/{id}
     */
    async update(taskId, taskData) {
        return apiRequest(`/tasks/${taskId}`, {
            method: 'PUT',
            body: JSON.stringify(taskData)
        });
    },

    /**
     * DELETE /api/tasks/{id}
     */
    async delete(taskId) {
        return apiRequest(`/tasks/${taskId}`, {
            method: 'DELETE'
        });
    },

    /**
     * GET /api/tasks/metrics/{eventId} (For Member 6 Event Dashboard)
     */
    async getMetrics(eventId = 1) {
        return apiRequest(`/tasks/metrics/${eventId}`, { method: 'GET' });
    }
};

/**
 * Schedule & Timeline API Service
 */
export const ScheduleAPI = {
    /**
     * GET /api/schedules (chronological list)
     */
    async getAll(eventId = 1) {
        const params = new URLSearchParams();
        if (eventId) params.append('eventId', eventId);

        const query = params.toString() ? `?${params.toString()}` : '';
        return apiRequest(`/schedules${query}`, { method: 'GET' });
    },

    /**
     * GET /api/schedules/{id}
     */
    async getById(scheduleId) {
        return apiRequest(`/schedules/${scheduleId}`, { method: 'GET' });
    },

    /**
     * POST /api/schedules
     */
    async create(scheduleData) {
        return apiRequest('/schedules', {
            method: 'POST',
            body: JSON.stringify(scheduleData)
        });
    },

    /**
     * PUT /api/schedules/{id}
     */
    async update(scheduleId, scheduleData) {
        return apiRequest(`/schedules/${scheduleId}`, {
            method: 'PUT',
            body: JSON.stringify(scheduleData)
        });
    },

    /**
     * DELETE /api/schedules/{id}
     */
    async delete(scheduleId) {
        return apiRequest(`/schedules/${scheduleId}`, {
            method: 'DELETE'
        });
    }
};