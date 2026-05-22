const healthService = require('../services/healthService');

const getVaccineInfo = async (req, res) => {
    try {
        const cccd = req.params.cccd; // Lấy CCCD từ URL

        if (!cccd) {
            return res.status(400).json({ message: 'Vui lòng cung cấp CCCD' });
        }

        const data = await healthService.getVaccineDosesByCCCD(cccd);

        if (data.length === 0) {
            return res.status(404).json({ message: 'Không tìm thấy dữ liệu tiêm chủng cho CCCD này' });
        }

        return res.status(200).json({
            message: 'Lấy dữ liệu thành công',
            data: data
        });

    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: 'Lỗi server khi lấy dữ liệu tiêm chủng' });
    }
};

const getQuarantineStatus = async (req, res) => {
    try {
        const cccd = req.params.cccd;

        if (!cccd) {
            return res.status(400).json({ 
                success: false, 
                message: 'Vui lòng cung cấp CCCD' 
            });
        }

        const quarantineList = await healthService.getActiveQuarantines(cccd);
        
        // Nếu mảng có phần tử tức là đang trong thời gian cách ly
        const isQuarantined = quarantineList.length > 0;

        return res.status(200).json({
            success: true,
            message: isQuarantined ? 'Công dân đang trong diện cách ly' : 'Công dân không bị cách ly',
            isQuarantined: isQuarantined,
            data: quarantineList // Chứa thông tin ngày bắt đầu, kết thúc, địa điểm (nếu có)
        });
    } catch (error) {
        console.error('Lỗi khi lấy dữ liệu cách ly:', error);
        return res.status(500).json({
            success: false,
            message: 'Đã xảy ra lỗi trên server khi lấy dữ liệu cách ly',
            error: error.message
        });
    }
};

const getTestHistory = async (req, res) => {
    try {
        const cccd = req.params.cccd;

        if (!cccd) {
            return res.status(400).json({ success: false, message: 'Vui lòng cung cấp CCCD' });
        }

        const data = await healthService.getTestHistoryByCCCD(cccd);

        if (data.length === 0) {
            return res.status(404).json({ 
                success: false, 
                message: 'Không tìm thấy lịch sử xét nghiệm cho công dân này' 
            });
        }

        return res.status(200).json({
            success: true,
            message: 'Lấy lịch sử xét nghiệm thành công',
            totalTests: data.length,
            data: data
        });

    } catch (error) {
        console.error('Lỗi khi lấy lịch sử xét nghiệm:', error);
        return res.status(500).json({ success: false, message: 'Lỗi server khi lấy lịch sử xét nghiệm' });
    }
};

const getTestStatus = async (req, res) => {
    try {
        const cccd = req.params.cccd;

        if (!cccd) {
            return res.status(400).json({ success: false, message: 'Vui lòng cung cấp CCCD' });
        }

        // Lấy lịch sử (đã sắp xếp mới nhất lên đầu)
        const data = await healthService.getTestHistoryByCCCD(cccd);

        if (data.length === 0) {
            return res.status(200).json({
                success: true,
                message: 'Công dân chưa có dữ liệu xét nghiệm',
                hasTested: false,
                latestTest: null
            });
        }

        // Tình trạng mới nhất là phần tử đầu tiên của mảng
        const latestTest = data[0];

        return res.status(200).json({
            success: true,
            message: 'Lấy tình trạng xét nghiệm mới nhất thành công',
            hasTested: true,
            latestTest: latestTest
        });

    } catch (error) {
        console.error('Lỗi khi lấy tình trạng xét nghiệm:', error);
        return res.status(500).json({ success: false, message: 'Lỗi server khi lấy tình trạng xét nghiệm' });
    }
};

const getTrendAnalysis = async (req, res) => {
    try {
        const trendData = await healthService.getF0TrendThisYear();
        
        return res.status(200).json({
            success: true,
            message: "Lấy dữ liệu xu hướng thành công",
            data: trendData // Sẽ trả về mảng ví dụ: [1200, 1500, 2800, 0, 0, ...]
        });
    } catch (error) {
        console.error("Lỗi khi lấy dữ liệu trend analysis:", error);
        return res.status(500).json({
            success: false,
            message: "Lỗi server khi lấy dữ liệu xu hướng"
        });
    }
};

const getVaccineRates = async (req, res) => {
    try {
        const rateData = await healthService.getVaccinationRates();
        
        return res.status(200).json({
            success: true,
            message: "Lấy dữ liệu tỷ lệ tiêm chủng thành công",
            data: rateData
        });
    } catch (error) {
        console.error("Lỗi khi lấy dữ liệu tỷ lệ tiêm chủng:", error);
        return res.status(500).json({
            success: false,
            message: "Lỗi server khi lấy tỷ lệ tiêm chủng"
        });
    }
};

const getDashboardSummary = async (req, res) => {
    try {
        const stats = await healthService.getDashboardStats();
        
        return res.status(200).json({
            success: true,
            message: "Lấy số liệu tổng quan thành công",
            data: stats
        });
    } catch (error) {
        console.error("Lỗi khi lấy số liệu dashboard:", error);
        return res.status(500).json({
            success: false,
            message: "Lỗi server khi lấy dữ liệu tổng quan"
        });
    }
};

const getContactStats = async (req, res) => {
    try {
        const cccd = req.params.cccd; // Lấy CCCD từ URL giống như các hàm trước

        if (!cccd) {
            return res.status(400).json({ 
                success: false, 
                message: 'Vui lòng cung cấp số CCCD' 
            });
        }

        const data = await healthService.getContactStatsByCCCD(cccd);

        // Trường hợp không tìm thấy người dùng hoặc không có lượt tiếp xúc nào (Tổng lượt tiếp xúc bằng 0)
        if (!data || data.TongLuotTiepXuc === 0) {
            return res.status(200).json({
                success: true,
                message: 'Không tìm thấy lịch sử tiếp xúc hoặc công dân chưa từng tiếp xúc ai',
                data: {
                    SoLuotF0: 0,
                    SoLuotF1: 0,
                    SoLuotF2: 0,
                    TongLuotTiepXuc: 0
                }
            });
        }

        return res.status(200).json({
            success: true,
            message: 'Lấy dữ liệu thống kê tiếp xúc thành công',
            data: data
        });

    } catch (error) {
        console.error('Lỗi khi lấy thống kê tiếp xúc theo CCCD:', error);
        return res.status(500).json({ 
            success: false, 
            message: 'Lỗi server khi lấy dữ liệu thống kê tiếp xúc' 
        });
    }
};

module.exports = {
    getVaccineInfo,
    getQuarantineStatus,
    getTestHistory, 
    getTestStatus,
    getTrendAnalysis,
    getVaccineRates,
    getDashboardSummary,
    getContactStats,
};