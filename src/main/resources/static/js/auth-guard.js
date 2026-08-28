(function () {
    const isAuthenticated = localStorage.getItem('art_authenticated') === 'true';
    const isLoginPage = window.location.pathname.endsWith('/login.html') || window.location.pathname === '/';
    if (!isAuthenticated && !isLoginPage) {
        window.location.href = '/login.html';
    }
})();
