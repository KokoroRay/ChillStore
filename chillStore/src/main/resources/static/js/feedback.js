// feedback.js - UI/UX hiện đại cho feedback sản phẩm

let currentUserId = window.currentUserId || null;
const FEEDBACK_PAGE_SIZE = 5;
let currentPage = 0;
let currentRatingFilter = null;

// --- DOMContentLoaded ---
document.addEventListener('DOMContentLoaded', function () {
    const productId = window.productId || getProductIdFromUrl();
    if (!productId) return;
    fetchStatsAndRender(productId);
    fetchFeedbackList(productId, null, 0);
    setupStarSelect();
    setupFormSubmit(productId);
    setupEditModal(productId);
    checkFeedbackPermission(productId);
});

function checkFeedbackPermission(productId) {
    // Nếu chưa đăng nhập, ẩn form và show thông báo
    if (!window.currentUserId || window.currentUserId === 'null') {
        const form = document.getElementById('fb-form');
        if (form) {
            form.style.display = 'none';
            const msg = document.createElement('div');
            msg.className = 'fb-login-required';
            msg.style = 'color:#d9534f;text-align:center;margin-bottom:16px;';
            msg.innerHTML = 'Bạn cần <a href="/login" style="color:#226597;text-decoration:underline;">đăng nhập</a> để đánh giá sản phẩm.';
            form.parentNode.insertBefore(msg, form);
        }
        return;
    }
    // Kiểm tra đã mua hàng và đã feedback chưa
    fetch(`/product/${productId}/feedbacks?page=0&size=1`)
        .then(res => res.json())
        .then(data => {
            let hasFeedback = false;
            if (data && data.content) {
                hasFeedback = data.content.some(fb => fb.customer && fb.customer.customerId == window.currentUserId);
            }
            if (hasFeedback) {
                const form = document.getElementById('fb-form');
                if (form) {
                    form.style.display = 'none';
                    const msg = document.createElement('div');
                    msg.className = 'fb-already-feedback';
                    msg.style = 'color:#888;text-align:center;margin-bottom:16px;';
                    msg.textContent = 'Bạn đã đánh giá sản phẩm này.';
                    form.parentNode.insertBefore(msg, form);
                }
            } else {
                // Gọi API kiểm tra đã mua hàng chưa
                fetch(`/product/${productId}/feedbacks/check-bought`)
                    .then(res => res.json())
                    .then(data => {
                        if (!data.bought) {
                            const form = document.getElementById('fb-form');
                            if (form) {
                                form.style.display = 'none';
                                const msg = document.createElement('div');
                                msg.className = 'fb-not-bought';
                                msg.style = 'color:#888;text-align:center;margin-bottom:16px;';
                                msg.textContent = 'Bạn chỉ có thể đánh giá sản phẩm đã mua.';
                                form.parentNode.insertBefore(msg, form);
                            }
                        }
                    });
            }
        });
}

function fetchStatsAndRender(productId) {
    fetch(`/product/${productId}/feedbacks/stats`)
        .then(res => res.json())
        .then(stats => renderStats(stats));
}

function renderStats(stats) {
    document.getElementById('avg-rating').textContent = stats.total === 0 ? '—' : stats.average.toFixed(1);
    document.getElementById('total-feedback').textContent = stats.total + ' lượt đánh giá';
    const barList = document.getElementById('star-bar-list');
    barList.innerHTML = '';
    for (let i = 5; i >= 1; i--) {
        const count = stats.countByStar[i] || 0;
        const percent = stats.total ? (count / stats.total * 100) : 0;
        barList.innerHTML += `
        <div class="fb-progress-row">
            <span style="width:22px;text-align:right;">${i}★</span>
            <div class="fb-progress-bar"><div class="fb-progress-bar-inner" style="width:${percent}%;"></div></div>
            <span style="width:32px;text-align:left;">${count}</span>
        </div>`;
    }
}

function fetchFeedbackList(productId, rating, page = 0) {
    let url = `/product/${productId}/feedbacks?page=${page}&size=${FEEDBACK_PAGE_SIZE}`;
    if (rating) url += `&rating=${rating}`;
    fetch(url)
        .then(res => res.json())
        .then(data => renderFeedbackList(data.content, data.page, data.totalPages, data.totalElements));
}

