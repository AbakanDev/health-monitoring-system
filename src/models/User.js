const pool = require('../config/db');

const User = {
    findByCCCD: async (cccd) => {
        const [rows] = await pool.execute(
            'SELECT * FROM users WHERE cccd = ?',
            [cccd]
        );
        return rows[0];
    },

    create: async (userData) => {
        const { cccd, password, fullName, dob, gender, address, email, phone } = userData;

        const [result] = await pool.execute(
            `INSERT INTO users 
            (cccd, password, fullName, dob, gender, address, email, phone) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
            [cccd, password, fullName, dob, gender, address, email, phone]
        );

        return result;
    }
};

module.exports = User;