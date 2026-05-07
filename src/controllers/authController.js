const bcrypt = require('bcryptjs');
const User = require('../models/User'); 

exports.register = async (req, res) => {
    // In ra log để bạn dễ dàng debug
    console.log(">>> DỮ LIỆU ANDROID GỬI LÊN:", req.body); 

    try {
        // Hỗ trợ bắt mọi trường hợp tên biến từ Android
        const cccd = req.body.cccd;
        const password = req.body.password;
        const fullName = req.body.fullName || req.body.full_name; 
        const dob = req.body.dob;
        const gender = req.body.gender;
        const address = req.body.address;
        const email = req.body.email;
        const phone = req.body.phone;

        // 1. Check tồn tại CCCD
        const existingUser = await User.findByCCCD(cccd);

        if (existingUser) {
            return res.status(400).json({
                status: 'error',
                message: 'CCCD này đã được đăng ký trên hệ thống!'
            });
        }

        // 2. Hash mật khẩu (Chuẩn bị cho cột password_hash)
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // 3. Lưu vào MySQL (Id và Created_at sẽ tự tăng)
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

        // 4. Báo thành công về Android
        res.status(201).json({
            status: 'success',
            message: 'Đăng ký tài khoản thành công!'
        });

    } catch (error) {
        console.error("Lỗi đăng ký MySQL:", error);
        
        // Bắt chính xác lỗi vi phạm UNIQUE (Trùng CCCD, SĐT hoặc Email)
        if (error.code === 'ER_DUP_ENTRY') {
            return res.status(400).json({ 
                status: 'error', 
                message: 'Thông tin CCCD, Số điện thoại hoặc Email đã tồn tại.' 
            });
        }

        // Bắt lỗi ENUM (Gửi sai chữ "Nam", "Nữ")
        if (error.code === 'WARN_DATA_TRUNCATED') {
            return res.status(400).json({ 
                status: 'error', 
                message: 'Dữ liệu giới tính không hợp lệ (Chỉ nhận Nam hoặc Nữ).' 
            });
        }

        res.status(500).json({
            status: 'error',
            message: 'Lỗi server nội bộ: ' + error.message
        });
    }
};