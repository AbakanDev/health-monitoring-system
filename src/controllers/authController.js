const bcrypt = require('bcryptjs');
const User = require('../models/User');

exports.register = async (req, res) => {
    try {
        const {
            cccd,
            password,
            fullName,
            dob,
            gender,
            address,
            email,
            phone
        } = req.body;

        // check CCCD tồn tại
        const existingUser = await User.findByCCCD(cccd);

        if (existingUser) {
            return res.status(400).json({
                status: 'error',
                message: 'CCCD này đã được đăng ký!'
            });
        }

        // hash password
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // lưu mysql
        await User.create({
            cccd,
            password: hashedPassword,
            fullName,
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