const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const bcrypt = require('bcryptjs'); // 1. THÊM THƯ VIỆN BCRYPTJS
require('dotenv').config();

const db = require('./config/db'); // 2. GÁN BIẾN db ĐỂ CÓ THỂ QUERY SQL

const app = express();
const PORT = process.env.PORT || 3000;

// 1. Setup Middlewares
app.use(helmet()); // Thêm các header bảo mật HTTP
app.use(cors());   // Xử lý lỗi CORS nếu gọi API từ trình duyệt/môi trường khác
app.use(express.json()); // Cực kỳ quan trọng: giúp Express đọc được body dạng JSON từ App Android
app.use(express.urlencoded({ extended: true }));

// 2. Route kiểm tra sức khỏe hệ thống (Health Check)
// Khi deploy lên Render, Render sẽ gọi vào route này để kiểm tra xem server đã khởi động thành công chưa
app.get('/api/health', (req, res) => {
    res.status(200).json({ 
        status: 'success', 
        message: 'Backend is running smoothly!' 
    });
});

// ==========================================
// 3. ROUTE ĐĂNG KÝ TÀI KHOẢN (THÊM MỚI VÀO ĐÂY)
// ==========================================
app.post('/api/register', async (req, res) => {
    try {
        const { cccd, password, full_name, dob, gender, address, email, phone } = req.body;

        // Kiểm tra xem CCCD hoặc SĐT đã tồn tại trong database chưa
        const checkQuery = 'SELECT * FROM users WHERE cccd = ? OR phone = ?';
        const [existingUsers] = await db.execute(checkQuery, [cccd, phone]);
        
        if (existingUsers.length > 0) {
            return res.status(400).json({ 
                success: false, 
                message: 'CCCD hoặc Số điện thoại đã được đăng ký!' 
            });
        }

        // Mã hóa mật khẩu trước khi lưu
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // Lưu thông tin người dùng mới vào database
        const insertQuery = `
            INSERT INTO users (cccd, password_hash, full_name, dob, gender, address, email, phone) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        `;
        await db.execute(insertQuery, [cccd, hashedPassword, full_name, dob, gender, address, email, phone]);

        // Trả về JSON thành công cho Android App
        res.status(201).json({ 
            success: true, 
            message: 'Đăng ký thành công!' 
        });

    } catch (error) {
        console.error('Lỗi khi đăng ký:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Lỗi server, vui lòng thử lại sau.' 
        });
    }
});
// ==========================================

// 4. Khởi động server
app.listen(PORT, () => {
    console.log(`🚀 Server is running on port ${PORT}`);
});