(function () {
    const isAuthenticated = localStorage.getItem('art_authenticated') === 'true';
    const path = window.location.pathname || '/';

    // Pages that should be accessible without authentication (landing/overview pages)
    const publicPages = [
        '/',
        '/index.html',
        '/login.html',
        '/departments.html',
        '/mandats.html',
        '/annual-dashboard.html',
        '/mission-payment.html'
    ];

    const isPublic = publicPages.includes(path);

    if (!isAuthenticated && !isPublic) {
        // Redirect to login for protected pages only
        window.location.href = '/login.html';
    }
})();
