// autocomplete.js

document.addEventListener('DOMContentLoaded', function () {
    const searchInput = document.getElementById('searchInput');
    const autocompleteList = document.getElementById('autocomplete-list');
    let currentFocus = -1;

    if (!searchInput || !autocompleteList) return;

    const RECENT_KEY = 'recent_searches';
    const RECENT_LIMIT = 5;

    // Helper: highlight match
    function highlight(text, keyword) {
        if (!keyword) return text;
        const re = new RegExp('(' + keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + ')', 'gi');
        return text.replace(re, '<span class="highlight">$1</span>');
    }

    // Helper: get/set recent searches
    function getRecentSearches() {
        return JSON.parse(localStorage.getItem(RECENT_KEY) || '[]');
    }
    function saveRecentSearch(keyword) {
        if (!keyword || !keyword.trim()) return;
        let recents = getRecentSearches();
        keyword = keyword.trim();
        recents = recents.filter(k => k.toLowerCase() !== keyword.toLowerCase());
        recents.unshift(keyword);
        if (recents.length > RECENT_LIMIT) recents = recents.slice(0, RECENT_LIMIT);
        localStorage.setItem(RECENT_KEY, JSON.stringify(recents));
    }
    function clearRecentSearches() {
        localStorage.removeItem(RECENT_KEY);
        renderRecentSearches();
    }

    // Render recent searches
    function renderRecentSearches() {
        const recents = getRecentSearches();
        if (!recents.length) return '';
        let html = '<div class="recent-searches-title">Từ nhập gần đây</div>';
        recents.forEach(keyword => {
            html += `<div class="recent-search-item">${keyword}</div>`;
        });
        return html;
    }

    // Show recent searches on focus or empty input
    searchInput.addEventListener('focus', function () {
        if (!this.value) {
            autocompleteList.innerHTML = renderRecentSearches();
            addRecentEvents();
        }
    });

    searchInput.addEventListener('input', function () {
        const val = this.value;
        if (!val) {
            autocompleteList.innerHTML = renderRecentSearches();
            addRecentEvents();
            return false;
        }
        // Call both keyword and product suggestion APIs
        Promise.all([
            fetch(`/Product/api/keywords/suggest?q=${encodeURIComponent(val)}`).then(res => res.json()),
            fetch(`/Product/api/products/suggest?q=${encodeURIComponent(val)}`).then(res => res.json())
        ]).then(([keywords, products]) => {
            let html = '';
            // Suggested keywords
            if (keywords && keywords.length) {
                html += '<div class="suggest-group-title"><i class="fa fa-history"></i> Từ khóa gợi ý</div>';
                keywords.forEach(kw => {
                    html += `<div class="suggest-keyword-item"><i class='fa fa-lightbulb'></i> <span class='suggest-keyword-text'>${highlight(kw, val)}</span></div>`;
                });
            }
            // Suggested products
            if (products && products.length) {
                html += '<div class="suggest-group-title"><i class="fa fa-box"></i> Sản phẩm gợi ý</div>';
                products.forEach(prod => {
                    html += `<div class="suggest-product-item" data-id="${prod.id}">
                        <img src="${prod.imageUrl || '/images/logo.png'}" class="suggest-product-img" alt="">
                        <span class="suggest-product-name">${highlight(prod.name, val)}</span>
                        <span class="suggest-product-price">${prod.price ? prod.price.toLocaleString('vi-VN', {style: 'currency', currency: 'VND'}) : ''}</span>
                    </div>`;
                });
            }
            if (!keywords.length && !products.length) {
                html += '<div class="no-result">Không tìm thấy kết quả</div>';
            }
            autocompleteList.innerHTML = html;
            addSuggestEvents();
        });
    });

    // Add click events for recent searches and clear button
    function addRecentEvents() {
        const items = autocompleteList.querySelectorAll('.recent-search-item');
        items.forEach(item => {
            item.addEventListener('click', function () {
                searchInput.value = this.textContent;
                autocompleteList.innerHTML = '';
                saveRecentSearch(this.textContent);
                searchInput.form.submit();
            });
        });
    }

    // Add click events for suggested keywords and products
    function addSuggestEvents() {
        // Keyword
        const kwItems = autocompleteList.querySelectorAll('.suggest-keyword-item');
        kwItems.forEach(item => {
            item.addEventListener('click', function () {
                const text = this.querySelector('.suggest-keyword-text').textContent;
                searchInput.value = text;
                autocompleteList.innerHTML = '';
                saveRecentSearch(text);
                searchInput.form.submit();
            });
        });
        // Product
        const prodItems = autocompleteList.querySelectorAll('.suggest-product-item');
        prodItems.forEach(item => {
            item.addEventListener('click', function () {
                const name = this.querySelector('.suggest-product-name').textContent;
                searchInput.value = name;
                autocompleteList.innerHTML = '';
                saveRecentSearch(name);
                searchInput.form.submit();
            });
        });
    }

    searchInput.addEventListener('keydown', function (e) {
        let items = autocompleteList.getElementsByTagName('div');
        if (e.key === 'ArrowDown') {
            currentFocus++;
            addActive(items);
        } else if (e.key === 'ArrowUp') {
            currentFocus--;
            addActive(items);
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (currentFocus > -1 && items[currentFocus] && !items[currentFocus].classList.contains('suggest-group-title')) {
                items[currentFocus].click();
            } else {
                searchInput.form.submit();
            }
        }
    });

    function addActive(items) {
        if (!items) return false;
        removeActive(items);
        if (currentFocus >= items.length) currentFocus = 0;
        if (currentFocus < 0) currentFocus = items.length - 1;
        // Bỏ qua suggest-group-title
        let loopCount = 0;
        while (items[currentFocus] && items[currentFocus].classList.contains('suggest-group-title')) {
            currentFocus++;
            loopCount++;
            if (currentFocus >= items.length) currentFocus = 0;
            if (loopCount > items.length) return; // tránh vòng lặp vô hạn nếu chỉ có tiêu đề
        }
        if (items[currentFocus] && !items[currentFocus].classList.contains('suggest-group-title')) {
            items[currentFocus].classList.add('autocomplete-active');
        }
    }

    function removeActive(items) {
        for (let i = 0; i < items.length; i++) {
            items[i].classList.remove('autocomplete-active');
        }
    }

    document.addEventListener('click', function (e) {
        // Nếu click không nằm trong input hoặc autocomplete-list thì mới ẩn
        if (e.target !== searchInput && !autocompleteList.contains(e.target)) {
            autocompleteList.innerHTML = '';
        }
    });
}); 