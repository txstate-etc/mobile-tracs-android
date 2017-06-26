document.querySelector('form').onsubmit = function() {
    var username = document.querySelector('form input[name="username"]').value = document.querySelector('form input[name="username"]').value.toLowerCase().trim();
    var password = document.querySelector('form input[name="password"]').value;
    document.querySelector('form input[name="publicWorkstation"]').checked = false;
    TracsWebView.deliver(username, password);
    return true;
}
