const pool = require('../config/db');
const jwt = require('jsonwebtoken');

const login = async (req, res) => {
    const { TenDangNhap, MatKhau } = req.body;

    // 1. Kiểm tra xem người dùng có gửi đủ dữ liệu không
    if (!TenDangNhap || !MatKhau) {
        return res.status(400).json({ message: 'Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!' });
    }

    try {
        // 2. Tìm tài khoản trong Database
        const [users] = await pool.query(
            'SELECT * FROM TAIKHOAN WHERE TenDangNhap = ?',
            [TenDangNhap]
        );

        if (users.length === 0) {
            return res.status(401).json({ message: 'Tài khoản không tồn tại!' });
        }

        const user = users[0];

        // 3. So sánh mật khẩu (KHÔNG MÃ HÓA, so sánh trực tiếp)
        if (MatKhau !== user.MatKhau) {
            return res.status(401).json({ message: 'Mật khẩu không chính xác!' });
        }

        // 4. Lấy thêm thông tin HoTen từ bảng NGUOIDUNG để hiển thị trên App
        const [userDetails] = await pool.query(
            'SELECT HoTen FROM NGUOIDUNG WHERE MaNguoiDung = ?',
            [user.MaNguoiDung]
        );
        const hoTen = userDetails.length > 0 ? userDetails[0].HoTen : 'Unknown';

        // 5. Tạo Token (Thẻ thông hành phiên đăng nhập)
        const token = jwt.sign(
            {
                MaTaiKhoan: user.MaTaiKhoan,
                MaNguoiDung: user.MaNguoiDung,
                MaVaiTro: user.MaVaiTro,
                MaToChuc: user.MaToChuc
            },
            process.env.JWT_SECRET,
            { expiresIn: '1d' } 
        );

        // 6. Trả kết quả thành công về cho Client
        return res.status(200).json({
            message: 'Đăng nhập thành công!',
            token: token,
            data: {
                MaNguoiDung: user.MaNguoiDung,
                HoTen: hoTen,
                MaVaiTro: user.MaVaiTro
            }
        });

    } catch (error) {
        console.error('Lỗi khi đăng nhập:', error);
        return res.status(500).json({ message: 'Lỗi server nội bộ' });
    }
};

module.exports = {
    login
};