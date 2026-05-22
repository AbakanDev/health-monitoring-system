const express = require('express');
const router = express.Router();
const healthDataController = require('../controllers/healthDataController');
const { verifyToken } = require('../middlewares/authMiddleware');

router.get('/vaccine-doses/:cccd', verifyToken, healthDataController.getVaccineInfo);
router.get('/quarantine-status/:cccd', verifyToken, healthDataController.getQuarantineStatus);
router.get('/test-history/:cccd', verifyToken, healthDataController.getTestHistory);
router.get('/test-status/:cccd', verifyToken, healthDataController.getTestStatus);
router.get('/trend-f0', healthDataController.getTrendAnalysis);
router.get('/vaccine-rates', healthDataController.getVaccineRates);
router.get('/dashboard-summary', healthDataController.getDashboardSummary);
router.get('/first-test/:cccd', verifyToken, healthDataController.getFirstTest);
module.exports = router;