const express = require('express');
const router = express.Router();
const healthDataController = require('../controllers/healthDataController');
const { verifyToken } = require('../middlewares/authMiddleware');

router.get('/vaccine-doses/:cccd', verifyToken, healthDataController.getVaccineInfo);
router.get('/quarantine-status/:cccd', verifyToken, healthDataController.getQuarantineStatus);

module.exports = router;