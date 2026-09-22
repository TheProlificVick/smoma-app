// Attaches the signed-in user's JWT to every API call, so the server's JwtAuthFilter can prove
// a request claiming an identity (X-User-Email) really was made by that logged-in user, instead
// of trusting the header alone. Patched once, globally, so no individual page's fetch() calls
// need to change.
(function () {
    if (window.fetch.__artAuthPatched) return;
    const originalFetch = window.fetch;
    window.fetch = function (input, init) {
        // fetch() accepts a plain string, a Request object, or a URL object — the URL case was
        // missing here (typeof a URL instance is 'object' with no '.url' property, so it fell
        // through to '', which never starts with '/api/'), silently skipping the Authorization
        // header — and the 401-detection below — for any page that calls fetch(new URL(...))
        // instead of fetch('...'). personnel.html does exactly that for its filtered search.
        const url = typeof input === 'string' ? input
            : (input instanceof URL) ? input.href
            : (input && input.url) || '';
        if (url.startsWith('/api/')) {
            const token = localStorage.getItem('art_auth_token');
            if (token) {
                init = init || {};
                const headers = new Headers(init.headers || (typeof input !== 'string' ? input.headers : undefined) || {});
                if (!headers.has('Authorization')) headers.set('Authorization', 'Bearer ' + token);
                init.headers = headers;
            }
        }
        return originalFetch.call(this, input, init).then(response => {
            // A stale localStorage "authenticated" flag with an expired/invalid JWT used to leave
            // every page silently failing every API call with an unexplained 401 — the client-side
            // page gate below only ever checks the flag, never the token's actual validity, so
            // nothing sent the user back to log in for a fresh one. JwtAuthFilter's own 401 carries
            // this exact, distinctive message; a business-logic 401 (e.g. "wrong current password"
            // on the change-password form) does not, and must NOT force a logout — the user should
            // just get to retype their password, not get bounced off the page entirely.
            if (url.startsWith('/api/') && response.status === 401) {
                response.clone().json().then(body => {
                    if (body && typeof body.error === 'string' && body.error.startsWith('Session invalide ou expirée')) {
                        localStorage.removeItem('art_authenticated');
                        localStorage.removeItem('art_auth_token');
                        localStorage.removeItem('art_user_role');
                        localStorage.removeItem('art_user_email');
                        localStorage.removeItem('art_user_matricule');
                        localStorage.removeItem('art_user_designation');
                        window.location.href = '/login.html';
                    }
                }).catch(() => {});
            }
            return response;
        });
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