function renderFeedbackList(list, page = 0, total = 1, totalElements = 0) {
    const container = document.getElementById('feedback-list');
    container.innerHTML = '';
    if (!list || list.length === 0) {
        container.innerHTML = `<div style='padding:24px 0;color:#888;text-align:center;font-size:1.1rem;'>Chưa có đánh giá nào cho sản phẩm này.<br>Hãy là người đầu tiên đánh giá!</div>`;
        renderPagination(page, total);
        return;
    }
    list.forEach(fb => {
        const stars = '★★★★★'.slice(0, fb.rating);
        const date = fb.createdAt ? new Date(fb.createdAt).toLocaleString('vi-VN') : '';
        let replyHtml = fb.replyContent ? `<div class='fb-reply'><b>Phản hồi từ shop:</b> ${escapeHtml(fb.replyContent)}</div>` : '';
        let actionHtml = '';
        if (currentUserId && fb.customer && fb.customer.customerId == currentUserId) {
            actionHtml = `<div style='margin-top:6px;'><button class='fb-btn fb-btn-warning' onclick='showEditFeedback(${fb.rating}, \"${escapeHtml(fb.comment)}\")'>Sửa</button> <button class='fb-btn fb-btn-danger' onclick='deleteFeedback()'>Xóa</button></div>`;
        }
        container.innerHTML += `
        <div class="fb-item">
            <div>
                <div class="fb-header">
                    <img class="fb-avatar" src="/static/images/default-avatar.svg" alt="avatar" />
                    <span class="fb-username">${fb.customer && fb.customer.name ? fb.customer.name : 'Ẩn danh'}</span>
                    <span class="fb-stars">${stars}</span>
                    <span class="fb-date">${date}</span>
                </div>
                <div class="fb-content">
                    ${fb.comment ? escapeHtml(fb.comment) : '<span style="color:#aaa;">(Không có bình luận)</span>'}
                </div>
                ${replyHtml}
                ${actionHtml}
            </div>
        </div>`;
    });
    renderPagination(page, total);
}

function renderPagination(page, total) {
    const container = document.getElementById('feedback-list');
    let html = '';
    if (total > 1) {
        html += `<div class='fb-pagination' style='text-align:center;margin:12px 0;'>`;
        if (page > 0) html += `<button class='fb-btn' onclick='gotoFeedbackPage(${page - 1})'>Trước</button> `;
        for (let i = 0; i < total; i++) {
            html += `<button class='fb-btn ${i === page ? 'fb-btn-primary' : ''}' onclick='gotoFeedbackPage(${i})'>${i + 1}</button> `;
        }
        if (page < total - 1) html += `<button class='fb-btn' onclick='gotoFeedbackPage(${page + 1})'>Sau</button>`;
        html += `</div>`;
    }
    container.innerHTML += html;
}

function gotoFeedbackPage(page) {
    const productId = window.productId || getProductIdFromUrl();
    fetchFeedbackList(productId, currentRatingFilter, page);
}

function setupStarSelect() {
    const stars = document.querySelectorAll('#feedback-stars .fb-star');
    let selected = 0;
    stars.forEach(star => {
        star.addEventListener('mouseenter', function () {
            highlightStars(stars, this.dataset.value);
        });
        star.addEventListener('mouseleave', function () {
            highlightStars(stars, selected);
        });
        star.addEventListener('click', function () {
            selected = this.dataset.value;
            document.getElementById('feedback-rating').value = selected;
            highlightStars(stars, selected);
        });
    });
}

function highlightStars(stars, value) {
    stars.forEach(star => {
        star.classList.toggle('selected', star.dataset.value <= value);
    });
}

function setupFormSubmit(productId) {
    const form = document.getElementById('fb-form');
    if (!form) return;
    form.addEventListener('submit', function (e) {
        e.preventDefault();
        const rating = form.querySelector('#feedback-rating').value;
        const comment = form.querySelector('#feedback-comment').value;
        if (!rating) return showToast('Vui lòng chọn số sao!');
        submitFeedback(productId, rating, comment);
    });
}

