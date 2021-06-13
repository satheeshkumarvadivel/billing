var properties = {
    api_host: 'http://localhost:8000'
}
function af() {
    if (localStorage.getItem('a') == null || localStorage.getItem('a') != 's') {
        window.location.href = "/login";
    }
}
af();