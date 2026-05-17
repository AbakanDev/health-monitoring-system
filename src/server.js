const express = require('express');
const authRoutes = require('./routes/authRoutes');
require('dotenv').config({ path: '../.env' }); // Chỉ đường dẫn tới file .env ở thư mục gốc

const app = express();

// Middleware để Express hiểu được dữ liệu JSON gửi lên
app.use(express.json());

// Đăng ký các Routes
app.use('/api/auth', authRoutes);

// Khởi chạy server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server đang chạy tại port ${PORT}`);
});