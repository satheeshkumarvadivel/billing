var properties = {
    api_host: 'http://3.6.139.82'
}
function af() {
    if (localStorage.getItem('a') == null || localStorage.getItem('a') != 's') {
        window.location.href = "/login";
    }
}
af();