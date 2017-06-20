document.getElementsByTagName('form')[0].onsubmit = function() {
    var username, password, publicStation;
    var inputs = document.getElementsByTagName('input');
    var publicStation = document.getElementsByName('publicWorkstation')[0].checked
    for (var i = 0; i < inputs.length; i++) {
        if (inputs[i].name.toLowerCase() === 'password') {
            password = inputs[i] === null ? '' : inputs[i].value;
        } else if (inputs[i].name.toLowerCase() === 'username') {
            inputs[i].value = inputs[i].value.trim();
            username = inputs[i] === null ? '' : inputs[i].value;
        } else if (inputs[i].name === 'publicWorkstation') {
            publicStation = inputs[i].checked;
        }
    }
    TracsWebView.deliver(username, password, !publicStation);

    return true;
}
