const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    cccd: {
        type: String,
        required: true,
        unique: true, // Không cho phép trùng CCCD
        trim: true
    },
    password: {
        type: String,
        required: true
    },
    fullName: {
        type: String,
        required: true
    },
    dob: {
        type: String, // Có thể đổi thành Date nếu muốn
        required: true
    },
    gender: {
        type: String,
        required: true
    },
    address: {
        type: String,
        required: true
    },
    email: {
        type: String,
        trim: true
    },
    phone: {
        type: String,
        required: true
    }
}, { timestamps: true }); // Tự động thêm createdAt và updatedAt

module.exports = mongoose.model('User', userSchema);