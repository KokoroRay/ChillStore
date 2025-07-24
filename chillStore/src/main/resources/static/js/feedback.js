function getFeedbackProductId() {
    return document.getElementById('feedback-product-id').value;
}
function getFeedbackCustomerId() {
    return parseInt(document.getElementById('feedback-customer-id').value);
}

function getCurrentRole() {
    return document.getElementById('current-role')?.value || 'GUEST';
}

function calculateFeedbackSummary(feedbacks) {
    const summary = { total: 0, avg: 0, count: [0,0,0,0,0,0] };
    if (!feedbacks.length) return summary;
    let sum = 0;
    feedbacks.forEach(fb => {
        if (fb.rating) {
            summary.count[fb.rating]++;
            sum += fb.rating;
            summary.total++;
        }
    });
    summary.avg = summary.total ? (sum / summary.total) : 0;
    return summary;
}

function renderFeedbackSummary(feedbacks) {
    const summaryDiv = document.getElementById('feedback-summary');
    const summary = calculateFeedbackSummary(feedbacks);
    if (!summary.total) {
        summaryDiv.innerHTML = `
            <div class='row align-items-center'>
                <div class='col-md-3 text-center mb-3 mb-md-0'>
                    <div style='font-size:2.5rem;font-weight:700;'>0.0<span style='font-size:1.2rem;font-weight:400;'>/5</span></div>
                    <div style='color:#ffc107;font-size:1.5rem;'>☆☆☆☆☆</div>
                    <div class='text-muted mt-1'>0 reviews</div>
                </div>
                <div class='col-md-9'>
                    ${[5,4,3,2,1].map(star => `
                        <div class='d-flex align-items-center mb-1'>
                            <span style='width:2rem;'>${star}★</span>
                            <div class='progress flex-grow-1 mx-2' style='height:10px;'>
                                <div class='progress-bar bg-warning' role='progressbar' style='width:0%' aria-valuenow='0' aria-valuemin='0' aria-valuemax='100'></div>
                            </div>
                            <span class='text-muted' style='width:2.5rem;'>0</span>
                        </div>
                    `).join('')}
                </div>
            </div>
            <div class='text-center text-muted mt-3'><i class='fas fa-comment-slash fa-2x mb-2'></i><br>No reviews yet. Be the first to review this product!</div>
        `;
        return;
    }
    let html = `<div class='row align-items-center'>
        <div class='col-md-3 text-center mb-3 mb-md-0'>
            <div style='font-size:2.5rem;font-weight:700;'>${summary.avg.toFixed(1)}<span style='font-size:1.2rem;font-weight:400;'>/5</span></div>
            <div style='color:#ffc107;font-size:1.5rem;'>${'★'.repeat(Math.round(summary.avg))}${'☆'.repeat(5-Math.round(summary.avg))}</div>
            <div class='text-muted mt-1'>${summary.total} reviews</div>
        </div>
        <div class='col-md-9'>
            ${[5,4,3,2,1].map(star => {
                const percent = summary.total ? Math.round(summary.count[star] * 100 / summary.total) : 0;
                return `<div class='d-flex align-items-center mb-1'>
                    <span style='width:2rem;'>${star}★</span>
                    <div class='progress flex-grow-1 mx-2' style='height:10px;'>
                        <div class='progress-bar bg-warning' role='progressbar' style='width:${percent}%' aria-valuenow='${percent}' aria-valuemin='0' aria-valuemax='100'></div>
                    </div>
                    <span class='text-muted' style='width:2.5rem;'>${summary.count[star]}</span>
                </div>`;
            }).join('')}
        </div>
    </div>`;
    summaryDiv.innerHTML = html;
}

let feedbackPage = 0;
let feedbackSize = 5;
let feedbackTotalPages = 1;
let feedbackLoading = false;
let feedbacksLoaded = [];
let feedbackRatingFilter = 0;

