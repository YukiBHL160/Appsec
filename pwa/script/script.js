const form = document.getElementById('stegForm');
const actionSelect = document.getElementById('action');
const messageInput = document.getElementById('messageInput');
const outputDiv = document.getElementById('output');

actionSelect.addEventListener('change', () => {
    if (actionSelect.value === 'decode') {
        messageInput.style.display = 'none';
    } else {
        messageInput.style.display = 'block';
    }
});

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new FormData(form);
    outputDiv.innerHTML = 'Processing...';

    try {
        const response = await fetch('/steganography/api', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            if (actionSelect.value === 'encode') {
                const blob = await response.blob();
                const url = URL.createObjectURL(blob);
                outputDiv.innerHTML = `<p>Encoded Image:</p><img src="${url}" alt="Encoded Image">`;
            } else {
                const text = await response.text();
                outputDiv.innerHTML = `<p>Decoded Message:</p><pre>${text}</pre>`;
            }
        } else {
            const errorText = await response.text();
            outputDiv.innerHTML = `<p style="color: red;">Error: ${errorText}</p>`;
        }
    } catch (error) {
        outputDiv.innerHTML = `<p style="color: red;">An unexpected error occurred: ${error.message}</p>`;
    }
});
