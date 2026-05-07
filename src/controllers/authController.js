const pool = require('../config/db');
const bcrypt = require('bcryptjs');

exports.register = async (req, res) => {
    try {
        // 1. Lấy dữ liệu từ body (Android gửi lên)
        const { cccd, password, fullName, dob, gender, address, email, phone } = req.body;

        // 2. Kiểm tra xem CCCD đã tồn tại trong DB chưa
        const existingUser = await User.findOne({ cccd });
        if (existingUser) {
            // Trả về lỗi để Android hứng và hiện Toast
            return res.status(400).json({
                status: 'error',
                message: 'CCCD này đã được đăng ký!'
            });
        }

        // 3. Mã hóa mật khẩu (băm 10 vòng)
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // 4. Tạo user mới
        const newUser = new User({
            cccd,
            password: hashedPassword, // Lưu mật khẩu đã mã hóa
            fullName,
            dob,
            gender,
            address,
            email,
            phone
        });

        // 5. Lưu vào Database
        await newUser.save();

        // 6. Phản hồi về Android (Khớp với check status == "success" của bạn)
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