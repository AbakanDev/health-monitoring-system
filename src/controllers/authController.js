const bcrypt = require('bcrypt');
const pool = require('../config/db');

const register = async (req, res) => {
    const connection = await pool.getConnection();

    try {
        // Hứng dữ liệu (đón cả chuẩn viết hoa lẫn viết thường để bọc lót lỗi)
        const { 
            cccd, password, fullName, dob, gender, address, email, phone,
            CCCD, HoTen, NgaySinh, GioiTinh, DiaChi, Email, SDT
        } = req.body;

        // Cơ chế bọc lót: Nếu biến thường undefined thì lấy biến hoa và ngược lại
        const final_cccd = cccd || CCCD;
        const final_password = password; // Mật khẩu nhận từ App
        const final_fullName = fullName || HoTen;
        const final_dob = dob || NgaySinh;
        const final_gender = gender || GioiTinh;
        const final_address = address || DiaChi;
        const final_email = email || Email;
        const final_phone = phone || SDT;

        // TẤM KHIÊN BẢO VỆ: Kiểm tra xem có ông nào bị undefined không
        if (!final_cccd || !final_password || !final_fullName) {
            connection.release();
            return res.status(400).json({
                status: 'error',
                message: `Dữ liệu gửi lên bị thiếu hoặc sai tên biến! Kiểm tra: cccd=${final_cccd}, fullName=${final_fullName}`
            });
        }

        await connection.beginTransaction();

        // Kiểm tra CCCD trùng lặp (Dùng biến an toàn final_cccd)
        const [existingUsers] = await connection.execute(
            'SELECT CCCD FROM NGUOIDUNG WHERE CCCD = ?',
            [final_cccd]
        );

        if (existingUsers.length > 0) {
            connection.release();
            return res.status(400).json({ 
                status: 'error', 
                message: 'CCCD này đã được đăng ký trong hệ thống!' 
            });
        }

        // Tạo bản ghi NGUOIDUNG
        const [userResult] = await connection.execute(
            `INSERT INTO NGUOIDUNG (HoTen, CCCD, NgaySinh, GioiTinh, SDT, DiaChi, Email)
             VALUES (?, ?, ?, ?, ?, ?, ?)`,
            [
                final_fullName, 
                final_cccd, 
                final_dob || null, 
                final_gender || null, 
                final_phone || null, 
                final_address || null, 
                final_email || null
            ]
        );

        const maNguoiDung = userResult.insertId;

        // Băm mật khẩu
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(final_password, salt);

        // Tạo tài khoản (Gán mặc định quyền USER)
        await connection.execute(
            `INSERT INTO TAIKHOAN (TenDangNhap, MatKhauHash, MaNguoiDung, MaVaiTro)
             VALUES (?, ?, ?, ?)`,
            [final_cccd, hashedPassword, maNguoiDung, 'USER']
        );

        await connection.commit();
        connection.release();

        return res.status(201).json({
            status: 'success',
            message: 'Đăng ký tài khoản thành công!'
        });

    } catch (error) {
        await connection.rollback();
        connection.release();
        
        console.error('❌ Lỗi tại API Đăng ký:', error);
        return res.status(500).json({ 
            status: 'error', 
            message: 'Lỗi máy chủ nội bộ.' 
        });
    }
};

module.exports = { register };