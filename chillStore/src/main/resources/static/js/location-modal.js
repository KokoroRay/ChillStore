// Location Modal JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('Location modal script loaded');
    
    const locationBtn = document.querySelector('.location-btn');
    const closeBtn = document.querySelector('.location-close');
    const modal = document.getElementById('locationModal');
    
    console.log('Location button found:', locationBtn);
    console.log('Close button found:', closeBtn);
    console.log('Modal found:', modal);
    
    function openLocationModal() {
        console.log('Opening location modal');
        if (modal) {
            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
        }
    }

    function closeLocationModal() {
        console.log('Closing location modal');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    }

    // Add event listeners
    if (locationBtn) {
        locationBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            openLocationModal();
        });
    }
    
    if (closeBtn) {
        closeBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            closeLocationModal();
        });
    }

    // Close modal when clicking outside
    if (modal) {
        modal.addEventListener('click', function(event) {
            if (event.target === modal) {
                closeLocationModal();
            }
        });
    }

    // Close modal with Escape key
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape' && modal && modal.style.display === 'flex') {
            closeLocationModal();
        }
    });
}); 