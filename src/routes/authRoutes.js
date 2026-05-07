const express = require('express');
const router = express.Router();
const { registerUser } = require('../controllers/authController');

// Khai báo route POST. URL thực tế sẽ là: /api/auth/register
router.post('/register', registerUser);

module.exports = router;