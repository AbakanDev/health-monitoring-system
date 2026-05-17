const bcrypt = require('bcrypt');
const pool = require('../config/db');

const register = async (req, res) => {
    const connection = await pool.getConnection();

    try {
        // LỆNH TRUY VẾT: In trực tiếp cấu trúc JSON mà Android gửi lên màn hình Render Log
        console.log("=== CỤC DATA ANDROID GỬI LÊN RENDER HÌNH THÙ NHƯ SAU ===");
        console.log(JSON.stringify(req.body, null, 2));
        console.log("======================================================");

        // BỌC LÓT TOÀN DIỆN: Hứng tất cả các trường hợp đặt tên biến có thể xảy ra
        const { 
            cccd, CCCD, cccd_number,
            password, Password, matKhau, MatKhau,
            fullName, FullName, hoTen, HoTen, full_name,
            dob, Dob, ngaySinh, NgaySinh,
            gender, Gender, gioiTinh, GioiTinh,
            address, Address, diaChi, DiaChi,
            email, Email,
            phone, Phone, sdt, SDT
        } = req.body;

        // Cơ chế tự động chọn biến có dữ liệu (Nếu biến này undefined thì lấy biến kia)
        const final_cccd = cccd || CCCD || cccd_number;
        const final_password = password || Password || matKhau || MatKhau;
        const final_fullName = fullName || FullName || hoTen || HoTen || full_name;
        const final_dob = dob || Dob || ngaySinh || NgaySinh;
        const final_gender = gender || Gender || gioiTinh || GioiTinh;
        const final_address = address || Address || diaChi || DiaChi;
        const final_email = email || Email;
        const final_phone = phone || Phone || sdt || SDT;

        // Kiểm tra tối hậu thư sau khi đã gom biến
        if (!final_cccd || final_cccd.trim() === "") {
            connection.release();
            return res.status(400).json({
                status: 'error',
                message: 'Lỗi Backend: Vẫn không tìm thấy CCCD! Hãy gửi file RegisterRequest.kt cho người đẹp check.'
            });
        }

        if (!final_password || final_password.trim() === "") {
            connection.release();
            return res.status(400).json({
                status: 'error',
                message: 'Lỗi Backend: Thiếu mật khẩu!'
            });
        }

        await connection.beginTransaction();

        // 3. Kiểm tra xem CCCD đã tồn tại chưa
        const [existingUsers] = await connection.execute(
            'SELECT CCCD FROM NGUOIDUNG WHERE CCCD = ?',
            [final_cccd.trim()]
        );

        if (existingUsers.length > 0) {
            await connection.rollback();
            connection.release();
            return res.status(400).json({ 
                status: 'error', 
                message: 'CCCD này đã được đăng ký trong hệ thống!' 
            });
        }

        // 4. Tạo bản ghi NGUOIDUNG
        const [userResult] = await connection.execute(
            `INSERT INTO NGUOIDUNG (HoTen, CCCD, NgaySinh, GioiTinh, SDT, DiaChi, Email)
             VALUES (?, ?, ?, ?, ?, ?, ?)`,
            [
                final_fullName ? final_fullName.trim() : null, 
                final_cccd.trim(), 
                final_dob && final_dob.trim() !== "" ? final_dob.trim() : null, 
                final_gender && final_gender.trim() !== "" ? final_gender.trim() : null, 
                final_phone && final_phone.trim() !== "" ? final_phone.trim() : null, 
                final_address && final_address.trim() !== "" ? final_address.trim() : null, 
                final_email && final_email.trim() !== "" ? final_email.trim() : null
            ]
        );

        const maNguoiDung = userResult.insertId;

        // 5. Băm mật khẩu
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(final_password, salt);

        // 6. Tạo bản ghi TAIKHOAN
        await connection.execute(
            `INSERT INTO TAIKHOAN (TenDangNhap, MatKhauHash, MaNguoiDung, MaVaiTro)
             VALUES (?, ?, ?, ?)`,
            [final_cccd.trim(), hashedPassword, maNguoiDung, 'USER']
        );

        await connection.commit();
        connection.release();

        console.log(`✅ Đăng ký thành công tài khoản cho CCCD: ${final_cccd}`);
        return res.status(201).json({
            status: 'success',
            message: 'Đăng ký tài khoản thành công!'
        });

    } catch (error) {
        await connection.rollback();
        connection.release();
        console.error('❌ Lỗi nghiêm trọng tại API Đăng ký:', error);
        return res.status(500).json({ 
            status: 'error', 
            message: 'Lỗi máy chủ nội bộ.' 
        });
    }
};

module.exports = { register };