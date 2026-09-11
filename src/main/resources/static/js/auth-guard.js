(function () {
    const isAuthenticated = localStorage.getItem('art_authenticated') === 'true';
    const role = (localStorage.getItem('art_user_role') || '').toUpperCase();
    const path = window.location.pathname || '/';

    // Pages that render without authentication (landing / overview pages).
    const publicPages = [
        '/',
        '/index.html',
        '/login.html',
        '/departments.html',
        '/mandats.html',
        '/annual-dashboard.html',
        '/mission-payment.html'
    ];

    // Pages reserved to the application administrator only.
    const adminOnlyPages = [
        '/admin-panel.html'
    ];

    const isPublic = publicPages.includes(path);
    const isAdminOnly = adminOnlyPages.includes(path);

    if (!isAuthenticated && !isPublic) {
        window.location.href = '/login.html';
        return;
    }

    if (isAdminOnly && role !== 'ROLE_ADMIN') {
        alert("Accès refusé : ce module est réservé à l'administrateur de l'application.\n"
            + "Access denied: this module is reserved for the application administrator.");
        window.location.href = '/index.html';
    }
})();
