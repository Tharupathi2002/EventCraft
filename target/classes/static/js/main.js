// EventCraft - Interactive Client-side Scripting
document.addEventListener('DOMContentLoaded', function () {
    console.log("EventCraft Vendor & Venue Module Initialized.");

    // Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.style.opacity = '0';
            alert.style.transition = 'opacity 0.5s ease';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    // Initialize Dashboard Charts if Canvas Elements exist
    initCharts();
});

function initCharts() {
    // Booking Activity Line Chart
    const lineCtx = document.getElementById('bookingTrendChart');
    if (lineCtx) {
        new Chart(lineCtx, {
            type: 'line',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep'],
                datasets: [{
                    label: 'Bookings & Requests',
                    data: [12, 19, 15, 25, 32, 28, 40, 48, 55],
                    borderColor: '#e67e22',
                    backgroundColor: 'rgba(230, 126, 34, 0.15)',
                    fill: true,
                    tension: 0.4,
                    borderWidth: 3,
                    pointBackgroundColor: '#f39c12',
                    pointRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: {
                        grid: { color: 'rgba(255, 255, 255, 0.05)' },
                        ticks: { color: '#a39288' }
                    },
                    y: {
                        grid: { color: 'rgba(255, 255, 255, 0.05)' },
                        ticks: { color: '#a39288' }
                    }
                }
            }
        });
    }

    // Vendor Category Breakdown Donut Chart
    const donutCtx = document.getElementById('categoryDonutChart');
    if (donutCtx) {
        new Chart(donutCtx, {
            type: 'doughnut',
            data: {
                labels: ['Catering', 'Photography', 'Decor', 'Music & DJ', 'Others'],
                datasets: [{
                    data: [35, 25, 15, 15, 10],
                    backgroundColor: ['#e67e22', '#f39c12', '#27ae60', '#3498db', '#9b59b6'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'right',
                        labels: { color: '#f5ebe6', font: { family: 'Plus Jakarta Sans' } }
                    }
                },
                cutout: '70%'
            }
        });
    }
}
