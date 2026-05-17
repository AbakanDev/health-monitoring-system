const bcrypt = require('bcrypt');
const pool = require('../config/db');

const register = async (req, res) => {
    // Lấy một kết nối từ pool để sử dụng Transaction
    const connection = await pool.getConnection();

    try {
        // 1. Nhận dữ liệu từ App Android
        // Nhờ @SerializedName trên Android, các field đã khớp chuẩn với CSDL của bạn
        const { cccd, password, HoTen, NgaySinh, GioiTinh, DiaChi, Email, SDT } = req.body;

        // Bắt đầu Transaction: Đảm bảo tính toàn vẹn dữ liệu (Insert cả 2 bảng hoặc không bảng nào)
        await connection.beginTransaction();

        // 2. Kiểm tra xem CCCD đã tồn tại trong hệ thống chưa
        const [existingUsers] = await connection.execute(
            'SELECT CCCD FROM NGUOIDUNG WHERE CCCD = ?',
            [cccd]
        );

        if (existingUsers.length > 0) {
            // Giải phóng connection và báo lỗi về App
            connection.release();
            return res.status(400).json({ 
                status: 'error', 
                message: 'CCCD này đã được đăng ký trong hệ thống!' 
            });
        }

        // 3. Tạo bản ghi NGUOIDUNG
        const [userResult] = await connection.execute(
            `INSERT INTO NGUOIDUNG (HoTen, CCCD, NgaySinh, GioiTinh, SDT, DiaChi, Email)
            VALUES (?, ?, ?, ?, ?, ?, ?)`,
            [HoTen, cccd, NgaySinh, GioiTinh, SDT, DiaChi, Email]
        );

        // 4. Lấy MaNguoiDung vừa được AUTO_INCREMENT sinh ra
        const maNguoiDung = userResult.insertId;

        // 5. Băm (hash) mật khẩu (Salt rounds = 10 là mức an toàn tiêu chuẩn)
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // 6. Tạo bản ghi TAIKHOAN
        // Lấy CCCD làm TenDangNhap và gán mặc định MaVaiTro là 'USER' (Bạn cần insert sẵn mã 'USER' vào bảng VAITRO)
        await connection.execute(
            `INSERT INTO TAIKHOAN (TenDangNhap, MatKhauHash, MaNguoiDung, MaVaiTro)
            VALUES (?, ?, ?, ?)`,
            [cccd, hashedPassword, maNguoiDung, 'USER']
        );

        // 7. Hoàn tất Transaction
        await connection.commit();
        connection.release();

        // 8. Trả phản hồi thành công về cho Android (Khớp với RegisterResponse bên Kotlin)
        res.status(201).json({
            status: 'success',
            message: 'Đăng ký tài khoản thành công!'
        });

    } catch (error) {
        // Nếu có lỗi (vd: rớt mạng, lỗi schema), Hoàn tác (Rollback) toàn bộ thao tác
        await connection.rollback();
        connection.release();
        
        console.error('❌ Lỗi tại API Đăng ký:', error);
        res.status(500).json({ 
            status: 'error', 
            message: 'Lỗi máy chủ nội bộ. Vui lòng thử lại sau.' 
        });
    }
};

module.exports = {
    register
};