function setRatingFilter(rating) {
    feedbackRatingFilter = rating;
    // Highlight nút đang chọn
    document.querySelectorAll('.rating-filter-btn').forEach(btn => {
        if (parseInt(btn.getAttribute('data-rating')) === rating) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
    loadFeedbacks(0, false);
}

async function loadFeedbacks(page = 0, append = false) {
    if (feedbackLoading) return;
    feedbackLoading = true;
    const productId = getFeedbackProductId();
    const customerId = getFeedbackCustomerId();
    let url = `/api/product/${productId}/feedbacks?page=${page}&size=${feedbackSize}`;
    if (feedbackRatingFilter && feedbackRatingFilter >= 1 && feedbackRatingFilter <= 5) {
        url += `&rating=${feedbackRatingFilter}`;
    }
    const res = await fetch(url);
    const data = await res.json();
    const feedbacks = data.content || [];
    feedbackTotalPages = data.totalPages;
    feedbackPage = data.currentPage;
    if (append) {
        feedbacksLoaded = feedbacksLoaded.concat(feedbacks);
    } else {
        feedbacksLoaded = feedbacks;
    }
    window._lastFeedbacks = feedbacksLoaded;
    window._lastCustomerId = customerId;
    renderFeedbackSummary(feedbacksLoaded);
    renderFeedbackList(feedbacksLoaded, customerId);
    renderFeedbackLoadMore();
    // Only render form if the container exists (for customer view)
    if (document.getElementById('feedback-form-container')) {
        renderFeedbackForm(feedbacksLoaded, customerId, productId);
    }
    feedbackLoading = false;
}

function renderFeedbackList(feedbacks, customerId) {
    const listDiv = document.getElementById('feedback-list');
    const currentRole = getCurrentRole();
    if (!feedbacks.length) {
        listDiv.innerHTML = '';
        return;
    }
    let html = '<div class="row">';
    feedbacks.forEach(fb => {
        const safeComment = (fb.comment || '').replace(/'/g, "\\'");
        const rating = fb.rating || 0;
        const ratingStars = '★'.repeat(rating) + '☆'.repeat(5 - rating);
        const date = fb.createdAt ? new Date(fb.createdAt).toLocaleDateString('vi-VN', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        }) : '';
        html += `<div class="col-12 mb-3">
            <div class="feedback-item">
                <div class="feedback-user">
                    <span><i class="fas fa-user me-2"></i>${fb.customer ? (fb.customer.name || 'Anonymous') : 'Anonymous'}</span>
                    <span class="feedback-rating">${ratingStars}</span>
                </div>
                <div class="feedback-comment">${fb.comment || ''}</div>
                <div class="feedback-date"><i class="fas fa-clock me-1"></i>${date}</div>`;
        // Customer: show edit/delete if owner
        if (currentRole === 'CUSTOMER' && customerId && fb.customer && fb.customer.customerId === customerId) {
            html += `<div class="feedback-actions">
                <button class="btn btn-sm btn-outline-primary" onclick="showEditFeedback(${fb.id}, ${fb.rating}, '${safeComment}')">
                    <i class="fas fa-edit me-1"></i>Edit
                </button>
                <button class="btn btn-sm btn-outline-danger ms-2" onclick="deleteFeedback(${fb.id})">
                    <i class="fas fa-trash me-1"></i>Delete
                </button>
            </div>`;
        }
        // Staff/Admin: show reply/edit reply
        html += `<div id="reply-for-feedback-${fb.id}"></div>`;
        if (currentRole === 'STAFF' || currentRole === 'ADMIN') {
            html += `<div id="staff-reply-action-${fb.id}"></div>`;
            loadReplyForFeedbackStaff(fb.id);
        } else {
            // Load reply for customer/guest view
            loadReplyForFeedback(fb.id);
        }
        html += '</div></div>';
    });
    html += '</div>';
    listDiv.innerHTML = html;
}

function renderFeedbackLoadMore() {
    const loadMoreDiv = document.getElementById('feedback-loadmore-container');
    if (feedbackPage < feedbackTotalPages - 1) {
        loadMoreDiv.innerHTML = `<button class="btn btn-outline-primary" onclick="loadFeedbacks(${feedbackPage + 1}, true)">
            <i class="fas fa-plus me-1"></i>View more reviews
        </button>`;
    } else {
        loadMoreDiv.innerHTML = '';
    }
}

function loadReplyForFeedback(feedbackId) {
    fetch(`/api/feedback/${feedbackId}/reply`)
        .then(r => r.json())
        .then(reply => {
            if (reply && reply.content) {
                const replyDiv = document.getElementById(`reply-for-feedback-${feedbackId}`);
                if (replyDiv) {
                    const replyDate = reply.createdAt ? new Date(reply.createdAt).toLocaleDateString('vi-VN', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                    }) : '';
                    let responderName = 'Staff';
                    if (reply.admin && reply.admin.name) {
                        responderName = 'Admin: ' + reply.admin.name;
                    } else if (reply.staff && reply.staff.name) {
                        responderName = 'Staff: ' + reply.staff.name;
                    }
                    replyDiv.innerHTML = `
                        <div class="feedback-reply">
                            <div class="reply-header">
                                <span class="reply-staff"><i class="fas fa-shield-alt me-1"></i>${responderName} Response</span>
                                <span class="feedback-date"><i class="fas fa-clock me-1"></i>${replyDate}</span>
                            </div>
                            <div class="reply-content">${reply.content}</div>
                        </div>`;
                }
            }
        })
        .catch(error => {
            console.log('No reply for feedback:', feedbackId);
        });
}

// Staff: load reply and show reply/edit button
function loadReplyForFeedbackStaff(feedbackId) {
    fetch(`/api/feedback/${feedbackId}/reply`).then(r => r.json()).then(reply => {
        const replyDiv = document.getElementById(`reply-for-feedback-${feedbackId}`);
        const actionDiv = document.getElementById(`staff-reply-action-${feedbackId}`);
        if (reply && reply.content) {
            // Show reply + edit button
            if (replyDiv) {
                const replyDate = reply.createdAt ? new Date(reply.createdAt).toLocaleDateString('vi-VN', {
                    year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
                }) : '';
                replyDiv.innerHTML = `
                    <div class="feedback-reply">
                        <div class="reply-header">
                            <span class="reply-staff"><i class="fas fa-shield-alt me-1"></i>Staff Response</span>
                            <span class="feedback-date"><i class="fas fa-clock me-1"></i>${replyDate}</span>
                        </div>
                        <div class="reply-content">${reply.content}</div>
                    </div>`;
            }
            if (actionDiv) {
                actionDiv.innerHTML = `<button class="btn btn-sm btn-outline-primary mt-2" onclick="showStaffReplyForm(${feedbackId}, '${reply.content.replace(/'/g, "&#39;")}')"><i class='fas fa-edit me-1'></i>Edit Reply</button>`;
            }
        } else {
            // Chưa có reply, show nút reply
            if (actionDiv) {
                actionDiv.innerHTML = `<button class="btn btn-sm btn-outline-success mt-2" onclick="showStaffReplyForm(${feedbackId}, '')"><i class='fas fa-reply me-1'></i>Reply</button>`;
            }
        }
    });
}

// Staff: show reply form inline
function showStaffReplyForm(feedbackId, content) {
    const actionDiv = document.getElementById(`staff-reply-action-${feedbackId}`);
    actionDiv.innerHTML = `
        <form class="mt-2" onsubmit="submitStaffReply(event, ${feedbackId})">
            <div class="mb-2">
                <textarea id="staff-reply-content-${feedbackId}" class="form-control" rows="3" required maxlength="1000" placeholder="Write your reply...">${content || ''}</textarea>
                <div class="form-text">Maximum 1000 characters. Be professional and helpful.</div>
            </div>
            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary btn-sm"><i class="fas fa-paper-plane me-1"></i>Save Reply</button>
                <button type="button" class="btn btn-secondary btn-sm" onclick="renderFeedbackList(window._lastFeedbacks, window._lastCustomerId)"><i class="fas fa-times me-1"></i>Cancel</button>
            </div>
        </form>
    `;
}

// Staff: submit reply or update
async function submitStaffReply(e, feedbackId) {
    e.preventDefault();
    const content = document.getElementById(`staff-reply-content-${feedbackId}`).value.trim();
    if (!content) {
        showAlert('Reply cannot be empty', 'warning');
        return;
    }
    if (content.length > 1000) {
        showAlert('Reply is too long. Maximum 1000 characters.', 'warning');
        return;
    }

    // Get CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

    // Check if a reply already exists to determine POST vs PUT
    let method = 'POST';
    try {
        const replyRes = await fetch(`/api/feedback/${feedbackId}/reply`);
        if (replyRes.ok) {
            const reply = await replyRes.json();
            if (reply && reply.content) {
                method = 'PUT';
            }
        }
    } catch (e) {
        // Ignore error, proceed with POST
    }
    
    let url = `/api/feedback/${feedbackId}/reply`;

    try {
        const res = await fetch(url, {
            method,
            headers,
            body: JSON.stringify({ content })
        });
        if (res.ok) {
            showAlert('Reply saved successfully!', 'success');
            loadFeedbacks();
        } else {
            showAlert('Error saving reply', 'danger');
        }
    } catch (error) {
        showAlert('Error saving reply. Please check the console for details.', 'danger');
        console.error("Error submitting staff reply:", error);
    }
}

function renderFeedbackForm(feedbacks, customerId, productId) {
    const formDiv = document.getElementById('feedback-form-container');
    if (!customerId) {
        formDiv.innerHTML = '<div class="alert alert-info feedback-alert"><i class="fas fa-info-circle me-2"></i>Please <a href="/login">log in</a> to leave a review.</div>';
        return;
    }
    const hasFeedback = feedbacks.some(fb => fb.customer && fb.customer.customerId === customerId);
    if (hasFeedback) {
        formDiv.style.display = 'none';
        return;
    }
    formDiv.innerHTML = `
        <div class="card">
            <div class="card-header">
                <h5 class="mb-0"><i class="fas fa-star me-2"></i>Write Your Review</h5>
            </div>
            <div class="card-body">
                <form id="feedback-form" onsubmit="submitFeedback(event, ${productId})">
                    <div class="mb-3">
                        <label for="rating" class="form-label">Rating:</label>
                        <div class="rating-stars">
                            <input type="radio" name="rating" value="5" id="star5" required>
                            <label for="star5" class="text-dark">★</label>
                            <input type="radio" name="rating" value="4" id="star4">
                            <label for="star4" class="text-dark">★</label>
                            <input type="radio" name="rating" value="3" id="star3">
                            <label for="star3" class="text-dark">★</label>
                            <input type="radio" name="rating" value="2" id="star2">
                            <label for="star2" class="text-dark">★</label>
                            <input type="radio" name="rating" value="1" id="star1">
                            <label for="star1" class="text-dark">★</label>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label for="comment" class="form-label">Your Review:</label>
                        <textarea id="comment" class="form-control" rows="4" placeholder="Share your experience with this product..." required maxlength="500"></textarea>
                        <div class="form-text">Maximum 500 characters</div>
                    </div>
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-paper-plane me-2"></i>Submit Review
                    </button>
                </form>
            </div>
        </div>
    `;
    setupRatingStars();
}

async function submitFeedback(e, productId) {
    e.preventDefault();
    const ratingInput = document.querySelector('input[name="rating"]:checked');
    const comment = document.getElementById('comment').value.trim();
    if (!ratingInput) {
        showAlert('You must select a star rating!', 'warning');
        // Focus vào vùng rating
        const ratingStars = document.querySelector('.rating-stars');
        if (ratingStars) {
            ratingStars.scrollIntoView({ behavior: 'smooth', block: 'center' });
            ratingStars.classList.add('border', 'border-danger');
            setTimeout(() => ratingStars.classList.remove('border', 'border-danger'), 2000);
        }
        return;
    }
    if (!comment) {
        showAlert('Please write your review', 'warning');
        return;
    }
    if (comment.length > 500) {
        showAlert('Review is too long. Maximum 500 characters.', 'warning');
        return;
    }
    const rating = parseInt(ratingInput.value);
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
    try {
        const res = await fetch(`/api/product/${productId}/feedback`, {
            method: 'POST',
            headers,
            body: JSON.stringify({ rating, comment })
        });
        if (res.ok) {
            showAlert('Thank you for your review!', 'success');
            // Reset form
            document.getElementById('feedback-form').reset();
            // Reload feedbacks (không append)
            loadFeedbacks(0, false);
            // Scroll lên feedback list
            setTimeout(() => {
                document.getElementById('feedback-section').scrollIntoView({ behavior: 'smooth', block: 'start' });
            }, 300);
        } else {
            const errorData = await res.json().catch(() => ({}));
            showAlert(errorData.message || 'You can only review this product after purchasing and receiving it!', 'danger');
        }
    } catch (error) {
        showAlert('Network error. Please try again.', 'danger');
    }
}

function showAlert(message, type) {
    const formContainer = document.getElementById('feedback-form-container');
    // Fallback for pages without a dedicated form container (e.g., staff page)
    if (!formContainer) {
        alert(message);
        return;
    }

    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'warning' ? 'exclamation-triangle' : 'times-circle'} me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    formContainer.insertBefore(alertDiv, formContainer.firstChild);
    setTimeout(() => {
        if (alertDiv.parentNode) alertDiv.remove();
    }, 5000);
}

