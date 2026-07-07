const express = require('express');
const authRoutes = require('./routes/authRoutes');
const healthRoutes = require('./routes/healthRoutes'); // Thêm dòng này: Import healthRoutes
require('dotenv').config({ path: '../.env' }); // Chỉ đường dẫn tới file .env ở thư mục gốc
process.env.TZ = 'Asia/Ho_Chi_Minh';
const app = express();

// Middleware để Express hiểu được dữ liệu JSON gửi lên
app.use(express.json());

// Đăng ký các Routes
app.use('/api/auth', authRoutes);
app.use('/api/health', healthRoutes); // Thêm dòng này: Đăng ký router, tiền tố là /api/health

// Khởi chạy server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server đang chạy tại port ${PORT}`);
});