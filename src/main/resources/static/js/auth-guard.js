// Attaches the signed-in user's JWT to every API call, so the server's JwtAuthFilter can prove
// a request claiming an identity (X-User-Email) really was made by that logged-in user, instead
// of trusting the header alone. Patched once, globally, so no individual page's fetch() calls
// need to change.
(function () {
    if (window.fetch.__artAuthPatched) return;
    const originalFetch = window.fetch;
    window.fetch = function (input, init) {
        const url = typeof input === 'string' ? input : (input && input.url) || '';
        if (url.startsWith('/api/')) {
            const token = localStorage.getItem('art_auth_token');
            if (token) {
                init = init || {};
                const headers = new Headers(init.headers || (typeof input !== 'string' ? input.headers : undefined) || {});
                if (!headers.has('Authorization')) headers.set('Authorization', 'Bearer ' + token);
                init.headers = headers;
            }
        }
        return originalFetch.call(this, input, init);
    };
    window.fetch.__artAuthPatched = true;
})();

function artLogout(event) {
    if (event) event.preventDefault();
    localStorage.removeItem('art_authenticated');
    localStorage.removeItem('art_auth_token');
    localStorage.removeItem('art_user_role');
    localStorage.removeItem('art_user_email');
    localStorage.removeItem('art_user_matricule');
    localStorage.removeItem('art_user_designation');
    window.location.href = '/login.html';
}

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