function showEditFeedback(id, rating, comment) {
    const formDiv = document.getElementById('feedback-form-container');
    formDiv.innerHTML = `
        <div class="card">
            <div class="card-header">
                <h5 class="mb-0"><i class="fas fa-edit me-2"></i>Edit Your Review</h5>
            </div>
            <div class="card-body">
                <form id="edit-feedback-form" onsubmit="updateFeedback(event, ${id})">
                    <div class="mb-3">
                        <label class="form-label">Rating:</label>
                        <div class="rating-stars">
                            <input type="radio" name="edit-rating" value="5" id="edit-star5" ${rating==5?'checked':''} required>
                            <label for="edit-star5">★</label>
                            <input type="radio" name="edit-rating" value="4" id="edit-star4" ${rating==4?'checked':''}>
                            <label for="edit-star4">★</label>
                            <input type="radio" name="edit-rating" value="3" id="edit-star3" ${rating==3?'checked':''}>
                            <label for="edit-star3">★</label>
                            <input type="radio" name="edit-rating" value="2" id="edit-star2" ${rating==2?'checked':''}>
                            <label for="edit-star2">★</label>
                            <input type="radio" name="edit-rating" value="1" id="edit-star1" ${rating==1?'checked':''}>
                            <label for="edit-star1">★</label>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label for="edit-comment" class="form-label">Your Review:</label>
                        <textarea id="edit-comment" class="form-control" rows="4" required maxlength="500">${comment}</textarea>
                        <div class="form-text">Maximum 500 characters</div>
                    </div>
                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-success">
                            <i class="fas fa-save me-2"></i>Update Review
                        </button>
                        <button type="button" class="btn btn-secondary" onclick="loadFeedbacks()">
                            <i class="fas fa-times me-2"></i>Cancel
                        </button>
                    </div>
                </form>
            </div>
        </div>
    `;
}

