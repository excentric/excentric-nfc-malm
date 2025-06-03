// Get the modal
const modal = document.getElementById("imageModal");
const modalImg = document.getElementById("fullImage");

// Function to open the modal
function openModal(imgSrc) {
    modal.style.display = "block";
    modalImg.src = imgSrc;
}

// Function to close the modal
function closeModal() {
    modal.style.display = "none";
}

// Close the modal when clicking outside the image
window.onclick = function (event) {
    if (event.target === modal) {
        closeModal();
    }
}

// Close the modal when ESC key is pressed
document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
        closeModal();
    }
});

// Function to select a cover art and refresh the page
function selectAndClose(url) {
    // Make an AJAX call to the selection endpoint
    fetch(url)
        .then(response => {
            // Reload the page to reflect the new state
            window.location.reload();
        })
        .catch(error => {
            console.error('Error selecting cover art:', error);
            alert('Error selecting cover art. Please try again.');
        });
}
