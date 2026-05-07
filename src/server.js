const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
require('dotenv').config();
require('./config/db'); 

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

// 3. Khởi động server
app.listen(PORT, () => {
    console.log(`🚀 Server is running on port ${PORT}`);
});