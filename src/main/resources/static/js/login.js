async function loginClick(){
    const login = document.getElementById('loginForm').value;
    const password = document.getElementById('passwordForm').value;
    const payload = {
        "login": login,
        "password": password
    };
    console.log(payload)
    try {
        const response = await fetch('/auth/signin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });
        console.log(response)
        if (response.ok) {
            const token = await response.text();
            console.log(token)
            window.location.href = `/login/success?token=${token}`;
        }
        else {
            const errorMsg = await response.text();
            alert('Ошибка: ' + errorMsg);
        }
    } catch (err) {
        alert(`Ошибка запроса: ${err.message}`);
        // document.getElementById('error-message').textContent = 'Ошибка запроса: ' + err.message;
    }
}