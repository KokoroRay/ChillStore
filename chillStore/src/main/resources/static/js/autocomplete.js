document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.querySelector('.search-input');
    const suggestionBox = document.getElementById('autocomplete-suggestions');
    const searchForm = document.querySelector('.search-bar');
    let timeout = null;
    let suggestions = [];
    let selectedIndex = -1;
    let productSuggestions = [];
    let isLoading = false;
    
    if (searchInput && suggestionBox && searchForm) {
        // Override submit để luôn chuyển hướng sang /search
        searchForm.addEventListener('submit', function(e) {
            e.preventDefault();
            if (searchInput.value.trim()) {
                saveRecentKeyword(searchInput.value.trim());
                window.location.href = '/search?keyword=' + encodeURIComponent(searchInput.value.trim());
            }
        });
        
        searchInput.addEventListener('input', function() {
            const keyword = this.value.trim();
            if (timeout) clearTimeout(timeout);
            
            // Nếu không có keyword, hiển thị từ khóa gần đây (nếu có)
            if (keyword.length === 0) {
                showRecentKeywords();
                suggestions = [];
                productSuggestions = [];
                selectedIndex = -1;
                return;
            }
            
            // Show loading state
            if (!isLoading) {
                suggestionBox.innerHTML = '<div class="suggestion-empty"><i class="fas fa-spinner fa-spin"></i> Đang tìm kiếm...</div>';
                suggestionBox.classList.add('show');
                isLoading = true;
            }
            
            timeout = setTimeout(() => {
                fetch('/Product/api/products/suggest?q=' + encodeURIComponent(keyword))
                    .then(res => res.json())
                    .then(data => {
                        suggestionBox.innerHTML = '';
                        suggestions = data.keywords || [];
                        productSuggestions = data.products || [];
                        selectedIndex = -1;
                        isLoading = false;
                        
                        // Render từ khóa gợi ý
                        if (suggestions.length > 0) {
                            const keywordHeader = document.createElement('div');
                            keywordHeader.className = 'suggestion-header';
                            keywordHeader.innerHTML = '<i class="fas fa-history"></i> Từ khóa gợi ý';
                            suggestionBox.appendChild(keywordHeader);
                            
                            suggestions.forEach((item, idx) => {
                                const div = document.createElement('div');
                                div.textContent = item;
                                div.className = 'suggestion-item';
                                div.onclick = () => {
                                    saveRecentKeyword(item);
                                    window.location.href = '/search?keyword=' + encodeURIComponent(item);
                                };
                                suggestionBox.appendChild(div);
                            });
                        }
                        
                        // Render sản phẩm gợi ý
                        if (productSuggestions.length > 0) {
                            const productHeader = document.createElement('div');
                            productHeader.className = 'suggestion-header';
                            productHeader.innerHTML = '<i class="fas fa-box"></i> Sản phẩm gợi ý';
                            suggestionBox.appendChild(productHeader);
                            
                            productSuggestions.forEach((item, idx) => {
                                const div = document.createElement('div');
                                div.className = 'product-suggestion';
                                
                                // Product image
                                const img = document.createElement('img');
                                img.src = item.image || '/images/default-product.png';
                                img.alt = item.name;
                                div.appendChild(img);
                                
                                // Product info
                                const info = document.createElement('div');
                                info.className = 'product-info';
                                
                                const name = document.createElement('div');
                                name.className = 'product-name';
                                name.textContent = item.name;
                                info.appendChild(name);
                                
                                const price = document.createElement('div');
                                price.className = 'product-price';
                                
                                if (item.hasDiscount) {
                                    price.innerHTML = `<span style="text-decoration: line-through; color: #999;">${formatPrice(item.price)}</span> <span class="discount-badge">-${item.discountPercent}%</span>`;
                                } else {
                                    price.textContent = formatPrice(item.price);
                                }
                                info.appendChild(price);
                                
                                div.appendChild(info);
                                
                                div.onclick = () => {
                                    window.location.href = '/Product/' + item.id;
                                };
                                suggestionBox.appendChild(div);
                            });
                        }
                        
                        if (suggestions.length > 0 || productSuggestions.length > 0) {
                            suggestionBox.classList.add('show');
                        } else {
                            suggestionBox.innerHTML = '<div class="suggestion-empty">Không tìm thấy kết quả</div>';
                            suggestionBox.classList.add('show');
                        }
                    })
                    .catch(error => {
                        console.error('Error fetching suggestions:', error);
                        suggestionBox.innerHTML = '<div class="suggestion-empty">Lỗi tải dữ liệu</div>';
                        suggestionBox.classList.add('show');
                        isLoading = false;
                    });
            }, 300); // Debounce 300ms
        });
        
        searchInput.addEventListener('focus', function() {
            if (this.value.trim().length === 0) {
                showRecentKeywords();
            }
        });
        
        // Keyboard navigation
        searchInput.addEventListener('keydown', function(e) {
            const items = suggestionBox.querySelectorAll('.suggestion-item, .product-suggestion');
            if (items.length === 0) return;
            
            switch(e.key) {
                case 'ArrowDown':
                    e.preventDefault();
                    selectedIndex = Math.min(selectedIndex + 1, items.length - 1);
                    updateSelection(items);
                    break;
                case 'ArrowUp':
                    e.preventDefault();
                    selectedIndex = Math.max(selectedIndex - 1, -1);
                    updateSelection(items);
                    break;
                case 'Enter':
                    e.preventDefault();
                    if (selectedIndex >= 0 && items[selectedIndex]) {
                        items[selectedIndex].click();
                    } else {
                        searchForm.submit();
                    }
                    break;
                case 'Escape':
                    suggestionBox.classList.remove('show');
                    selectedIndex = -1;
                    break;
            }
        });
        
        function updateSelection(items) {
            items.forEach((item, index) => {
                if (index === selectedIndex) {
                    item.style.backgroundColor = '#0074d9';
                    item.style.color = 'white';
                } else {
                    item.style.backgroundColor = 'transparent';
                    item.style.color = 'inherit';
                }
            });
        }
        
        function formatPrice(price) {
            return new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND'
            }).format(price);
        }
        
        // Hàm lưu từ khóa vào localStorage
        function saveRecentKeyword(keyword) {
            if (!keyword) return;
            let history = JSON.parse(localStorage.getItem('recentKeywords') || '[]');
            history = history.filter(item => item.toLowerCase() !== keyword.toLowerCase());
            history.unshift(keyword);
            if (history.length > 8) history = history.slice(0, 8);
            localStorage.setItem('recentKeywords', JSON.stringify(history));
        }

        // Hàm hiển thị từ khóa gần đây
        function showRecentKeywords() {
            let history = JSON.parse(localStorage.getItem('recentKeywords') || '[]');
            suggestionBox.innerHTML = '';
            if (history.length === 0) {
                suggestionBox.classList.remove('show');
                return;
            }
            const header = document.createElement('div');
            header.className = 'suggestion-header';
            header.innerHTML = '<i class="fas fa-history"></i> Từ khóa gần đây';
            suggestionBox.appendChild(header);
            history.forEach(item => {
                const div = document.createElement('div');
                div.textContent = item;
                div.className = 'suggestion-item';
                div.onclick = () => {
                    saveRecentKeyword(item);
                    window.location.href = '/search?keyword=' + encodeURIComponent(item);
                };
                suggestionBox.appendChild(div);
            });
            suggestionBox.classList.add('show');
        }
        
        document.addEventListener('click', function(e) {
            if (!suggestionBox.contains(e.target) && e.target !== searchInput) {
                suggestionBox.innerHTML = '';
                suggestionBox.classList.remove('show');
                suggestions = [];
                productSuggestions = [];
                selectedIndex = -1;
            }
        });
    }
}); 