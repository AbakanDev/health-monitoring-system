const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

// Định nghĩa đường dẫn POST /api/auth/register [cite: 49]
// Khi có Request bắn vào đây, nó sẽ tự động gọi hàm register trong authController
router.post('/register', authController.register);

module.exports = router;