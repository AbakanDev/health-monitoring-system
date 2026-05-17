const bcrypt = require('bcrypt');
const pool = require('../config/db');

const register = async (req, res) => {
    // Lấy kết nối từ pool an toàn
    const connection = await pool.getConnection();

    try {
        // 1. Nhận dữ liệu từ App Android
        const { cccd, password, fullName, dob, gender, address, email, phone } = req.body;

        console.log("➡️ Nhận request đăng ký với data:", req.body);

        // 2. TẤM KHIÊN BẢO VỆ (Validation): Kiểm tra dữ liệu rỗng hoặc sai tên biến
        if (!cccd || cccd.trim() === "") {
            connection.release();
            return res.status(400).json({
                status: 'error',
                message: 'Lỗi Backend: CCCD gửi lên bị rỗng hoặc undefined! Hãy kiểm tra lại Intent truyền giữa 2 Màn hình Android.'
            });
        }

        if (!password || password.trim() === "") {
            connection.release();
            return res.status(400).json({
                status: 'error',
                message: 'Lỗi Backend: Password gửi lên bị rỗng hoặc undefined!'
            });
        }

        if (!fullName || fullName.trim() === "") {
            connection.release();
            return res.status(400).json({
                status: 'error',
                message: 'Lỗi Backend: fullName gửi lên bị rỗng hoặc undefined!'
            });
        }

        // Bắt đầu Transaction toàn vẹn dữ liệu
        await connection.beginTransaction();

        // 3. Kiểm tra xem CCCD đã tồn tại trong hệ thống chưa
        const [existingUsers] = await connection.execute(
            'SELECT CCCD FROM NGUOIDUNG WHERE CCCD = ?',
            [cccd.trim()]
        );

        if (existingUsers.length > 0) {
            await connection.rollback();
            connection.release();
            return res.status(400).json({ 
                status: 'error', 
                message: 'CCCD này đã được đăng ký trong hệ thống!' 
            });
        }

        // 4. Tạo bản ghi NGUOIDUNG (Ép khớp chuẩn các biến chữ thường với cột Database viết hoa)
        const [userResult] = await connection.execute(
            `INSERT INTO NGUOIDUNG (HoTen, CCCD, NgaySinh, GioiTinh, SDT, DiaChi, Email)
             VALUES (?, ?, ?, ?, ?, ?, ?)`,
            [
                fullName.trim(), 
                cccd.trim(), 
                dob && dob.trim() !== "" ? dob.trim() : null, 
                gender && gender.trim() !== "" ? gender.trim() : null, 
                phone && phone.trim() !== "" ? phone.trim() : null, 
                address && address.trim() !== "" ? address.trim() : null, 
                email && email.trim() !== "" ? email.trim() : null
            ]
        );

        // Lấy MaNguoiDung vừa sinh ra
        const maNguoiDung = userResult.insertId;

        // 5. Băm mật khẩu bảo mật bằng bcrypt
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // 6. Tạo bản ghi TAIKHOAN (Gán TenDangNhap = CCCD, MaVaiTro mặc định = USER)
        await connection.execute(
            `INSERT INTO TAIKHOAN (TenDangNhap, MatKhauHash, MaNguoiDung, MaVaiTro)
             VALUES (?, ?, ?, ?)`,
            [cccd.trim(), hashedPassword, maNguoiDung, 'USER']
        );

        // Hoàn tất Transaction thành công
        await connection.commit();
        connection.release();

        console.log(`✅ Đăng ký thành công tài khoản cho CCCD: ${cccd}`);
        return res.status(201).json({
            status: 'success',
            message: 'Đăng ký tài khoản thành công!'
        });

    } catch (error) {
        // Hoàn tác dữ liệu rác nếu có bất kỳ lỗi hệ thống nào xảy ra
        await connection.rollback();
        connection.release();
        
        console.error('❌ Lỗi nghiêm trọng tại API Đăng ký:', error);
        return res.status(500).json({ 
            status: 'error', 
            message: 'Lỗi máy chủ nội bộ. Vui lòng thử lại sau.' 
        });
    }
};

module.exports = {
    register
};