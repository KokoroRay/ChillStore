document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.querySelector('.search-bar input[name="keyword"]');
    const suggestionBox = document.querySelector('.autocomplete-suggestions');
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
                suggestionBox.innerHTML = '<div style="padding: 12px; text-align: center; color: #666;"><i class="fas fa-spinner fa-spin"></i> Đang tìm kiếm...</div>';
                suggestionBox.style.display = 'block';
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
                            keywordHeader.innerHTML = '<i class="fas fa-history"></i> Từ khóa gợi ý';
                            keywordHeader.style.fontWeight = 'bold';
                            keywordHeader.style.padding = '8px 12px 4px 12px';
                            keywordHeader.style.color = '#666';
                            keywordHeader.style.fontSize = '0.9rem';
                            suggestionBox.appendChild(keywordHeader);
                            
                            suggestions.forEach((item, idx) => {
                                const div = document.createElement('div');
                                div.textContent = item;
                                div.className = 'suggestion-item';
                                div.style.padding = '8px 16px';
                                div.style.cursor = 'pointer';
                                div.style.borderBottom = '1px solid #f0f0f0';
                                div.onmouseenter = () => {
                                    div.style.backgroundColor = '#f8f9fa';
                                };
                                div.onmouseleave = () => {
                                    div.style.backgroundColor = 'transparent';
                                };
                                div.onclick = () => {
                                    window.location.href = '/search?keyword=' + encodeURIComponent(item);
                                };
                                suggestionBox.appendChild(div);
                            });
                        }
                        
                        // Render sản phẩm gợi ý
                        if (productSuggestions.length > 0) {
                            const productHeader = document.createElement('div');
                            productHeader.innerHTML = '<i class="fas fa-box"></i> Sản phẩm gợi ý';
                            productHeader.style.fontWeight = 'bold';
                            productHeader.style.padding = '8px 12px 4px 12px';
                            productHeader.style.color = '#666';
                            productHeader.style.fontSize = '0.9rem';
                            suggestionBox.appendChild(productHeader);
                            
                            productSuggestions.forEach((item, idx) => {
                                const div = document.createElement('div');
                                div.style.padding = '8px 16px';
                                div.style.cursor = 'pointer';
                                div.style.borderBottom = '1px solid #f0f0f0';
                                div.style.display = 'flex';
                                div.style.alignItems = 'center';
                                div.style.gap = '12px';
                                
                                // Product image
                                const img = document.createElement('img');
                                img.src = item.image || '/images/default-product.png';
                                img.style.width = '40px';
                                img.style.height = '40px';
                                img.style.objectFit = 'cover';
                                img.style.borderRadius = '4px';
                                div.appendChild(img);
                                
                                // Product info
                                const info = document.createElement('div');
                                info.style.flex = '1';
                                
                                const name = document.createElement('div');
                                name.textContent = item.name;
                                name.style.fontWeight = '500';
                                name.style.fontSize = '0.9rem';
                                info.appendChild(name);
                                
                                const price = document.createElement('div');
                                price.style.fontSize = '0.8rem';
                                price.style.color = '#e53935';
                                price.style.fontWeight = 'bold';
                                
                                if (item.hasDiscount) {
                                    price.innerHTML = `<span style="text-decoration: line-through; color: #999;">${formatPrice(item.price)}</span> <span style="color: #e53935;">-${item.discountPercent}%</span>`;
                                } else {
                                    price.textContent = formatPrice(item.price);
                                }
                                info.appendChild(price);
                                
                                div.appendChild(info);
                                
                                div.onmouseenter = () => {
                                    div.style.backgroundColor = '#f8f9fa';
                                };
                                div.onmouseleave = () => {
                                    div.style.backgroundColor = 'transparent';
                                };
                                div.onclick = () => {
                                    window.location.href = '/Product/' + item.id;
                                };
                                suggestionBox.appendChild(div);
                            });
                        }
                        
                        if (suggestions.length > 0 || productSuggestions.length > 0) {
                            suggestionBox.style.display = 'block';
                        } else {
                            suggestionBox.innerHTML = '<div style="padding: 12px; text-align: center; color: #666;">Không tìm thấy kết quả</div>';
                            suggestionBox.style.display = 'block';
                        }
                    })
                    .catch(error => {
                        console.error('Error fetching suggestions:', error);
                        suggestionBox.innerHTML = '<div style="padding: 12px; text-align: center; color: #e53935;">Lỗi tải dữ liệu</div>';
                        suggestionBox.style.display = 'block';
                        isLoading = false;
                    });
            }, 300); // Debounce 300ms
        });
        
        // Keyboard navigation
        searchInput.addEventListener('keydown', function(e) {
            const items = suggestionBox.querySelectorAll('.suggestion-item');
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
                    suggestionBox.style.display = 'none';
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
                suggestionBox.style.display = 'none';
                return;
            }
            const header = document.createElement('div');
            header.innerHTML = '<i class="fas fa-history"></i> Từ khóa gần đây';
            header.style.fontWeight = 'bold';
            header.style.padding = '8px 12px 4px 12px';
            header.style.color = '#666';
            header.style.fontSize = '0.9rem';
            suggestionBox.appendChild(header);
            history.forEach(item => {
                const div = document.createElement('div');
                div.textContent = item;
                div.className = 'suggestion-item';
                div.style.padding = '8px 16px';
                div.style.cursor = 'pointer';
                div.style.borderBottom = '1px solid #f0f0f0';
                div.onclick = () => {
                    window.location.href = '/search?keyword=' + encodeURIComponent(item);
                };
                suggestionBox.appendChild(div);
            });
            suggestionBox.style.display = 'block';
        }
        
        document.addEventListener('click', function(e) {
            if (!suggestionBox.contains(e.target) && e.target !== searchInput) {
                suggestionBox.innerHTML = '';
                suggestionBox.style.display = 'none';
                suggestions = [];
                productSuggestions = [];
                selectedIndex = -1;
            }
        });
    }
}); 