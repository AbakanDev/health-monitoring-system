const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

// Khi Android gọi POST /api/auth/register, nó sẽ chạy hàm register trong controller
router.post('/register', authController.register);

router.post('/login', authController.login);

module.exports = router;