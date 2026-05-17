const bcrypt = require('bcrypt');
const pool = require('../config/db');

const register = async (req, res) => {
    const connection = await pool.getConnection();

    try {
        // HỨNG ĐÚNG TÊN BIẾN MÀ ANDROID GỬI LÊN (Chữ thường theo chuẩn CamelCase của Kotlin)
        const { cccd, password, fullName, dob, gender, address, email, phone } = req.body;

        await connection.beginTransaction();

        // 2. Kiểm tra xem CCCD đã tồn tại chưa
        const [existingUsers] = await connection.execute(
            'SELECT CCCD FROM NGUOIDUNG WHERE CCCD = ?',
            [cccd]
        );

        if (existingUsers.length > 0) {
            connection.release();
            return res.status(400).json({ 
                status: 'error', 
                message: 'CCCD này đã được đăng ký trong hệ thống!' 
            });
        }

        // 3. Tạo bản ghi NGUOIDUNG - Truyền các biến chữ thường vào đúng vị trí cột viết hoa
        const [userResult] = await connection.execute(
            `INSERT INTO NGUOIDUNG (HoTen, CCCD, NgaySinh, GioiTinh, SDT, DiaChi, Email)
             VALUES (?, ?, ?, ?, ?, ?, ?)`,
            [fullName, cccd, dob, gender, phone, address, email] // Khớp vị trí là ăn tiền!
        );

        const maNguoiDung = userResult.insertId;

        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // 6. Tạo bản ghi TAIKHOAN
        await connection.execute(
            `INSERT INTO TAIKHOAN (TenDangNhap, MatKhauHash, MaNguoiDung, MaVaiTro)
             VALUES (?, ?, ?, ?)`,
            [cccd, hashedPassword, maNguoiDung, 'USER']
        );

        await connection.commit();
        connection.release();

        res.status(201).json({
            status: 'success',
            message: 'Đăng ký tài khoản thành công!'
        });

    } catch (error) {
        await connection.rollback();
        connection.release();
        
        console.error('❌ Lỗi tại API Đăng ký:', error);
        res.status(500).json({ 
            status: 'error', 
            message: 'Lỗi máy chủ nội bộ. Vui lòng thử lại sau.' 
        });
    }
};

module.exports = { register };