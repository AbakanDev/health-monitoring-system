const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

// Khai báo API đăng nhập: POST /api/auth/login
router.post('/login', authController.login);
router.post('/register', authController.register);

// THÊM DÒNG NÀY: Khai báo API lấy dữ liệu tiêm chủng
router.get('/tiemchung/:cccd', authController.getThongTinTiemChung);

module.exports = router;