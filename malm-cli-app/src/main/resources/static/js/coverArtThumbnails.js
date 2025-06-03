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
