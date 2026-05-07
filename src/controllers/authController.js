const pool = require('../config/db');
const bcrypt = require('bcrypt');

const registerUser = async (req, res) => {
    try {
        // 1. Lấy dữ liệu từ body của request (do App Android gửi lên)
        const { cccd, password, full_name, dob, gender, address, email, phone } = req.body;

        // 2. Mã hóa mật khẩu
        const saltRounds = 10;
        const password_hash = await bcrypt.hash(password, saltRounds);

        // 3. Chuẩn bị câu lệnh SQL an toàn (dùng ? để chống SQL Injection)
        const sql = `
            INSERT INTO users (cccd, password_hash, full_name, dob, gender, address, email, phone) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        `;
        
        // Mảng giá trị tương ứng với các dấu ? ở trên
        const values = [cccd, password_hash, full_name, dob, gender, address, email, phone];

        // 4. Thực thi câu lệnh
        const [result] = await pool.execute(sql, values);

        // 5. Trả về phản hồi cho App Android
        res.status(201).json({
            status: 'success',
            message: 'Đăng ký người dùng thành công!',
            data: {
                id_user: result.insertId,
                cccd: cccd,
                full_name: full_name
            }
        });

    } catch (error) {
        console.error("Lỗi khi thêm user:", error);
        
        // Xử lý lỗi trùng lặp dữ liệu (do set UNIQUE ở cccd, email, phone)
        if (error.code === 'ER_DUP_ENTRY') {
            return res.status(400).json({ 
                status: 'error', 
                message: 'CCCD, Số điện thoại hoặc Email đã tồn tại trong hệ thống.' 
            });
        }

        // Lỗi server khác
        res.status(500).json({ 
            status: 'error', 
            message: 'Lỗi server nội bộ. Không thể thêm người dùng.' 
        });
    }
};

module.exports = { registerUser };