async function updateFeedback(e, id) {
    e.preventDefault();
    
    const ratingInput = document.querySelector('input[name="edit-rating"]:checked');
    const comment = document.getElementById('edit-comment').value.trim();
    
    if (!ratingInput) {
        showAlert('Please select a rating', 'warning');
        return;
    }
    
    if (!comment) {
        showAlert('Please write your review', 'warning');
        return;
    }
    
    if (comment.length > 500) {
        showAlert('Review is too long. Maximum 500 characters.', 'warning');
        return;
    }
    
    const rating = parseInt(ratingInput.value);
    
    // Lấy CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
    try {
        const res = await fetch(`/api/feedback/${id}`, {
            method: 'PUT',
            headers,
            body: JSON.stringify({ rating, comment })
        });
        
        if (res.ok) {
            showAlert('Review updated successfully!', 'success');
            loadFeedbacks();
        } else {
            const errorData = await res.json();
            showAlert(errorData.message || 'Error updating review', 'danger');
        }
    } catch (error) {
        showAlert('Network error. Please try again.', 'danger');
    }
}

async function deleteFeedback(id) {
    if (!confirm('Are you sure you want to delete your review? This action cannot be undone.')) {
        return;
    }
    
    // Lấy CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

    try {
        const res = await fetch(`/api/feedback/${id}`, { 
            method: 'DELETE',
            headers: headers
        });
        if (res.ok) {
            showAlert('Review deleted successfully!', 'success');
            loadFeedbacks();
        } else {
            const errorData = await res.json();
            showAlert(errorData.message || 'Error deleting review', 'danger');
        }
    } catch (error) {
        showAlert('Network error. Please try again.', 'danger');
    }
}

