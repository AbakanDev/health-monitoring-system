const pool = require('../config/db');

const User = {
    findByCCCD: async (cccd) => {
        const [rows] = await pool.execute(
            'SELECT * FROM users WHERE cccd = ?',
            [cccd || null]
        );
        return rows[0];
    },

    create: async (userData) => {
        const cccd = userData.cccd || null;
        const password_hash = userData.password || null; 
        const full_name = userData.fullName || userData.full_name || null;
        const dob = userData.dob || null;
        const gender = userData.gender || null;
        const address = userData.address || null;
        
        // XỬ LÝ QUAN TRỌNG: Nếu email rỗng (""), bắt buộc phải chuyển thành null để không vi phạm UNIQUE
        const email = (userData.email && userData.email.trim() !== "") ? userData.email.trim() : null;
        
        const phone = userData.phone || null;

        // Bỏ qua id_user và created_at vì MySQL tự động sinh ra
        const [result] = await pool.execute(
            `INSERT INTO users 
            (cccd, password_hash, full_name, dob, gender, address, email, phone) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
            [cccd, password_hash, full_name, dob, gender, address, email, phone]
        );

        return result;
    }
};

module.exports = User;