function submitFeedback(productId, rating, comment) {
    fetch(`/product/${productId}/feedbacks`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `rating=${encodeURIComponent(rating)}&comment=${encodeURIComponent(comment || '')}`
    })
    .then(async res => {
        if (!res.ok) {
            const text = await res.text();
            throw new Error(text || 'Gửi đánh giá thất bại!');
        }
        return res.json();
    })
    .then(fb => {
        showToast('Gửi đánh giá thành công!');
        fetchStatsAndRender(productId);
        fetchFeedbackList(productId, null, 0);
    })
    .catch(err => {
        showToast(err.message);
    });
}

function setupEditModal(productId) {
    const editModal = document.getElementById('fb-edit-modal');
    if (!editModal) return;
    let selectedEditRating = 0;
    const stars = editModal.querySelectorAll('.fb-star');
    stars.forEach(star => {
        star.addEventListener('mouseenter', function () {
            highlightStars(stars, this.dataset.value);
        });
        star.addEventListener('mouseleave', function () {
            highlightStars(stars, selectedEditRating);
        });
        star.addEventListener('click', function () {
            selectedEditRating = this.dataset.value;
            editModal.querySelector('#edit-feedback-rating').value = selectedEditRating;
            highlightStars(stars, selectedEditRating);
        });
    });
    const closeBtn = document.getElementById('close-edit-modal');
    if (closeBtn) closeBtn.onclick = () => editModal.style.display = 'none';
    const editForm = document.getElementById('edit-feedback-form');
    if (editForm) editForm.onsubmit = function(e) {
        e.preventDefault();
        const rating = editModal.querySelector('#edit-feedback-rating').value;
        const comment = editModal.querySelector('#edit-feedback-comment').value;
        if (!rating) return showToast('Vui lòng chọn số sao!');
        updateFeedback(productId, rating, comment);
    };
}

function showEditFeedback(rating, comment) {
    const modal = document.getElementById('fb-edit-modal');
    if (!modal) return;
    modal.style.display = 'flex';
    modal.querySelector('#edit-feedback-rating').value = rating;
    modal.querySelector('#edit-feedback-comment').value = comment;
    highlightStars(modal.querySelectorAll('.fb-star'), rating);
}

function updateFeedback(productId, rating, comment) {
    fetch(`/product/${productId}/feedbacks`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `rating=${encodeURIComponent(rating)}&comment=${encodeURIComponent(comment || '')}`
    })
    .then(async res => {
        if (!res.ok) {
            const text = await res.text();
            throw new Error(text || 'Sửa đánh giá thất bại!');
        }
        return res.json();
    })
    .then(fb => {
        showToast('Sửa đánh giá thành công!');
        document.getElementById('fb-edit-modal').style.display = 'none';
        fetchStatsAndRender(productId);
        fetchFeedbackList(productId, null, 0);
    })
    .catch(err => {
        showToast(err.message);
    });
}

function deleteFeedback() {
    const productId = window.productId || getProductIdFromUrl();
    if (!productId) return;
    if (!confirm('Bạn chắc chắn muốn xóa đánh giá này?')) return;
    fetch(`/product/${productId}/feedbacks`, {
        method: 'DELETE'
    })
    .then(async res => {
        if (!res.ok) {
            const text = await res.text();
            throw new Error(text || 'Xóa đánh giá thất bại!');
        }
        showToast('Đã xóa đánh giá!');
        fetchStatsAndRender(productId);
        fetchFeedbackList(productId, null, 0);
    })
    .catch(err => {
        showToast(err.message);
    });
}

function showToast(msg) {
    const toast = document.getElementById('fb-toast-message');
    if (!toast) return alert(msg);
    toast.textContent = msg;
    toast.style.display = 'block';
    setTimeout(() => { toast.style.display = 'none'; }, 3000);
}

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/[&<>"']/g, function (c) {
        return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;','\'':'&#39;'}[c];
    });
}

function getProductIdFromUrl() {
    const m = window.location.pathname.match(/product\/(\d+)/);
    return m ? m[1] : null;
} 