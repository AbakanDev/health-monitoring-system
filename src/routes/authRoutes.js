const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

// Khai báo API đăng nhập: POST /api/auth/login
router.post('/login', authController.login);
router.post('/register', authController.register);

module.exports = router;