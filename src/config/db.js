const mysql = require('mysql2/promise');
const fs = require('fs');
const path = require('path');
require('dotenv').config();

const pool = mysql.createPool({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: process.env.DB_PORT,
    // Cấu hình SSL sử dụng chứng chỉ của Aiven
    ssl: {
        ca: fs.readFileSync(path.join(__dirname, 'ca.pem')),
        rejectUnauthorized: true // Đảm bảo tính bảo mật chặt chẽ
    },
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

pool.getConnection()
    .then(connection => {
        console.log('✅ Database connected successfully to Aiven MySQL!');
        connection.release();
    })
    .catch(err => {
        console.error('❌ Error connecting to Aiven database:', err.message);
    });

module.exports = pool;