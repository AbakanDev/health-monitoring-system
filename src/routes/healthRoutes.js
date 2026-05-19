const express = require('express');
const router = express.Router();
const healthDataController = require('../controllers/healthDataController');
const { verifyToken } = require('../middlewares/authMiddleware');

// Áp dụng middleware verifyToken trước khi gọi controller
router.get('/vaccine-doses/:cccd', verifyToken, healthDataController.getVaccineInfo);

module.exports = router;