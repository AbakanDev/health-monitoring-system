const mysql = require('mysql2/promise');
const fs = require('fs');
const path = require('path');
require('dotenv').config();

// Đọc file chứng chỉ SSL (ca.pem) nằm cùng thư mục với file db.js này
const sslCertPath = path.join(__dirname, 'ca.pem');

// Tạo Connection Pool thay vì một connection đơn lẻ
const pool = mysql.createPool({
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,
    ssl: {
        ca: fs.readFileSync(sslCertPath)
    }
});

// Hàm kiểm tra kết nối để chắc chắn mọi thứ hoạt động khi khởi động server
const testConnection = async () => {
    try {
        const connection = await pool.getConnection();
        console.log('✅ Kết nối thành công đến Aiven MySQL Database!');
        connection.release(); // Trả connection lại cho pool sau khi test xong
    } catch (error) {
        console.error('❌ Lỗi kết nối Database:', error.message);
        process.exit(1); // Dừng server nếu không kết nối được DB (tránh chạy mù)
    }
};

testConnection();

module.exports = pool;