const express = require('express');
const cors = require('cors');
require('dotenv').config();

// Khởi tạo app Express [cite: 58]
const app = express();

// Gọi file db.js để nó chạy hàm testConnection() ngay khi khởi động server [cite: 59]
require('./config/db'); 

// Gắn các middlewares tổng [cite: 60]
app.use(cors()); // Cho phép client (Android/Web) gọi API mà không bị chặn
app.use(express.json()); // Giúp server đọc và hiểu được cục data JSON mà Android gửi lên [cite: 60]
app.use(express.urlencoded({ extended: true }));

// Import các routes
const authRoutes = require('./routes/authRoutes');

// Nhúng các routes vào app [cite: 61]
// Mọi đường dẫn trong authRoutes sẽ có tiền tố là /api/auth
app.use('/api/auth', authRoutes); // Tạo thành: POST /api/auth/register [cite: 61]

// Route test nhẹ để kiểm tra server có sống không
app.get('/', (req, res) => {
    res.json({ message: 'Health App Backend đang hoạt động bình thường! :Đ' });
});

// Lắng nghe ở một cổng cụ thể [cite: 62]
// Lấy PORT từ file .env, nếu không có thì mặc định chạy ở 3000
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`🚀 Server đang chạy bốc lửa tại port ${PORT} [cite: 62]`);
});