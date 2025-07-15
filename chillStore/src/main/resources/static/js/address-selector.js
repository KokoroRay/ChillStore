// Vietnam Address Selector Component
class AddressSelector {
    constructor(containerId, hiddenInputId) {
        this.container = document.getElementById(containerId);
        this.hiddenInput = document.getElementById(hiddenInputId);
        this.provinces = [];
        this.districts = [];
        this.wards = [];
        this.init();
    }

    async init() {
        await this.loadProvinces();
        this.render();
        this.bindEvents();
    }

    async loadProvinces() {
        try {
            const response = await fetch('https://provinces.open-api.vn/api/p/');
            this.provinces = await response.json();
        } catch (error) {
            console.error('Error loading provinces:', error);
            this.provinces = this.getStaticProvinces();
        }
    }

    async loadDistricts(provinceCode) {
        try {
            const response = await fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`);
            const data = await response.json();
            this.districts = data.districts || [];
        } catch (error) {
            console.error('Error loading districts:', error);
            this.districts = [];
        }
    }

    async loadWards(districtCode) {
        try {
            const response = await fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`);
            const data = await response.json();
            this.wards = data.wards || [];
        } catch (error) {
            console.error('Error loading wards:', error);
            this.wards = [];
        }
    }

    render() {
        this.container.innerHTML = `
            <div class="address-selector-grid">
                <div class="form-group">
                    <label class="form-label">
                        <i class="fas fa-map-marker-alt"></i> Tỉnh/Thành phố *
                    </label>
                    <select id="province-select" class="form-input" required>
                        <option value="">Chọn Tỉnh/Thành phố</option>
                        ${this.provinces.map(p => `<option value="${p.code}">${p.name}</option>`).join('')}
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label">
                        <i class="fas fa-building"></i> Quận/Huyện *
                    </label>
                    <select id="district-select" class="form-input" required disabled>
                        <option value="">Chọn Quận/Huyện</option>
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label">
                        <i class="fas fa-home"></i> Phường/Xã *
                    </label>
                    <select id="ward-select" class="form-input" required disabled>
                        <option value="">Chọn Phường/Xã</option>
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label">
                        <i class="fas fa-road"></i> Địa chỉ chi tiết *
                    </label>
                    <input type="text" id="detail-address" class="form-input" placeholder="Số nhà, tên đường..." required>
                </div>
            </div>
        `;
    }

    bindEvents() {
        const provinceSelect = document.getElementById('province-select');
        const districtSelect = document.getElementById('district-select');
        const wardSelect = document.getElementById('ward-select');
        const detailAddress = document.getElementById('detail-address');

        provinceSelect.addEventListener('change', async (e) => {
            const provinceCode = e.target.value;
            districtSelect.disabled = !provinceCode;
            wardSelect.disabled = true;

            if (provinceCode) {
                await this.loadDistricts(provinceCode);
                districtSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>' +
                    this.districts.map(d => `<option value="${d.code}">${d.name}</option>`).join('');
            } else {
                districtSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
                wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
            }
            this.updateHiddenInput();
        });

        districtSelect.addEventListener('change', async (e) => {
            const districtCode = e.target.value;
            wardSelect.disabled = !districtCode;

            if (districtCode) {
                await this.loadWards(districtCode);
                wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>' +
                    this.wards.map(w => `<option value="${w.code}">${w.name}</option>`).join('');
            } else {
                wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
            }
            this.updateHiddenInput();
        });

        wardSelect.addEventListener('change', () => this.updateHiddenInput());
        detailAddress.addEventListener('input', () => this.updateHiddenInput());
    }

    updateHiddenInput() {
        const province = document.getElementById('province-select');
        const district = document.getElementById('district-select');
        const ward = document.getElementById('ward-select');
        const detail = document.getElementById('detail-address');

        const parts = [];
        if (detail.value.trim()) parts.push(detail.value.trim());
        if (ward.selectedOptions[0]?.text && ward.value) parts.push(ward.selectedOptions[0].text);
        if (district.selectedOptions[0]?.text && district.value) parts.push(district.selectedOptions[0].text);
        if (province.selectedOptions[0]?.text && province.value) parts.push(province.selectedOptions[0].text);

        this.hiddenInput.value = parts.join(', ');
    }

    async setAddress(address) {
        if (!address) return;

        // Parse address parts (assuming format: "detail, ward, district, province")
        const parts = address.split(', ');
        if (parts.length >= 1) {
            const detailInput = document.getElementById('detail-address');
            if (detailInput) {
                detailInput.value = parts[0];
            }
        }

        // Try to match province, district, ward if available
        if (parts.length >= 4) {
            const provinceName = parts[3];
            const districtName = parts[2];
            const wardName = parts[1];

            // Find and select province
            const provinceSelect = document.getElementById('province-select');
            const province = this.provinces.find(p => p.name.includes(provinceName) || provinceName.includes(p.name));
            if (province && provinceSelect) {
                provinceSelect.value = province.code;
                await this.loadDistricts(province.code);

                // Find and select district
                const districtSelect = document.getElementById('district-select');
                const district = this.districts.find(d => d.name.includes(districtName) || districtName.includes(d.name));
                if (district && districtSelect) {
                    districtSelect.disabled = false;
                    districtSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>' +
                        this.districts.map(d => `<option value="${d.code}">${d.name}</option>`).join('');
                    districtSelect.value = district.code;

                    await this.loadWards(district.code);

                    // Find and select ward
                    const wardSelect = document.getElementById('ward-select');
                    const ward = this.wards.find(w => w.name.includes(wardName) || wardName.includes(w.name));
                    if (ward && wardSelect) {
                        wardSelect.disabled = false;
                        wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>' +
                            this.wards.map(w => `<option value="${w.code}">${w.name}</option>`).join('');
                        wardSelect.value = ward.code;
                    }
                }
            }
        }

        this.updateHiddenInput();
    }

    getStaticProvinces() {
        return [
            {code: "01", name: "Hà Nội"},
            {code: "79", name: "Thành phố Hồ Chí Minh"},
            {code: "48", name: "Đà Nẵng"},
            {code: "92", name: "Cần Thơ"},
            {code: "31", name: "Hải Phòng"},
            {code: "02", name: "Hà Giang"},
            {code: "04", name: "Cao Bằng"},
            {code: "06", name: "Bắc Kạn"},
            {code: "08", name: "Tuyên Quang"},
            {code: "10", name: "Lào Cai"},
            {code: "11", name: "Điện Biên"},
            {code: "12", name: "Lai Châu"},
            {code: "14", name: "Sơn La"},
            {code: "15", name: "Yên Bái"},
            {code: "17", name: "Hoà Bình"},
            {code: "19", name: "Thái Nguyên"},
            {code: "20", name: "Lạng Sơn"},
            {code: "22", name: "Quảng Ninh"},
            {code: "24", name: "Bắc Giang"},
            {code: "25", name: "Phú Thọ"},
            {code: "26", name: "Vĩnh Phúc"},
            {code: "27", name: "Bắc Ninh"},
            {code: "30", name: "Hải Dương"},
            {code: "33", name: "Hưng Yên"},
            {code: "34", name: "Thái Bình"},
            {code: "35", name: "Hà Nam"},
            {code: "36", name: "Nam Định"},
            {code: "37", name: "Ninh Bình"},
            {code: "38", name: "Thanh Hóa"},
            {code: "40", name: "Nghệ An"},
            {code: "42", name: "Hà Tĩnh"},
            {code: "44", name: "Quảng Bình"},
            {code: "45", name: "Quảng Trị"},
            {code: "46", name: "Thừa Thiên Huế"},
            {code: "49", name: "Quảng Nam"},
            {code: "51", name: "Quảng Ngãi"},
            {code: "52", name: "Bình Định"},
            {code: "54", name: "Phú Yên"},
            {code: "56", name: "Khánh Hòa"},
            {code: "58", name: "Ninh Thuận"},
            {code: "60", name: "Bình Thuận"},
            {code: "62", name: "Kon Tum"},
            {code: "64", name: "Gia Lai"},
            {code: "66", name: "Đắk Lắk"},
            {code: "67", name: "Đắk Nông"},
            {code: "68", name: "Lâm Đồng"},
            {code: "70", name: "Bình Phước"},
            {code: "72", name: "Tây Ninh"},
            {code: "74", name: "Bình Dương"},
            {code: "75", name: "Đồng Nai"},
            {code: "77", name: "Bà Rịa - Vũng Tàu"},
            {code: "80", name: "Long An"},
            {code: "82", name: "Tiền Giang"},
            {code: "83", name: "Bến Tre"},
            {code: "84", name: "Trà Vinh"},
            {code: "86", name: "Vĩnh Long"},
            {code: "87", name: "Đồng Tháp"},
            {code: "89", name: "An Giang"},
            {code: "91", name: "Kiên Giang"},
            {code: "93", name: "Hậu Giang"},
            {code: "94", name: "Sóc Trăng"},
            {code: "95", name: "Bạc Liêu"},
            {code: "96", name: "Cà Mau"}
        ];
    }
}