async function checkCanReview() {
    const productId = getFeedbackProductId();
    const formDiv = document.getElementById('feedback-form-container');
    // Ensure formDiv exists before proceeding
    if (!formDiv) return;

    const res = await fetch(`/api/product/${productId}/can-review`);
    const canReview = await res.json();
    if (!canReview) {
        formDiv.innerHTML = '<div class="alert alert-info feedback-alert"><i class="fas fa-info-circle me-2"></i>You can only review this product after purchasing and receiving it.</div>';
    }
}

// Hiệu ứng hover cho rating stars (ngược: sao 5 bên trái, sao 1 bên phải, hover đúng hướng)
function setupRatingStars() {
    const stars = document.querySelectorAll('.rating-stars label');
    stars.forEach((label, idx) => {
        label.addEventListener('mouseenter', function() {
            for (let i = 0; i < stars.length; i++) {
                if (i >= idx) {
                    stars[i].classList.add('text-warning');
                    stars[i].classList.remove('text-dark');
                } else {
                    stars[i].classList.remove('text-warning');
                    stars[i].classList.add('text-dark');
                }
            }
        });
        label.addEventListener('mouseleave', function() {
            const checked = document.querySelector('input[name="rating"]:checked');
            const checkedVal = checked ? parseInt(checked.value) : 0;
            stars.forEach((l, i) => {
                const starVal = 5 - i;
                if (starVal <= checkedVal) {
                    l.classList.add('text-warning');
                    l.classList.remove('text-dark');
                } else {
                    l.classList.remove('text-warning');
                    l.classList.add('text-dark');
                }
            });
        });
        label.addEventListener('click', function() {
            for (let i = 0; i < stars.length; i++) {
                if (i >= idx) {
                    stars[i].classList.add('text-warning');
                    stars[i].classList.remove('text-dark');
                } else {
                    stars[i].classList.remove('text-warning');
                    stars[i].classList.add('text-dark');
                }
            }
        });
    });
}

document.addEventListener('DOMContentLoaded', function() {
    loadFeedbacks(0, false);
    // Only run customer-specific checks if the form container exists
    if (document.getElementById('feedback-form-container')) {
        checkCanReview();
    }
    setupRatingStars();
}); 