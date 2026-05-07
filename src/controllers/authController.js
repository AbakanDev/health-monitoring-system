const bcrypt = require('bcryptjs');
// Hãy chắc chắn đường dẫn này đúng và file là User.js
const User = require('../models/User'); 

exports.register = async (req, res) => {
    try {
        const { cccd, password, full_name, dob, gender, address, email, phone } = req.body;

        // Check tồn tại
        const existingUser = await User.findByCCCD(cccd);

        if (existingUser) {
            return res.status(400).json({
                status: 'error',
                message: 'CCCD này đã được đăng ký!'
            });
        }

        // Hash mật khẩu
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // Lưu vào MySQL thông qua Model User
        await User.create({
            cccd,
            password: hashedPassword,
            full_name,
            dob,
            gender,
            address,
            email,
            phone
        });

        res.status(201).json({
            status: 'success',
            message: 'Đăng ký tài khoản thành công!'
        });

    } catch (error) {
        console.error("Lỗi đăng ký:", error);
        res.status(500).json({
            status: 'error',
            message: 'Lỗi server: ' + error.message
        });
    }
};